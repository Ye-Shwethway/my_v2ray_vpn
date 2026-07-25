package com.example.vpn.ui

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.vpn.data.AppDatabase
import com.example.vpn.data.ServerNode
import com.example.vpn.data.Subscription
import com.example.vpn.service.SubscriptionSyncWorker
import com.example.vpn.utils.SubscriptionParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class VpnViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dataStore = application.dataStore

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _activeNode = MutableStateFlow<ServerNode?>(null)
    val activeNode: StateFlow<ServerNode?> = _activeNode

    val allNodes = db.serverNodeDao().getAllNodes()
    val allSubscriptions = db.subscriptionDao().getAll()

    // Settings flows
    val bypassLan = dataStore.data.map { prefs -> prefs[booleanPreferencesKey("bypass_lan")] ?: true }
    val globalMode = dataStore.data.map { prefs -> prefs[booleanPreferencesKey("global_mode")] ?: false }
    val primaryDns = dataStore.data.map { prefs -> prefs[stringPreferencesKey("primary_dns")] ?: "1.1.1.1" }
    val secondaryDns = dataStore.data.map { prefs -> prefs[stringPreferencesKey("secondary_dns")] ?: "8.8.8.8" }
    val mtuSize = dataStore.data.map { prefs -> prefs[stringPreferencesKey("mtu_size")] ?: "1500" }

    init {
        // Schedule auto updates every 12 hours
        val updateWork = PeriodicWorkRequestBuilder<SubscriptionSyncWorker>(12, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(application).enqueueUniquePeriodicWork(
            "subscription_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            updateWork
        )
    }

    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
    }
    
    fun setActiveNode(node: ServerNode) {
        _activeNode.value = node
    }
    
    fun addNode(node: ServerNode) {
        viewModelScope.launch {
            db.serverNodeDao().insertNode(node)
        }
    }

    fun addSubscription(url: String, name: String) {
        viewModelScope.launch {
            val sub = Subscription(url = url, name = name)
            db.subscriptionDao().insert(sub)
        }
    }

    fun seedSubscriptions(context: Context) {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("vpn_prefs", Context.MODE_PRIVATE)
            val seeded = prefs.getBoolean("seeded", false)
            if (!seeded) {
                val defaultSubs = listOf(
                    Subscription(url = "https://raw.githubusercontent.com/barry-far/V2ray-config/main/Sub1.txt", name = "Free Sub 1"),
                    Subscription(url = "https://raw.githubusercontent.com/ebrasha/free-v2ray-public-list/main/all_base64.txt", name = "Free Sub 2")
                )
                for (sub in defaultSubs) {
                    db.subscriptionDao().insert(sub)
                }
                prefs.edit().putBoolean("seeded", true).apply()
                updateAllSubscriptions()
            }
        }
    }

    fun updateAllSubscriptions() {
        viewModelScope.launch(Dispatchers.IO) {
            val subs = db.subscriptionDao().getAllSync()
            for (sub in subs) {
                try {
                    val url = URL(sub.url)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        val base64Content = connection.inputStream.bufferedReader().use { it.readText() }
                        val nodes = SubscriptionParser.parseSubscription(base64Content, sub.id)

                        if (nodes.isNotEmpty()) {
                            db.serverNodeDao().deleteBySubId(sub.id)
                            db.serverNodeDao().insertNodes(nodes)
                            db.subscriptionDao().insert(sub.copy(lastUpdated = System.currentTimeMillis()))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun updateSetting(key: String, value: Any) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                when (value) {
                    is Boolean -> prefs[booleanPreferencesKey(key)] = value
                    is String -> prefs[stringPreferencesKey(key)] = value
                }
            }
        }
    }
}
