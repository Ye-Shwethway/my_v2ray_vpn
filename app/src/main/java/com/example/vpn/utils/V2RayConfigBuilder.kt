package com.example.vpn.utils

import com.example.vpn.data.ServerNode
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.net.URLDecoder

object V2RayConfigBuilder {
    fun buildConfig(node: ServerNode): String {
        return when (node.protocol.lowercase()) {
            "vmess" -> buildVmessConfig(node)
            "vless" -> buildVlessConfig(node)
            "trojan" -> buildTrojanConfig(node)
            else -> "{}"
        }
    }

    private fun buildVmessConfig(node: ServerNode): String {
        val obj = try {
            Gson().fromJson(node.configJson, JsonObject::class.java)
        } catch (e: Exception) { JsonObject() }
        
        val net = obj.get("net")?.asString ?: "tcp"
        val type = obj.get("type")?.asString ?: "none"
        val host = obj.get("host")?.asString ?: ""
        val path = obj.get("path")?.asString ?: "/"
        val tls = obj.get("tls")?.asString ?: ""
        val sni = obj.get("sni")?.asString ?: host
        val streamSettings = buildStreamSettings(net, type, host, path, tls, sni)

        return """
        {
          "log": { "loglevel": "warning" },
          "inbounds": [
            {
              "port": 10808,
              "listen": "127.0.0.1",
              "protocol": "socks",
              "settings": { "auth": "noauth", "udp": true }
            },
            {
              "port": 10809,
              "listen": "127.0.0.1",
              "protocol": "http",
              "settings": {}
            }
          ],
          "outbounds": [
            {
              "protocol": "vmess",
              "settings": {
                "vnext": [
                  {
                    "address": "${node.address}",
                    "port": ${node.port},
                    "users": [
                      { "id": "${node.uuid}", "alterId": 0, "security": "auto" }
                    ]
                  }
                ]
              },
              $streamSettings
            }
          ]
        }
        """.trimIndent()
    }

    private fun buildVlessConfig(node: ServerNode): String {
        val params = parseQueryParams(node.configJson)
        val net = params["type"] ?: "tcp"
        val security = params["security"] ?: "none"
        val host = params["host"] ?: ""
        val path = params["path"] ?: "/"
        val sni = params["sni"] ?: host
        val headerType = params["headerType"] ?: "none"
        
        val streamSettings = buildStreamSettings(net, headerType, host, path, security, sni)

        return """
        {
          "log": { "loglevel": "warning" },
          "inbounds": [
            {
              "port": 10808,
              "listen": "127.0.0.1",
              "protocol": "socks",
              "settings": { "auth": "noauth", "udp": true }
            },
            {
              "port": 10809,
              "listen": "127.0.0.1",
              "protocol": "http",
              "settings": {}
            }
          ],
          "outbounds": [
            {
              "protocol": "vless",
              "settings": {
                "vnext": [
                  {
                    "address": "${node.address}",
                    "port": ${node.port},
                    "users": [
                      { "id": "${node.uuid}", "encryption": "none" }
                    ]
                  }
                ]
              },
              $streamSettings
            }
          ]
        }
        """.trimIndent()
    }

    private fun buildTrojanConfig(node: ServerNode): String {
        val params = parseQueryParams(node.configJson)
        val net = params["type"] ?: "tcp"
        val security = params["security"] ?: "tls"
        val host = params["host"] ?: ""
        val path = params["path"] ?: "/"
        val sni = params["sni"] ?: host
        val headerType = params["headerType"] ?: "none"
        
        val streamSettings = buildStreamSettings(net, headerType, host, path, security, sni)

        return """
        {
          "log": { "loglevel": "warning" },
          "inbounds": [
            {
              "port": 10808,
              "listen": "127.0.0.1",
              "protocol": "socks",
              "settings": { "auth": "noauth", "udp": true }
            },
            {
              "port": 10809,
              "listen": "127.0.0.1",
              "protocol": "http",
              "settings": {}
            }
          ],
          "outbounds": [
            {
              "protocol": "trojan",
              "settings": {
                "servers": [
                  {
                    "address": "${node.address}",
                    "port": ${node.port},
                    "password": "${node.uuid}"
                  }
                ]
              },
              $streamSettings
            }
          ]
        }
        """.trimIndent()
    }

    private fun parseQueryParams(query: String): Map<String, String> {
        if (query.isBlank()) return emptyMap()
        return query.split("&").mapNotNull {
            val p = it.split("=")
            if (p.size == 2) {
                p[0] to URLDecoder.decode(p[1], "UTF-8")
            } else null
        }.toMap()
    }

    private fun buildStreamSettings(
        net: String,
        headerType: String,
        host: String,
        path: String,
        security: String,
        sni: String
    ): String {
        val parts = mutableListOf<String>()
        
        when (net) {
            "ws" -> {
                parts.add("\"network\": \"ws\"")
                parts.add("\"wsSettings\": { \"path\": \"$path\", \"headers\": { \"Host\": \"$host\" } }")
            }
            "grpc" -> {
                parts.add("\"network\": \"grpc\"")
                parts.add("\"grpcSettings\": { \"serviceName\": \"$path\" }")
            }
            "tcp" -> {
                parts.add("\"network\": \"tcp\"")
                if (headerType == "http") {
                    parts.add("\"tcpSettings\": { \"header\": { \"type\": \"http\", \"request\": { \"path\": [\"$path\"], \"headers\": { \"Host\": [\"$host\"] } } } }")
                }
            }
            else -> parts.add("\"network\": \"tcp\"")
        }

        if (security == "tls" || security == "reality") {
            parts.add("\"security\": \"tls\"")
            parts.add("\"tlsSettings\": { \"allowInsecure\": true, \"serverName\": \"$sni\" }")
        } else {
            parts.add("\"security\": \"none\"")
        }

        return "\"streamSettings\": {\n" + parts.joinToString(",\n") + "\n}"
    }
}
