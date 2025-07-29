import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Tool
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

class GetEventsTool : GenericTool {
    override val name: String
        get() = "Get Events Tool"
    override val description: String
        get() = "Get events from Google Calendar"
    override val inputSchema: Tool.Input?
        get() = Tool.Input(
            properties = buildJsonObject {
                putJsonObject("paramOne") {
                    put("type", "string")
                    put("description", "Parameter One")
                    put("required", true)
                }
                putJsonObject("paramTwo") {
                    put("type", "string")
                    put("description", "Parameter Two")
                    put("required", false)
                }
            }
        )

    override suspend fun execute(request: CallToolRequest): CallToolResult {
        TODO("Not yet implemented")
    }
}