import kotlinx.coroutines.runBlocking
import org.example.api.YouTrackApiImpl
import org.example.factory.DependencyFactory
import org.example.http.IHttpService
import org.example.service.YouTrackService
import org.example.tools.IssueTool
import org.example.tools.WorkflowRulesTool
import org.example.tools.WorkflowsTool
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class MockYouTrackHttpService : IHttpService {
    override suspend fun get(url: String, params: Map<String, String>): String = when {
        url.contains("/api/issues/DEMO-42") ->
            """
            {
                "id": "DEMO-42",
                "summary": "Test issue that cannot be set to Fixed state",
                "project": {
                    "name": "Demo Project",
                    "shortName": "DEMO",
                    "${'$'}type": "Project"
                },
                "description": "This is a test issue for workflow analysis"
            }
            """.trimIndent()

        url.contains("/api/admin/workflows") && url.contains("query=usages.project.shortName:DEMO") ->
            """
            [
                {
                    "id": "148-68",
                    "title": "State Guard Workflow",
                    "rules": [
                        {
                            "id": "150-241",
                            "name": "state-handler",
                            "title": "Block Fixed State Without Resolution",
                            "type": "StatelessRule"
                        }
                    ],
                    "${'$'}type": "Workflow"
                }
            ]
            """.trimIndent()

        url.contains("/api/admin/workflows/148-68/rules") ->
            """
            [
                {
                    "id": "150-241",
                    "name": "state-handler",
                    "title": "Block Fixed State Without Resolution",
                    "type": "StatelessRule",
                    "description": null,
                    "readOnly": false,
                    "enabled": true,
                    "script": "var entities = require('@jetbrains/youtrack-scripting-api/entities');\nvar workflow = require('@jetbrains/youtrack-scripting-api/workflow');\n\nexports.rule = entities.Issue.onChange({\n  title: 'Block Fixed State Without Resolution',\n  guard: function(ctx) {\n    var issue = ctx.issue;\n    return issue.fields.isChanged(ctx.State) && \n           issue.fields.State && \n           issue.fields.State.name === 'Fixed';\n  },\n  action: function(ctx) {\n    var issue = ctx.issue;\n    \n    if (!issue.fields.Resolution || issue.fields.Resolution.name === 'Unresolved') {\n      workflow.message(\"Cannot set state to Fixed: Resolution must be set before marking as Fixed\");\n      issue.fields.State = ctx.issue.fields.oldValue(ctx.State);\n      return;\n    }\n    \n    if (!issue.fields.Assignee) {\n      workflow.message(\"Cannot set state to Fixed: Issue must be assigned before marking as Fixed\");\n      issue.fields.State = ctx.issue.fields.oldValue(ctx.State);\n    }\n  },\n  requirements: {\n    State: {\n      type: entities.State.fieldType,\n      name: 'State'\n    },\n    Resolution: {\n      type: entities.EnumField.fieldType,\n      name: 'Resolution'\n    },\n    Assignee: {\n      type: entities.User.fieldType,\n      name: 'Assignee'\n    }\n  }\n});",
                    "${'$'}type": "WorkflowRule"
                }
            ]
            """.trimIndent()

        else -> "[]"
    }

    override fun close() {}
}

class WorkflowAnalysisAgentTest {

    @Test
    fun testCompleteWorkflowAnalysisPipeline() = runBlocking {

        val mockHttpService = MockYouTrackHttpService()
        val api = YouTrackApiImpl(mockHttpService)
        val service = YouTrackService(api)
        
        val issueTool = IssueTool(service)
        val workflowsTool = WorkflowsTool(service)
        val workflowRulesTool = WorkflowRulesTool(service)
        

        val issueResult = issueTool.execute(org.example.tools.IssueToolArgs("DEMO-42"))
        val workflowsResult = workflowsTool.execute(org.example.tools.WorkflowsToolArgs("DEMO"))
        val rulesResult = workflowRulesTool.execute(org.example.tools.WorkflowRulesToolArgs("148-68"))
        
        assertTrue(issueResult.contains("DEMO-42"), "Issue result should contain issue ID")
        assertTrue(issueResult.contains("Test issue"), "Issue result should contain summary")
        assertTrue(issueResult.contains("Demo Project"), "Issue result should contain project name")
        
        assertTrue(workflowsResult.contains("148-68"), "Workflows result should contain workflow ID")
        assertTrue(workflowsResult.contains("State Guard Workflow"), "Workflows result should contain workflow title")
        
        assertTrue(rulesResult.contains("150-241"), "Rules result should contain rule ID")
        assertTrue(rulesResult.contains("Block Fixed State Without Resolution"), "Rules result should contain rule title")
        assertTrue(rulesResult.contains("StatelessRule"), "Rules result should contain rule type")
        assertTrue(rulesResult.contains("Script Preview"), "Rules result should contain script preview")
        
        mockHttpService.close()
    }
    
