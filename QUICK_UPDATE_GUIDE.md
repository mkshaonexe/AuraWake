# 🚀 Quick Reference: Publishing a New Update

## Step-by-Step Checklist

### 1️⃣ Update Version Numbers

**File**: `app/build.gradle.kts`

```kotlin
defaultConfig {
    versionCode = 2  // ← Increment this
    versionName = "1.1.0"  // ← Update this
}
```

### 2️⃣ Update Changelog

**File**: `CHANGELOG.md`

Add your new version at the top:

```markdown
## Version 1.1.0 (Build 2) - December 21, 2024

### ✨ New Features
- Feature 1
- Feature 2

### 🐛 Bug Fixes
- Fix 1
- Fix 2
```

### 3️⃣ Add Version to Supabase

**Go to**: Supabase SQL Editor

```sql
INSERT INTO app_versions (
    version_code, 
    version_name, 
    changelog, 
    is_critical
)
VALUES (
    2,
    '1.1.0',
    '✨ New Features
• Feature 1
• Feature 2

🐛 Bug Fixes
• Fix 1
• Fix 2',
    false
);
```

### 4️⃣ Build Release APK/AAB

```bash
# Build release bundle
./gradlew bundleRelease

# Or build APK
./gradlew assembleRelease
```

### 5️⃣ Upload to Play Store

1. Go to [Google Play Console](https://play.google.com/console)
2. Select your app
3. Navigate to "Production" → "Create new release"
4. Upload the AAB/APK from `app/build/outputs/`
5. Fill in release notes (copy from CHANGELOG.md)
6. Submit for review

### 6️⃣ Test Update Notification

After publishing:

1. Install the old version on a test device
2. Open the app
3. You should see the update dialog
4. Tap "Update" to verify Play Store link works

---

## 🔥 Critical Update

For urgent/security updates:

```sql
INSERT INTO app_versions (
    version_code, 
    version_name, 
    changelog, 
    is_critical,  -- ← Set to true
    min_supported_version
)
VALUES (
    3,
    '1.2.0',
    '🔒 Critical Security Update
• Fixed security vulnerability
• Must update to continue using app',
    true,  -- ← This makes it mandatory
    1  -- ← Versions below this won't work
);
```

---

## 📊 Monitor Update Adoption

**Firebase Analytics Events**:
- `update_checked` - Users who saw the update
- `update_initiated` - Users who tapped "Update"
- `update_dismissed` - Users who dismissed (non-critical only)

**Supabase Analytics** (optional):
```sql
SELECT 
    action,
    COUNT(*) as count,
    to_version_code
FROM update_analytics
WHERE to_version_code = 2
GROUP BY action, to_version_code;
```

---

## ⚡ Quick Commands

```bash
# Check current version
grep "versionCode\|versionName" app/build.gradle.kts

# Build and install debug
./gradlew installDebug

# Build release
./gradlew bundleRelease

# View recent analytics
# Go to Firebase Console → Analytics → Events
```

---

## 🎯 Version Numbering Strategy

**Version Code**: Increment by 1 for each release
- 1, 2, 3, 4, ...

**Version Name**: Semantic versioning
- `1.0.0` - Major.Minor.Patch
- `1.1.0` - New features (minor)
- `1.1.1` - Bug fixes (patch)
- `2.0.0` - Breaking changes (major)

---

## 📝 Changelog Template

```markdown
## Version X.Y.Z (Build N) - Date

### ✨ New Features
- Feature description

### 🎨 Improvements
- Improvement description

### 🐛 Bug Fixes
- Bug fix description

### 🔒 Security
- Security update description

### ⚠️ Breaking Changes
- Breaking change description
```

---

**Pro Tip**: Keep this file handy for quick reference when releasing updates!
