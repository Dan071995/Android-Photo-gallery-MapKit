package com.example.cameraroomrecyclerview.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.cameraroomrecyclerview.entity.FileInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface FileInfoDAO {

    @Query(value = "SELECT * FROM fileInfo")
    fun getAllFiles(): Flow<List<FileInfo>>

    @Insert
    suspend fun putFileIntoDB(file:FileInfo)

    //@Delete
    @Query(value = "DELETE FROM FileInfo WHERE uri = :uri")
    suspend fun deleteFileFromDB(uri:String)

}