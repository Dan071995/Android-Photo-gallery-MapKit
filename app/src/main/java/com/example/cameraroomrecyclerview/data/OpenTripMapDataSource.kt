package com.example.cameraroomrecyclerview.data

import com.example.cameraroomrecyclerview.constants.OPEN_TRIP_MAP_API_HOST
import com.example.cameraroomrecyclerview.constants.OPEN_TRIP_MAP_API_PATH
import com.example.cameraroomrecyclerview.entity.AttractionsInfo
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class OpenTripMapDataSource {

    //Ответ который нужно обрабатывать чеерз корутину
    suspend fun getAttractions(radius:Int, lon:Double, lat:Double): List <AttractionsInfo> {
        return RetrofitInstance.OpenTripMapApi.getInfo(radius,lon,lat)
    }
}

private object RetrofitInstance{
    //Создадим клиен okHttp - с помощью него, мы будем мониторить (в логах) какой именно запрос на сервер мы отправили и что именно нам вернулось.
    //Создаем ИНТЕРСЕПТОР
    val interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY //Будем выводить в ЛОГ только ТЕЛО ответа
    }
    //Создаем okHttp КЛИЕНТ:
    val okHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()

    //Экземпляр Ретрофита OpenTripMapApi
    private val retrofitOpenTripMapAPI = Retrofit.Builder()
        .baseUrl(OPEN_TRIP_MAP_API_HOST) //С этим URL работает Postman
        .client(okHttpClient) //Передаем КЛИЕНТ для перехвата и вывода ТЕЛА ответа в ЛОГ
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    //Ретрофит сервис OpenTripMapApi:
    val OpenTripMapApi:OpenTripMap = retrofitOpenTripMapAPI.create(OpenTripMap::class.java)

}

//Корутины
private interface OpenTripMap{
    @GET(OPEN_TRIP_MAP_API_PATH)
    suspend fun getInfo(@Query("radius") radius:Int = 1000, @Query("lon") lon:Double, @Query("lat") lat:Double): List<AttractionsInfo>
}