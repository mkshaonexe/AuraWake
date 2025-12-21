# 🔔 Update Notification System

## Overview

A complete, production-ready update notification system for AuraWake that automatically notifies users when new app versions are available.

## ✨ Features

- ✅ **Automatic Update Checking** - Checks for updates once per day on app startup
- ✅ **Beautiful UI** - Modern Material Design 3 dialog with gradients and animations
- ✅ **Changelog Display** - Shows users what's new in the update
- ✅ **Critical Updates** - Support for mandatory updates that block app usage
- ✅ **Smart Dismissal** - Remembers dismissed updates (except critical ones)
- ✅ **Analytics Integration** - Tracks update events in Firebase Analytics
- ✅ **Supabase Backend** - Cloud-based version management
- ✅ **Play Store Integration** - One-tap update via Play Store

## 🚀 Quick Start

### 1. Set Up Supabase Database

```bash
# Copy and run the SQL in your Supabase SQL Editor
cat supabase_app_versions.sql
```

### 2. Build the App

```bash
./gradlew build
```

### 3. Test It

```bash
# Install the app
./gradlew installDebug

# Add a test version in Supabase to see the dialog
```

## 📚 Documentation

- **[UPDATE_IMPLEMENTATION_SUMMARY.md](UPDATE_IMPLEMENTATION_SUMMARY.md)** - Complete implementation details
- **[UPDATE_SYSTEM_GUIDE.md](UPDATE_SYSTEM_GUIDE.md)** - Comprehensive setup and usage guide
- **[QUICK_UPDATE_GUIDE.md](QUICK_UPDATE_GUIDE.md)** - Quick reference for publishing updates
- **[CHANGELOG.md](CHANGELOG.md)** - Version history

## 🎯 How to Publish an Update

### Quick Steps:

1. **Update version** in `app/build.gradle.kts`
   ```kotlin
   versionCode = 2
   versionName = "1.1.0"
   ```

2. **Update changelog** in `CHANGELOG.md`

3. **Add version to Supabase**
   ```sql
   INSERT INTO app_versions (version_code, version_name, changelog)
   VALUES (2, '1.1.0', 'Your changelog here');
   ```

4. **Build and upload** to Play Store
   ```bash
   ./gradlew bundleRelease
   ```

See [QUICK_UPDATE_GUIDE.md](QUICK_UPDATE_GUIDE.md) for detailed steps.

## 🎨 Screenshots

### Update Dialog (Normal)
- Green gradient background
- Shows version info and changelog
- "Update" and "Later" buttons

### Update Dialog (Critical)
- Red gradient background
- Shows warning message
- Only "Update Now" button (no dismiss)

## 🔧 Technical Details

### Architecture

```
MainActivity
    ↓
UpdateViewModel
    ↓
UpdateRepository
    ↓
Supabase (app_versions table)
```

### Components

1. **Data Layer**
   - `AppVersion.kt` - Data models
   - `UpdateRepository.kt` - Business logic

2. **UI Layer**
   - `UpdateAvailableDialog.kt` - Dialog component
   - `UpdateViewModel.kt` - State management

3. **Database**
   - `app_versions` - Version storage
   - `update_analytics` - Event tracking

### Dependencies

```kotlin
// Already added to your project:
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
implementation("io.github.jan-tennert.supabase:postgrest-kt:3.2.6")
```

## 📊 Analytics Events

The system tracks:
- `update_checked` - Update availability checked
- `update_initiated` - User tapped "Update"
- `update_dismissed` - User dismissed update

View in Firebase Console → Analytics → Events

## 🔒 Security

- **Row Level Security (RLS)** enabled on Supabase tables
- **Read-only access** for app users
- **Admin-only writes** for version management
- **HTTPS communication** via Supabase

## 🐛 Troubleshooting

### Dialog Not Showing?

1. Check if 24 hours passed since last check
2. Verify Supabase has a higher version code
3. Check if user dismissed this version
4. Force check: `updateViewModel.checkForUpdates(forceCheck = true)`

### Supabase Connection Error?

1. Verify credentials in `BuildConfig.kt`
2. Check internet connection
3. Verify `app_versions` table exists
4. Check Supabase dashboard for errors

### Build Errors?

1. Sync Gradle files
2. Clean project: `./gradlew clean`
3. Rebuild: `./gradlew build`
4. Check all imports are correct

## 💡 Best Practices

1. ✅ Test updates in staging before production
2. ✅ Write clear, user-friendly changelogs
3. ✅ Use semantic versioning (1.0.0)
4. ✅ Reserve critical updates for emergencies
5. ✅ Monitor update adoption in analytics
6. ✅ Use Play Store's staged rollout

## 🎓 Learn More

- [Supabase Documentation](https://supabase.com/docs)
- [Material Design 3](https://m3.material.io/)
- [Android App Updates](https://developer.android.com/guide/playcore/in-app-updates)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)

## 📝 Example: Adding Version 1.1.0

### 1. Update build.gradle.kts
```kotlin
versionCode = 2
versionName = "1.1.0"
```

### 2. Update CHANGELOG.md
```markdown
## Version 1.1.0 (Build 2) - December 21, 2024

### ✨ New Features
- Added social features
- Community page

### 🐛 Bug Fixes
- Fixed alarm issues
```

### 3. Add to Supabase
```sql
INSERT INTO app_versions (version_code, version_name, changelog, is_critical)
VALUES (
    2,
    '1.1.0',
    '✨ New Features
• Added social features
• Community page

🐛 Bug Fixes
• Fixed alarm issues',
    false
);
```

### 4. Build & Upload
```bash
./gradlew bundleRelease
# Upload to Play Store Console
```

## 🎉 Success!

Your update notification system is now ready! Users will automatically be notified when you publish new versions.

---

**Status**: ✅ Production Ready  
**Last Updated**: December 21, 2024  
**Version**: 1.0  

For questions or issues, check the documentation files or review the code comments.
