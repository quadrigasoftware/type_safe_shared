# Connecting Your Okta Organization to Quadriga Applications

This guide explains how to authorize a Quadriga application (e.g., Portfolio-AI) to securely access your organization's directory (Users and Groups) using the Okta API.

## Overview
The integration uses an **Okta OIDC Application** as a secure bridge between Portfolio-AI and your Okta tenant.

## 1. Create the Okta Application
1.  Sign in to your [Okta Admin Console](https://admin.okta.com/).
2.  Navigate to **Applications > Applications**.
3.  Click **Create App Integration**.
4.  **Sign-in method**: Select **OIDC - OpenID Connect**.
5.  **Application type**: Select **Web Application**.
6.  **Name**: "Quadriga Integration - [App Name] - [Company Name]"
7.  **Sign-in redirect URIs**:
    -   `https://[app-name]-[your-company].quadrigasoftware.com/callback/okta`
8.  **Assignments**: Select your desired controlled access (e.g., "Allow everyone in your organization").
9.  Click **Save**.

## 2. Enable the App "Tile" (Optional)
To allow employees to launch the app directly from their Okta dashboard:
1.  In the app settings, go to the **General** tab.
2.  Click **Edit** in the **General Settings** section.
3.  Set **Login initiated by** to **Either Okta or App**.
4.  Set **Initiate login URI** to:
    -   `https://[app-name]-[your-company].quadrigasoftware.com/login/okta`
5.  Save.

## 3. Configure API Scopes
Ensure the application has permission to read user profiles and groups:
1.  Navigate to the **Okta API Scopes** tab (if using Okta Identity Engine) or ensure the following scopes are requested by the app:
    -   `openid`, `profile`, `email`
    -   `okta.users.read`
    -   `okta.groups.read`

## 4. Summary Checklist
Please provide the following to the Quadriga hosting team:
- [ ] **Application Name** (e.g., Portfolio-AI)
- [ ] **Client ID**
- [ ] **Client Secret**
- [ ] **Okta Domain** (e.g., `dev-12345.okta.com`)
- [ ] **Organization Domain** (e.g., `acme.com`)
