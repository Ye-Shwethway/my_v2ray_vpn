package com.example.vpn.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "server_nodes")
data class ServerNode(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val address: String,
    val port: Int,
    val protocol: String, // vmess, vless, trojan, shadowsocks
    val uuid: String,
    val latency: Int = -1
)
