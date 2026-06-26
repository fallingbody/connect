package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    private val viewModel: CoupleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CoupleConnectApp(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoupleConnectApp(viewModel: CoupleViewModel) {
    val coupleState by viewModel.coupleState.collectAsState()
    val activePoke by viewModel.activePoke.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        TabItem("Us Today", Icons.Default.Favorite),
        TabItem("Our Prompts", Icons.Default.QuestionAnswer),
        TabItem("Sweet Chat", Icons.Default.ChatBubble),
        TabItem("Memory Box", Icons.Default.AutoStories),
        TabItem("Decision Wheel", Icons.Default.Casino)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Couple Connect",
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("app_title")
                        )
                        Text("💖", fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ),
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = if (coupleState.partnerGhostMode) "Ghost On 👻" else "Stats Live ✨",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (coupleState.partnerGhostMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = coupleState.partnerGhostMode,
                            onCheckedChange = { viewModel.toggleGhostMode() },
                            modifier = Modifier.scale(0.7f).testTag("ghost_mode_switch")
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.title.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main content based on selection
            when (selectedTab) {
                0 -> StatusDashboardScreen(viewModel, coupleState)
                1 -> PromptsScreen(viewModel)
                2 -> ChatScreen(viewModel)
                3 -> MemoriesScreen(viewModel)
                4 -> DecisionWheelScreen(viewModel)
            }

            // Peer-to-peer Voice/Video Calling Overlay
            val activeCallType by viewModel.activeCallType.collectAsState()
            AnimatedVisibility(
                visible = activeCallType != null,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                activeCallType?.let { callType ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF140F1D))
                            .padding(24.dp)
                            .clickable(enabled = false) {}
                            .testTag("call_overlay_container"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Text(
                                text = "🔒 Secure P2P WebRTC Connection",
                                color = Color.Green.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Glowing avatars
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // User
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🐣", fontSize = 40.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("You (Live)", color = Color.White, fontSize = 12.sp)
                                }
                                
                                // Glowing double arrow
                                Text("⚡", fontSize = 32.sp)
                                
                                // Partner
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
                                            .border(2.dp, MaterialTheme.colorScheme.tertiary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🐰", fontSize = 40.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Darling", color = Color.White, fontSize = 12.sp)
                                }
                            }
                            
                            // Call Type Indicators
                            Text(
                                text = "ACTIVE ${callType.uppercase()} STREAM",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            )
                            
                            // Video feeds simulation
                            if (callType == "video") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.DarkGray)
                                        .border(1.dp, Color.LightGray.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🎥 WebRTC Peer Stream Running...", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                                    // Tiny self camera view
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp, 40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black)
                                            .align(Alignment.BottomEnd)
                                            .padding(4.dp)
                                    ) {
                                        Text("🐣 (Self)", color = Color.White, fontSize = 8.sp)
                                    }
                                }
                            } else {
                                // Voice wave animation
                                val infiniteTransition = rememberInfiniteTransition(label = "voice_wave")
                                val waveFactor by infiniteTransition.animateFloat(
                                    initialValue = 10f,
                                    targetValue = 60f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(600, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "wave"
                                )
                                Row(
                                    modifier = Modifier.height(80.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf(0.5f, 0.8f, 1f, 0.7f, 0.4f, 0.9f).forEach { scale ->
                                        Box(
                                            modifier = Modifier
                                                .width(6.dp)
                                                .height((waveFactor * scale).dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // End Call Button
                            Button(
                                onClick = { viewModel.endCall() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.testTag("end_call_button")
                            ) {
                                Icon(Icons.Default.CallEnd, "End", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Hang Up", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // A.10 / B.9 Full Screen Poke Animation Overlay
            AnimatedVisibility(
                visible = activePoke != null,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f)
            ) {
                activePoke?.let { pokeText ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.75f))
                            .clickable { viewModel.dismissPoke() },
                        contentAlignment = Alignment.Center
                    ) {
                        // Soft glowing heart-shaped pulsating background
                        val infiniteTransition = rememberInfiniteTransition(label = "poke_glow")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "poke_scale"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            modifier = Modifier
                                .padding(32.dp)
                                .scale(scale)
                        ) {
                            Text(
                                "👉 BOOP! 👈",
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .border(2.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(24.dp))
                                    .padding(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        pokeText,
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Tap anywhere to send a happy emoji back! 💕",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Beautiful custom Isometric Bedroom/Kitchen/Theatre/Garden Room
@Composable
fun IsometricRoomCanvas(
    userAction: String,
    partnerAction: String,
    weather: String,
    plantProgress: Int,
    petHappiness: Int
) {
    // Rain/snow particle logic
    val infiniteTransition = rememberInfiniteTransition(label = "room_particles")
    val particleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2E243D),
                        Color(0xFF140F1D)
                    )
                )
            )
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f + 15.dp.toPx()

            // Draw Isometric Floor Grid (diamond shape)
            val pathFloor = Path().apply {
                moveTo(cx, cy - 65.dp.toPx()) // Top corner
                lineTo(cx + 140.dp.toPx(), cy) // Right corner
                lineTo(cx, cy + 50.dp.toPx()) // Bottom corner
                lineTo(cx - 140.dp.toPx(), cy) // Left corner
                close()
            }
            drawPath(pathFloor, color = Color(0xFF4A3E5C))

            // Draw Left Wall
            val pathLeftWall = Path().apply {
                moveTo(cx, cy - 65.dp.toPx())
                lineTo(cx - 140.dp.toPx(), cy)
                lineTo(cx - 140.dp.toPx(), cy - 60.dp.toPx())
                lineTo(cx, cy - 120.dp.toPx())
                close()
            }
            drawPath(pathLeftWall, color = Color(0xFF382F47))

            // Draw Right Wall
            val pathRightWall = Path().apply {
                moveTo(cx, cy - 65.dp.toPx())
                lineTo(cx + 140.dp.toPx(), cy)
                lineTo(cx + 140.dp.toPx(), cy - 60.dp.toPx())
                lineTo(cx, cy - 120.dp.toPx())
                close()
            }
            drawPath(pathRightWall, color = Color(0xFF30273D))

            // Draw Cozy Bed (Left Wall)
            val bedX = cx - 85.dp.toPx()
            val bedY = cy - 35.dp.toPx()
            // Mattress
            drawRect(
                color = Color(0xFFE28597),
                topLeft = Offset(bedX, bedY),
                size = Size(40.dp.toPx(), 20.dp.toPx())
            )
            // Pillows
            drawRect(
                color = Color.White,
                topLeft = Offset(bedX + 4.dp.toPx(), bedY - 4.dp.toPx()),
                size = Size(12.dp.toPx(), 8.dp.toPx())
            )

            // Draw Kitchen Stove/Counter (Right Wall)
            val stoveX = cx + 45.dp.toPx()
            val stoveY = cy - 30.dp.toPx()
            drawRect(
                color = Color(0xFF7D728D),
                topLeft = Offset(stoveX, stoveY),
                size = Size(32.dp.toPx(), 22.dp.toPx())
            )
            // Little frying pan circle
            drawCircle(
                color = Color.DarkGray,
                radius = 4.dp.toPx(),
                center = Offset(stoveX + 10.dp.toPx(), stoveY + 10.dp.toPx())
            )

            // Draw Couch (Center Back)
            val couchX = cx - 15.dp.toPx()
            val couchY = cy - 40.dp.toPx()
            drawRect(
                color = Color(0xFFE0A96D),
                topLeft = Offset(couchX, couchY),
                size = Size(35.dp.toPx(), 16.dp.toPx())
            )

            // Draw TV Screen (Far Right Wall)
            drawRect(
                color = Color.Black,
                topLeft = Offset(cx + 90.dp.toPx(), cy - 75.dp.toPx()),
                size = Size(25.dp.toPx(), 18.dp.toPx())
            )
            // Glowing TV light beam
            if (userAction == "watching_tv" || partnerAction == "watching_tv") {
                val pathGlow = Path().apply {
                    moveTo(cx + 90.dp.toPx(), cy - 65.dp.toPx())
                    lineTo(cx + 10.dp.toPx(), cy - 5.dp.toPx())
                    lineTo(cx + 50.dp.toPx(), cy + 15.dp.toPx())
                    close()
                }
                drawPath(pathGlow, color = Color(0x2200E5FF))
            }

            // Draw Love Flower Pot (Center Front)
            val potX = cx - 5.dp.toPx()
            val potY = cy + 12.dp.toPx()
            drawRect(
                color = Color(0xFFC39B7A),
                topLeft = Offset(potX, potY),
                size = Size(10.dp.toPx(), 10.dp.toPx())
            )
            val stemHeight = (12.dp.toPx() * (plantProgress / 100f)).coerceAtLeast(3.dp.toPx())
            drawLine(
                color = Color(0xFF4CAF50),
                start = Offset(potX + 5.dp.toPx(), potY),
                end = Offset(potX + 5.dp.toPx(), potY - stemHeight),
                strokeWidth = 2.dp.toPx()
            )
            if (plantProgress >= 50) {
                drawCircle(
                    color = Color(0xFFFF4081),
                    radius = 4.dp.toPx(),
                    center = Offset(potX + 5.dp.toPx(), potY - stemHeight)
                )
            }

            // Draw Sleeping Puppy (Center Bottom Floor)
            val dogX = cx - 45.dp.toPx()
            val dogY = cy + 15.dp.toPx()
            drawCircle(
                color = Color(0xFFD7CCC8),
                radius = 8.dp.toPx(),
                center = Offset(dogX, dogY)
            )
            drawCircle(
                color = Color(0xFFD7CCC8),
                radius = 5.dp.toPx(),
                center = Offset(dogX + 6.dp.toPx(), dogY - 2.dp.toPx())
            )

            // --- DRAW AVATARS ---
            val userPos = when (userAction) {
                "sleeping" -> Offset(bedX + 20.dp.toPx(), bedY + 10.dp.toPx())
                "cooking" -> Offset(stoveX + 8.dp.toPx(), stoveY + 25.dp.toPx())
                "watching_tv" -> Offset(cx + 50.dp.toPx(), cy - 20.dp.toPx())
                "couch" -> Offset(couchX + 10.dp.toPx(), couchY + 8.dp.toPx())
                else -> Offset(cx - 15.dp.toPx(), cy - 8.dp.toPx())
            }

            val partnerPos = when (partnerAction) {
                "sleeping" -> Offset(bedX + 10.dp.toPx(), bedY + 6.dp.toPx())
                "cooking" -> Offset(stoveX + 24.dp.toPx(), stoveY + 25.dp.toPx())
                "watching_tv" -> Offset(cx + 34.dp.toPx(), cy - 28.dp.toPx())
                "couch" -> Offset(couchX + 25.dp.toPx(), couchY + 8.dp.toPx())
                else -> Offset(cx + 12.dp.toPx(), cy - 8.dp.toPx())
            }

            // Draw rings
            drawCircle(color = MoodGlowYellow.copy(alpha = 0.25f), radius = 10.dp.toPx(), center = userPos)
            drawCircle(color = MoodGlowPink.copy(alpha = 0.25f), radius = 10.dp.toPx(), center = partnerPos)

            // Draw center dots
            drawCircle(color = MoodGlowYellow, radius = 7.dp.toPx(), center = userPos)
            drawCircle(color = MoodGlowPink, radius = 7.dp.toPx(), center = partnerPos)

            // Draw faces
            drawCircle(color = Color.Black, radius = 1.dp.toPx(), center = Offset(userPos.x - 2.dp.toPx(), userPos.y - 1.dp.toPx()))
            drawCircle(color = Color.Black, radius = 1.dp.toPx(), center = Offset(userPos.x + 2.dp.toPx(), userPos.y - 1.dp.toPx()))
            drawLine(color = Color.Black, start = Offset(userPos.x - 1.5f, userPos.y + 2.dp.toPx()), end = Offset(userPos.x + 1.5f, userPos.y + 2.dp.toPx()), strokeWidth = 1.dp.toPx())

            drawCircle(color = Color.Black, radius = 1.dp.toPx(), center = Offset(partnerPos.x - 2.dp.toPx(), partnerPos.y - 1.dp.toPx()))
            drawCircle(color = Color.Black, radius = 1.dp.toPx(), center = Offset(partnerPos.x + 2.dp.toPx(), partnerPos.y - 1.dp.toPx()))
            drawLine(color = Color.Black, start = Offset(partnerPos.x - 1.5f, partnerPos.y + 2.dp.toPx()), end = Offset(partnerPos.x + 1.5f, partnerPos.y + 2.dp.toPx()), strokeWidth = 1.dp.toPx())

            if (userAction == "idle" && partnerAction == "idle") {
                val hx = (userPos.x + partnerPos.x) / 2
                val hy = (userPos.y + partnerPos.y) / 2 - 12.dp.toPx()
                drawCircle(color = Color(0xFFFF4081), radius = 3.dp.toPx(), center = Offset(hx, hy))
                drawCircle(color = Color(0xFFFF4081), radius = 2.dp.toPx(), center = Offset(hx - 1.5f, hy - 0.7f))
                drawCircle(color = Color(0xFFFF4081), radius = 2.dp.toPx(), center = Offset(hx + 1.5f, hy - 0.7f))
            }

            // --- WEATHER PARTICLES OVERLAY ---
            if (weather == "Rainy 🌧️") {
                val numRaindrops = 20
                for (i in 0 until numRaindrops) {
                    val rx = ((i * 37) % w.toInt() + particleOffset * 0.4f) % w
                    val ry = ((i * 111) % h.toInt() + particleOffset * 1.5f) % h
                    drawLine(
                        color = Color(0x7F90CAF9),
                        start = Offset(rx, ry),
                        end = Offset(rx - 3.dp.toPx(), ry + 10.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            } else if (weather == "Snowy ❄️") {
                val numSnowflakes = 15
                for (i in 0 until numSnowflakes) {
                    val sx = ((i * 53) % w.toInt() + sin(particleOffset * 0.02f + i) * 12.dp.toPx()) % w
                    val sy = ((i * 97) % h.toInt() + particleOffset * 0.6f) % h
                    drawCircle(
                        color = Color.White.copy(alpha = 0.7f),
                        radius = 2.dp.toPx(),
                        center = Offset(sx, sy)
                    )
                }
            }
        }
    }
}

data class TabItem(val title: String, val icon: ImageVector)

// --- SCREEN 1: STATUS DASHBOARD ---
@Composable
fun StatusDashboardScreen(viewModel: CoupleViewModel, state: CoupleState) {
    var showScreentimeStats by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Row (A.2 Battery, A.1 Location, A.5 Ghost Mode, A.3 Screentime, A.4 Sleep Engine)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Our Device & Sync Status",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = { showScreentimeStats = !showScreentimeStats },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                if (showScreentimeStats) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                                contentDescription = "Screentime Stats",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Details Column
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, "Location", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (state.partnerGhostMode) "Incognito 👻" else state.partnerLocationName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.BatteryChargingFull, "Battery", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (state.partnerGhostMode) "Hidden 🔒" else "Partner: ${state.partnerBatteryPercent}% (Plugged in ⚡)",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Smartphone, "User Battery", tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Your Battery (Live): ${state.userBatteryPercent}%",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WatchLater, "Activity", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (state.partnerGhostMode) "Sleeping / Inactive 💤" else "Screen Active 📱 (Sleep Engine Active)",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Heart visual state indicator
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (state.partnerGhostMode) "👻" else "💑", fontSize = 32.sp)
                        }
                    }

                    // A.3 Live Screentime App stats breakdown expansion card
                    AnimatedVisibility(visible = showScreentimeStats) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text("Partner's Today Screen-time 📊", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(6.dp))
                            listOf(
                                "🐦 Twitter/X" to "14 mins",
                                "💬 WhatsApp" to "32 mins",
                                "🦉 Duolingo" to "20 mins",
                                "🎨 Canva" to "8 mins"
                            ).forEach { (app, duration) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(app, fontSize = 11.sp)
                                    Text(duration, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Our Co-Roaming Virtual Home 🏡 (E.24, E.25, E.26 Bedroom, E.27 Kitchen, E.28 Theatre screen share, E.29 Garden flower/pet, E.30 weather)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Our Virtual 3D World 🏡",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Co-Roaming Active",
                            fontSize = 11.sp,
                            color = Color.Green,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Renders the customized beautiful Isometric Floor Wall 3D Canvas
                    IsometricRoomCanvas(
                        userAction = state.avatarUserAction,
                        partnerAction = state.avatarPartnerAction,
                        weather = state.weatherCondition,
                        plantProgress = state.plantGrowth,
                        petHappiness = state.petHappiness
                    )

                    // Avatar placement controller buttons
                    Text("Select your Virtual Room Activity:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val actions = listOf(
                            "idle" to "🛋️ Lounge",
                            "sleeping" to "🛌 Snuggle",
                            "cooking" to "🍳 Cook",
                            "watching_tv" to "🎬 Cinema"
                        )
                        actions.forEach { (actionKey, label) ->
                            val isSelected = state.avatarUserAction == actionKey
                            Button(
                                onClick = { viewModel.setAvatarAction(actionKey) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(32.dp)
                            ) {
                                Text(label, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Weather selection row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Weather:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        val weathers = listOf("Sunny ☀️", "Rainy 🌧️", "Snowy ❄️", "Starry ✨")
                        weathers.forEach { weatherOption ->
                            val isSelected = state.weatherCondition == weatherOption
                            Button(
                                onClick = { viewModel.changeWeather(weatherOption) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(28.dp)
                            ) {
                                Text(weatherOption.substringAfter(" "), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // Garden and feeding actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Water Flower Action Button
                        Button(
                            onClick = { viewModel.waterPlant() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Icon(Icons.Default.LocalFlorist, "Water", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Water Flower (+10%)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Feed Pet Action Button
                        Button(
                            onClick = { viewModel.feedPet() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.tertiary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Icon(Icons.Default.Pets, "Feed", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Feed Puppy (+12%)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Simple stats labels
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("🌱 Love Flower Growth: ${state.plantGrowth}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("🐶 Puppy Happiness: ${state.petHappiness}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // C.20 Synced Shared Music Radio 📻
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("📻", fontSize = 20.sp)
                            Text("Synced Shared Radio", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        if (state.radioIsPlaying) {
                            Text("STREAMING LIVE 🔊", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        } else {
                            Text("OFFLINE 💤", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }

                    // Track Info Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(state.radioTrackName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Synced with your partner in real-time", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        IconButton(
                            onClick = { viewModel.toggleRadio() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(
                                if (state.radioIsPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "PlayPause",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Progress slider bar
                    LinearProgressIndicator(
                        progress = { if (state.radioIsPlaying) 0.55f else 0.3f },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                    )

                    // Track change options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val tracks = listOf(
                            "Cozy Lo-Fi Beats 🎵",
                            "Midnight Synth-Wave 🌌",
                            "Warm Acoustic Guitar 🎸"
                        )
                        tracks.forEach { track ->
                            Button(
                                onClick = { viewModel.changeRadioTrack(track) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                contentPadding = PaddingValues(horizontal = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(28.dp)
                            ) {
                                Text(track.substringAfter(" ").take(12) + "..", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Mood Orb Section (C.12 Mood Orb Sync)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Our Syncing Mood Orbs",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Orb column
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Your Mood", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            GlowingMoodOrb(mood = state.userMood)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(state.userMood, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        // Divider arrow
                        Icon(
                            Icons.Default.CompareArrows,
                            contentDescription = "Sync",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )

                        // Partner Orb column
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Partner's Mood", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            GlowingMoodOrb(mood = if (state.partnerGhostMode) "Calm Blue" else state.partnerMood)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                if (state.partnerGhostMode) "Incognito" else state.partnerMood,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Tap to change your Mood Orb",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Change User Mood selector buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val moods = listOf("Glowy Pink", "Cozy Yellow", "Calm Blue", "Energetic Red")
                        moods.forEach { mood ->
                            Button(
                                onClick = { viewModel.changeUserMood(mood) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when (mood) {
                                        "Glowy Pink" -> MoodGlowPink.copy(alpha = 0.2f)
                                        "Cozy Yellow" -> MoodGlowYellow.copy(alpha = 0.2f)
                                        "Calm Blue" -> MoodGlowBlue.copy(alpha = 0.2f)
                                        else -> MoodGlowPurple.copy(alpha = 0.2f)
                                    },
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .testTag("mood_btn_${mood.lowercase().replace(" ", "_")}"),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    mood.substringAfter(" "),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Interactive "Poke / Nudge" (B.9 Poke / Nudge)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Send a Love Poke! 👉",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Instantly nudge your partner with cute local haptics and animations!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Button(
                        onClick = { viewModel.sendPoke() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .testTag("send_poke_button")
                    ) {
                        Text("Boop! 💖", fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // B.10 Safe Word Emergency SOS Notification Trigger 🚨
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color.Red.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Emergency Safe Word SOS 🚨",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFFC62828)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Vibrates partner's device and sounds high-priority alarms immediately.",
                            fontSize = 11.sp,
                            color = Color(0xFFC62828).copy(alpha = 0.8f)
                        )
                    }
                    Button(
                        onClick = { viewModel.triggerEmergencySOS() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("trigger_emergency_sos_btn")
                    ) {
                        Text("SOS PING", fontWeight = FontWeight.Black, color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// Custom mood-pulsing orb canvas drawing
@Composable
fun GlowingMoodOrb(mood: String) {
    val color = when (mood) {
        "Glowy Pink" -> MoodGlowPink
        "Cozy Yellow" -> MoodGlowYellow
        "Calm Blue" -> MoodGlowBlue
        "Energetic Red" -> Color(0xFFFF5252)
        else -> MoodGlowPurple
    }

    val infiniteTransition = rememberInfiniteTransition(label = "glow_orb")
    val glowRadiusFactor by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radius_pulse"
    )

    Canvas(
        modifier = Modifier
            .size(72.dp)
            .padding(4.dp)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.width / 2.2f

        // Glow Layer (drawn with alpha)
        drawCircle(
            color = color.copy(alpha = 0.25f),
            radius = radius * glowRadiusFactor,
            center = center
        )

        // Outer Neon Rim
        drawCircle(
            color = color,
            radius = radius,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )

        // Soft Inner Solid Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color, color.copy(alpha = 0.4f), Color.Transparent),
                center = center,
                radius = radius
            ),
            radius = radius * 0.95f,
            center = center
        )
    }
}

// --- SCREEN 2: DAILY PROMPTS ---
@Composable
fun PromptsScreen(viewModel: CoupleViewModel) {
    val prompts by viewModel.prompts.collectAsState()
    var currentAnswerText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Daily Vulnerability Prompts 🕯️",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("prompts_screen_title")
            )
            Text(
                "Share deep thoughts. Answers are blurred until BOTH have responded to keep it mysterious!",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items(prompts) { prompt ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Question ${prompt.id}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = prompt.question,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // User's Answer Area
                    if (prompt.isAnsweredUser) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text("Your Answer 💖", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(prompt.answerUser, fontSize = 14.sp)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = currentAnswerText,
                                onValueChange = { currentAnswerText = it },
                                label = { Text("Write your honest answer...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("prompt_input_${prompt.id}"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Button(
                                onClick = {
                                    viewModel.submitPromptAnswer(prompt.id, currentAnswerText)
                                    currentAnswerText = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .testTag("prompt_submit_btn_${prompt.id}")
                            ) {
                                Text("Reveal with Love 🪄", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Partner's Answer Area (with blur conditional)
                    val showBlurredPartner = !prompt.isAnsweredUser && prompt.isAnsweredPartner
                    val showFullPartner = prompt.isAnsweredUser && prompt.isAnsweredPartner

                    if (showFullPartner) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text("Partner's Answer 💞", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(prompt.answerPartner, fontSize = 14.sp)
                        }
                    } else if (showBlurredPartner) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.blur(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Partner's Secret Answer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("This answer is very sweet and romantic and I cannot wait for you to read it!")
                            }
                            // Locker badge on top of blur
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Lock, "Locked", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Text("Submit your answer to unlock! 🔒", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        // Not answered by partner yet
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Partner is still writing their answer... ✍️",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 3: SWEET COZY CHAT ---
@Composable
fun ChatScreen(viewModel: CoupleViewModel) {
    val messages by viewModel.messages.collectAsState()
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Chat Header with Voice & Video Calling integration (D.15, D.16, D.17, D.18)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💑", fontSize = 20.sp)
                    }
                    Column {
                        Text("My Darling Partner", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Connected & listening", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }

                // Voice / Video P2P Calling Triggers
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.startCall("voice") },
                        modifier = Modifier.testTag("voice_call_btn")
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = "P2P Voice Call",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.startCall("video") },
                        modifier = Modifier.testTag("video_call_btn")
                    ) {
                        Icon(
                            Icons.Default.Videocam,
                            contentDescription = "P2P Video Call",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { message ->
                val isMe = message.sender == "me"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    if (!isMe) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
                                .align(Alignment.Bottom),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🐰", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = if (isMe) {
                            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
                        } else {
                            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
                        },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .testTag("chat_msg_card_${if (isMe) "me" else "partner"}")
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                text = message.text,
                                color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Quick Emojis Drawer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val quickEmojis = listOf("❤️", "🥰", "✨", "😘", "🥺", "🌸", "🍕", "🧸")
            quickEmojis.forEach { emoji ->
                Text(
                    text = emoji,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .clickable { viewModel.sendMessage(emoji) }
                        .padding(4.dp)
                )
            }
        }

        // Bottom Chat Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Send a sweet message...", fontSize = 14.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text_field"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                ),
                maxLines = 3
            )
            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.sendMessage(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .testTag("chat_send_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// --- SCREEN 4: MEMORY BOX & MILESTONES ---
@Composable
fun MemoriesScreen(viewModel: CoupleViewModel) {
    val memories by viewModel.memories.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val wishlistItems by viewModel.wishlistItems.collectAsState()
    val bucketList by viewModel.bucketList.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) } // 0: Diary 📔, 1: Gift Wishlist 🎁, 2: Bucket List 🗺️

    var showAddMemoryDialog by remember { mutableStateOf(false) }
    var memoryTitle by remember { mutableStateOf("") }
    var memoryContent by remember { mutableStateOf("") }

    var showAddWishlistItemDialog by remember { mutableStateOf(false) }
    var wishlistName by remember { mutableStateOf("") }
    var wishlistPrice by remember { mutableStateOf("") }
    var wishlistLink by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Row selector
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().testTag("memories_sub_tabs")
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = { Text("Love Diary 📔", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = { Text("Gift Wishlist 🎁", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = activeSubTab == 2,
                onClick = { activeSubTab = 2 },
                text = { Text("Bucket List 🗺️", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        when (activeSubTab) {
            0 -> {
                // DIARY SUB-TAB
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Shared Goals Card
                    item {
                        Text(
                            "Our Love Goals & Streaks 💖",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("goals_section_title")
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    RoundedCornerShape(20.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                goals.forEach { goal ->
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(goal.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "${goal.current} / ${goal.target}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { goal.current.toFloat() / goal.target.toFloat() },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = if (goal.current == goal.target) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Memories Timeline Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Our Memory Diary 📔",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("memories_section_title")
                            )
                            Button(
                                onClick = { showAddMemoryDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("add_memory_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Memory", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Memory Timeline List
                    if (memories.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No memories saved yet. Tap 'Add Memory' to save a beautiful moment together! 🧸✨",
                                    textAlign = TextAlign.Center,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        items(memories) { memory ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = memory.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Today",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = memory.content,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Favorite, "Love", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                        Text("Saved to our shared hearts", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // WISHLIST SUB-TAB (D.11 Gift Wishlist CRUD)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Cozy Gift Wishlist 🎁",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("A list of surprises & wishes to spoil each other with!", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Button(
                                onClick = { showAddWishlistItemDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("add_wishlist_item_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Item", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Gift", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (wishlistItems.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No gifts added yet. Surprise your lover by adding your sweet wishes here! 💝🎨",
                                    textAlign = TextAlign.Center,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        items(wishlistItems) { gift ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                        RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🎁", fontSize = 20.sp)
                                        }
                                        Column {
                                            Text(gift.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Text(gift.price, fontWeight = FontWeight.Black, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                                if (gift.link.isNotEmpty()) {
                                                    Text(
                                                        "Store Link Attached 🔗",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.secondary,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Delete wishlist item button
                                        IconButton(
                                            onClick = { viewModel.removeWishlistItem(gift.id) },
                                            modifier = Modifier.testTag("delete_wish_btn_${gift.id}")
                                        ) {
                                            Icon(Icons.Default.Delete, "Delete Gift", tint = Color.Red.copy(alpha = 0.7f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // BUCKET LIST SUB-TAB (D.12 Bucket List Card Polaroid & stamp)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Column {
                            Text(
                                "Our Shared Bucket List 🗺️✨",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("Couples' adventures, trips, and milestones to achieve together!", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }

                    items(bucketList) { postcard ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (postcard.isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    if (postcard.isCompleted) 2.dp else 1.dp,
                                    if (postcard.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    RoundedCornerShape(24.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(postcard.title, fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                            Icon(Icons.Default.Explore, "Location", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(postcard.location, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                        }
                                    }

                                    // Interactive Complete / Check checkbox button
                                    Button(
                                        onClick = { viewModel.toggleBucketListCompleted(postcard.id) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (postcard.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        modifier = Modifier.height(32.dp).testTag("bucket_toggle_btn_${postcard.id}")
                                    ) {
                                        Text(if (postcard.isCompleted) "✓ DONE" else "ACHIEVE! ✨", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }

                                Text(
                                    postcard.description,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )

                                // Visual Polaroid Stamp badge if completed
                                if (postcard.isCompleted) {
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.End)
                                            .border(1.5.dp, Color(0xFFE53935), RoundedCornerShape(4.dp))
                                            .background(Color(0xFFFFEBEE))
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("PASSPORT STAMPED ✈️", color = Color(0xFFE53935), fontSize = 9.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Memory Dialog
    if (showAddMemoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddMemoryDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addMemory(memoryTitle, memoryContent)
                        memoryTitle = ""
                        memoryContent = ""
                        showAddMemoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("dialog_confirm_memory")
                ) {
                    Text("Save to Diary 🔒", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemoryDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Record a Happy Memory 🌹", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = memoryTitle,
                        onValueChange = { memoryTitle = it },
                        label = { Text("What did we do?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_memory_title"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = memoryContent,
                        onValueChange = { memoryContent = it },
                        label = { Text("Tell the beautiful story...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("dialog_memory_content"),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 5
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Add Wishlist Item Dialog
    if (showAddWishlistItemDialog) {
        AlertDialog(
            onDismissRequest = { showAddWishlistItemDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (wishlistName.isNotEmpty()) {
                            viewModel.addWishlistItem(wishlistName, wishlistPrice, wishlistLink)
                            wishlistName = ""
                            wishlistPrice = ""
                            wishlistLink = ""
                            showAddWishlistItemDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("dialog_confirm_wishlist")
                ) {
                    Text("Add SURPRISE Gift 🎁", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddWishlistItemDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Add Wishlist Surprise 🎉", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = wishlistName,
                        onValueChange = { wishlistName = it },
                        label = { Text("What's the gift name?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_wishlist_name"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = wishlistPrice,
                        onValueChange = { wishlistPrice = it },
                        label = { Text("How much is it? (e.g. $19.99)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_wishlist_price"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = wishlistLink,
                        onValueChange = { wishlistLink = it },
                        label = { Text("Link to buy (optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_wishlist_link"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// --- SCREEN 5: DECISION WHEEL ---
@Composable
fun DecisionWheelScreen(viewModel: CoupleViewModel) {
    val isSpinning by viewModel.isSpinning.collectAsState()
    val targetIndex by viewModel.wheelTargetIndex.collectAsState()

    val wheelOptions = listOf(
        "Cook Dinner Together 🍳",
        "Watch a Cozy Movie 🍿",
        "Go for a Long Walk 🌲",
        "Board Game Night 🎲",
        "Order Delicious Pizza 🍕",
        "Pamper Spa Night 💆"
    )

    // Calculate rotation animation
    val currentAngle = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isSpinning) {
        if (isSpinning) {
            // Spin multiple full rounds rapidly
            currentAngle.animateTo(
                targetValue = currentAngle.value + 1440f + (360f / wheelOptions.size),
                animationSpec = tween(
                    durationMillis = 2000,
                    easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)
                )
            )
        }
    }

    LaunchedEffect(targetIndex) {
        targetIndex?.let { index ->
            // Calculate precision angle to land exact on option index
            val sliceDegrees = 360f / wheelOptions.size
            val targetDegrees = 360f - (index * sliceDegrees) - (sliceDegrees / 2)
            // Animate smoothly to resting degree
            currentAngle.animateTo(
                targetValue = currentAngle.value - (currentAngle.value % 360f) + 1440f + targetDegrees,
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Indecisive Tonight? Spin the Wheel! 🎯",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("wheel_screen_title")
        )
        Text(
            "Can't agree on what to eat, watch, or do? Spin the wheel together. The result is instantly sent to Chat!",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Pointer/Marker on Top
        Icon(
            Icons.Default.ArrowDownward,
            contentDescription = "Pointer",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(36.dp)
                .scale(1.2f)
        )

        // Visual Pie Wheel Canvas
        Box(
            modifier = Modifier
                .size(260.dp)
                .rotate(currentAngle.value)
                .border(6.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .testTag("decision_wheel_canvas"),
            contentAlignment = Alignment.Center
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val tertiaryColor = MaterialTheme.colorScheme.tertiary

            Canvas(modifier = Modifier.fillMaxSize()) {
                val numSlices = wheelOptions.size
                val sweepAngle = 360f / numSlices

                val sliceColors = listOf(
                    primaryColor,
                    tertiaryColor,
                    secondaryColor,
                    MoodGlowYellow,
                    MoodGlowBlue,
                    MoodGlowPurple
                )

                for (i in 0 until numSlices) {
                    drawArc(
                        color = sliceColors[i % sliceColors.size].copy(alpha = 0.75f),
                        startAngle = i * sweepAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        size = size
                    )
                }

                // Draw central sweet couple core circle
                drawCircle(
                    color = primaryColor,
                    radius = 32.dp.toPx()
                )
            }

            // Draw text directly inside each slice rotationally
            wheelOptions.forEachIndexed { index, option ->
                val angleInRad = (index * (360f / wheelOptions.size) + (180f / wheelOptions.size)) * (PI / 180f)
                val textRadius = 75.dp
                val xOffset = textRadius * cos(angleInRad).toFloat()
                val yOffset = textRadius * sin(angleInRad).toFloat()

                Box(
                    modifier = Modifier
                        .offset(x = xOffset, y = yOffset)
                        .width(70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option.substringBefore(" ").take(4) + "..",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Central heart decoration
            Text("💖", fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Spin Button
        Button(
            onClick = { viewModel.spinDecisionWheel(wheelOptions) },
            enabled = !isSpinning,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .width(200.dp)
                .height(48.dp)
                .testTag("spin_wheel_button")
        ) {
            if (isSpinning) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("SPIN OUR WHEEL 🚀", fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }

        // Display Selected choice beautifully
        targetIndex?.let { index ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("The Universe has decided! ✨", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        wheelOptions[index],
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
