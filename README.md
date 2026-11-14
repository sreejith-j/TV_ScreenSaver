# Ambient TV Screensaver

A modern, minimalist, phone-style ambient screensaver for **Android TV** and **Google TV**, inspired by Google Pixel's At-a-Glance / Always-on Display and modern smart displays.

Built with **Kotlin**, **AndroidX**, and **Open-Meteo**, designed specifically for living room TVs with OLED burn-in protection, ultra-low resource usage, and smooth 60 FPS weather particle animations.

---

## ✨ Features

### 🕒 4 Ambient Display Modes
- **Minimal Horizontal**: Ultra-clean, single-line typography with an accent AM/PM pill.
- **Pixel Stacked**: Google Pixel / Samsung AOD inspired stacked hour on top, bold minute below.
- **Minimalist Bauhaus Analog**: Custom Canvas-rendered watchface with subtle bezel ring, 12 tick marks (major accents at 12, 3, 6, 9), styled hour and minute hands, and central pivot.
- **Cyber Dot Matrix**: Monospaced high-tech digital clock with live ticking seconds.

### 🌧️ Live Weather Particle Effects
Hardware-accelerated Canvas particle animations running at 60 FPS with < 1% CPU overhead:
- **Auto Mode (Default)**: Dynamically binds to real-time weather and time of day:
  - 🌧️ **Rain / Showers**: Slanted, multi-speed raindrops with 3D parallax depth.
  - ⛈️ **Thunderstorm**: Rainfall with occasional subtle, distant sheet lightning illumination.
  - ❄️ **Snow**: Soft circular snowflakes floating down with gentle horizontal swaying.
  - 🌌 **Clear Night**: Deep atmospheric starfield with soft twinkling stars.
  - ☀️ **Sunny / Clear Day**: Warm golden sun motes drifting lazily across the ambient aura.
- **Manual Overrides**: Choose your favorite effect directly or turn particles off for pure minimalism.

### 🎨 8 Color Themes
- **Google Blue** (`#8AB4F8`)
- **Sunset Gold** (`#FFB74D`)
- **Emerald Mint** (`#00E676`)
- **Cyber Cyan** (`#00E5FF`)
- **Lavender Violet** (`#B388FF`)
- **Coral Peach** (`#FF7043`)
- **Minimal White** (`#FFFFFF`)
- **Dynamic Circadian**: Automatically shifts theme color based on the time of day:
  - *Morning (6 AM – 12 PM)*: Sunrise Gold
  - *Afternoon (12 PM – 6 PM)*: Sky Blue
  - *Evening (6 PM – 10 PM)*: Sunset Coral
  - *Night (10 PM – 6 AM)*: Moonlight Indigo

### 🌤️ Zero-Config Weather Engine
- **No API Key Required**: Fully powered by [Open-Meteo](https://open-meteo.com/).
- **Auto Location**: Discovers coordinates and city via lightweight IP geolocation (no GPS hardware needed).
- **Detailed Forecast**: Current temperature, "Feels Like", Today's High & Low, humidity level, and condition summaries.
- **Offline Resilient**: Local cache ensures weather is always displayed even if Wi-Fi temporarily disconnects.

### 🛡️ OLED & Bright TV Protection
- **Anti-Burn-In Pixel Shifting**: The entire content cluster gently drifts (±35 dp) every 2 minutes with smooth interpolation to prevent image retention on OLED and bright LED panels.
- **Pure OLED Black Option**: Option to disable the breathing ambient aura for true `#000000` pixels, eliminating room glare and saving panel energy.
- **Circadian Night Dimmer**: Automatically reduces brightness to 42% between **10:00 PM and 7:00 AM**.

### 📺 TV-First User Interface
- **D-Pad Navigation**: Scrollable 10-foot UI designed specifically for TV remotes with high-visibility focus states.
- **Instant Fullscreen Preview**: Test any theme, layout, or particle combination immediately with one click.
- **Format Toggles**: 12-Hour (AM/PM) vs. 24-Hour, Celsius (°C) vs. Fahrenheit (°F).

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 1.9
- **Platform**: Android TV / Google TV (API 21+)
- **Screensaver Core**: Android `DreamService`
- **Networking**: Lightweight `HttpURLConnection` with Kotlin Coroutines (`Dispatchers.IO`)
- **Graphics**: Hardware-accelerated custom Canvas views (`WeatherParticleView`, `AmbientAnalogClockView`, `AmbientAuraView`)
- **Privacy**: Zero analytics, zero telemetry, zero advertising SDKs.

---

## 🚀 Building from Source

### Prerequisites
- JDK 17 or higher
- Android SDK (API 34+)

### Build Debug APK
```bash
# Clone the repository
git clone https://github.com/sreejith-j/TV_ScreenSaver.git
cd TV_ScreenSaver

# Build APK using Gradle Wrapper
./gradlew assembleDebug
```
The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📲 Installing on Android TV / Google TV

### Step 1: Enable Wireless Debugging on your TV
1. On your TV, navigate to **Settings > System / Device Preferences > About**.
2. Click **Android TV OS Build** 7 times until you see *"You are now a developer!"*.
3. Go back to **System > Developer options**.
4. Enable **USB Debugging** and **Wireless debugging**.

### Step 2: Install via ADB
Connect to your TV from your computer (replace `TV_IP` and `PORT` with the values displayed on your TV's Wireless Debugging screen):

```bash
# Pair device (one-time on first connect)
adb pair <TV_IP>:<PAIRING_PORT> <PAIRING_CODE>

# Connect ADB
adb connect <TV_IP>:<PORT>

# Install the screensaver
adb -s <TV_IP>:<PORT> install -r app/build/outputs/apk/debug/app-debug.apk

# Grant secure settings permission (allows in-app default screensaver activation)
adb -s <TV_IP>:<PORT> shell pm grant com.ambient.tv.screensaver android.permission.WRITE_SECURE_SETTINGS

# Set Ambient TV as default screensaver
adb -s <TV_IP>:<PORT> shell settings put secure screensaver_components com.ambient.tv.screensaver/.AmbientDreamService
adb -s <TV_IP>:<PORT> shell settings put secure screensaver_enabled 1
```

> **Note for Google TV Users**: Google TV replaces the standard Android daydream picker with its own "Ambient Mode" and hides third-party screensavers from the visual system menu. Granting `WRITE_SECURE_SETTINGS` via the command above allows Ambient TV to activate itself directly from within the app UI!

---

## 📄 License

This project is licensed under the Apache 2.0 License.
