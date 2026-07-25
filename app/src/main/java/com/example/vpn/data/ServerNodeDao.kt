package com.example.vpn.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerNodeDao {
    @Query("SELECT * FROM server_nodes")
    fun getAllNodes(): Flow<List<ServerNode>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: ServerNode)

    @Delete
    suspend fun deleteNode(node: ServerNode)
}
