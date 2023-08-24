package com.example.gpstrackercurse

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.gpstrackercurse.db.MainDb
import com.example.gpstrackercurse.db.TrackItem
import com.example.gpstrackercurse.location.LocationModel
import com.example.gpstrackercurse.location.LocationService
import kotlinx.coroutines.launch



@Suppress("UNCHECKED_CAST")
class MainViewModel(db: MainDb) : ViewModel() { // объявление класса MainViewModel, который наследуется от ViewModel
    val dao = db.getDao()
    val locationUpdates = MutableLiveData<LocationModel>() // объявление и инициализация изменяемых данных для обновления местоположения
    val timeData = MutableLiveData<String>() // объявление и инициализация изменяемых данных для времени
    val tracks = dao.getAllTracks().asLiveData()

    fun insertTrack(trackItem: TrackItem) = viewModelScope.launch {
        dao.insertTrack(trackItem)
    }

    class ViewModelFactory(private val db: MainDb) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(db) as T
            }
            throw IllegalAccessException("Unknown ViewModel class")
        }
    }
}