package com.example.cameraroomrecyclerview.presentation

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.cameraroomrecyclerview.R
import com.example.cameraroomrecyclerview.data.MainRepository
import com.example.cameraroomrecyclerview.databinding.FragmentAttractionsMapBinding
import com.example.cameraroomrecyclerview.entity.AttractionsInfo
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.*
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.runtime.image.ImageProvider
import java.util.*


class AttractionsMapFragment : Fragment(){

    private var _binding: FragmentAttractionsMapBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                //val fileInfoDAO = (requireContext().applicationContext as App).dataBase.fileInfoDao()
                val repository = MainRepository(requireContext())
                return MainViewModel(repository, requireContext()) as T
            }
        }
    }

    private lateinit var imageProviderLocationPoint: ImageProvider //Создаем ImageProvider, в который устанавливаем картинку из ресурсов (картинка метки)
    private lateinit var mapKit: MapKit //Инстанс карты
    private lateinit var mapKitLocationManager: LocationManager //Инстанс менеджера локаций. Через него будем добавлять точки и работвть с картой
    private lateinit var userLocationOnMapKit: UserLocationLayer //Слой с локацией пользователя

    private var lastLocation : Location? = null //последняя сохраненная координата пользователя

    //Обработчик клика по маркеру у яндекса реализован со "слабой связанностью", т.е. сборщик мусора его уничтожит при ближайшей возможности.
    //Чтобы этого не происходило, маркеры нужно хранить в отдельном списке.
    private var attractionsList = ArrayList<PlacemarkMapObject>() // храним маркеры тут

    private var isAttractionShow = false //флаг. Когда карточка с дестопримечательностью видна = true

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // ЛИСЕНЕРЫ (В НИХ ПРОИСХОДИТ ОБРАБОТКА НАЖАТИЙ НА КАРТУ И ЛОГИКА РАБОТЫ ПРИ ТАКИХ НАЖАТИЯХ) //

    //Слушаем ложение камеры. Если пользователь повернул камеру, показываем кнопку с возвращением в исходную позицию
    //Если камера и так в исходной аозиции, скрывем эту кнопку
    private val cameraListener = CameraListener { map, cameraPosition, cameraUpdateReason, b ->
        if (cameraPosition.azimuth != 0.0f || cameraPosition.tilt != 0.0f) binding.cardViewCameraDefaultPosition.visibility = View.VISIBLE
        else binding.cardViewCameraDefaultPosition.visibility = View.INVISIBLE
    }

    //Создаем слушатель нажатий на крту (закрываем окно с дестопримечательностью)
    private val inputListener : InputListener = object : InputListener {
        //Короткое нажатие на крту
        override fun onMapTap(p0: Map, p1: Point) {
            if (isAttractionShow){
                isAttractionShow = false
                //анимация (скрываем attractionsCardView за 1 сек)
                val animHide = ObjectAnimator.ofFloat(binding.attractionsCardView,"alpha",1f,0f).apply {
                    duration = 500
                    interpolator = AccelerateDecelerateInterpolator()
                }
                AnimatorSet().apply {
                    play(animHide)
                    start()
                }

            }
        }
        //Долгое нажатие на карту
        override fun onMapLongTap(p0: Map, p1: Point) {}
    }

    //Слущатель нажатия на маркер (к каждому созданному маркеру на карте нужно прикремпить этот слущатель)
    //Обрабатываем в лямбде код, при нажатии на маркер (открываем окно с дестопримечательностью)
    private val attractionsInfoMapObjectTapListener = MapObjectTapListener { mapObject, point ->
        //Пытаемся привести полученнй таб к объекту AttractionsInfo (as? вернет null если такое привдение не возможно)
        val currentMarkerData = mapObject.userData as? AttractionsInfo

        if (currentMarkerData != null) {
            //Задаем параметры для отображения
            binding.attractionsTextViewName.text = currentMarkerData.name
            binding.textViewDistance.text = getString(R.string.distance_to_place) + " %.${1}f".format(currentMarkerData.dist) + " m"

            //Отображаем окно
            if (!isAttractionShow){
                isAttractionShow = true
                //анимация (проявляем attractionsCardView за 1 сек)
                val animShow = ObjectAnimator.ofFloat(binding.attractionsCardView,"alpha",0f,1f).apply {
                    duration = 500
                    interpolator = AccelerateDecelerateInterpolator()
                }
                AnimatorSet().apply {
                    play(animShow)
                    start()
                }
            }
        }
        true
    }

    // Слушатель текущей локации пользователя
    // Создаем слушатель локации как отдельную переменную (чтобы ее можно было закрыть при выходе из экрана с картой, чтобы небыло утечек памяти)
    private val locationListener = object : LocationListener {
        override fun onLocationUpdated(location: Location) { //Когда кординаты будут получены, запустится этот  метод
            lastLocation = location //Сохраняем последнюю полученную локацию

            //Передаем локацию в viewModel. Данный метод передает координыты пользователя в OpenTripMap API, который вернет список дестопримеячательностей
            //в радиусе 3000м (задается программно).
            viewModel.loadInfo(3000,location.position.longitude,location.position.latitude)

            binding.cardViewLocation.visibility = View.VISIBLE //Делаем кнопку возвращения на позици пользователя видимой
            moveCamera(location) //Наводим камеру на пользователя
        }
        override fun onLocationStatusUpdated(p0: LocationStatus) {}
    }

    //                                                                                           //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /**
         * Инициализация библиотеки для загрузки необходимых нативных библиотек.
         * Рекомендуется инициализировать библиотеку MapKit в методе Activity.onCreate()
         * Инициализация в методе Application.onCreate() может привести к лишним вызовам и увеличенному использованию батареи.
         */
        MapKitFactory.initialize(requireContext())

        _binding = FragmentAttractionsMapBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapview.map.addInputListener(inputListener) //Прикрепляем к карте слущатель нажатий на нее

        imageProviderLocationPoint = ImageProvider.fromResource(requireContext(), R.drawable.location_pin)
        mapKit = MapKitFactory.getInstance() //Получаем инстанс карты, с которым мы будем работать
        mapKitLocationManager = mapKit.createLocationManager()

        //Получаем локацию пользователя (пермищены уже полуены) и отображаем маркер пользователя на карте
        userLocationOnMapKit = mapKit.createUserLocationLayer(binding.mapview.mapWindow)
        userLocationOnMapKit.isVisible = true

        //Переводим камеру на позицию Пользователя 1 раз (при старте приложения)
        mapKitLocationManager.requestSingleUpdate(locationListener)
        //updateUserPositionPeriodically()
        binding.mapview.map.addCameraListener(cameraListener) //Добавляем слущатель нажатий на карту
        //Меняем зум при нажатии на кнопки + возвращаем камеру в начальное положение
        binding.imageButtonZoomUp.setOnClickListener { zoomUp() }
        binding.imageButtonZoomDown.setOnClickListener { zoomDown() }
        binding.imageButtonSetCameraDefaultPosition.setOnClickListener { setDefaultCameraPosition() }

        //Кнопка перехода на последнюю полученную пользовательскую координату
        binding.imageButtonLocation.setOnClickListener {
            lastLocation?.let { location ->
                moveCamera(location)
            }
        }


        //Слущаем поток с дестопримечательностями и отображаем их на карте
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.flowWithAttractions.collect{ it ->
                //Добавляем метки на карту
                it.forEach { attractionsInfo ->
                    attractionsList.add(
                        addMarker(
                            imageRes = R.drawable.location_pin,
                            objectInfo = attractionsInfo
                        )
                    )
                }
            }
        }

    }

    //Метод добавления маркера (Это обёртка над addPlacemark в которой к маркеру цепляется обработчик событий и полезная нагрузка)
    private fun addMarker(
        @DrawableRes imageRes: Int,
        objectInfo: AttractionsInfo
    ): PlacemarkMapObject
    {   //Добавляем маркер на карту
        val marker = binding.mapview.map.mapObjects
            .addPlacemark(
                Point(objectInfo.point.lat, objectInfo.point.lon),
                ImageProvider
                    .fromResource(
                        requireContext(),
                        imageRes)
            )
        marker.userData = objectInfo
        marker.addTapListener(attractionsInfoMapObjectTapListener) //Добавляем к маркеру слущатель нажатия на него

        return marker
    }

    //Подписываемся на изменение положения Пользователя, и переодически обновляем данные: lastLocation
    /*private fun updateUserPositionPeriodically() {
        mapKitLocationManager.subscribeForLocationUpdates(0.0,0,
            0.0, true, FilteringMode.ON, object : LocationListener {
                override fun onLocationUpdated(location: Location) {
                    //Сохраняем последнюю полученную позицию
                    lastLocation = location
                }
                override fun onLocationStatusUpdated(p0: LocationStatus) {}
            })
    }*/

    //Переместить камеру на указанную позицию
    private fun moveCamera(location: Location){
        binding.mapview.map.move(
            CameraPosition(location.position, 14.0f, 0.0f, 0.0f),
            Animation(
                Animation.Type.SMOOTH,
                1.5f
            ),//Анимация приблежения к указанной точке длительностью 1.5 сек
            null
        )
    }

    private fun setDefaultCameraPosition(){
        var cameraPosition = binding.mapview.map.cameraPosition //Текущая позиция камеры
        cameraPosition = CameraPosition(cameraPosition.target,cameraPosition.zoom,0.0f,0.0f) //Меняем позицию (зум)
        binding.mapview.map.move(cameraPosition, Animation(Animation.Type.SMOOTH, 1f), null) // move camera
    }

    private fun zoomUp(){
        var cameraPosition = binding.mapview.map.cameraPosition //Текущая позиция камеры
        cameraPosition = CameraPosition(cameraPosition.target,cameraPosition.zoom + 0.5f,cameraPosition.azimuth,cameraPosition.tilt) //Меняем позицию (зум)
        binding.mapview.map.move(cameraPosition, Animation(Animation.Type.SMOOTH, 0f), null) // move camera
    }

    private fun zoomDown(){
        var cameraPosition = binding.mapview.map.cameraPosition //Текущая позиция камеры
        cameraPosition = CameraPosition(cameraPosition.target,cameraPosition.zoom - 0.5f,cameraPosition.azimuth,cameraPosition.tilt) //Меняем позицию (зум)
        binding.mapview.map.move(cameraPosition, Animation(Animation.Type.SMOOTH, 0f), null) // move camera
    }

    //Останавливаем сервисы по работе с картой когда экран не видимый
    override fun onStop() {
        super.onStop()
        binding.mapview.onStop()
        MapKitFactory.getInstance().onStop()
    }

    //Запускаем сервисы по работе с картой когда экран не видимый
    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapview.onStart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapview.map.removeCameraListener(cameraListener)
        binding.mapview.map.removeInputListener(inputListener)
        binding.mapview.map.mapObjects.removeTapListener(attractionsInfoMapObjectTapListener)
        mapKitLocationManager.unsubscribe(locationListener)
        mapKit.onStop()
        _binding = null
    }

}