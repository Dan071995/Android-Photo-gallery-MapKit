package com.example.cameraroomrecyclerview.entity

import com.google.gson.annotations.SerializedName

data class AttractionsInfo(
    @SerializedName("xid") val xid : String, //Уникальный идентификатор объекта
    @SerializedName("name") val name : String,
    @SerializedName("dist")  val dist : Double, //Расстояние в метрах от выбранной точки (только для запроса по радиусу)
    @SerializedName("rate") val rate : Double, //Рейтинг известности объекта
    @SerializedName("osm") val osm : String, //Уникальный идентификатор объекта в OpenStreetMap
    @SerializedName("kinds") val kinds : String, //Категория объекта
    @SerializedName("point")  val point : Coordinates //Координаты объекта
)

data class Coordinates (
    @SerializedName("lon")  val lon : Double, //Долгота
    @SerializedName("lat")  val lat : Double  //Широта
    )
