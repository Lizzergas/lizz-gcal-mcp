# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

This is a Kotlin-based Model Context Protocol (MCP) server that provides Google Calendar management functionality. The project uses Gradle for build management and is designed to run as a standalone JAR that can be integrated with Claude Code.

## Setup Requirements

Before building, you need to configure OAuth2 credentials:
1. Copy `config.properties.template` to `config.properties`
2. Fill in your Google OAuth2 credentials from Google Cloud Console

## Build and Run Commands

```bash
# Build the executable JAR
./gradlew shadowJar
# or
./gradlew build

# Run the server directly with Gradle
./gradlew run

# Run the standalone JAR
java -jar build/libs/lizz-gcal-mcp-1.0-SNAPSHOT-all.jar

# Run tests
./gradlew test
```

## Configuration

- OAuth2 credentials are loaded from `config.properties` or environment variables
- The server automatically handles OAuth authorization flow on first run
- Credentials are stored in `~/.gcal-mcp-credentials.properties` for subsequent runs

## Architecture

The codebase follows a simple MCP server architecture:

- **Main.kt**: Entry point that sets up the MCP server with STDIO transport and registers available tools
- **Tool Classes**: Each MCP tool is implemented as a separate class (e.g., `TestTool.kt`, `GetEventsTool.kt`) that extends `GenericTool`
- **GenericTool.kt**: Base class for all tools that handles registration with the MCP server
- **Dependencies**: Uses the Model Context Protocol Kotlin SDK (`io.modelcontextprotocol:kotlin-sdk`) and Google Calendar API client libraries

The server communicates via STDIO, making it suitable for integration with Claude Code and other MCP-compatible clients.

## Key Technical Details

- **JVM Version**: 23
- **Kotlin Version**: 2.2.0
- **Build System**: Gradle with Shadow plugin for creating fat JARs
- **MCP SDK Version**: 0.6.0
- **Main Class**: `MainKt`

## Current State

The project is in early development with a stub implementation of the GetEventsTool. The TODO section in README.md indicates planned tools for calendar event management (create, list, update, delete).