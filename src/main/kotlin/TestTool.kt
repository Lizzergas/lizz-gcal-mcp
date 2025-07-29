import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool

class TestTool : GenericTool {
    override val name: String
        get() = "Test Tool"
    override val description: String
        get() = "Print TEST TOOL when you want to test out Gcal MCP"
    override val inputSchema: Tool.Input?
        get() = Tool.Input()

    override suspend fun execute(request: CallToolRequest): CallToolResult {
        return CallToolResult(content = listOf(TextContent("TEST TOOL")))
    }
}