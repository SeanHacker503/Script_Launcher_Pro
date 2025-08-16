package com.example.scriptlauncherpro

import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

// Compatible 2025 Color Scheme using standard Material colors
private val modernDarkColorScheme = darkColorScheme(
    primary = Color(0xFF6C63FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A42CC),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF018786),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFFF6B6B),
    onTertiary = Color.White,
    background = Color(0xFF0D0D0D),
    onBackground = Color(0xFFF0F0F0),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFF0F0F0),
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFFCCCCCC),
    outline = Color(0xFF404040),
    error = Color(0xFFFF5252),
    onError = Color.White
)

data class Script(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val category: ScriptCategory,
    val path: String,
    val isTermux: Boolean = true,
    val lastUsed: Long = 0L
)

enum class ScriptCategory(val displayName: String, val icon: ImageVector, val color: Color) {
    SECURITY("Security", Icons.Filled.Security, Color(0xFFFF6B6B)),
    NETWORK("Network", Icons.Filled.Wifi, Color(0xFF4ECDC4)),
    SYSTEM("System", Icons.Filled.Computer, Color(0xFF45B7D1)),
    CUSTOM("Custom", Icons.Filled.Code, Color(0xFF96CEB4)),
    TOOLS("Tools", Icons.Filled.Build, Color(0xFFFECA57))
}

sealed class NavigationItem(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : NavigationItem("dashboard", "Dashboard", Icons.Filled.Dashboard)
    object Scripts : NavigationItem("scripts", "Scripts", Icons.Filled.PlayArrow)
    object Terminal : NavigationItem("terminal", "Terminal", Icons.Filled.Terminal)
    object Settings : NavigationItem("settings", "Settings", Icons.Filled.Settings)
}

class MainActivity : ComponentActivity() {

    private var soundPool: SoundPool? = null
    private var buttonSoundId1: Int = 0
    private var buttonSoundId2: Int = 0

    companion object {
        private const val TAG = "MainActivity"
        private const val TERMUX_PACKAGE = "com.termux"
        private const val TERMUX_HOME = "/data/data/com.termux/files/home"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupStatusBar()
        initializeSoundPool()

        setContent {
            MaterialTheme(colorScheme = modernDarkColorScheme) {
                ScriptLauncherApp(
                    onPlaySound = { playSound(buttonSoundId1) },
                    onRunScript = { script -> runScript(script) }
                )
            }
        }
    }

    private fun setupStatusBar() {
        window.statusBarColor = android.graphics.Color.parseColor("#0D0D0D")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
    }

    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()

