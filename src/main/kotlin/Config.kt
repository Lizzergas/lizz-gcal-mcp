import java.io.File
import java.util.*

object Config {
    private val properties = Properties()

    init {
        loadConfig()
    }

    private fun loadConfig() {
        // Try multiple locations for config.properties
        val configLocations = listOfNotNull(
            // 1. Same directory as the JAR
            getJarDirectory()?.let { File(it, "config.properties") },
            // 2. Current working directory
            File("config.properties"),
            // 3. User home directory
            File(System.getProperty("user.home"), ".gcal-mcp-config.properties")
        )

        var configLoaded = false

        for (configFile in configLocations) {
            if (configFile.exists()) {
                try {
                    configFile.inputStream().use {
                        properties.load(it)
                    }
                    System.err.println("Loaded config from: ${configFile.absolutePath}")
                    configLoaded = true
                    break
                } catch (e: Exception) {
                    System.err.println("Error loading config file ${configFile.absolutePath}: ${e.message}")
                }
            }
        }

        if (!configLoaded) {
            System.err.println("Warning: config.properties not found in any of these locations:")
            configLocations.forEach {
                System.err.println("  - ${it.absolutePath}")
            }
            System.err.println("Using environment variables or defaults.")
        }
    }

    private fun getJarDirectory(): File? {
        return try {
            val jarPath = Config::class.java.protectionDomain.codeSource.location.toURI().path
            val jarFile = File(jarPath)
            if (jarFile.isFile && jarFile.name.endsWith(".jar")) {
                jarFile.parentFile
            } else {
                null // Running from IDE/gradle, not from JAR
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getGoogleClientId(): String {
        return getProperty("google.oauth.client.id", "GOOGLE_CLIENT_ID")
            ?: throw IllegalStateException(
                "Google Client ID not found. Please set it in config.properties or GOOGLE_CLIENT_ID environment variable."
            )
    }

    fun getGoogleClientSecret(): String {
        return getProperty("google.oauth.client.secret", "GOOGLE_CLIENT_SECRET")
            ?: throw IllegalStateException(
                "Google Client Secret not found. Please set it in config.properties or GOOGLE_CLIENT_SECRET environment variable."
            )
    }

    fun getApplicationName(): String {
        return getProperty("google.application.name", "GOOGLE_APPLICATION_NAME") ?: "Lizz GCal MCP"
    }

    private fun getProperty(propertyName: String, envVarName: String): String? {
        // First try properties file
        properties.getProperty(propertyName)?.let { value ->
            if (value.isNotBlank() && value != "YOUR_CLIENT_ID" && value != "YOUR_CLIENT_SECRET") {
                return value
            }
        }

        // Then try environment variable
        return System.getenv(envVarName)
    }
}