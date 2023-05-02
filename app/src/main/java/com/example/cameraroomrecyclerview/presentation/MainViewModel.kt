package com.example.cameraroomrecyclerview.presentation


import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cameraroomrecyclerview.data.MainRepository
import com.example.cameraroomrecyclerview.entity.AttractionsInfo
import com.example.cameraroomrecyclerview.entity.FileInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File


class MainViewModel(private val repository: MainRepository, private val context: Context) : ViewModel() {

    private val fileInfoDAO = repository.fileInfoDAO

    //Поток с элементами для удаления из RV
    val itemToRemoveFlow = MutableStateFlow<Int?>(null)

    //Поток с списками дестопримечательностей, которые нужно нанести на карту
    private val _flowWithAttractions = MutableStateFlow<List<AttractionsInfo>>(emptyList())
    val flowWithAttractions = _flowWithAttractions.asStateFlow()

    val allFilesFromDB = this.fileInfoDAO.getAllFiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

   fun addFileInDB(file:FileInfo){
       //Добаляем файл в БД
       viewModelScope.launch { fileInfoDAO.putFileIntoDB(file) }
    }

    fun deleteFile(uri:String){
        viewModelScope.launch {
            try {
                fileInfoDAO.deleteFileFromDB(uri) //Удаляем файл из БД
                val fileNameFromUri = File(uri).name //Получаем имя файла по его Uri
                context.deleteFile(fileNameFromUri) //Удаляем файл из внутреннего хранилища
                Toast.makeText(context,"Файл удален!", Toast.LENGTH_SHORT).show()
            }catch (t:Throwable){
                Toast.makeText(context,"Error!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Загружаем инфу с сервера
    fun loadInfo(radius:Int, lon:Double, lat:Double){
        viewModelScope.launch {
            try {
                val serverResponse = repository.getAttractions(radius,lon,lat) // Получаем список с детопримечательностями от сервера
                val listWithAttractions = emptyList<AttractionsInfo>().toMutableList() // В данном списке могут быть объекты без названия
                serverResponse.forEach {                                  // (с пустым полем name). Отфильтровываем такие элементы
                    if (it.name.isNotEmpty()) listWithAttractions.add(it)              // и кладем в список только те объекты, у которых есть название
                }
                listWithAttractions.toList()
                _flowWithAttractions.value = listWithAttractions //Кладем список с метками в поток

            }catch (t:Throwable){
                throw java.lang.Error(t.message)
            }
        }
    }

}