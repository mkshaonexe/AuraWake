# 📸 Media Permission Guide - Overlay Customization

## Overview

The AuraWake app now properly requests **media/storage permissions** to allow users to select photos from their device for customizing the alarm overlay background.

## ✅ What Was Fixed

### Problem
- App couldn't access photos from device storage
- Photo picker would fail silently
- No permission request dialog appeared

### Solution
✅ Added proper media permissions to `AndroidManifest.xml`  
✅ Implemented runtime permission request in `OverlaySettingsScreen.kt`  
✅ Added user-friendly permission info card  
✅ Updated button text to indicate permission status  
✅ Support for both modern (Android 13+) and legacy permissions  

---

## 🔒 Permissions Added

### AndroidManifest.xml

```xml
<!-- Modern Photo Picker (Android 13+) -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />

<!-- Legacy Storage (Android 12 and below) -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" 
    android:maxSdkVersion="32" />
```

### How It Works

**Android 13+ (API 33+)**
- Uses `READ_MEDIA_IMAGES` permission
- More privacy-focused
- Only grants access to images

**Android 12 and below (API 32 and below)**
- Uses `READ_EXTERNAL_STORAGE` permission
- Legacy permission system
- Automatically limited by `maxSdkVersion="32"`

---

## 📱 User Experience

### Step 1: User Opens Overlay Settings
- Navigates to Settings → Overlay Settings
- Sees preview of alarm screen

### Step 2: Permission Info Card (if not granted)
```
┌─────────────────────────────────────────────┐
│ 🖼️  Media Access Required                  │
│                                             │
│ Grant permission to select photos from     │
│ your device for the alarm overlay          │
│ background.                                 │
└─────────────────────────────────────────────┘
```

### Step 3: User Taps "Grant Permission & Select Image"
- System permission dialog appears
- User grants permission

### Step 4: Photo Picker Opens
- User can browse and select photos
- Selected photo is saved and displayed

### Step 5: Photo Applied
- Photo appears as alarm overlay background
- Preview updates in real-time

---

## 🎨 UI Features

### Permission Status Indicator
The button text changes based on permission status:
- **Not Granted**: "Grant Permission & Select Image"
- **Granted**: "Select Image"

### Permission Info Card
Only shows when permission is **not granted**:
- Orange image icon
- Clear explanation
- Friendly, non-technical language

### Preview
- Shows real-time preview of selected image
- Displays how it will look when alarm rings
- Includes time, moon/image, and snooze button

---

## 🔧 Technical Implementation

### Permission Request Flow

```kotlin
// 1. Determine which permission to request
val mediaPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    android.Manifest.permission.READ_MEDIA_IMAGES
} else {
    android.Manifest.permission.READ_EXTERNAL_STORAGE
}

// 2. Create permission state
val permissionState = rememberPermissionState(mediaPermission)

// 3. Request permission when needed
fun selectPhoto() {
    when {
        permissionState.status.isGranted -> {
            // Open photo picker
            photoPickerLauncher.launch(...)
        }
        else -> {
            // Request permission
            permissionState.launchPermissionRequest()
        }
    }
}
```

### Photo Picker

Uses the modern **Photo Picker API**:
```kotlin
val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
) { uri: Uri? ->
    if (uri != null) {
        // Take persistable permission
        context.contentResolver.takePersistableUriPermission(
            uri, 
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        // Save URI
        settingsRepository.saveOverlayImageUri(uri.toString())
    }
}
```

### Persistable URI Permission

**Important**: We request persistable permission so the app can access the image even after restart:

```kotlin
context.contentResolver.takePersistableUriPermission(
    uri, 
    Intent.FLAG_GRANT_READ_URI_PERMISSION
)
```

---

## 🧪 Testing

### Test Scenarios

1. **First Time User (No Permission)**
   - Open Overlay Settings
   - See permission info card
   - Tap button
   - Grant permission
   - Photo picker opens
   - Select photo
   - Photo appears in preview

2. **Permission Already Granted**
   - Open Overlay Settings
   - No permission info card
   - Tap "Select Image"
   - Photo picker opens immediately

3. **Permission Denied**
   - Open Overlay Settings
   - Tap button
   - Deny permission
   - Info card still shows
   - Tap button again
   - Permission dialog appears again

4. **Photo Persistence**
   - Select a photo
   - Close app
   - Reopen app
   - Photo still appears (persistable permission)

---

## 🔍 Debugging

### Check Permission Status

```kotlin
// In code
val isGranted = permissionState.status.isGranted

// Via ADB
adb shell dumpsys package com.aura.wake | grep "READ_MEDIA_IMAGES"
```

### Reset Permissions

```bash
# Clear app data (resets all permissions)
adb shell pm clear com.aura.wake

# Or revoke specific permission
adb shell pm revoke com.aura.wake android.permission.READ_MEDIA_IMAGES
```

### View Logs

```bash
# Filter for permission-related logs
adb logcat | grep -i "permission"

# Filter for photo picker
adb logcat | grep -i "picker"
```

---

## 📊 Android Version Compatibility

| Android Version | API Level | Permission Used | Status |
|----------------|-----------|-----------------|--------|
| Android 13+ | 33+ | READ_MEDIA_IMAGES | ✅ Supported |
| Android 12 | 32 | READ_EXTERNAL_STORAGE | ✅ Supported |
| Android 11 | 30 | READ_EXTERNAL_STORAGE | ✅ Supported |
| Android 10 | 29 | READ_EXTERNAL_STORAGE | ✅ Supported |
| Android 9 | 28 | READ_EXTERNAL_STORAGE | ✅ Supported |

---

## 🎯 Best Practices

### ✅ Do's
- Request permission only when needed (when user taps button)
- Explain why permission is needed (info card)
- Use modern Photo Picker API
- Request persistable URI permission
- Handle permission denial gracefully

### ❌ Don'ts
- Don't request permission on app startup
- Don't use deprecated storage APIs
- Don't assume permission is always granted
- Don't forget to handle permission denial

---

## 🚀 Future Enhancements

Potential improvements:
- [ ] Add "Go to Settings" button if permission is permanently denied
- [ ] Support for selecting multiple images (slideshow)
- [ ] Image cropping/editing before applying
- [ ] Cloud storage integration (Google Drive, Dropbox)
- [ ] Pre-made overlay image gallery
- [ ] Image filters and effects

---

## 📝 Common Issues

### Issue: Photo picker doesn't open
**Solution**: Check if permission is granted. Look for permission denial in logs.

### Issue: Selected photo disappears after app restart
**Solution**: Ensure `takePersistableUriPermission()` is called.

### Issue: Permission dialog doesn't appear
**Solution**: Check if permission is already granted or permanently denied.

### Issue: "Permission denied" error
**Solution**: User denied permission. Show rationale and request again.

---

## 📞 Support

If you encounter issues:
1. Check Logcat for error messages
2. Verify permissions in AndroidManifest.xml
3. Test on different Android versions
4. Check if permission is permanently denied

---

**Last Updated**: December 21, 2024  
**Status**: ✅ Fully Implemented and Tested  
**Compatibility**: Android 9+ (API 28+)
