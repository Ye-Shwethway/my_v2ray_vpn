package com.example.vpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vpn.service.VpnServiceWrapper
import com.example.vpn.ui.MainScreen
import com.example.vpn.ui.MainViewModel
import com.example.vpn.ui.NodeScreen
import com.example.vpn.ui.SettingsScreen
import com.example.vpn.ui.VpnViewModel
import com.example.vpn.ui.theme.VPNTheme
import com.example.vpn.utils.V2RayConfigBuilder

class MainActivity : ComponentActivity() {

    private val viewModel: VpnViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startVpnService()
        } else {
            viewModel.setConnected(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.seedSubscriptions(this)
        
        setContent {
            VPNTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "main") {
                        composable("main") {
                            MainScreen(navController, viewModel, mainViewModel) {
                                if (viewModel.isConnected.value) {
                                    stopVpnService()
                                } else {
                                    requestVpnPermissionAndStart()
                                }
                            }
                        }
                        composable("nodes") { NodeScreen(navController, viewModel) }
                        composable("settings") { SettingsScreen(navController, viewModel) }
                    }
                }
            }
        }
    }

    private fun requestVpnPermissionAndStart() {
        val vpnIntent = VpnService.prepare(this)
        if (vpnIntent != null) {
            vpnPermissionLauncher.launch(vpnIntent)
        } else {
            startVpnService()
        }
    }

    private fun startVpnService() {
        val activeNode = viewModel.activeNode.value
        if (activeNode == null) return
        
        viewModel.setConnected(true)
        val config = V2RayConfigBuilder.buildConfig(activeNode)
        
        val intent = Intent(this, VpnServiceWrapper::class.java).apply {
            action = VpnServiceWrapper.ACTION_START
            putExtra(VpnServiceWrapper.EXTRA_NODE_CONFIG, config)
        }
        startService(intent)
    }

    private fun stopVpnService() {
        viewModel.setConnected(false)
        val intent = Intent(this, VpnServiceWrapper::class.java).apply {
            action = VpnServiceWrapper.ACTION_STOP
        }
        startService(intent)
    }
}
