package com.example.gpstrackercurse.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gpstrackercurse.databinding.ViewTrackBinding


class ViewTrackFragment : Fragment() { // Определение биндинга для этого фрагмента
    private lateinit var binding: ViewTrackBinding

    override fun onCreateView( // Метод, вызываемый при создании видов этого фрагмента
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ViewTrackBinding.inflate(inflater, container, false) // Инициализируем биндинг, который будет использоваться для управления внешним видом этого фрагмента
        return binding.root // Возвращаем корневой вид из биндинга как основной вид для этого фрагмента
    }

    companion object { // Компаньон-объект для этого класса

        @JvmStatic
        fun newInstance() = ViewTrackFragment() // Статический метод для создания нового экземпляра этого фрагмента
    }
}
