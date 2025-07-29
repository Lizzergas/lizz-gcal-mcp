import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server

interface GenericTool {
    val name: String
    val description: String
    val inputSchema: Tool.Input?

    suspend fun execute(request: CallToolRequest): CallToolResult

    fun register(server: Server) {
        val schema = inputSchema
        if (schema != null) {
            server.addTool(
                name = name,
                description = description,
                inputSchema = schema
            ) { request ->
                execute(request)
            }
        } else {
            server.addTool(
                name = name,
                description = description
            ) { request ->
                execute(request)
            }
        }
    }
}