package com.example.vpn.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: VpnViewModel) {
    val bypassLan by viewModel.bypassLan.collectAsState(initial = true)
    val globalMode by viewModel.globalMode.collectAsState(initial = false)
    val primaryDns by viewModel.primaryDns.collectAsState(initial = "1.1.1.1")
    val secondaryDns by viewModel.secondaryDns.collectAsState(initial = "8.8.8.8")
    val mtuSize by viewModel.mtuSize.collectAsState(initial = "1500")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        TopAppBar(
            title = { Text("Routing & Settings", color = Color.White) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
                Text("Routing Mode", color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Bypass Local LAN", color = Color.White)
                    Switch(checked = bypassLan, onCheckedChange = { viewModel.updateSetting("bypass_lan", it) })
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Global Proxy", color = Color.White)
                    Switch(checked = globalMode, onCheckedChange = { viewModel.updateSetting("global_mode", it) })
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("DNS Configuration", color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = primaryDns,
                    onValueChange = { viewModel.updateSetting("primary_dns", it) },
                    label = { Text("Primary DNS") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = secondaryDns,
                    onValueChange = { viewModel.updateSetting("secondary_dns", it) },
                    label = { Text("Secondary DNS") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text("Advanced", color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = mtuSize,
                    onValueChange = { viewModel.updateSetting("mtu_size", it) },
                    label = { Text("MTU Size") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        }
    }
}