        loadSounds()
    }

    private fun loadSounds() {
        try {
            soundPool?.let { pool ->
                try {
                    // Try to load sound files, but handle gracefully if they don't exist
                    buttonSoundId1 = pool.load(this, R.raw.button_sound_1, 1)
                } catch (e: Exception) {
                    Log.w(TAG, "button_sound_1.mp3 not found, sounds disabled", e)
                    buttonSoundId1 = 0
                }

                try {
                    buttonSoundId2 = pool.load(this, R.raw.button_sound_2, 1)
                } catch (e: Exception) {
                    Log.w(TAG, "button_sound_2.mp3 not found, sounds disabled", e)
                    buttonSoundId2 = 0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing sounds", e)
        }
    }

    private fun playSound(soundId: Int) {
        if (soundId != 0) {
            soundPool?.play(soundId, 0.5f, 0.5f, 1, 0, 1.0f)
        }
    }

    private fun runScript(script: Script) {
        if (script.isTermux) {
            runInTermux(script.path)
        } else {
            runInternalScriptAsync()
        }
    }

    private fun runInTermux(scriptPath: String) {
        if (!isTermuxInstalled()) {
            showToast("Termux is not installed on this device")
            return
        }

        try {
            val intent = Intent("com.termux.RUN_COMMAND").apply {
                setPackage(TERMUX_PACKAGE)
                putExtra("com.termux.RUN_COMMAND_PATH", scriptPath)
                putExtra("com.termux.RUN_COMMAND_BACKGROUND", false)
                putExtra("com.termux.RUN_COMMAND_WORKDIR", TERMUX_HOME)
            }
            sendBroadcast(intent)
            showToast("Command sent to Termux: ${scriptPath.substringAfterLast("/")}")
        } catch (e: Exception) {
            Log.e(TAG, "Error running command in Termux", e)
            showToast("Failed to run command: ${e.message}")
        }
    }

    private fun isTermuxInstalled(): Boolean {
        return try {
            packageManager.getPackageInfo(TERMUX_PACKAGE, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun runInternalScriptAsync() {
        lifecycleScope.launch {
            try {
                showToast("Running internal script demo...")
                // Simple internal script simulation
                delay(1000)
                showToast("Internal script completed successfully!")
            } catch (e: Exception) {
                Log.e(TAG, "Internal script execution failed", e)
                showToast("Script execution failed: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool?.release()
        soundPool = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptLauncherApp(
    onPlaySound: () -> Unit,
    onRunScript: (Script) -> Unit
) {
    var currentRoute by remember { mutableStateOf("dashboard") }
    var terminalOutput by remember { mutableStateOf("") }

    val navigationItems = listOf(
        NavigationItem.Dashboard,
        NavigationItem.Scripts,
        NavigationItem.Terminal,
        NavigationItem.Settings
    )

    Scaffold(
        bottomBar = {
            ModernNavigationBar(
                currentRoute = currentRoute,
                navigationItems = navigationItems,
                onNavigate = { route -> currentRoute = route }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Animated background
            AnimatedBackground()

            // Content based on current route
            when (currentRoute) {
                "dashboard" -> DashboardScreen(
                    onPlaySound = onPlaySound,
                    onRunScript = onRunScript,
                    onNavigateToScripts = { currentRoute = "scripts" }
                )
                "scripts" -> ScriptsScreen(
                    onPlaySound = onPlaySound,
                    onRunScript = onRunScript
                )
                "terminal" -> TerminalScreen(
                    output = terminalOutput,
                    onClearOutput = { terminalOutput = "" }
                )
                "settings" -> SettingsScreen()
            }
        }
    }
}

@Composable
fun AnimatedBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetX"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0D0D0D),
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0D0D0D)
                    ),
                    start = androidx.compose.ui.geometry.Offset(offsetX, 0f),
                    end = androidx.compose.ui.geometry.Offset(offsetX + 500f, 500f)
                )
            )
    )
}

@Composable
fun ModernNavigationBar(
    currentRoute: String,
    navigationItems: List<NavigationItem>,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 8.dp
    ) {
        navigationItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun DashboardScreen(
    onPlaySound: () -> Unit,
    onRunScript: (Script) -> Unit,
    onNavigateToScripts: () -> Unit
) {
    val recentScripts = remember { getSampleScripts().take(3) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            WelcomeCard()
        }

        item {
            QuickStatsCard()
        }

        item {
            SectionHeader(
                title = "Recent Scripts",
                actionText = "View All",
                onActionClick = onNavigateToScripts
            )
        }

        items(recentScripts) { script ->
            ScriptCard(
                script = script,
                onClick = {
                    onPlaySound()
                    onRunScript(script)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun WelcomeCard() {
    val currentTime = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Welcome Back!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Script Launcher Pro • $currentTime",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuickStatsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("12", "Scripts", Icons.Filled.Code)
            StatItem("5", "Categories", Icons.Filled.Folder)
            StatItem("8", "Recent", Icons.Filled.History)
        }
    }
}

@Composable
fun StatItem(value: String, label: String, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ScriptsScreen(
    onPlaySound: () -> Unit,
    onRunScript: (Script) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<ScriptCategory?>(null) }
    val scripts = remember { getSampleScripts() }
    val filteredScripts = if (selectedCategory != null) {
        scripts.filter { it.category == selectedCategory }
    } else {
        scripts
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Script Library",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            CategoryFilter(
                categories = ScriptCategory.values().toList(),
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = if (selectedCategory == it) null else it }
            )
        }

        items(filteredScripts) { script ->
            ScriptCard(
                script = script,
                onClick = {
                    onPlaySound()
                    onRunScript(script)
                }
            )
        }
    }
}

@Composable
fun CategoryFilter(
    categories: List<ScriptCategory>,
    selectedCategory: ScriptCategory?,
    onCategorySelected: (ScriptCategory) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category.displayName) },
                leadingIcon = {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.displayName,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = category.color.copy(alpha = 0.3f),
                    selectedLabelColor = category.color,
                    selectedLeadingIconColor = category.color
                )
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScriptCard(
    script: Script,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { /* TODO: Show context menu */ }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(script.category.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = script.icon,
                    contentDescription = script.name,
                    tint = script.category.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = script.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = script.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { },
                        label = { Text(script.category.displayName, fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = script.category.color.copy(alpha = 0.1f),
                            labelColor = script.category.color
                        ),
                        modifier = Modifier.height(24.dp)
                    )

                    if (script.isTermux) {
                        Icon(
                            imageVector = Icons.Filled.Terminal,
                            contentDescription = "Termux",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Run Script",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun TerminalScreen(
    output: String,
    onClearOutput: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Terminal Output",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onClearOutput) {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = "Clear Output"
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F0F0F)
            )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (output.isEmpty()) {
                    item {
                        Text(
                            text = "No output yet. Run a script to see results here.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                } else {
                    items(output.split("\n")) { line ->
                        Text(
                            text = line,
                            color = Color(0xFF00FF00),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            SettingsSection("General") {
                SettingsItem(
                    title = "Sound Effects",
                    subtitle = "Enable button sound feedback",
                    icon = Icons.Filled.VolumeUp,
                    trailing = {
                        Switch(
                            checked = true,
                            onCheckedChange = { /* TODO: Implement */ }
                        )
                    }
                )

                SettingsItem(
                    title = "Theme",
                    subtitle = "Dark theme enabled",
                    icon = Icons.Filled.Palette,
                    onClick = { /* TODO: Implement theme picker */ }
                )
            }
        }

        item {
            SettingsSection("Scripts") {
                SettingsItem(
                    title = "Backup Scripts",
                    subtitle = "Export your script collection",
                    icon = Icons.Filled.Backup,
                    onClick = { /* TODO: Implement backup */ }
                )

                SettingsItem(
                    title = "Restore Scripts",
                    subtitle = "Import script collection",
                    icon = Icons.Filled.Restore,
                    onClick = { /* TODO: Implement restore */ }
                )

                SettingsItem(
                    title = "Default Terminal",
                    subtitle = "Termux (Recommended)",
                    icon = Icons.Filled.Terminal,
                    onClick = { /* TODO: Implement terminal selector */ }
                )
            }
        }

        item {
            SettingsSection("About") {
                SettingsItem(
                    title = "Version",
                    subtitle = "Script Launcher Pro 2.0",
                    icon = Icons.Filled.Info
                )

                SettingsItem(
                    title = "Developer",
                    subtitle = "Sean Hacker © 2025",
                    icon = Icons.Filled.Person
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = trailing ?: if (onClick != null) {
            { Icon(Icons.Filled.ChevronRight, contentDescription = null) }
        } else null,
        modifier = if (onClick != null) {
            Modifier.clickable { onClick() }
        } else {
            Modifier
        }
    )
}

// Sample data for demonstration
fun getSampleScripts(): List<Script> {
    val termuxHome = "/data/data/com.termux/files/home"
    return listOf(
        Script(
            id = "1",
            name = "All Security Tools",
            description = "Complete security analysis suite",
            icon = Icons.Filled.Security,
            category = ScriptCategory.SECURITY,
            path = "$termuxHome/alhack.sh",
            isTermux = true
        ),
        Script(
            id = "2",
            name = "SMS/Call Analysis",
            description = "Analyze SMS, MMS and call logs",
            icon = Icons.Filled.Message,
            category = ScriptCategory.SECURITY,
            path = "$termuxHome/option2.sh",
            isTermux = true
        ),
        Script(
            id = "3",
            name = "Network Scanner",
            description = "Network discovery and analysis",
            icon = Icons.Filled.Wifi,
            category = ScriptCategory.NETWORK,
            path = "$termuxHome/option3.sh",
            isTermux = true
        ),
        Script(
            id = "4",
            name = "Internal Test Script",
            description = "Built-in demonstration script",
            icon = Icons.Filled.Code,
            category = ScriptCategory.CUSTOM,
            path = "my_internal_script.sh",
            isTermux = false
        ),
        Script(
            id = "5",
            name = "Port Scanner",
            description = "Scan for open ports on network",
            icon = Icons.Filled.Router,
            category = ScriptCategory.NETWORK,
            path = "$termuxHome/nmap_scan.sh",
            isTermux = true
        ),
        Script(
            id = "6",
            name = "System Info",
            description = "Display detailed system information",
            icon = Icons.Filled.Computer,
            category = ScriptCategory.SYSTEM,
            path = "$termuxHome/sysinfo.sh",
            isTermux = true
        ),
        Script(
            id = "7",
            name = "WiFi Analyzer",
            description = "Analyze nearby WiFi networks",
            icon = Icons.Filled.WifiFind,
            category = ScriptCategory.NETWORK,
            path = "$termuxHome/wifi_analyzer.sh",
            isTermux = true
        ),
        Script(
            id = "8",
            name = "File Permissions",
            description = "Check and modify file permissions",
            icon = Icons.Filled.Folder,
            category = ScriptCategory.SYSTEM,
            path = "$termuxHome/permissions.sh",
            isTermux = true
        ),
        Script(
            id = "9",
            name = "Backup Tool",
            description = "Backup important files and data",
            icon = Icons.Filled.Backup,
            category = ScriptCategory.TOOLS,
            path = "$termuxHome/backup.sh",
            isTermux = true
        ),
        Script(
            id = "10",
            name = "Custom Script Builder",
            description = "Create and edit custom scripts",
            icon = Icons.Filled.Edit,
            category = ScriptCategory.CUSTOM,
            path = "$termuxHome/script_builder.sh",
            isTermux = true
        ),
        Script(
            id = "11",
            name = "Process Monitor",
            description = "Monitor running processes",
            icon = Icons.Filled.Memory,
            category = ScriptCategory.SYSTEM,
            path = "$termuxHome/process_monitor.sh",
            isTermux = true
        ),
        Script(
            id = "12",
            name = "Log Analyzer",
            description = "Analyze system and app logs",
            icon = Icons.Filled.Description,
            category = ScriptCategory.TOOLS,
            path = "$termuxHome/log_analyzer.sh",
            isTermux = true
        )
    )
}