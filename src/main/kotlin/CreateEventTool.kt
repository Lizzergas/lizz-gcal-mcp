import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import kotlinx.serialization.json.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class CreateEventTool : GenericTool {
    override val name: String
        get() = "create_event"
    override val description: String
        get() = "Create a new event in Google Calendar"
    override val inputSchema: Tool.Input?
        get() = Tool.Input(
            properties = buildJsonObject {
                putJsonObject("title") {
                    put("type", "string")
                    put("description", "The title/summary of the event (required)")
                }
                putJsonObject("datetime") {
                    put("type", "string")
                    put(
                        "description",
                        "Event date and time. Accepts: 'YYYY-MM-DD HH:MM', 'YYYY-MM-DD' for all-day, or partial info like '10am', 'tomorrow 3pm', etc. If incomplete, we'll ask for clarification."
                    )
                }
                putJsonObject("duration_minutes") {
                    put("type", "number")
                    put("description", "Duration of the event in minutes (default: 60)")
                }
                putJsonObject("description") {
                    put("type", "string")
                    put("description", "Event description (optional)")
                }
                putJsonObject("location") {
                    put("type", "string")
                    put("description", "Event location (optional)")
                }
                putJsonObject("attendees") {
                    put("type", "array")
                    put("description", "List of attendee email addresses (optional)")
                    putJsonObject("items") {
                        put("type", "string")
                    }
                }
                putJsonObject("reminder_minutes") {
                    put("type", "number")
                    put("description", "Minutes before event to send reminder (optional, default: 10)")
                }
            },
            required = listOf("title", "datetime")
        )

    override suspend fun execute(request: CallToolRequest): CallToolResult {
        return try {
            val arguments = request.arguments

            val title = arguments.jsonObject["title"]?.jsonPrimitive?.content
                ?: return CallToolResult(listOf(TextContent("Title is required")), isError = true)

            val datetimeStr = arguments.jsonObject["datetime"]?.jsonPrimitive?.content
                ?: return CallToolResult(listOf(TextContent("Datetime is required")), isError = true)

            val durationMinutes = arguments.jsonObject["duration_minutes"]?.jsonPrimitive?.intOrNull ?: 60
            val description = arguments.jsonObject["description"]?.jsonPrimitive?.contentOrNull
            val location = arguments.jsonObject["location"]?.jsonPrimitive?.contentOrNull
            val attendees = arguments.jsonObject["attendees"]?.jsonArray?.map {
                it.jsonPrimitive.content
            } ?: emptyList()
            val reminderMinutes = arguments.jsonObject["reminder_minutes"]?.jsonPrimitive?.intOrNull ?: 10

            // Parse datetime - check if it needs clarification
            val parseResult = parseDatetimeWithElicitation(datetimeStr)
            when (parseResult) {
                is DatetimeParseResult.NeedsClarification -> {
                    return CallToolResult(listOf(TextContent(parseResult.message)))
                }

                is DatetimeParseResult.Success -> {
                    val (startDateTime, isAllDay) = parseResult.value

                    val calendarService = GoogleCalendarService()
                    val eventId = calendarService.createEvent(
                        title = title,
                        startDateTime = startDateTime,
                        durationMinutes = durationMinutes,
                        description = description,
                        location = location,
                        attendees = attendees,
                        reminderMinutes = reminderMinutes,
                        isAllDay = isAllDay
                    )

                    val resultMessage = buildString {
                        append("Event created successfully!\n")
                        append("Title: $title\n")
                        append("Date/Time: ${formatDateTimeForDisplay(startDateTime, isAllDay)}\n")
                        if (!isAllDay) append("Duration: $durationMinutes minutes\n")
                        location?.let { append("Location: $it\n") }
                        if (attendees.isNotEmpty()) append("Attendees: ${attendees.joinToString(", ")}\n")
                        append("Event ID: $eventId")
                    }

                    CallToolResult(listOf(TextContent(resultMessage)))
                }
            }
        } catch (e: Exception) {
            CallToolResult(
                listOf(TextContent("Error creating event: ${e.message}")),
                isError = true
            )
        }
    }

    sealed class DatetimeParseResult {
        data class Success(val value: Pair<LocalDateTime, Boolean>) : DatetimeParseResult()
        data class NeedsClarification(val message: String) : DatetimeParseResult()
    }

    private fun parseDatetimeWithElicitation(datetimeStr: String): DatetimeParseResult {
        val input = datetimeStr.trim().lowercase()
        val today = LocalDate.now()

        // First try standard formats
        try {
            // Try full datetime format
            val dateTime = LocalDateTime.parse(datetimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            return DatetimeParseResult.Success(Pair(dateTime, false))
        } catch (e: DateTimeParseException) {
            // Try date only format
            try {
                val date = LocalDate.parse(datetimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                return DatetimeParseResult.Success(Pair(date.atStartOfDay(), true))
            } catch (e2: DateTimeParseException) {
                // Continue to fuzzy parsing
            }
        }

        // Handle time-only inputs (e.g., "10am", "3:30pm", "15:00")
        val timeOnlyPattern = """^(\d{1,2}):?(\d{0,2})\s*(am|pm)?$""".toRegex()
        val timeMatch = timeOnlyPattern.find(input)
        if (timeMatch != null) {
            val (_, hourStr, minuteStr, amPm) = timeMatch.groupValues
            var hour = hourStr.toInt()
            val minute = minuteStr.toIntOrNull() ?: 0

            // Handle AM/PM
            if (amPm == "pm" && hour < 12) hour += 12
            if (amPm == "am" && hour == 12) hour = 0

            return DatetimeParseResult.NeedsClarification(
                "I need to know which date for the event at ${formatTime(hour, minute)}. Please specify:\n" +
                        "- Today (${today.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))})\n" +
                        "- Tomorrow (${today.plusDays(1).format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))})\n" +
                        "- A specific date (e.g., '2025-08-05 ${formatTime(hour, minute)}')"
            )
        }

        // Handle relative dates without time
        when {
            input == "today" -> {
                return DatetimeParseResult.NeedsClarification(
                    "What time today should I schedule '$datetimeStr'? Please provide a time (e.g., '${today} 14:00')"
                )
            }

            input == "tomorrow" -> {
                val tomorrow = today.plusDays(1)
                return DatetimeParseResult.NeedsClarification(
                    "What time tomorrow should I schedule the event? Please provide a time (e.g., '${tomorrow} 10:00')"
                )
            }

            input.contains("next") -> {
                return DatetimeParseResult.NeedsClarification(
                    "Please provide a specific date and time for '$datetimeStr' (e.g., '2025-08-05 15:00')"
                )
            }
        }

        // If we can't parse it at all, ask for clarification
        return DatetimeParseResult.NeedsClarification(
            "I couldn't understand the date/time '$datetimeStr'. Please provide it in one of these formats:\n" +
                    "- Full date and time: '2025-08-05 14:30'\n" +
                    "- All-day event: '2025-08-05'\n" +
                    "- Or describe it more specifically"
        )
    }

    private fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }

    private fun formatDateTimeForDisplay(dateTime: LocalDateTime, isAllDay: Boolean): String {
        return if (isAllDay) {
            dateTime.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) + " (all day)"
        } else {
            dateTime.format(DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm"))
        }
    }
}