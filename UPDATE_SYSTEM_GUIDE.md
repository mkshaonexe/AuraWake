# Update Notification System - Setup Guide

This guide explains how to set up and use the update notification system in AuraWake.

## 📋 Overview

The update notification system automatically checks for new app versions and notifies users when updates are available. It includes:

- **Automatic update checking** on app startup (once per day)
- **Beautiful update dialog** with changelog display
- **Critical update support** for mandatory updates
- **Analytics tracking** for update events
- **Supabase backend** for version management

## 🚀 Setup Instructions

### 1. Set Up Supabase Database

Run the SQL script to create the necessary tables:

```bash
# Execute the SQL file in your Supabase SQL Editor
cat supabase_app_versions.sql
```

Or manually run the SQL in your Supabase dashboard:
1. Go to https://app.supabase.com
2. Select your project
3. Navigate to SQL Editor
4. Copy and paste the contents of `supabase_app_versions.sql`
5. Click "Run"

### 2. Verify Database Tables

After running the SQL, verify these tables exist:
- `app_versions` - Stores version information
- `update_analytics` - Tracks update events (optional)

### 3. Build and Test

```bash
# Sync Gradle dependencies
./gradlew build

# Run the app
./gradlew installDebug
```

## 📱 How It Works

### For Users

1. **Automatic Checks**: The app checks for updates once per day on startup
2. **Update Dialog**: When a new version is available, a beautiful dialog appears
3. **Changelog**: Users can see what's new in the update
4. **Update Action**: Tapping "Update" opens the Play Store
5. **Dismiss Option**: Users can dismiss non-critical updates

### For Developers

#### Adding a New Version

When you release a new version, add it to Supabase:

```sql
INSERT INTO app_versions (
    version_code, 
    version_name, 
    changelog, 
    is_critical,
    min_supported_version
)
VALUES (
    2,  -- Increment version code
    '1.1.0',  -- New version name
    '✨ New Features

• Added social features
• Community page
• Google Sign-In

🐛 Bug Fixes

• Fixed alarm issues
• UI improvements',
    false,  -- Set to true for critical updates
    1  -- Minimum supported version
);
```

#### Update App Version

Don't forget to update the version in `app/build.gradle.kts`:

```kotlin
defaultConfig {
    applicationId = "com.aura.wake"
    minSdk = 24
    targetSdk = 36
    versionCode = 2  // Increment this
    versionName = "1.1.0"  // Update this
    // ...
}
```

#### Update Changelog File

Update `CHANGELOG.md` with the new version details.

## 🎨 Customization

### Update Dialog Appearance

Edit `UpdateAvailableDialog.kt` to customize:
- Colors and gradients
- Icon and layout
- Button styles
- Text content

### Update Check Frequency

Modify the check interval in `UpdateRepository.kt`:

```kotlin
fun shouldCheckForUpdates(): Boolean {
    val prefs = context.getSharedPreferences("update_prefs", Context.MODE_PRIVATE)
    val lastCheck = prefs.getLong("last_update_check", 0)
    val oneDayInMillis = 24 * 60 * 60 * 1000  // Change this
    return System.currentTimeMillis() - lastCheck > oneDayInMillis
}
```

### Force Update Check

To manually trigger an update check:

```kotlin
updateViewModel.checkForUpdates(forceCheck = true)
```

## 🔒 Security

### Row Level Security (RLS)

The Supabase tables have RLS enabled:
- **Read**: Anyone can read version info (for update checks)
- **Write**: Only admins can insert/update versions

To allow admin writes, update the policies in Supabase:

```sql
-- Example: Allow service role to insert
CREATE POLICY "Service role can insert versions"
    ON app_versions
    FOR INSERT
    WITH CHECK (auth.role() = 'service_role');
```

## 📊 Analytics

The system automatically tracks:
- `update_checked` - When users check for updates
- `update_initiated` - When users tap "Update"
- `update_dismissed` - When users dismiss updates

View these events in Firebase Analytics.

## 🐛 Troubleshooting

### Update Check Not Working

1. **Check Supabase Connection**: Verify `BuildConfig.kt` has correct credentials
2. **Check Network**: Ensure device has internet connection
3. **Check Logs**: Look for errors in Logcat with tag "UpdateViewModel"
4. **Verify Table**: Ensure `app_versions` table exists and has data

### Dialog Not Showing

1. **Check Version**: Ensure Supabase has a higher version code
2. **Check Dismissed**: User may have dismissed this version
3. **Check Frequency**: Update check may be rate-limited (once per day)
4. **Force Check**: Use `checkForUpdates(forceCheck = true)`

### Critical Update Not Blocking

Ensure `is_critical = true` in the database and version code is higher than current.

## 🔄 Update Flow Diagram

```
App Startup
    ↓
Check if 24h passed since last check
    ↓ (Yes)
Fetch latest version from Supabase
    ↓
Compare with current version
    ↓ (New version available)
Check if user dismissed this version
    ↓ (Not dismissed OR critical)
Show Update Dialog
    ↓
User taps "Update"
    ↓
Open Play Store
    ↓
User installs update
    ↓
App restarts with new version
```

## 📝 Best Practices

1. **Always test updates** in a staging environment first
2. **Write clear changelogs** that highlight user-facing changes
3. **Use critical updates sparingly** - only for security or breaking changes
4. **Monitor analytics** to see update adoption rates
5. **Keep version history** in the database for reference

## 🎯 Future Enhancements

Potential improvements:
- In-app update using Google Play Core Library
- Background download of APK
- Automatic update installation
- Update scheduling (install later)
- Update size display
- Rollback mechanism

## 📞 Support

If you encounter issues:
1. Check the logs in Logcat
2. Verify Supabase connection
3. Review the SQL schema
4. Check Firebase Analytics for events

---

**Last Updated**: December 21, 2024
**Version**: 1.0
