# AuraWake - Onboarding Flow Documentation

## Overview
When users first install AuraWake, they are guided through a comprehensive onboarding tutorial that helps them set up their first alarm and grant necessary permissions.

## Onboarding Flow Steps

### 1. Welcome Screen
**File:** `WelcomeScreen.kt`
- **Purpose:** Welcome users to AuraWake
- **Content:**
  - Animated app icon (red circle with alarm icon)
  - App name: "AuraWake"
  - Tagline: "The best way to wake up on time."
  - "Get Started" button
- **Action:** User taps "Get Started" to proceed

### 2. Set Alarm Time
**File:** `OnboardingTimeScreen.kt`
- **Purpose:** Let users set their first alarm time
- **Content:**
  - Title: "Set your alarm time"
  - Interactive time picker with hour, minute, and AM/PM wheels
  - Default time: 7:00 AM
- **Action:** User selects time and taps "Next"

### 3. Select Alarm Tone
**File:** `OnboardingSoundScreen.kt`
- **Purpose:** Choose alarm sound/ringtone
- **Content:**
  - Title: "Alarm tone"
  - List of system alarm ringtones
  - Radio button selection
  - Default: First available ringtone (or "Orkney" if available)
- **Action:** User selects tone and taps "Next"

### 4. Choose Wake-up Challenge
**File:** `OnboardingMissionScreen.kt`
- **Purpose:** Select a challenge to dismiss the alarm
- **Content:**
  - Title: "Choose a wake-up mission"
  - Available challenges:
    - **Math** - Solve math problems
    - **Typing** - Type a phrase correctly
    - **Shake** - Shake the phone
    - **Off** - No challenge (simple dismiss)
- **Action:** User selects challenge and taps "Next"

### 5. Display Overlay Permission ⭐ NEW
**File:** `OnboardingPermissionScreen.kt`
- **Purpose:** Request "Display over other apps" permission
- **Content:**
  - Title: "Display Over Other Apps"
  - Explanation of why permission is needed
  - Information card explaining:
    - Show alarm screen when it's time to wake up
    - Display challenges over any app
    - Ensure you never miss an alarm
  - "Grant Permission" button
  - "Skip for now" option
- **Action:** 
  - User taps "Grant Permission" → Opens system settings
  - After granting permission, automatically proceeds to next step
  - User can skip if they prefer

### 6. Final Setup Screen
**File:** `OnboardingSetupScreen.kt`
- **Purpose:** Create the alarm and complete onboarding
- **Content:**
  - Loading indicator
  - Text: "Trusted by 100M+"
  - Text: "Your alarm is almost ready"
  - Progress message: "Finding the right mission 53%"
  - Automatically proceeds after 2 seconds
- **Action:** 
  - Creates alarm with selected settings
  - Marks first run as completed
  - Navigates to home screen

### 7. Home Screen
**File:** `HomeScreen.kt`
- **Purpose:** Main app interface
- **Content:**
  - List of alarms
  - Add alarm button
  - Settings and profile access
- **Action:** User can now use the app normally

## Technical Implementation

### Navigation Flow
```
onboarding (nested navigation)
  ├── welcome
  ├── time
  ├── sound
  ├── mission
  ├── permission (NEW)
  └── setup
      └── home (exit onboarding)
```

### ViewModel
**File:** `OnboardingViewModel.kt`
- Stores user selections:
  - `selectedHour` (default: 7)
  - `selectedMinute` (default: 0)
  - `selectedSound` (default: "Orkney")
  - `selectedChallenge` (default: NONE)
- Creates alarm with selected settings
- Marks onboarding as complete

### First Run Detection
- Uses `SettingsRepository.isFirstRun()`
- If true, shows onboarding flow
- After completion, sets `setFirstRunCompleted()`
- Subsequent app launches go directly to home screen

## Design Principles
- **Dark theme:** Consistent dark background (#1C1C1E)
- **Brand color:** Red accent (#FF3B30)
- **Smooth animations:** Breathing logo, smooth transitions
- **Clear hierarchy:** Large titles, readable text
- **User-friendly:** Skip options where appropriate
- **Educational:** Clear explanations for permissions

## Permission Handling
The onboarding flow now properly requests the display overlay permission, which is essential for:
- Showing alarm screens over lock screen
- Displaying challenges over any app
- Ensuring alarms can't be accidentally dismissed

Users can skip this permission during onboarding, but they'll be prompted again later when trying to use features that require it.
