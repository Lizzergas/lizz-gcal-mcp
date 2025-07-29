import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.TextContent
import kotlinx.serialization.json.buildJsonObject

class GetEventsTool : GenericTool {
    override val name: String
        get() = "get_todays_events"
    override val description: String
        get() = "Get today's events from Google Calendar"
    override val inputSchema: Tool.Input?
        get() = Tool.Input(
            properties = buildJsonObject {
                // No parameters needed for now - just gets today's events
            }
        )

    override suspend fun execute(request: CallToolRequest): CallToolResult {
        return try {
            val calendarService = GoogleCalendarService()
            val events = calendarService.getTodaysEvents()
            
            if (events.isEmpty()) {
                CallToolResult(listOf(TextContent("No events scheduled for today.")))
            } else {
                val eventList = events.mapIndexed { index, event ->
                    "${index + 1}. ${calendarService.formatEvent(event)}"
                }.joinToString("\n\n")
                
                CallToolResult(listOf(TextContent("Today's events:\n\n$eventList")))
            }
        } catch (e: Exception) {
            CallToolResult(
                listOf(TextContent("Error fetching calendar events: ${e.message}")),
                isError = true
            )
        }
    }
}