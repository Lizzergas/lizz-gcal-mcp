import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.CalendarScopes
import java.io.File
import java.util.Properties

object GoogleAuthService {
    private val JSON_FACTORY = GsonFactory.getDefaultInstance()
    private val CREDENTIALS_FILE = File(System.getProperty("user.home"), ".gcal-mcp-credentials.properties")
    private val SCOPES = setOf(CalendarScopes.CALENDAR)
    private val SCOPE_HASH = SCOPES.hashCode().toString()
    
    // OAuth2 credentials from configuration
    private val CLIENT_ID: String by lazy { Config.getGoogleClientId() }
    private val CLIENT_SECRET: String by lazy { Config.getGoogleClientSecret() }
    
    private var cachedCredential: Credential? = null
    
    fun getCredential(httpTransport: NetHttpTransport): Credential {
        // Return cached credential if available
        cachedCredential?.let { 
            if (it.accessToken != null || it.refreshToken != null) {
                return it
            }
        }
        
        // Try to load refresh token from file
        val refreshToken = loadRefreshToken()
        
        return if (refreshToken != null) {
            System.err.println("Using stored refresh token")
            createCredentialFromRefreshToken(httpTransport, refreshToken)
        } else {
            System.err.println("No refresh token found. Starting OAuth2 authorization flow...")
            System.err.println("A browser window will open for authorization.")
            val credential = authorizeNewCredential(httpTransport)
            saveRefreshToken(credential.refreshToken)
            credential
        }.also { cachedCredential = it }
    }
    
    private fun createCredentialFromRefreshToken(httpTransport: NetHttpTransport, refreshToken: String): Credential {
        return try {
            val tokenResponse = GoogleRefreshTokenRequest(
                httpTransport,
                JSON_FACTORY,
                refreshToken,
                CLIENT_ID,
                CLIENT_SECRET
            ).execute()
            
            object : Credential(
                Credential.Builder(
                    com.google.api.client.auth.oauth2.BearerToken.authorizationHeaderAccessMethod()
                ).setTransport(httpTransport)
                    .setJsonFactory(JSON_FACTORY)
                    .setTokenServerUrl(com.google.api.client.http.GenericUrl(
                        com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants.TOKEN_SERVER_URL
                    ))
                    .setClientAuthentication(
                        com.google.api.client.auth.oauth2.ClientParametersAuthentication(CLIENT_ID, CLIENT_SECRET)
                    )
            ) {
                init {
                    setAccessToken(tokenResponse.accessToken)
                    setRefreshToken(refreshToken)
                    setExpiresInSeconds(tokenResponse.expiresInSeconds)
                }
            }
        } catch (e: Exception) {
            System.err.println("Failed to use refresh token: ${e.message}")
            System.err.println("Starting new authorization flow...")
            val credential = authorizeNewCredential(httpTransport)
            saveRefreshToken(credential.refreshToken)
            credential
        }
    }
    
    private fun authorizeNewCredential(httpTransport: NetHttpTransport): Credential {
        val flow = GoogleAuthorizationCodeFlow.Builder(
            httpTransport,
            JSON_FACTORY,
            CLIENT_ID,
            CLIENT_SECRET,
            SCOPES
        )
        .setAccessType("offline")
        .setApprovalPrompt("force")
        .build()
        
        val receiver = LocalServerReceiver.Builder()
            .setPort(8888)
            .setCallbackPath("/Callback")
            .build()
            
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }
    
    private fun loadRefreshToken(): String? {
        if (!CREDENTIALS_FILE.exists()) return null
        
        return try {
            val props = Properties()
            CREDENTIALS_FILE.inputStream().use { props.load(it) }
            val storedScope = props.getProperty("scope_hash")
            if (storedScope != SCOPE_HASH) {
                System.err.println("OAuth scopes have changed. Re-authorization required.")
                return null
            }
            props.getProperty("refresh_token")
        } catch (e: Exception) {
            System.err.println("Error loading refresh token: ${e.message}")
            null
        }
    }
    
    private fun saveRefreshToken(refreshToken: String?) {
        if (refreshToken == null) return
        
        try {
            val props = Properties()
            props.setProperty("refresh_token", refreshToken)
            props.setProperty("scope_hash", SCOPE_HASH)
            CREDENTIALS_FILE.outputStream().use { 
                props.store(it, "Google Calendar MCP Credentials")
            }
            System.err.println("Refresh token saved to ${CREDENTIALS_FILE.absolutePath}")
        } catch (e: Exception) {
            System.err.println("Error saving refresh token: ${e.message}")
        }
    }
}