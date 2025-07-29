import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.EventAttendee
import com.google.api.services.calendar.model.EventReminder
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.util.DateTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.TimeZone

class GoogleCalendarService {
    private val APPLICATION_NAME = Config.getApplicationName()
    private val JSON_FACTORY = GsonFactory.getDefaultInstance()
    private val httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
    
    private fun getCalendarService(): Calendar {
        val credential = GoogleAuthService.getCredential(httpTransport)
        return Calendar.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }
    
    fun getTodaysEvents(): List<Event> {
        val service = getCalendarService()
        
        // Get start and end of today
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        
        val events = service.events().list("primary")
            .setTimeMin(DateTime(Date.from(startOfDay)))
            .setTimeMax(DateTime(Date.from(endOfDay)))
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .execute()
        
        return events.items ?: emptyList()
    }
    
    fun formatEvent(event: Event): String {
        val start = event.start?.dateTime ?: event.start?.date
        val end = event.end?.dateTime ?: event.end?.date
        
        val startStr = if (start != null) {
            if (start.isDateOnly) {
                "All day"
            } else {
                val instant = java.time.Instant.ofEpochMilli(start.value)
                val localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
                localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            }
        } else {
            "No start time"
        }
        
        return buildString {
            append("${event.summary ?: "No title"} - $startStr")
            event.location?.let { append(" at $it") }
            event.description?.let { append("\n  Description: $it") }
        }
    }
    
    fun createEvent(
        title: String,
        startDateTime: LocalDateTime,
        durationMinutes: Int = 60,
        description: String? = null,
        location: String? = null,
        attendees: List<String> = emptyList(),
        reminderMinutes: Int = 10,
        isAllDay: Boolean = false
    ): String {
        val service = getCalendarService()
        val event = Event()
        
        // Set basic properties
        event.summary = title
        event.description = description
        event.location = location
        
        // Set date/time
        val zoneId = ZoneId.systemDefault()
        val timeZone = TimeZone.getTimeZone(zoneId)
        
        if (isAllDay) {
            // All-day event
            val startDate = DateTime(true, startDateTime.toLocalDate().toEpochDay() * 86400000, 0)
            val endDate = DateTime(true, startDateTime.toLocalDate().plusDays(1).toEpochDay() * 86400000, 0)
            
            event.start = EventDateTime()
                .setDate(startDate)
                .setTimeZone(timeZone.id)
                
            event.end = EventDateTime()
                .setDate(endDate)
                .setTimeZone(timeZone.id)
        } else {
            // Timed event
            val startInstant = startDateTime.atZone(zoneId).toInstant()
            val endInstant = startDateTime.plusMinutes(durationMinutes.toLong()).atZone(zoneId).toInstant()
            
            event.start = EventDateTime()
                .setDateTime(DateTime(Date.from(startInstant)))
                .setTimeZone(timeZone.id)
                
            event.end = EventDateTime()
                .setDateTime(DateTime(Date.from(endInstant)))
                .setTimeZone(timeZone.id)
        }
        
        // Add attendees
        if (attendees.isNotEmpty()) {
            event.attendees = attendees.map { email ->
                EventAttendee().setEmail(email)
            }
        }
        
        // Add reminder
        val reminder = EventReminder()
            .setMethod("popup")
            .setMinutes(reminderMinutes)
            
        event.reminders = Event.Reminders()
            .setUseDefault(false)
            .setOverrides(listOf(reminder))
        
        // Create the event
        val createdEvent = service.events().insert("primary", event).execute()
        
        return createdEvent.id
    }
}