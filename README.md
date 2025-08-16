# 🚀 Script Launcher Pro 2.0

**A Beautiful, Modern Android App for Managing and Executing Scripts with Termux Integration**

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Material Design 3](https://img.shields.io/badge/Design-Material%203-orange.svg)](https://m3.material.io)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## ✨ Features

### 🎨 **Modern UI/UX**
- **Clean Material Design 3** interface with dynamic animations
- **Bottom Navigation** with 4 main sections: Dashboard, Scripts, Terminal, Settings
- **Category-based Script Organization** (Security, Network, System, Custom, Tools)
- **Professional Dashboard** with welcome card and quick stats
- **Animated Background** with dynamic gradient effects
- **Dark Theme** optimized for terminal enthusiasts

### 🔧 **Script Management**
- **Termux Integration** - Seamlessly execute scripts in Termux
- **Script Library** - Organize scripts by categories with visual icons
- **Quick Access** - Recent scripts on dashboard for fast execution
- **Internal Script Support** - Run scripts within the app environment
- **Script Categories** with color-coded organization

### 📱 **Core Sections**

#### 📊 **Dashboard**
- Welcome card with current time
- Quick stats overview (Scripts, Categories, Recent)
- Recent scripts with one-tap execution
- Beautiful card-based layout

#### 📝 **Scripts Library**
- Browse all scripts by category
- Filter by Security, Network, System, Custom, or Tools
- Visual script cards with descriptions and category badges
- Termux integration indicators

#### 🖥️ **Terminal Output**
- Live terminal output viewer
- Monospace font for proper formatting
- Clear output functionality
- Terminal-style dark interface

#### ⚙️ **Settings**
- Sound effects toggle
- Theme preferences
- Backup & restore functionality
- App information and developer credits

### 🎵 **Enhanced Experience**
- **Sound Effects** - Audio feedback for interactions
- **Smooth Animations** - Polished micro-interactions
- **Responsive Design** - Optimized for various screen sizes
- **Professional Feel** - Enterprise-grade UI design

## 🛠️ **Technology Stack**

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Design System**: Material Design 3
- **Architecture**: Modern Android Architecture (MVVM pattern ready)
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 34)
- **Animations**: Compose Animation APIs
- **Sound**: Android SoundPool API

## 📦 **Installation**

