package com.example.cameraroomrecyclerview.entity

data class AttractionsInfo(
    val xid : String, //Уникальный идентификатор объекта
    val name : String,
    val kinds : String, //Категория объекта
    val osm : String, //Уникальный идентификатор объекта в OpenStreetMap
    val rate : Double, //Рейтинг известности объекта
    val dist : Double, //Расстояние в метрах от выбранной точки (только для запроса по радиусу)
    val point : Coordinates //Координаты объекта
)

data class Coordinates (
    val lon : Double, //Долгота
    val lat : Double  //Широта
    )
