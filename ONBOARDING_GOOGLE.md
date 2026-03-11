# Google Workspace Onboarding Guide

To enable Portfolio-AI for your organization, a Google Workspace Administrator must complete the following steps in the **Google Cloud Console** and the **Google Workspace Admin Console**.

## 1. Google Cloud Project Setup
1.  **Create/Select a Project**: Go to the [Google Cloud Console](https://console.cloud.google.com/) and ensure you have a project selected.
2.  **Enable Admin SDK API**:
    -   Navigate to **APIs & Services > Library**.
    -   Search for **"Admin SDK API"**.
    -   Click **Enable**. (This allows the app to see the organizational hierarchy).

## 2. Configure OAuth Consent Screen
1.  Navigate to **APIs & Services > OAuth consent screen**.
2.  **User Type**: Select **Internal** (This restricts the app to your organization only).
3.  **Scopes**: Click **Add or Remove Scopes** and manually add:
    -   `openid`, `https://www.googleapis.com/auth/userinfo.email`, `https://www.googleapis.com/auth/userinfo.profile`
    -   `https://www.googleapis.com/auth/admin.directory.user.readonly` (Required for organization search and hierarchy).
4.  Complete the app registration and save.

## 3. Create OAuth 2.0 Credentials
1.  Navigate to **APIs & Services > Credentials**.
2.  Click **Create Credentials > OAuth client ID**.
3.  **Application Type**: Select **Web application**.
4.  **Name**: "Portfolio-AI"
5.  **Authorized Redirect URIs**: Add your application's callback URL:
    -   `https://your-app-url.a.run.app/callback/google`
    -   (For local testing): `http://localhost:8080/callback/google`
6.  **Copy Credentials**: Save your **Client ID** and **Client Secret**. These will be needed for deployment.

## 4. Google Workspace Admin Permissions
By default, Google Workspace restricts organizational directory access.
1.  Go to the [Google Workspace Admin Console](https://admin.google.com/).
2.  Navigate to **Security > Access and data control > API controls**.
3.  Ensure **"Trust internal apps"** is enabled, or add your OAuth Client ID as a **Trusted App**.
4.  **Admin Role**: The user who first logs into the app to perform directory-wide functions MUST have at least **Help Desk Admin** or **User Management Admin** privileges (specifically "Users: Read" permission).

## 5. Deployment Configuration
Provide the following environment variables to your Cloud Run instance:
-   `GOOGLE_CLIENT_ID`: (From Step 3)
-   `GOOGLE_CLIENT_SECRET`: (From Step 3)
-   `SESSION_SECRET`: (A random 64-character hex string)
-   `ALLOWED_DOMAINS`: Your organization domain (e.g., `acme.com`)
