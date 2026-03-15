# Connecting Your Microsoft Entra ID to Quadriga Applications

This guide explains how to authorize a Quadriga application (e.g., Portfolio-AI) to securely access your organization's directory (Users, Managers, and Groups) using the Microsoft Graph API.

## Overview
The integration uses an **Azure App Registration** as a secure bridge between Portfolio-AI and your Entra ID (formerly Azure AD) tenant. This ensures that you retain full ownership and control over the connection.

## 1. Register the Application
1.  Sign in to the [Microsoft Entra admin center](https://entra.microsoft.com/) or [Azure Portal](https://portal.azure.com/).
2.  Navigate to **Identity > Applications > App registrations**.
3.  Click **New registration**.
4.  **Name**: "Quadriga Integration - [App Name] - [Company Name]"
5.  **Supported account types**: "Accounts in this organizational directory only (Single tenant)".
6.  **Redirect URI**: Select **Web** and enter:
    -   `https://[app-name]-[your-company].quadrigasoftware.com/callback/entra`
7.  Click **Register**.

## 2. Configure API Permissions
Portfolio-AI requires read-only access to your directory to visualize team structures:
1.  Navigate to **API permissions** in your new app registration.
2.  Click **Add a permission > Microsoft Graph > Delegated permissions**.
3.  Search for and select:
    -   `User.Read.All` (To search users and see profiles)
    -   `Directory.Read.All` (To see groups and functional relationships)
4.  **CRITICAL STEP**: Click **"Grant admin consent for [Your Organization]"**. 
    -   *Note: Without this step, individual employees will not be able to sign in.*

## 3. Generate Integration Keys
1.  Navigate to **Certificates & secrets**.
2.  Select **Client secrets > New client secret**.
3.  **Description**: "Portfolio-AI Connection"
4.  **Expiry**: We recommend 24 months.
5.  **Copy Value**: Copy the **Secret Value** immediately (it will be hidden later).
6.  **Copy Identifiers**: Go back to the **Overview** tab and copy the **Application (client) ID** and **Directory (tenant) ID**.

## 4. Summary Checklist
Please provide the following to the Quadriga hosting team:
- [ ] **Application Name** (e.g., Portfolio-AI)
- [ ] **Application (client) ID**
- [ ] **Directory (tenant) ID**
- [ ] **Client Secret Value**
- [ ] **Organization Domain** (e.g., `acme.com`)
