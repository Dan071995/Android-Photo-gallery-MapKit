package com.example.cameraroomrecyclerview.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fileInfo")
data class FileInfo(
    @ColumnInfo(name = "uri") @PrimaryKey val uri:String,
    @ColumnInfo(name = "fileName") val fileName:String
)
