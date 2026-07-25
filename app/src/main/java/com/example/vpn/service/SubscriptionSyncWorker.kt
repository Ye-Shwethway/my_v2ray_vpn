package com.example.vpn.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.vpn.data.AppDatabase
import com.example.vpn.utils.SubscriptionParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class SubscriptionSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(applicationContext)
        val subscriptionDao = db.subscriptionDao()
        val serverNodeDao = db.serverNodeDao()

        try {
            val subscriptions = subscriptionDao.getAllSync()
            for (sub in subscriptions) {
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
                            serverNodeDao.deleteBySubId(sub.id)
                            serverNodeDao.insertNodes(nodes)
                            subscriptionDao.insert(sub.copy(lastUpdated = System.currentTimeMillis()))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
