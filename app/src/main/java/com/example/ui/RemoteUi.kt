package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.BorderStroke
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainRemoteScreen(viewModel: TvViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    
    val connectedTv by viewModel.lastConnectedDevice.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val showPairingByTv by viewModel.showPairingDialog.collectAsState()
    
    // Dialog control states in view
    var showScanDialog by remember { mutableStateOf(false) }
    var showAddTvDialog by remember { mutableStateOf(false) }
    var showPremiumDialog by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var showMacroDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        containerColor = BackgroundDark,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsInputAntenna,
                            contentDescription = null,
                            tint = PrimaryNeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TV CONTROL IA",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = 1.5.sp,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(
                        onClick = { showPremiumDialog = true },
                        modifier = Modifier.testTag("premium_gold_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Premium Status",
                            tint = if (isPremium) AccentOrange else Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.SettingsRemote, contentDescription = "Remote") },
                    label = { Text("Controle") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryNeonCyan,
                        selectedTextColor = PrimaryNeonCyan,
                        indicatorColor = SurfaceVariantDark,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("tab_remote")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Tv, contentDescription = "TVs") },
                    label = { Text("Dispositivos") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryNeonCyan,
                        selectedTextColor = PrimaryNeonCyan,
                        indicatorColor = SurfaceVariantDark,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("tab_devices")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.KeyboardVoice, contentDescription = "IA Voice") },
                    label = { Text("IA & Logs") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryNeonCyan,
                        selectedTextColor = PrimaryNeonCyan,
                        indicatorColor = SurfaceVariantDark,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("tab_ia_voice")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                    label = { Text("Perfil") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryNeonCyan,
                        selectedTextColor = PrimaryNeonCyan,
                        indicatorColor = SurfaceVariantDark,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("tab_profile")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> RemoteControlTab(
                    viewModel = viewModel,
                    onConnectRequest = { selectedTab = 1 },
                    onScanRequest = {
                        showScanDialog = true
                        viewModel.startScanning()
                    }
                )
                1 -> DevicesTab(
                    viewModel = viewModel,
                    onScanRequest = {
                        showScanDialog = true
                        viewModel.startScanning()
                    },
                    onAddTvRequest = { showAddTvDialog = true },
                    onCreateMacroRequest = { showMacroDialog = true }
                )
                2 -> AiVoiceTab(viewModel = viewModel)
                3 -> ProfileTab(
                    viewModel = viewModel,
                    onLoginRequest = { showLoginDialog = true },
                    onPremiumRequest = { showPremiumDialog = true }
                )
            }

            // Global pairing Dialog
            showPairingByTv?.let { tv ->
                PairingDialog(
                    tv = tv,
                    onConfirm = { code -> viewModel.confirmPairing(code) },
                    onDismiss = { viewModel.cancelPairing() }
                )
            }

            // Radar Scan Dialog
            if (showScanDialog) {
                ScanRadarDialog(
                    viewModel = viewModel,
                    onDismiss = { showScanDialog = false }
                )
            }

            // Add Custom TV Dialog
            if (showAddTvDialog) {
                AddTvDialog(
                    onAdd = { name, brand, ip, connType ->
                        viewModel.addCustomDevice(name, brand, ip, connType)
                        showAddTvDialog = false
                    },
                    onDismiss = { showAddTvDialog = false }
                )
            }

            // Premium Upgrade Dialog
            if (showPremiumDialog) {
                PremiumDialog(
                    isPremium = isPremium,
                    onTogglePremium = { viewModel.setPremium(!isPremium) },
                    onDismiss = { showPremiumDialog = false }
                )
            }

            // Login / Signup Dialog
            if (showLoginDialog) {
                LoginDialog(
                    onLogin = { email, pwd -> viewModel.login(email, pwd) },
                    onRegister = { email, pwd -> viewModel.register(email, pwd) },
                    onDismiss = { showLoginDialog = false }
                )
            }

            // Macro Creator Dialog
            if (showMacroDialog) {
                MacroCreatorDialog(
                    onSave = { name, commands ->
                        viewModel.addMacro(name, commands)
                        showMacroDialog = false
                    },
                    onDismiss = { showMacroDialog = false }
                )
            }
        }
    }
}

