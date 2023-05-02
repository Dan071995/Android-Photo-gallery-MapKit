package com.example.cameraroomrecyclerview.data

import android.content.Context
import com.example.cameraroomrecyclerview.App
import com.example.cameraroomrecyclerview.entity.AttractionsInfo

class MainRepository(private val context: Context) {

    //Методы для работы с БД
    val fileInfoDAO = (context.applicationContext as App).dataBase.fileInfoDao()

    //Получаем дестопримечательности в зависимости от переданных координт и радауса поска
    suspend fun getAttractions (radius:Int, lon:Double, lat:Double): List <AttractionsInfo> {
        return OpenTripMapDataSource().getAttractions(radius,lon,lat)
    }
}