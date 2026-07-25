package com.example.vpn.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.vpn.data.ServerNode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeScreen(navController: NavController, viewModel: VpnViewModel) {
    var subUrl by remember { mutableStateOf("") }
    val nodes by viewModel.allNodes.collectAsState(initial = emptyList())
    val activeNode by viewModel.activeNode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        TopAppBar(
            title = { Text("Server Nodes", color = Color.White) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = subUrl,
                onValueChange = { subUrl = it },
                placeholder = { Text("Paste Subscription URL (VMess/VLESS/Trojan)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { 
                    viewModel.addNode(ServerNode(name = "New Node", address = subUrl, port = 443, protocol = "vmess", uuid = "fake-uuid")) 
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
            ) {
                Text("Fetch & Update")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Available Nodes", color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(nodes) { node ->
                    NodeItem(
                        node = node,
                        isActive = activeNode?.id == node.id,
                        onClick = { viewModel.setActiveNode(node) }
                    )
                }
            }
        }
    }
}

@Composable
fun NodeItem(node: ServerNode, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isActive) Color(0xFF3B82F6).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(node.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(node.protocol.uppercase(), color = Color.Gray, fontSize = 12.sp)
            }
            Text("${node.latency}ms", color = Color(0xFF10B981), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