### **Prerequisites**
1. **Android Device** running Android 7.0+ (API 24+)
2. **Termux** installed from [F-Droid](https://f-droid.org/packages/com.termux/) (recommended)
3. **Android Studio** for development (if building from source)

### **Download**
```bash
git clone https://github.com/yourusername/script-launcher-pro.git
cd script-launcher-pro
```

### **Build Instructions**
1. Open in Android Studio
2. Sync project with Gradle files
3. Build and run on device or emulator

## 🔧 **Setup & Configuration**

### **1. Termux Configuration**
Enable external app integration in Termux:
```bash
mkdir -p ~/.termux
echo "allow-external-apps=true" >> ~/.termux/termux.properties
# Restart Termux after this change
```

### **2. Script Setup**
Place your scripts in Termux home directory:
```bash
cd /data/data/com.termux/files/home/
# Add your .sh scripts here
chmod +x *.sh  # Make executable
```

### **3. App Permissions**
The app requires these permissions:
- `com.termux.permission.RUN_COMMAND` - Execute commands in Termux
- `READ_EXTERNAL_STORAGE` - Access script files
- `INTERNET` & `ACCESS_NETWORK_STATE` - For network-related scripts
- `VIBRATE` - Haptic feedback

## 📝 **Script Categories & Examples**

### 🔒 **Security Scripts**
- **All Security Tools** (`alhack.sh`) - Complete security analysis suite
- **SMS/Call Analysis** (`option2.sh`) - Analyze communication logs
- **Permission Scanner** (`permissions.sh`) - Check file permissions

### 🌐 **Network Scripts**
- **Network Scanner** (`option3.sh`) - Network discovery and analysis
- **Port Scanner** (`nmap_scan.sh`) - Scan for open ports
- **WiFi Analyzer** (`wifi_analyzer.sh`) - Analyze nearby networks

### 💻 **System Scripts**
- **System Info** (`sysinfo.sh`) - Display detailed system information
- **Process Monitor** (`process_monitor.sh`) - Monitor running processes

### 🔧 **Tools**
- **Backup Tool** (`backup.sh`) - Backup important files
- **Log Analyzer** (`log_analyzer.sh`) - Analyze system logs

### 📝 **Custom Scripts**
- **Script Builder** (`script_builder.sh`) - Create and edit scripts
- **Internal Demo** - Built-in demonstration script

## 🎨 **Screenshots**

| Dashboard | Scripts Library | Terminal Output | Settings |
|:---------:|:---------------:|:---------------:|:--------:|
| ![Dashboard](<img width="1080" height="2400" alt="17553685440023645919993039181044" src="https://github.com/user-attachments/assets/ba9f7f62-39c5-4426-bd52-ae756299f164" />
) | ![Scripts](screenshots/scripts.png) | ![Terminal](screenshots/terminal.png) | ![Settings](screenshots/settings.png) |

*Modern dark theme with beautiful animations and professional UI*

## 🚀 **Usage**

1. **Launch the app** and explore the dashboard
2. **Browse scripts** in the Scripts section
3. **Filter by category** using the category chips
4. **Tap any script** to execute it in Termux
5. **View output** in the Terminal section
6. **Customize settings** in the Settings section

### **Adding Custom Scripts**
1. Create your `.sh` script in Termux home directory
2. Make it executable: `chmod +x yourscript.sh`
3. The app will automatically integrate it (or modify the sample scripts list)

## 🔧 **Development**

### **Project Structure**
```
app/src/main/
├── java/com/example/scriptlauncherpro/
│   └── MainActivity.kt                 # Main app logic
├── res/
│   ├── values/
│   │   ├── colors.xml                 # Color scheme
│   │   ├── strings.xml                # App strings
│   │   └── themes.xml                 # Material theme
│   └── xml/
│       ├── backup_rules.xml           # Backup configuration
│       └── data_extraction_rules.xml  # Data extraction rules
└── AndroidManifest.xml                # App permissions & config
```

### **Key Components**
- **ScriptLauncherApp** - Main Compose UI
- **NavigationItem** - Bottom navigation management
- **Script** data class - Script representation
- **ScriptCategory** enum - Category definitions
- **Termux Integration** - External script execution

### **Customization**
- **Colors**: Modify `colors.xml` for custom color schemes
- **Scripts**: Update `getSampleScripts()` function for your scripts
- **Categories**: Modify `ScriptCategory` enum for custom categories
- **Sounds**: Add MP3 files to `res/raw/` for custom sound effects

## 🤝 **Contributing**

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first.

### **Development Setup**
1. Fork the repository
2. Create your feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

## 📋 **Roadmap**

- [ ] **Script Editor** - Built-in script editing capabilities
- [ ] **Cloud Sync** - Sync scripts across devices
- [ ] **Script Templates** - Pre-built script templates
- [ ] **Output History** - Save and review script outputs
- [ ] **Scheduled Execution** - Run scripts on schedule
- [ ] **Script Sharing** - Share scripts between users
- [ ] **Plugin System** - Extensible plugin architecture
- [ ] **Backup/Restore** - Complete script collection management

## 🐛 **Known Issues**

- Sound files are optional - app works without them
- Termux must be installed from F-Droid for full compatibility
- External storage permissions may require manual approval on Android 11+

## 📄 **License**

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 **Author**

**Sean Hacker**  
© 2025 Script Launcher Pro

---

## 🙏 **Acknowledgments**

- **Termux Team** for the amazing terminal environment
- **Jetpack Compose Team** for the modern UI toolkit  
- **Material Design Team** for the beautiful design system
- **Android Community** for continuous inspiration

---

### 💡 **Pro Tips**

1. **Use F-Droid Termux** - Google Play version has limitations
2. **Organize Scripts** - Use clear naming conventions for better management
3. **Test First** - Always test scripts in Termux before adding to the app
4. **Backup Scripts** - Keep backups of your important scripts
5. **Stay Updated** - Keep both the app and Termux updated for best experience

---

**⭐ Star this repo if you find it useful!**

**🐛 Found a bug?** [Open an issue](https://github.com/yourusername/script-launcher-pro/issues)

**💡 Have an idea?** [Start a discussion](https://github.com/yourusername/script-launcher-pro/discussions)
