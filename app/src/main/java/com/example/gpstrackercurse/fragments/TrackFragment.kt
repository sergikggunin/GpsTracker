package com.example.gpstrackercurse.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpstrackercurse.MainApp
import com.example.gpstrackercurse.MainViewModel
import com.example.gpstrackercurse.databinding.TracksBinding
import com.example.gpstrackercurse.db.TrackAdapter


class TrackFragment : Fragment() { // Определение биндинга для этого фрагмента
    private lateinit var binding: TracksBinding
    private lateinit var adapter: TrackAdapter
    private val model: MainViewModel by activityViewModels {
        MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)
    }

    override fun onCreateView( // Метод, вызываемый при создании видов этого фрагмента
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TracksBinding.inflate(inflater, container, false) // Инициализируем биндинг, который будет использоваться для управления внешним видом этого фрагмента
        return binding.root // Возвращаем корневой вид из биндинга как основной вид для этого фрагмента
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
        getTracks()
    }

    private fun getTracks(){
        model.tracks.observe(viewLifecycleOwner){
            adapter.submitList(it)
            binding.tvEmpty.visibility = if(it.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun initRcView() = with(binding){
        adapter = TrackAdapter()
        rcView.layoutManager = LinearLayoutManager(requireContext())
        rcView.adapter = adapter
    }


    companion object { // Компаньон-объект для этого класса

        @JvmStatic
        fun newInstance() = TrackFragment() // Статический метод для создания нового экземпляра этого фрагмента
    }
}