// ==========================================
// REMOTE CONTROL TAB (TAB 0)
// ==========================================
@Composable
fun RemoteControlTab(
    viewModel: TvViewModel,
    onConnectRequest: () -> Unit,
    onScanRequest: () -> Unit
) {
    val connectedTv by viewModel.lastConnectedDevice.collectAsState()
    var isTouchpadActive by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Connected device card
        item {
            ConnectedTvBanner(
                tv = connectedTv,
                onDisconnect = { viewModel.disconnectTv() },
                onConnectClick = onConnectRequest,
                onScanClick = onScanRequest
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Active control or Touchpad control switcher
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { isTouchpadActive = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isTouchpadActive) SurfaceVariantDark else Color.Transparent,
                        contentColor = if (!isTouchpadActive) PrimaryNeonCyan else Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_classic_remote"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.SettingsRemote, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Básico", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { isTouchpadActive = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTouchpadActive) SurfaceVariantDark else Color.Transparent,
                        contentColor = if (isTouchpadActive) PrimaryNeonCyan else Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_touchpad_remote"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Gesture, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Touchpad Mouse", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isTouchpadActive) {
            item {
                TouchpadControlArea(viewModel)
                Spacer(modifier = Modifier.height(20.dp))
            }
        } else {
            // Power and Sound controls Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Power Button
                    IconButton(
                        onClick = { viewModel.sendCommand("POWER") },
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(6.dp, CircleShape)
                            .background(
                                Brush.radialGradient(listOf(Color(0xFFFF2E5A), Color(0xFFC2002A))),
                                CircleShape
                            )
                            .border(1.dp, Color(0xFFFF859F), CircleShape)
                            .testTag("remote_power_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Power",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Connected indicator info
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MÉTODO", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            connectedTv?.connectionType ?: "Wi-Fi Mode",
                            color = PrimaryNeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Mute Button
                    IconButton(
                        onClick = { viewModel.sendCommand("MUTE") },
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(4.dp, CircleShape)
                            .background(SurfaceDark, CircleShape)
                            .border(1.dp, SurfaceVariantDark, CircleShape)
                            .testTag("remote_mute_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeOff,
                            contentDescription = "Mute",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Directional Nav D-Pad Circle
            item {
                NavDPad(
                    onUp = { viewModel.sendCommand("UP") },
                    onDown = { viewModel.sendCommand("DOWN") },
                    onLeft = { viewModel.sendCommand("LEFT") },
                    onRight = { viewModel.sendCommand("RIGHT") },
                    onOk = { viewModel.sendCommand("OK") }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Volume and Channel sliders/double controls
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Volume Control Card
                    VerticalControlStack(
                        title = "VOLUME",
                        iconUp = Icons.Default.Add,
                        iconDown = Icons.Default.Remove,
                        onUpClick = { viewModel.sendCommand("VOLUME_UP") },
                        onDownClick = { viewModel.sendCommand("VOLUME_DOWN") },
                        tagPrefix = "vol"
                    )

                    // Secondary system actions (Back, Home, Input)
                    Column(
                        modifier = Modifier.height(140.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = { viewModel.sendCommand("BACK") },
                            modifier = Modifier
                                .size(48.dp)
                                .background(SurfaceDark, CircleShape)
                                .testTag("remote_back_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextSecondary)
                        }
                        IconButton(
                            onClick = { viewModel.sendCommand("HOME") },
                            modifier = Modifier
                                .size(48.dp)
                                .background(SurfaceDark, CircleShape)
                                .testTag("remote_home_btn")
                        ) {
                            Icon(Icons.Default.Home, contentDescription = "Home", tint = TextSecondary)
                        }
                        IconButton(
                            onClick = { viewModel.sendCommand("MENU") },
                            modifier = Modifier
                                .size(48.dp)
                                .background(SurfaceDark, CircleShape)
                                .testTag("remote_menu_btn")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu/Config", tint = TextSecondary)
                        }
                    }

                    // Channel Control Card
                    VerticalControlStack(
                        title = "CANAL",
                        iconUp = Icons.Default.KeyboardArrowUp,
                        iconDown = Icons.Default.KeyboardArrowDown,
                        onUpClick = { viewModel.sendCommand("CHANNEL_UP") },
                        onDownClick = { viewModel.sendCommand("CHANNEL_DOWN") },
                        tagPrefix = "ch"
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Quick Streaming Banners Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceVariantDark)
                Text(
                    "ATALHOS DE STREAMING",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceVariantDark)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Streaming Quick Launcher Button List
        item {
            StreamingButtonGrid(onLaunch = { streamName ->
                viewModel.sendCommand("OPEN_$streamName")
            })
        }
    }
}

// ==========================================
// SUB-COMPONENTS FOR REMOTE TAB
// ==========================================
@Composable
fun ConnectedTvBanner(
    tv: TvDevice?,
    onDisconnect: () -> Unit,
    onConnectClick: () -> Unit,
    onScanClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tv_status_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, if (tv != null) PrimaryNeonCyan.copy(alpha = 0.3f) else SurfaceVariantDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Pulse Green indicator
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(SurfaceVariantDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (tv != null) Icons.Default.Tv else Icons.Default.TvOff,
                        contentDescription = null,
                        tint = if (tv != null) PrimaryNeonCyan else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    if (tv != null) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                                .padding(1.dp)
                                .background(AccentGreen.copy(alpha = pulseAlpha), CircleShape)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = tv?.name ?: "Nenhuma TV Conectada",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (tv != null) "Conectado • ${tv.ipAddress}" else "Toque para parear e conectar",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
            if (tv != null) {
                IconButton(
                    onClick = onDisconnect,
                    modifier = Modifier.testTag("disconnect_btn")
                ) {
                    Icon(Icons.Default.LinkOff, contentDescription = "Desconectar", tint = AccentRed)
                }
            } else {
                Row {
                    TextButton(onClick = onScanClick) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Escanear", fontSize = 12.sp, color = PrimaryNeonCyan)
                    }
                }
            }
        }
    }
}

@Composable
fun NavDPad(
    onUp: () -> Unit,
    onDown: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onOk: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(SurfaceDark, CircleShape)
            .border(2.dp, SurfaceVariantDark, CircleShape)
            .testTag("remote_dpad"),
        contentAlignment = Alignment.Center
    ) {
        // Up Directional
        IconButton(
            onClick = onUp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .size(48.dp)
                .testTag("dpad_up")
        ) {
            Icon(Icons.Default.ArrowDropUp, contentDescription = "Up", tint = Color.White, modifier = Modifier.size(36.dp))
        }

        // Down Directional
        IconButton(
            onClick = onDown,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
                .size(48.dp)
                .testTag("dpad_down")
        ) {
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Down", tint = Color.White, modifier = Modifier.size(36.dp))
        }

        // Left Directional
        IconButton(
            onClick = onLeft,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
                .size(48.dp)
                .testTag("dpad_left")
        ) {
            Icon(Icons.Default.ArrowLeft, contentDescription = "Left", tint = Color.White, modifier = Modifier.size(36.dp))
        }

        // Right Directional
        IconButton(
            onClick = onRight,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .size(48.dp)
                .testTag("dpad_right")
        ) {
            Icon(Icons.Default.ArrowRight, contentDescription = "Right", tint = Color.White, modifier = Modifier.size(36.dp))
        }

        // Center OK Button
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(8.dp, CircleShape)
                .background(
                    Brush.radialGradient(listOf(SurfaceVariantDark, SurfaceDark)),
                    CircleShape
                )
                .border(2.dp, PrimaryNeonCyan, CircleShape)
                .clickable { onOk() }
                .testTag("dpad_ok"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "OK",
                color = PrimaryNeonCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun VerticalControlStack(
    title: String,
    iconUp: ImageVector,
    iconDown: ImageVector,
    onUpClick: () -> Unit,
    onDownClick: () -> Unit,
    tagPrefix: String
) {
    Card(
        modifier = Modifier
            .width(68.dp)
            .height(148.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, SurfaceVariantDark)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onUpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("${tagPrefix}_up")
            ) {
                Icon(imageVector = iconUp, contentDescription = "Mais", tint = Color.White, modifier = Modifier.size(24.dp))
            }

            Text(
                text = title,
                fontSize = 10.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = onDownClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("${tagPrefix}_down")
            ) {
                Icon(imageVector = iconDown, contentDescription = "Menos", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun TouchpadControlArea(viewModel: TvViewModel) {
    val touchpadText by viewModel.touchpadLog.collectAsState()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, PrimaryNeonCyan.copy(alpha = 0.4f))
    ) {
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            val threshold = 100f
                            if (abs(offsetX) > abs(offsetY)) {
                                if (offsetX > threshold) {
                                    viewModel.registerTouchpadMovement("Direita")
                                } else if (offsetX < -threshold) {
                                    viewModel.registerTouchpadMovement("Esquerda")
                                }
                            } else {
                                if (offsetY > threshold) {
                                    viewModel.registerTouchpadMovement("Baixo")
                                } else if (offsetY < -threshold) {
                                    viewModel.registerTouchpadMovement("Cima")
                                }
                            }
                            offsetX = 0f
                            offsetY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { viewModel.registerTouchpadClick() }
                    )
                }
                .testTag("remote_touchpad"),
            contentAlignment = Alignment.Center
        ) {
            // Draw sleek graphic grid lines to feel like a high-end mouse touchpad
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridColor = Color(0xFF202538)
                val spacing = 30.dp.toPx()
                
                var x = 0f
                while (x < size.width) {
                    drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                    x += spacing
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    y += spacing
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Gesture,
                    contentDescription = null,
                    tint = PrimaryNeonCyan.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Mova o dedo para Navegar",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Toque rápido para Confirmar (OK)",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    touchpadText,
                    color = PrimaryNeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(SurfaceVariantDark, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun StreamingButtonGrid(onLaunch: (String) -> Unit) {
    val streams = listOf(
        Triple("Netflix", "NETFLIX", Color(0xFFE50914)),
        Triple("Prime Video", "PRIME_VIDEO", Color(0xFF00A8E0)),
        Triple("Disney+", "DISNEY", Color(0xFF09173D)),
        Triple("Max", "MAX", Color(0xFF002BE7)),
        Triple("Globoplay", "GLOBOPLAY", Color(0xFFFF4B00)),
        Triple("YouTube", "YOUTUBE", Color(0xFFFF0000)),
        Triple("Spotify", "SPOTIFY", Color(0xFF1DB954)),
        Triple("Crunchyroll", "CRUNCHYROLL", Color(0xFFF78C25))
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in streams.chunked(2)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (item in row) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable { onLaunch(item.second) }
                            .testTag("stream_${item.second}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, item.third.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(item.third, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.first,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// DEVICES & MACROS TAB (TAB 1)
// ==========================================
@Composable
fun DevicesTab(
    viewModel: TvViewModel,
    onScanRequest: () -> Unit,
    onAddTvRequest: () -> Unit,
    onCreateMacroRequest: () -> Unit
) {
    val devices by viewModel.allDevices.collectAsState()
    val connectedTv by viewModel.lastConnectedDevice.collectAsState()
    val macros by viewModel.allMacros.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Quick Actions Row
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onScanRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeonCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_scan_radar")
                ) {
                    Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Radar de TVs", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onAddTvRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, SurfaceVariantDark),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_add_manual_tv")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Adicionar Manual", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Saved Devices List Section
        item {
            Text(
                "DISPOSITIVOS ENCONTRADOS (${devices.size})",
                color = PrimaryNeonCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (devices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.TvOff, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nenhum dispositivo cadastrado", color = TextSecondary, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        } else {
            items(devices) { device ->
                val isCurrent = connectedTv?.id == device.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("tv_device_item_${device.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) SurfaceVariantDark else SurfaceDark
                    ),
                    border = BorderStroke(1.dp, if (isCurrent) PrimaryNeonCyan else SurfaceVariantDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.connectToTv(device) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (isCurrent) PrimaryNeonCyan.copy(alpha = 0.1f) else SurfaceVariantDark,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tv,
                                    contentDescription = null,
                                    tint = if (isCurrent) PrimaryNeonCyan else Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(device.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${device.brand} • ${device.ipAddress} • ${device.connectionType}", color = TextSecondary, fontSize = 11.sp)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Star Favorite indicator
                            IconButton(onClick = { viewModel.toggleFavoriteDevice(device) }) {
                                Icon(
                                    imageVector = if (device.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Favorito",
                                    tint = if (device.isFavorite) AccentOrange else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(onClick = { viewModel.deleteDevice(device) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Excluir",
                                    tint = AccentRed.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        // Macros Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "MACROS INTELIGENTES (${macros.size})",
                    color = PrimaryNeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                TextButton(onClick = onCreateMacroRequest) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Novo Macro", fontSize = 11.sp, color = PrimaryNeonCyan)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (macros.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Crie sequências de comandos personalizados", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(macros) { macro ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("macro_item_${macro.id}"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, SurfaceVariantDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(macro.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Sequência: ${macro.commands.replace(",", " ➔ ")}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.runMacro(macro) },
                                modifier = Modifier.testTag("run_macro_btn_${macro.id}")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Executar", tint = AccentGreen)
                            }
                            IconButton(onClick = { viewModel.deleteMacro(macro) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = AccentRed.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// IA VOICE & COMMMANDS HISTORIC (TAB 2)
// ==========================================
@Composable
fun AiVoiceTab(viewModel: TvViewModel) {
    val aiThinking by viewModel.aiThinking.collectAsState()
    val lastAiResult by viewModel.lastAiResult.collectAsState()
    val historyLogs by viewModel.history.collectAsState()
    
    var commandText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // AI command input block
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_control_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, PrimaryNeonCyan.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "CONTROLE INTELIGENTE DE VOZ / TEXTO",
                        color = PrimaryNeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Digite ou diga ações, a IA traduz em comandos de controle.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = commandText,
                        onValueChange = { commandText = it },
                        placeholder = { Text("Ex: Abrir Netflix e aumentar o volume...", fontSize = 14.sp, color = TextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voice_command_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryNeonCyan,
                            unfocusedBorderColor = SurfaceVariantDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        trailingIcon = {
                            if (commandText.isNotBlank()) {
                                IconButton(onClick = { commandText = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Gray)
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.processVoiceCommand(commandText)
                                commandText = ""
                            },
                            enabled = commandText.isNotBlank() && !aiThinking,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryNeonCyan,
                                disabledContainerColor = SurfaceVariantDark,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("send_command_btn")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Enviar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Simulated audio recording triggers
                        IconButton(
                            onClick = {
                                val simulatedPrompts = listOf(
                                    "Abrir YouTube",
                                    "Ligar a TV",
                                    "Mudo na TV",
                                    "Diminuir volume",
                                    "HDMI 2",
                                    "Abrir Spotify"
                                )
                                commandText = simulatedPrompts.random()
                            },
                            modifier = Modifier
                                .background(SurfaceVariantDark, RoundedCornerShape(10.dp))
                                .size(40.dp)
                                .testTag("mic_simulate_btn")
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Simulate voice", tint = PrimaryNeonCyan)
                        }
                    }

                    // AI waveform visual simulation
                    if (aiThinking) {
                        Spacer(modifier = Modifier.height(16.dp))
                        AiWaveformAnimation()
                        Text("A IA está interpretando o comando...", color = PrimaryNeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Display last AI interpretation card
        lastAiResult?.let { result ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_result_card"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
                    border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = PrimaryNeonCyan, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Interpretação da IA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Comando Identificado: ${result.action}",
                            color = PrimaryNeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        if (result.parameter != null) {
                            Text("Parâmetro extra: ${result.parameter}", color = TextSecondary, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Ação executada: ${result.reason}", color = AccentGreen, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Historical Log Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "LOGS DE COMANDO RECENTES",
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                if (historyLogs.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearHistory() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Limpar", fontSize = 12.sp, color = AccentRed)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (historyLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sem histórico de comandos executados.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(historyLogs) { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .testTag("history_item_${log.id}"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, SurfaceVariantDark.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (log.command.startsWith("OPEN_")) Icons.Default.Launch else Icons.Default.Adjust,
                                contentDescription = null,
                                tint = if (log.command.startsWith("OPEN_")) PrimaryNeonBlue else PrimaryNeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(log.command, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("No dispositivo: ${log.deviceName}", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                        
                        // Parse timestamp to human readable relative time simply
                        Text(
                            text = "Agora",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiWaveformAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val heights = listOf(0.4f, 1.0f, 0.6f, 1.2f, 0.5f, 0.9f, 0.4f)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEachIndexed { idx, maxH ->
            val animHeight by infiniteTransition.animateFloat(
                initialValue = 4.dp.value,
                targetValue = (maxH * 32.dp.value),
                animationSpec = infiniteRepeatable(
                    animation = tween(400 + idx * 80, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "wave_bar_$idx"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .width(4.dp)
                    .height(animHeight.dp)
                    .background(PrimaryNeonCyan, RoundedCornerShape(2.dp))
            )
        }
    }
}


// ==========================================
// USER PROFILE & PREMIUM TAB (TAB 3)
// ==========================================
@Composable
fun ProfileTab(
    viewModel: TvViewModel,
    onLoginRequest: () -> Unit,
    onPremiumRequest: () -> Unit
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Premium Badge or Promo Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPremiumRequest() }
                    .testTag("premium_promo_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(
                    1.dp,
                    if (isPremium) AccentOrange.copy(alpha = 0.8f) else PrimaryNeonCyan.copy(alpha = 0.4f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            if (isPremium) {
                                drawCircle(
                                    color = AccentOrange.copy(alpha = 0.15f),
                                    center = Offset(size.width, 0f),
                                    radius = size.width * 0.4f
                                )
                            }
                        }
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = if (isPremium) AccentOrange else PrimaryNeonCyan,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPremium) "Sócio Premium Ativo" else "Seja TV Control Premium",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isPremium) {
                                "Parabéns! Você tem controle ilimitado, suporte a voz IA contínuo e criação de macros ilimitadas."
                            } else {
                                "Desbloqueie controle remoto multi-TVs, macros ilimitadas, comandos ilimitados de IA e remova anúncios."
                            },
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isPremium) "Gerenciar Assinatura" else "Ver Planos (A partir de R$ 9,90)",
                            color = if (isPremium) AccentOrange else PrimaryNeonCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Account / Login Module
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, SurfaceVariantDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryNeonCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Conta de Usuário", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    if (isLoggedIn) {
                        Text("Identificado como:", color = TextSecondary, fontSize = 12.sp)
                        Text(userEmail, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.logout() },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_logout")
                        ) {
                            Text("Desconectar Conta")
                        }
                    } else {
                        Text(
                            "Faça login para sincronizar suas TVs cadastradas e macros em nuvem de forma segura.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onLoginRequest,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeonCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_request_login")
                        ) {
                            Text("Entrar / Cadastrar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Technical App details
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, SurfaceVariantDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("SOBRE O TV CONTROL IA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Versão", color = TextSecondary, fontSize = 13.sp)
                        Text("1.0.0 (IA Powered)", color = Color.White, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Engine IA", color = TextSecondary, fontSize = 13.sp)
                        Text("Google Gemini-3.5-Flash", color = PrimaryNeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Banco de dados", color = TextSecondary, fontSize = 13.sp)
                        Text("Room SQLite Local", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}


// ==========================================
// RADAR SCANNER DIALOG
// ==========================================
@Composable
fun ScanRadarDialog(viewModel: TvViewModel, onDismiss: () -> Unit) {
    val isScanning by viewModel.isScanning.collectAsState()
    val foundDevices by viewModel.allDevices.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("scan_radar_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Buscando Smart TVs...",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Buscando nas frequências Wi-Fi (SSDP, mDNS, DLNA)",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Radar scanning circular canvas drawing
                if (isScanning) {
                    RadarPulseCircle()
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Concluído",
                        tint = AccentGreen,
                        modifier = Modifier.size(72.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "DISPOSITIVOS ENCONTRADOS:",
                    color = PrimaryNeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp)
                ) {
                    foundDevices.take(3).forEach { tv ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.connectToTv(tv)
                                    onDismiss()
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Tv, contentDescription = null, tint = PrimaryNeonCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(tv.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(tv.brand, color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                            Text("Pronto", color = AccentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PrimaryNeonCyan, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    TextButton(onClick = onDismiss) {
                        Text(if (isScanning) "Cancelar" else "Fechar", color = PrimaryNeonCyan)
                    }
                }
            }
        }
    }
}

@Composable
fun RadarPulseCircle() {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val radarScale1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse1"
    )
    val radarScale2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse2"
    )

    Box(
        modifier = Modifier
            .size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw pulse 1
            drawCircle(
                color = PrimaryNeonCyan.copy(alpha = 1f - radarScale1),
                radius = size.minDimension / 2 * radarScale1,
                style = Stroke(width = 4f)
            )
            // Draw pulse 2 (delayed offset simply)
            val scaleDelayed = if (radarScale2 > 0.5f) radarScale2 - 0.5f else radarScale2 + 0.5f
            drawCircle(
                color = PrimaryNeonCyan.copy(alpha = 1f - scaleDelayed),
                radius = size.minDimension / 2 * scaleDelayed,
                style = Stroke(width = 2f)
            )
        }
        
        // Static central core
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(PrimaryNeonCyan, CircleShape)
        )
    }
}


// ==========================================
// PAIRING DIALOG
// ==========================================
@Composable
fun PairingDialog(tv: TvDevice, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var code1 by remember { mutableStateOf("") }
    var code2 by remember { mutableStateOf("") }
    var code3 by remember { mutableStateOf("") }
    var code4 by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("pairing_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Link, contentDescription = null, tint = PrimaryNeonCyan, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Conectando a ${tv.name}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Insira o código de 4 dígitos exibido na tela da sua TV para validar a autorização.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Displays mock digits shown on "TV" screen so they can quickly enter them
                Text(
                    "Código exibido na TV: 4 8 2 1",
                    color = PrimaryNeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(SurfaceVariantDark, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Input fields
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tfColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryNeonCyan,
                        unfocusedBorderColor = SurfaceVariantDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                    OutlinedTextField(
                        value = code1,
                        onValueChange = { if (it.length <= 1) code1 = it },
                        modifier = Modifier.width(44.dp).testTag("code_digit_1"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = tfColors
                    )
                    OutlinedTextField(
                        value = code2,
                        onValueChange = { if (it.length <= 1) code2 = it },
                        modifier = Modifier.width(44.dp).testTag("code_digit_2"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = tfColors
                    )
                    OutlinedTextField(
                        value = code3,
                        onValueChange = { if (it.length <= 1) code3 = it },
                        modifier = Modifier.width(44.dp).testTag("code_digit_3"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = tfColors
                    )
                    OutlinedTextField(
                        value = code4,
                        onValueChange = { if (it.length <= 1) code4 = it },
                        modifier = Modifier.width(44.dp).testTag("code_digit_4"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = tfColors
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = Color.Gray)
                    }
                    Button(
                        onClick = { onConfirm("$code1$code2$code3$code4") },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeonCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("pairing_confirm_btn")
                    ) {
                        Text("Conectar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// ==========================================
// ADD MANUAL TV DIALOG
// ==========================================
@Composable
fun AddTvDialog(onAdd: (String, String, String, String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("Samsung") }
    var ipAddress by remember { mutableStateOf("") }
    var connectionType by remember { mutableStateOf("Wi-Fi") }

    val brands = listOf("Samsung", "LG", "Sony", "Philips", "TCL", "AOC", "Roku TV", "Android TV")
    val connTypes = listOf("Wi-Fi", "Infravermelho (IR)", "Bluetooth")

    var expandedBrandMenu by remember { mutableStateOf(false) }
    var expandedConnMenu by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_tv_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Cadastrar Smart TV Manual",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da TV (Ex: TV Quarto)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_tv_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryNeonCyan, focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    )
                )

                // Brand Selector field
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fabricante / Marca") },
                        modifier = Modifier.fillMaxWidth().clickable { expandedBrandMenu = true }.testTag("add_tv_brand"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        ),
                        trailingIcon = {
                            IconButton(onClick = { expandedBrandMenu = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expandedBrandMenu,
                        onDismissRequest = { expandedBrandMenu = false },
                        modifier = Modifier.background(SurfaceVariantDark)
                    ) {
                        brands.forEach { b ->
                            DropdownMenuItem(
                                text = { Text(b, color = Color.White) },
                                onClick = {
                                    brand = b
                                    expandedBrandMenu = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { ipAddress = it },
                    label = { Text("Endereço IP (Ex: 192.168.1.50)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_tv_ip"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryNeonCyan, focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    )
                )

                // Connection Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = connectionType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Método de Conexão") },
                        modifier = Modifier.fillMaxWidth().clickable { expandedConnMenu = true }.testTag("add_tv_conn_type"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        ),
                        trailingIcon = {
                            IconButton(onClick = { expandedConnMenu = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expandedConnMenu,
                        onDismissRequest = { expandedConnMenu = false },
                        modifier = Modifier.background(SurfaceVariantDark)
                    ) {
                        connTypes.forEach { ct ->
                            DropdownMenuItem(
                                text = { Text(ct, color = Color.White) },
                                onClick = {
                                    connectionType = ct
                                    expandedConnMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Voltar", color = Color.Gray)
                    }
                    Button(
                        onClick = { onAdd(name.ifBlank { "$brand Smart TV" }, brand, ipAddress, connectionType) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeonCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_tv_save_btn")
                    ) {
                        Text("Cadastrar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// ==========================================
// PREMIUM UPGRADE DIALOG
// ==========================================
@Composable
fun PremiumDialog(isPremium: Boolean, onTogglePremium: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, AccentOrange),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("premium_modal_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = AccentOrange,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "TV CONTROL IA PREMIUM",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Desbloqueie todo o poder da inteligência universal",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                val benefits = listOf(
                    "Controle ilimitado de Smart TVs (Sem limites!)",
                    "Ações e macros ilimitados (Ligue tudo de uma vez)",
                    "Comandos IA de voz sem franquias mensais",
                    "Suporte prioritário e widgets exclusivos",
                    "Experiência limpa, totalmente livre de anúncios"
                )

                benefits.forEach { b ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(b, color = Color.White, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onTogglePremium()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("premium_cta_btn")
                ) {
                    Text(
                        if (isPremium) "Desativar Modo Premium (Teste)" else "Assinar Premium - R$ 9,90/mês",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text("Agora não", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}


// ==========================================
// LOGIN & REGISTRATION DIALOG
// ==========================================
@Composable
fun LoginDialog(
    onLogin: (String, String) -> Boolean,
    onRegister: (String, String) -> Boolean,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("login_modal")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isRegistering) "Criar Nova Conta" else "Entrar no TV Control",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Acesse suas configurações de Smart TV em qualquer dispositivo",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                if (errorMessage.isNotBlank()) {
                    Text(errorMessage, color = AccentRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") },
                    modifier = Modifier.fillMaxWidth().testTag("login_email"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryNeonCyan, focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Senha") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("login_password"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryNeonCyan, focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val success = if (isRegistering) {
                            onRegister(email, password)
                        } else {
                            onLogin(email, password)
                        }
                        if (success) {
                            onDismiss()
                        } else {
                            errorMessage = "E-mail inválido ou senha menor de 6 caracteres."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeonCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_submit_btn")
                ) {
                    Text(if (isRegistering) "Cadastrar" else "Entrar", fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = { isRegistering = !isRegistering },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isRegistering) "Já tenho conta? Entrar" else "Não tem conta? Cadastrar-se",
                        color = PrimaryNeonCyan,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}


// ==========================================
// MACRO CREATOR DIALOG
// ==========================================
@Composable
fun MacroCreatorDialog(onSave: (String, String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    
    // Commands checklist simply
    val availableCmds = listOf(
        "POWER", "VOLUME_UP", "VOLUME_DOWN", "CHANNEL_UP", "CHANNEL_DOWN",
        "MUTE", "HOME", "OPEN_NETFLIX", "OPEN_SPOTIFY", "OPEN_YOUTUBE"
    )
    val selectedCmds = remember { mutableStateListOf<String>() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("macro_creator_dialog")
        ) {
            LazyColumn(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Criar Macro de Comandos",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Combine múltiplos botões em um único toque",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome da Macro (Ex: Cinema)") },
                        modifier = Modifier.fillMaxWidth().testTag("macro_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryNeonCyan, focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    Text("SELECIONE OS COMANDOS (Ordem sequencial):", color = PrimaryNeonCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Show current sequence built
                    Text(
                        text = "Sequência atual: ${if (selectedCmds.isEmpty()) "Vazia" else selectedCmds.joinToString(" ➔ ")}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceVariantDark, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(availableCmds) { cmd ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedCmds.contains(cmd)) {
                                    selectedCmds.remove(cmd)
                                } else {
                                    selectedCmds.add(cmd)
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedCmds.contains(cmd),
                            onCheckedChange = { checked ->
                                if (checked == true) selectedCmds.add(cmd) else selectedCmds.remove(cmd)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = PrimaryNeonCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(cmd, color = Color.White, fontSize = 13.sp)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancelar", color = Color.Gray)
                        }
                        Button(
                            onClick = {
                                if (name.isNotBlank() && selectedCmds.isNotEmpty()) {
                                    onSave(name, selectedCmds.joinToString(","))
                                }
                            },
                            enabled = name.isNotBlank() && selectedCmds.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeonCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("macro_save_btn")
                        ) {
                            Text("Salvar Macro", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
