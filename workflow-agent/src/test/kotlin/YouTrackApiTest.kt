import kotlinx.coroutines.runBlocking
import org.example.api.YouTrackApiImpl
import org.example.http.IHttpService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class FakeHttpService : IHttpService {
    override suspend fun get(url: String, params: Map<String, String>): String = when {
        url.contains("/api/admin/workflows") && url.contains("query=usages.project.shortName:") ->
            """[{"id":"148-0","title":"Test Workflow","rules":[{"id":"150-0","name":"test-rule","title":"Test Rule","type":"StatelessRule"}]}]"""
        
        url.contains("/api/admin/workflows/") && url.contains("/rules") ->
            """[{"id":"150-0","name":"test-rule","title":"Test Rule","type":"StatelessRule","description":"Test description","readOnly":false,"enabled":true}]"""
        
        url.contains("/api/issues/") && url.contains("fields=id,summary,project") ->
            """{"id":"DEMO-123","summary":"Test Issue","project":{"name":"Demo project"},"description":"Test description"}"""
        
        else -> "[]"
    }

    override fun close() {}
}


class YouTrackApiTest {
    private val api = YouTrackApiImpl(FakeHttpService())

    @Test
    fun testGetWorkflows() = runBlocking {
        val workflows = api.getWorkflows("DEMO")
        assertEquals(1, workflows.size)
        assertEquals("148-0", workflows.first().id)
        assertEquals("Test Workflow", workflows.first().title)
        assertEquals(1, workflows.first().rules.size)
    }

    @Test
    fun testGetWorkflowRules() = runBlocking {
        val rules = api.getWorkflowRules("148-0")
        assertEquals(1, rules.size)
        assertEquals("150-0", rules.first().id)
        assertEquals("Test Rule", rules.first().title)
        assertEquals("StatelessRule", rules.first().type)
    }

    @Test
    fun testGetIssue() = runBlocking {
        val issue = api.getIssue("DEMO-123")
        assertTrue(issue.contains("DEMO-123"))
        assertTrue(issue.contains("Test Issue"))
        assertTrue(issue.contains("Demo project"))
    }
}