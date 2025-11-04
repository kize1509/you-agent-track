import kotlinx.coroutines.runBlocking
import org.example.api.YouTrackApiImpl
import org.example.http.IHttpService
import kotlin.test.Test
import kotlin.test.assertEquals

class FakeHttpService : IHttpService {
    override suspend fun get(path: String, params: Map<String, String>): String = when {
        path.contains("/projects/") -> """[{"id":"wf1","name":"Test Workflow","rules":[]}]"""  // workflows
        path.contains("/workflows/") -> """[{"id":"r1","name":"Test Rule","type":"onChange"}]"""  // rules
        path.contains("/issues/") -> """{"id":"123","summary":"Issue summary"}"""
        else -> "[]"
    }

    override fun close() {}
}


class YouTrackApiTest {
    private val api = YouTrackApiImpl(FakeHttpService())

    @Test
    fun testGetWorkflows() = runBlocking {
        val workflows = api.getWorkflows("PROJ")
        assertEquals(1, workflows.size)
        assertEquals("wf1", workflows.first().id)
    }

    @Test
    fun testGetWorkflowRules() = runBlocking {
        val rules = api.getWorkflowRules("wf1")
        assertEquals(1, rules.size)
        assertEquals("r1", rules.first().id)
    }

    @Test
    fun testGetIssue() = runBlocking {
        val issue = api.getIssue("123")
        assert(issue.contains("123"))
    }
}