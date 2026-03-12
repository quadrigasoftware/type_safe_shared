# Connecting Portfolio-AI to Your Google Workspace

This guide explains how to authorize Portfolio-AI to securely access your organization's directory (Users, Managers, and Groups) using standard Google Workspace integration protocols.

## Overview
The integration uses a Google Cloud "Service Project" as a secure bridge between Portfolio-AI and your Workspace Directory. This ensures that you retain full ownership and control over the connection at all times.

## 1. Enable Directory Access (Admin SDK)
Your Workspace Directory is accessed via the **Admin SDK**. This must be activated in your integration project:
1.  Open the [Integration Library](https://console.cloud.google.com/apis/library/admin.googleapis.com).
2.  Click **Enable**. This allows Portfolio-AI to visualize your organizational hierarchy and team structures.

## 2. Configure Your Integration Brand
Define how the sign-in screen appears to your employees:
1.  Navigate to the [Internal Consent Configuration](https://console.cloud.google.com/apis/credentials/consent).
2.  Select **Internal** (limiting access only to your verified employees).
3.  **Scopes**: Add the following Directory permissions to allow the app to read-only the org structure:
    -   `openid`, `email`, `profile`
    -   `https://www.googleapis.com/auth/admin.directory.user.readonly` (Organization/Manager search)
    -   `https://www.googleapis.com/auth/admin.directory.group.readonly` (Team/Group discovery)

## 3. Generate Secure Integration Keys
Create the unique keys that Portfolio-AI will use to communicate with your Directory:
1.  Go to the [API Credentials Page](https://console.cloud.google.com/apis/credentials).
2.  Click **Create Credentials > OAuth client ID**.
3.  **Application Type**: Select **Web application**.
4.  **Name**: "Portfolio-AI Directory Integration"
5.  **Authorized Redirect URIs**: Add your organization's dedicated Portfolio-AI URL:
    -   `https://[your-company-subdomain].a.run.app/callback/google`
6.  **Secure Handover**: Copy the **Client ID** and **Client Secret**. These act as the "Key" and "Lock" for your integration.

## 4. Finalize Workspace Trust
To complete the connection, you must explicitly trust this integration within your Workspace security policy:
1.  Open the [Workspace Admin API Controls](https://admin.google.com/ac/owl).
2.  Ensure **"Trust internal apps"** is enabled, or manually add your **Client ID** (from Step 3) as a **Trusted App**.
3.  **Privilege Check**: The first user to authorize the app must be a Workspace Administrator with "Users: Read" permissions to verify the initial directory sync.

## 5. Summary Checklist
Please provide the following to the Portfolio-AI hosting team:
- [ ] **Directory Client ID**
- [ ] **Directory Client Secret**
- [ ] **Organization Domain** (e.g., `acme.com`)
