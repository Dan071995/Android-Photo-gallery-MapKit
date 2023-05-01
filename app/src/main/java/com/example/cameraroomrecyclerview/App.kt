package com.example.cameraroomrecyclerview

import android.app.Application
import androidx.room.Room
import com.example.cameraroomrecyclerview.data.AppDataBase
import com.yandex.mapkit.MapKitFactory

class App:Application() {
    lateinit var dataBase:AppDataBase

    override fun onCreate() {
        super.onCreate()
        //Устанавливаем ключ для работы с картой Yandex
        MapKitFactory.setApiKey("8def492c-9ed5-4046-a51a-7cdf58b9b634")

        //Экзеипляр БД
        dataBase = Room.databaseBuilder(
            applicationContext,
            AppDataBase::class.java,
            "fileDataBase"
        )
            //.fallbackToDestructiveMigration() //Удаляет БД если нет инструкции по мигнрации данных
            .build()
    }

}