package com.example.vpn.utils

import android.util.Base64
import com.example.vpn.data.ServerNode
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.net.URLDecoder

object SubscriptionParser {
    fun parseSubscription(base64Content: String, subId: Int): List<ServerNode> {
        val nodes = mutableListOf<ServerNode>()
        try {
            val decodedString = try {
                String(Base64.decode(base64Content, Base64.DEFAULT)).trim()
            } catch (e: Exception) {
                // If it's not base64, assume it's raw text
                base64Content.trim()
            }
            val lines = decodedString.split("\n", "\r\n").filter { it.isNotBlank() }
            
            for (line in lines) {
                try {
                    when {
                        line.startsWith("vmess://") -> {
                            val b64 = line.substring(8)
                            val json = String(Base64.decode(b64, Base64.DEFAULT))
                            val obj = Gson().fromJson(json, JsonObject::class.java)
                            
                            val name = obj.get("ps")?.asString ?: "VMess Node"
                            val address = obj.get("add")?.asString ?: ""
                            val port = obj.get("port")?.let { if (it.isJsonPrimitive && it.asJsonPrimitive.isNumber) it.asInt else it.asString.toIntOrNull() } ?: 443
                            val uuid = obj.get("id")?.asString ?: ""
                            
                            nodes.add(
                                ServerNode(
                                    subId = subId,
                                    name = name,
                                    address = address,
                                    port = port,
                                    protocol = "vmess",
                                    uuid = uuid,
                                    configJson = json
                                )
                            )
                        }
                        line.startsWith("vless://") -> {
                            val withoutPrefix = line.substring(8)
                            val atSplit = withoutPrefix.split("@")
                            if (atSplit.size < 2) continue
                            val uuid = atSplit[0]
                            val hashSplit = atSplit[1].split("#")
                            val name = if (hashSplit.size > 1) URLDecoder.decode(hashSplit[1], "UTF-8") else "VLESS Node"
                            val hostPortQuery = hashSplit[0]
                            val querySplit = hostPortQuery.split("?")
                            val hostPort = querySplit[0].split(":")
                            if (hostPort.size < 2) continue
                            val address = hostPort[0]
                            val port = hostPort[1].substringBefore("/").toIntOrNull() ?: 443
                            
                            val queryParams = if (querySplit.size > 1) querySplit[1] else ""
                            
                            nodes.add(
                                ServerNode(
                                    subId = subId,
                                    name = name,
                                    address = address,
                                    port = port,
                                    protocol = "vless",
                                    uuid = uuid,
                                    configJson = queryParams
                                )
                            )
                        }
                        line.startsWith("trojan://") -> {
                            val withoutPrefix = line.substring(9)
                            val atSplit = withoutPrefix.split("@")
                            if (atSplit.size < 2) continue
                            val uuid = atSplit[0] // password
                            val hashSplit = atSplit[1].split("#")
                            val name = if (hashSplit.size > 1) URLDecoder.decode(hashSplit[1], "UTF-8") else "Trojan Node"
                            val hostPortQuery = hashSplit[0]
                            val querySplit = hostPortQuery.split("?")
                            val hostPort = querySplit[0].split(":")
                            if (hostPort.size < 2) continue
                            val address = hostPort[0]
                            val port = hostPort[1].substringBefore("/").toIntOrNull() ?: 443
                            
                            val queryParams = if (querySplit.size > 1) querySplit[1] else ""
                            
                            nodes.add(
                                ServerNode(
                                    subId = subId,
                                    name = name,
                                    address = address,
                                    port = port,
                                    protocol = "trojan",
                                    uuid = uuid,
                                    configJson = queryParams
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return nodes
    }
}
