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
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNodes(nodes: List<ServerNode>)

    @Delete
    suspend fun deleteNode(node: ServerNode)
    
    @Query("DELETE FROM server_nodes WHERE subId = :subId")
    suspend fun deleteBySubId(subId: Int)
}
