# Google Calendar MCP Server

A Kotlin-based Model Context Protocol (MCP) server that provides Google Calendar management functionality for AI assistants like Claude.

## Prerequisites

- JVM 23
- Gradle 8.13+ (wrapper included)

## Quick Start

### Build the Server

```bash
# Create the executable JAR
./gradlew shadowJar
or
./gradlew build
```

### Run the Server

```bash
# Run directly with Gradle
./gradlew run

# Or run the JAR file
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

## TODO: Available Tools

- `create_event` - Create a new calendar event
- `list_events` - List calendar events
- `update_event` - Update an existing calendar event
- `delete_event` - Delete a calendar event