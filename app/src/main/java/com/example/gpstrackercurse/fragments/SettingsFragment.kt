package com.example.gpstrackercurse.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.gpstrackercurse.R

class SettingsFragment : PreferenceFragmentCompat() { // Определение двух настроек: интервал обновления и цвет трека
    private lateinit var timePref: Preference
    private lateinit var colorPref: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) { // Метод, вызываемый при создании экрана настроек
        setPreferencesFromResource(R.xml.main_preference, rootKey) // Загружаем настройки из XML-файла
        init() // Инициализируем настройки
    }

    private fun init() { // Инициализация настроек
        timePref = findPreference("update_time_key")!!  // Находим настройки по их ключам
        colorPref = findPreference("color_key")!!
        val changeListener = onChangeListener()  // Создаем слушатель изменений настроек
        timePref.onPreferenceChangeListener = changeListener
        colorPref.onPreferenceChangeListener = changeListener  // Привязываем слушателя к настройкам
        initPrefs() // Инициализируем значения настроек

    }

    private fun onChangeListener(): Preference.OnPreferenceChangeListener { // Создаем слушатель изменений настроек
        return Preference.OnPreferenceChangeListener{
            pref, value ->
                when(pref.key){  // В зависимости от того, какая настройка изменилась, выполняем определенные действия
                    "update_time_key" -> onTimeChange(value.toString()) // Если изменилась настройка времени обновления, обновляем ее значение
                    "color_key" -> pref.icon?.setTint(Color.parseColor(value.toString())) // Если изменился цвет трека, меняем цвет иконки на новый
                }
            true
        }
    }

    private fun onTimeChange(value: String){  // Обновляем настройку времени обновления
        val nameArray = resources.getStringArray(R.array.loc_time_update_name) // Получаем массивы значений и их представлений из ресурсов
        val valueArray = resources.getStringArray(R.array.loc_time_update_value)
        val title = timePref.title.toString().substringBefore(":")  // Получаем текущее значение настройки
        val pos = valueArray.indexOf(value) // Находим позицию нового значения в массиве значений
        timePref.title = "$title: ${nameArray[pos]}" // Обновляем настройку новым значением
    }

    private fun initPrefs(){ // Инициализация значений настроек
        val pref = timePref.preferenceManager.sharedPreferences // Получаем текущие значения настроек
        val nameArray = resources.getStringArray(R.array.loc_time_update_name) // Получаем массивы значений и их представлений из ресурсов
        val valueArray = resources.getStringArray(R.array.loc_time_update_value)
        val title = timePref.title // Получаем текущее значение настройки времени обновления
        val pos = valueArray.indexOf(pref?.getString("update_time_key", "3000")) // Находим позицию текущего значения в массиве значений
        timePref.title = "$title: ${nameArray[pos]}" // Обновляем настройку текущим значением

        val trackColor = pref?.getString("color_key", "#FF050FC1")
        colorPref.icon?.setTint(Color.parseColor(trackColor)) // Обновляем иконку настройки цвета трека текущим цветом
    }
}