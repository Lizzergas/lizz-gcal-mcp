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

    runBlocking {
        server.connect(transport)
        val done = Job()
        server.onClose {
            done.complete()
        }
        done.join()
    }
}