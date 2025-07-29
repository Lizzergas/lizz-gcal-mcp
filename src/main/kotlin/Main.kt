import io.ktor.utils.io.streams.*
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.buffered

fun main() {
    `run mcp server`()
}

fun `run mcp server`() {
    // Initialize Google Calendar authentication early
    System.err.println("Initializing Google Calendar authentication...")
    try {
        val httpTransport = com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport()
        GoogleAuthService.getCredential(httpTransport)
        System.err.println("Google Calendar authentication initialized successfully")
    } catch (e: Exception) {
        System.err.println("Warning: Failed to initialize Google Calendar authentication: ${e.message}")
        System.err.println("The server will still start, but calendar operations may fail.")
    }
    
    val server = Server(
        serverInfo = Implementation(
            name = "Lizz Gcal MCP",
            version = "0.0.1",
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(tools = ServerCapabilities.Tools(listChanged = true))
        )
    )

    val transport = StdioServerTransport(
        inputStream = System.`in`.asInput(),
        outputStream = System.out.asSink().buffered()
    )

    // Register tools
    TestTool().register(server)
    GetEventsTool().register(server)
    CreateEventTool().register(server)

    runBlocking {
        server.connect(transport)
        val done = Job()
        server.onClose {
            done.complete()
        }
        done.join()
    }
}