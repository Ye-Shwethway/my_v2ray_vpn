package com.example.vpn.service

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor

class VpnServiceWrapper : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "START") {
            startVpn()
        } else if (intent?.action == "STOP") {
            stopVpn()
        }
        return START_STICKY
    }

    private fun startVpn() {
        val builder = Builder()
        builder.setSession("V2RayVPN")
            .addAddress("10.0.0.2", 24)
            .addDnsServer("8.8.8.8")
            .addRoute("0.0.0.0", 0)
        
        vpnInterface = builder.establish()
        // Core V2Ray logic goes here
    }

    private fun stopVpn() {
        vpnInterface?.close()
        vpnInterface = null
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }
}
