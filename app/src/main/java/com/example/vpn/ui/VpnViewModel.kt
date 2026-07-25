package com.example.vpn.ui

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vpn.data.AppDatabase
import com.example.vpn.data.ServerNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class VpnViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dataStore = application.dataStore

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _activeNode = MutableStateFlow<ServerNode?>(null)
    val activeNode: StateFlow<ServerNode?> = _activeNode

    val allNodes = db.serverNodeDao().getAllNodes()

    // Settings flows
    val bypassLan = dataStore.data.map { prefs -> prefs[booleanPreferencesKey("bypass_lan")] ?: true }
    val globalMode = dataStore.data.map { prefs -> prefs[booleanPreferencesKey("global_mode")] ?: false }
    val primaryDns = dataStore.data.map { prefs -> prefs[stringPreferencesKey("primary_dns")] ?: "1.1.1.1" }
    val secondaryDns = dataStore.data.map { prefs -> prefs[stringPreferencesKey("secondary_dns")] ?: "8.8.8.8" }
    val mtuSize = dataStore.data.map { prefs -> prefs[stringPreferencesKey("mtu_size")] ?: "1500" }

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
