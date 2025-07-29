# Google Calendar MCP Server

A Kotlin-based Model Context Protocol (MCP) server that provides Google Calendar management functionality for AI assistants like Claude.

## Prerequisites

- JVM 23
- Gradle 8.13+ (wrapper included)
- Google account with Calendar access

## Setup

📋 **[Complete Setup Guide](HOW-TO.md)** - Follow the step-by-step instructions to configure Google Cloud, OAuth2 credentials, and run your first calendar operation.

## Quick Commands

```bash
# Build and run the server
./gradlew shadowJar && ./gradlew run

# Or run the JAR directly
java -jar build/libs/lizz-gcal-mcp-1.0-SNAPSHOT-all.jar
```

## Claude Code Integration

Add this configuration to your Claude Code settings:

```json
{
  "lizz-gcal-mcp": {
    "command": "java",
    "args": [
      "-jar",
      "/path/to/your/project/build/libs/lizz-gcal-mcp-1.0-SNAPSHOT-all.jar"
    ]
  }
}
```

Replace `/path/to/your/project/` with the actual path to your project directory.

## Available Tools

- `get_todays_events` - Get today's calendar events
- `create_event` - Create a new calendar event with title, date/time, and optional parameters

## Tool Usage Examples

### Create Event
```
create_event with:
- title: "Meeting with Thomas" (required)
- datetime: "2025-08-05 12:00" (required, format: YYYY-MM-DD HH:MM)
- duration_minutes: 60 (optional, default: 60)
- description: "Discuss project updates" (optional)
- location: "Conference Room A" (optional)
- attendees: ["thomas@example.com"] (optional)
- reminder_minutes: 15 (optional, default: 10)
```

For all-day events, use date only: "2025-08-05"

## TODO: Future Tools

- `list_events` - List calendar events for a date range
- `update_event` - Update an existing calendar event
- `delete_event` - Delete a calendar event