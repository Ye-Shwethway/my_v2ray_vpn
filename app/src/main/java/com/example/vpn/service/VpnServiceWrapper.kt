package com.example.vpn.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.vpn.R

class VpnServiceWrapper : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null

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
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "vpn_channel")
            .setContentTitle("VPN is Active")
            .setContentText("Connected to proxy")
            .setSmallIcon(android.R.drawable.ic_secure)
            .build()
        startForeground(1, notification)

        try {
            vpnInterface?.close()
            
            // Standard Android VpnService builder for a custom tunnel
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
                libv2ray.Libv2ray.startV2Ray(fd, config)
            }
        } catch (e: Exception) {
            Log.e("VpnServiceWrapper", "Failed to establish VPN", e)
            stopVpn()
        }
    }

    private fun stopVpn() {
        try {
            vpnInterface?.close()
            vpnInterface = null
            libv2ray.Libv2ray.stopV2Ray()
        } catch (e: Exception) {
            Log.e("VpnServiceWrapper", "Failed to close VPN interface", e)
        }
        stopForeground(true)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "vpn_channel",
                "VPN Status",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
