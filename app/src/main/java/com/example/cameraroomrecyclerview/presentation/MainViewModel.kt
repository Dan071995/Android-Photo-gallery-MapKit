package com.example.cameraroomrecyclerview.presentation


import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cameraroomrecyclerview.data.FileInfoDAO
import com.example.cameraroomrecyclerview.entity.FileInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File


class MainViewModel(private val fileInfoDAO: FileInfoDAO, private val context: Context) : ViewModel() {
    //Поток с элементами для удаления из RV
    val itemToRemoveFlow = MutableStateFlow<Int?>(null)

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

}