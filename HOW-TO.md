# Google Calendar MCP Server Setup Guide

## 1. Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Click "Select a project" → "New Project"
3. Enter project name (e.g., "gcal-mcp") → Create
4. Select your new project from the dropdown

## 2. Enable Google Calendar API

1. In the left sidebar, go to "APIs & Services" → "Library"
2. Search for "Google Calendar API"
3. Click on it and press "Enable"

## 3. Configure OAuth Consent Screen

1. Go to "APIs & Services" → "OAuth consent screen"
2. Choose "External" user type → Create
3. Fill in required fields:
   - App name: "Google Calendar MCP"
   - User support email: Your email
   - Developer contact: Your email
4. Click "Save and Continue"
5. Skip "Scopes" screen (Save and Continue)
6. **Add Test Users**:
   - Click "Add Users"
   - Add your Google account email
   - Save and Continue
7. Review and go back to dashboard

## 4. Create OAuth2 Credentials

1. Go to "APIs & Services" → "Credentials"
2. Click "Create Credentials" → "OAuth client ID"
3. Choose "Desktop application"
4. Name it "GCal MCP Client"
5. Click "Create"
6. **Important**: Download the JSON file or copy the Client ID and Client Secret

## 5. Configure the MCP Server

1. In your project directory, copy the template:
   ```bash
   cp config.properties.template config.properties
   ```

2. Edit `config.properties` with your credentials:
   ```properties
   google.oauth.client.id=YOUR_CLIENT_ID_HERE
   google.oauth.client.secret=YOUR_CLIENT_SECRET_HERE
   google.application.name=Google Calendar MCP
   ```

3. **Important**: If using the JAR directly (not via gradle), also copy the config file to the JAR directory:
   ```bash
   cp config.properties build/libs/
   ```

## 6. Build and Run

```bash
# Build the server
./gradlew shadowJar

# Run the server
./gradlew run
```

## 7. First Run Authorization

1. The server will automatically open your browser
2. Sign in with the Google account you added as a test user
3. Grant permission to access your Google Calendar
4. The browser will show "Received verification code. You may now close this window."
5. The server is now ready to use!

## Troubleshooting

**"Access blocked"**: Make sure you added your email as a test user in step 3.6

**"Client ID not found"**: Check that your `config.properties` file has the correct credentials

**"No events found"**: The server only shows events from your primary calendar

**Browser doesn't open**: Check the console for the authorization URL and open it manually

## Production Notes

For production deployment, consider:
- Publishing your OAuth app (removes test user limitations)
- Using environment variables instead of config files
- Implementing service account authentication for server-to-server access