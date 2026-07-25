package com.example.vpn.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Brush

@Composable
fun MainScreen(navController: NavController, viewModel: VpnViewModel, mainViewModel: MainViewModel, onConnectClick: () -> Unit) {
    val isConnected by viewModel.isConnected.collectAsState()
    val activeNode by viewModel.activeNode.collectAsState()
    
    val uptime by mainViewModel.uptime.collectAsState()
    val downloadSpeed by mainViewModel.downloadSpeed.collectAsState()
    val uploadSpeed by mainViewModel.uploadSpeed.collectAsState()

    LaunchedEffect(isConnected) {
        if (isConnected) {
            mainViewModel.startTracking()
        } else {
            mainViewModel.stopTracking()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nexus Proxy",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TextButton(onClick = { navController.navigate("nodes") }) {
                    Text("Nodes", color = Color(0xFF3B82F6))
                }
                TextButton(onClick = { navController.navigate("settings") }) {
                    Text("Settings", color = Color(0xFF94A3B8))
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))

        // Connection Button
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(
                    if (isConnected) Color(0xFF10B981).copy(alpha = 0.2f)
                    else Color(0xFF3B82F6).copy(alpha = 0.2f)
                )
                .padding(24.dp)
                .clickable { onConnectClick() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        if (isConnected) Color(0xFF10B981)
                        else Color(0xFF3B82F6)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isConnected) "CONNECTED" else "CONNECT",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Metrics Card
        GlassCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Download", color = Color.Gray, fontSize = 12.sp)
                        Text(if (isConnected) formatSpeed(downloadSpeed) else "0 B/s", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Upload", color = Color.Gray, fontSize = 12.sp)
                        Text(if (isConnected) formatSpeed(uploadSpeed) else "0 B/s", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Active Node", color = Color.Gray, fontSize = 12.sp)
                        Text(activeNode?.name ?: "None Selected", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Uptime", color = Color.Gray, fontSize = 12.sp)
                        val mins = uptime / 60
                        val secs = uptime % 60
                        Text(String.format("%02d:%02d", mins, secs), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun GlassCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.05f))
    ) {
        content()
    }
}

fun formatSpeed(bytesPerSec: Long): String {
    if (bytesPerSec < 1024) return "$bytesPerSec B/s"
    val kb = bytesPerSec / 1024.0
    if (kb < 1024) return String.format("%.1f KB/s", kb)
    val mb = kb / 1024.0
    return String.format("%.2f MB/s", mb)
}