    @Test
    fun testToolsReturnFormattedData() = runBlocking {
        val mockHttpService = MockYouTrackHttpService()
        val api = YouTrackApiImpl(mockHttpService)
        val service = YouTrackService(api)
        val workflowRulesTool = WorkflowRulesTool(service)
        
        val result = workflowRulesTool.execute(org.example.tools.WorkflowRulesToolArgs("148-68"))
        
        assertTrue(result.contains("Rule:"), "Should have 'Rule:' label")
        assertTrue(result.contains("ID:"), "Should have 'ID:' label")
        assertTrue(result.contains("Type:"), "Should have 'Type:' label")
        assertTrue(result.contains("Enabled:"), "Should have 'Enabled:' label")
        assertTrue(result.contains("ReadOnly:"), "Should have 'ReadOnly:' label")
        
        mockHttpService.close()
    }
    
    @Test
    fun testDependencyFactoryCreatesAllComponents() {
        val config = org.example.config.YouTrackConfig(
            baseUrl = "https://test.youtrack.cloud",
            token = "test-token"
        )
        val factory = DependencyFactory(config, "test-api-key")
        
        val httpService = factory.createHttpService()
        val api = factory.createYouTrackApi()
        val service = factory.createYouTrackService()
        val tools = factory.createTools()
        
        assertTrue(httpService is org.example.http.KtorHttpService, "Should create KtorHttpService")
        assertTrue(api is YouTrackApiImpl, "Should create YouTrackApiImpl")
        assertTrue(service is YouTrackService, "Should create YouTrackService")
        assertTrue(tools.first is IssueTool, "Should create IssueTool")
        assertTrue(tools.second is WorkflowsTool, "Should create WorkflowsTool")
        assertTrue(tools.third is WorkflowRulesTool, "Should create WorkflowRulesTool")
        
        httpService.close()
    }
    
    @Test
    fun testServiceLayerCoordinatesApiCalls() = runBlocking {
        val mockHttpService = MockYouTrackHttpService()
        val api = YouTrackApiImpl(mockHttpService)
        val service = YouTrackService(api)
        
        val workflows = service.getProjectWorkflows("DEMO")
        val rules = service.getRules("148-68")
        val issue = service.getIssueDetails("DEMO-42")
        
        assertEquals(1, workflows.size, "Should return 1 workflow")
        assertEquals("148-68", workflows.first().id, "Workflow ID should match")
        
        assertEquals(1, rules.size, "Should return 1 rule")
        assertEquals("150-241", rules.first().id, "Rule ID should match")
        
        assertTrue(issue.contains("DEMO-42"), "Issue should contain ID")
        
        mockHttpService.close()
    }
    
    @Test
    fun testWorkflowModelHandlesOptionalFields() = runBlocking {

        val mockHttpService = object : IHttpService {
            override suspend fun get(url: String, params: Map<String, String>): String =
                """[{"id":"148-0","rules":[]}]"""  // Missing title and name fields
            override fun close() {}
        }
        val api = YouTrackApiImpl(mockHttpService)
        
        val workflows = api.getWorkflows("TEST")
        
        assertEquals(1, workflows.size)
        assertEquals("148-0", workflows.first().id)
        assertEquals(null, workflows.first().title, "Title should be null when missing")
        assertEquals(null, workflows.first().name, "Name should be null when missing")
        assertEquals(0, workflows.first().rules.size)
        
        mockHttpService.close()
    }
    
    @Test
    fun testWorkflowRuleModelHandlesAllFields() = runBlocking {
        val mockHttpService = object : IHttpService {
            override suspend fun get(url: String, params: Map<String, String>): String =
                """[{
                    "id":"150-0",
                    "name":"test",
                    "title":"Test Rule",
                    "type":"StatelessRule",
                    "description":"Test desc",
                    "readOnly":true,
                    "enabled":false,
                    "script":"console.log('test');"
                }]"""
            override fun close() {}
        }
        val api = YouTrackApiImpl(mockHttpService)
        
        val rules = api.getWorkflowRules("148-0")
        
        val rule = rules.first()
        assertEquals("150-0", rule.id)
        assertEquals("test", rule.name)
        assertEquals("Test Rule", rule.title)
        assertEquals("StatelessRule", rule.type)
        assertEquals("Test desc", rule.description)
        assertEquals(true, rule.readOnly)
        assertEquals(false, rule.enabled)
        assertTrue(rule.script!!.contains("console.log"))
        
        mockHttpService.close()
    }
}

