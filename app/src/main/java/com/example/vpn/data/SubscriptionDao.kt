package com.example.vpn.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions")
    fun getAll(): Flow<List<Subscription>>
    
    @Query("SELECT * FROM subscriptions")
    suspend fun getAllSync(): List<Subscription>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: Subscription): Long
    
    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteById(id: Int)
}
