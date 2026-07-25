package com.example.vpn.ui

import android.app.Application
import android.net.TrafficStats
import android.os.Process
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _uptime = MutableStateFlow(0L)
    val uptime: StateFlow<Long> = _uptime

    private val _downloadSpeed = MutableStateFlow(0L)
    val downloadSpeed: StateFlow<Long> = _downloadSpeed

    private val _uploadSpeed = MutableStateFlow(0L)
    val uploadSpeed: StateFlow<Long> = _uploadSpeed

    private var statsJob: Job? = null
    private var lastRx = 0L
    private var lastTx = 0L

    fun startTracking() {
        if (statsJob?.isActive == true) return
        _uptime.value = 0L
        lastRx = TrafficStats.getUidRxBytes(Process.myUid())
        lastTx = TrafficStats.getUidTxBytes(Process.myUid())
        
        statsJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _uptime.value += 1
                
                val currentRx = TrafficStats.getUidRxBytes(Process.myUid())
                val currentTx = TrafficStats.getUidTxBytes(Process.myUid())
                
                val rxDiff = if (currentRx >= lastRx) currentRx - lastRx else 0L
                val txDiff = if (currentTx >= lastTx) currentTx - lastTx else 0L
                
                _downloadSpeed.value = rxDiff
                _uploadSpeed.value = txDiff
                
                lastRx = currentRx
                lastTx = currentTx
            }
        }
    }

    fun stopTracking() {
        statsJob?.cancel()
        statsJob = null
        _uptime.value = 0L
        _downloadSpeed.value = 0L
        _uploadSpeed.value = 0L
    }
}
