package com.example.vpn.service

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import libv2ray.Libv2ray
import libv2ray.CoreController
import libv2ray.CoreCallbackHandler
import kotlin.concurrent.thread
import java.io.File
import java.io.FileOutputStream

class VpnServiceWrapper : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var coreController: CoreController? = null

    companion object {
        const val ACTION_START = "com.example.vpn.START"
        const val ACTION_STOP = "com.example.vpn.STOP"
        const val EXTRA_NODE_CONFIG = "extra_node_config"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val config = intent.getStringExtra(EXTRA_NODE_CONFIG) ?: ""
                startVpn(config)
            }
            ACTION_STOP -> stopVpn()
        }
        return START_STICKY
    }

    private fun startVpn(config: String) {
        try {
            vpnInterface?.close()
            
            val builder = Builder()
                .setSession("NexusProxy")
                .addAddress("10.0.0.2", 24)
                .addDnsServer("8.8.8.8")
                .addDnsServer("1.1.1.1")
                .addRoute("0.0.0.0", 0)
                .setMtu(1500)
            
            vpnInterface = builder.establish()
            
            val fd = vpnInterface?.fd
            if (fd != null) {
                Log.d("VpnServiceWrapper", "VPN Established, FileDescriptor: $fd")
                
                copyAsset("geoip.dat")
                copyAsset("geosite.dat")
                
                Libv2ray.initCoreEnv(applicationContext.filesDir.absolutePath, "nexus-proxy-key")
                
                coreController = Libv2ray.newCoreController(object : CoreCallbackHandler {
                    override fun onEmitStatus(l: Long, s: String?): Long {
                        Log.d("VpnServiceWrapper", "Status: $l $s")
                        return 0L
                    }
                    override fun shutdown(): Long {
                        Log.d("VpnServiceWrapper", "Shutdown")
                        return 0L
                    }
                    override fun startup(): Long {
                        Log.d("VpnServiceWrapper", "Startup")
                        return 0L
                    }
                })
                
                thread {
                    try {
                        coreController?.startLoop(config)
                    } catch (e: Exception) {
                        Log.e("VpnServiceWrapper", "startLoop error", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("VpnServiceWrapper", "Failed to establish VPN", e)
            stopVpn()
        }
    }

    private fun copyAsset(filename: String) {
        try {
            val outFile = File(applicationContext.filesDir, filename)
            if (!outFile.exists()) {
                applicationContext.assets.open(filename).use { inputStream ->
                    FileOutputStream(outFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("VpnServiceWrapper", "Failed to copy asset: $filename", e)
        }
    }

    private fun stopVpn() {
        try {
            coreController?.stopLoop()
            coreController = null
            
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            Log.e("VpnServiceWrapper", "Failed to close VPN interface", e)
        }
        stopSelf()
    }
}
