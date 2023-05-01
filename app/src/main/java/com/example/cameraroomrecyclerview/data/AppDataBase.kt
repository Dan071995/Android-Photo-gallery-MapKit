package com.example.cameraroomrecyclerview.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.cameraroomrecyclerview.entity.FileInfo

@Database(entities = [FileInfo::class], version = 1)
abstract class AppDataBase:RoomDatabase() {
    abstract fun fileInfoDao():FileInfoDAO
}