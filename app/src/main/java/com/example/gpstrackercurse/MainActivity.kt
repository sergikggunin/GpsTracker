package com.example.gpstrackercurse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.gpstrackercurse.databinding.ActivityMainBinding
import com.example.gpstrackercurse.fragments.MainFragment
import com.example.gpstrackercurse.fragments.SettingsFragment
import com.example.gpstrackercurse.fragments.TrackFragment
import com.example.gpstrackercurse.utils.openFragment


class MainActivity : AppCompatActivity() { // объявление класса MainActivity, который наследуется от AppCompatActivity
    private lateinit var binding: ActivityMainBinding // объявление переменной для привязки

    override fun onCreate(savedInstanceState: Bundle?) { // метод, вызывающийся при создании активности
        super.onCreate(savedInstanceState) // вызов метода родительского класса
        binding = ActivityMainBinding.inflate(layoutInflater) // инициализация привязки
        setContentView(binding.root) // установка макета активности
        onBottomNavClick() // обработка кликов по нижней навигационной панели
        openFragment(MainFragment.newInstance()) // открытие главного фрагмента при запуске активности
    }

    private fun onBottomNavClick(){ // функция для обработки кликов по нижней навигационной панели
        binding.bNan.setOnItemSelectedListener { // установка слушателя для элементов навигационной панели
            when(it.itemId){  // в зависимости от выбранного пункта меню
                R.id.id_home -> openFragment(MainFragment.newInstance()) // если выбран домой, то открываем главный фрагмент
                R.id.id_tracks -> openFragment(TrackFragment.newInstance()) // если выбраны треки, то открываем фрагмент треков
                R.id.id_settings -> openFragment(SettingsFragment()) // если выбраны настройки, то открываем фрагмент настроек
            }
            true // возвращаем true, чтобы сообщить, что событие было обработано
        }
    }
}