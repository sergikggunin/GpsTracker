package com.example.gpstrackercurse.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.gpstrackercurse.MainApp
import com.example.gpstrackercurse.MainViewModel
import com.example.gpstrackercurse.R
import com.example.gpstrackercurse.databinding.FragmentMainBinding
import com.example.gpstrackercurse.db.TrackItem
import com.example.gpstrackercurse.location.LocationModel
import com.example.gpstrackercurse.location.LocationService
import com.example.gpstrackercurse.utils.DialogManager
import com.example.gpstrackercurse.utils.TimeUtils
import com.example.gpstrackercurse.utils.checkPermission
import com.example.gpstrackercurse.utils.showToast
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.lang.StringBuilder
import java.util.Timer
import java.util.TimerTask


@Suppress("DEPRECATION")
class MainFragment : Fragment() {   // Объявление переменных: locationModel, pl, isServiceRunning, firstStart, timer, startTime, pLauncher, binding, model
    private var locationModel: LocationModel? = null
    private var pl: Polyline? = null
    private var isServiceRunning = false
    private var firstStart = true
    private var timer: Timer? = null
    private var startTime = 0L
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var binding: FragmentMainBinding
    private val model: MainViewModel by activityViewModels{
        MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)
    }

    override fun onCreateView(  // Здесь создается вид фрагмента, настраивается основная карта OSM (OpenStreetMap)
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsOsm()
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { // При создании вида регистрируются разрешения, устанавливаются обработчики нажатий, проверяется состояние сервиса, обновляется время, регистрируется приемник местоположения и обновляются данные о местоположении
        super.onViewCreated(view, savedInstanceState)
        registerPermission()
        setOnClicks()
        checkServiceState()
        updateTime()
        registerLocReceiver()
        locationUpdates()
    }

    private fun setOnClicks() = with(binding) {  // Установка обработчиков нажатий на элементы
        val listener = onClicks()
        fStartStop.setOnClickListener(listener)


    }

    private fun onClicks(): View.OnClickListener {   // Обработка нажатий на определенные элементы интерфейса
        return View.OnClickListener {
            when (it.id) {
                R.id.fStartStop -> startStopService()
            }
        }
    }

    private fun locationUpdates() = with(binding) {  // Обновление данных о местоположении
        model.locationUpdates.observe(viewLifecycleOwner) {
            val distance = "Distance: ${String.format("%.1f", it.distance)} m"
            val velocity = "Velocity: ${String.format("%.1f", 3.6f * it.velocity)} km/h"
            val aVelocity = "Average Velocity: ${getAverageSpeed(it.distance)} km/h"
            tvDistance.text = distance
            tvVelocity.text = velocity
            tvAveragevelo.text = aVelocity
            locationModel = it
            updatePolyline(it.geoPointList)

        }
    }

    private fun updateTime() {  // Обновление времени
        model.timeData.observe(viewLifecycleOwner) {
            binding.tvTime.text = it
        }
    }

    private fun startTimer() {    // Запуск таймера для обновления времени
        timer?.cancel()
        timer = Timer()
        startTime = LocationService.StartTime
        timer?.schedule(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    model.timeData.value = getCurrentTime()
                }

            }

        }, 1, 1)
    }

    private fun getAverageSpeed(distance: Float): String {  // Вычисление средней скорости
        return String.format(
            "%.1f",
            distance / (3.6f * (System.currentTimeMillis() - startTime / 1000.0f))
        )
    }

    private fun getCurrentTime(): String {  // Получение текущего времени
        return "Time: ${TimeUtils.getTime(System.currentTimeMillis() - startTime)}"
    }

    private fun geoPointsToString(list: List<GeoPoint>): String{  // Конвертация списка географических точек в строку
        val sb = StringBuilder()
        list.forEach {
            sb.append("${it.latitude}, ${it.longitude}/")
        }
        Log.d("MyLog", "Points: $sb")
        return sb.toString()
    }

    private fun startStopService() {
        // Запуск или остановка службы отслеживания
        if (!isServiceRunning) {
            startLocService()
        } else {
            activity?.stopService(Intent(activity, LocationService::class.java))
            binding.fStartStop.setImageResource(R.drawable.ic_play)
            val cancel = timer?.cancel()
            val track = getTrackItem()
            DialogManager.showSaveDialog(requireContext(),
                track,
                object: DialogManager.Listener{
                override fun onClick() {
                        showToast("Track Saved!")
                    model.insertTrack(track)
                }

            })
        }
        isServiceRunning = !isServiceRunning
    }

    private fun getTrackItem(): TrackItem {     // Получение данных трека для сохранения
        return TrackItem(
            null,
            getCurrentTime(),
            TimeUtils.getDate(),
            String.format("%.1f", locationModel?.distance?.div(1000) ?: 0),
            getAverageSpeed(locationModel?.distance ?: 0.0f),
            geoPointsToString(locationModel?.geoPointList ?: listOf())
        )
    }

    private fun checkServiceState() {   // Проверка состояния службы отслеживания
        isServiceRunning = LocationService.isRunning
        if (isServiceRunning) {
            binding.fStartStop.setImageResource(R.drawable.ic_stop)
            startTimer()
        }
    }

    private fun startLocService() {   // Запуск службы отслеживания
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(Intent(activity, LocationService::class.java))
        } else {
            activity?.startService(Intent(activity, LocationService::class.java))
        }
        binding.fStartStop.setImageResource(R.drawable.ic_stop)
        LocationService.StartTime = System.currentTimeMillis()
        startTimer()
    }




    override fun onResume() {  // Проверка разрешения на использование геолокации при возвращении в приложение
        super.onResume()


        checkLocPermission()
    }


    private fun settingsOsm() {  // Настройка конфигурации OSM (OpenStreetMap)
        Configuration.getInstance().load(
            activity as AppCompatActivity,
            activity?.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.LIBRARY_PACKAGE_NAME
    }

    private fun initOSM() = with(binding) {   // Инициализация карты OSM с отображением маршрута и текущего положения
        pl = Polyline()
        pl?.outlinePaint?.color = Color.BLUE
        map.controller.setZoom(20.0)
        val mLocProvider = GpsMyLocationProvider(activity)
        val mLocOverlay = MyLocationNewOverlay(mLocProvider, map)
        mLocOverlay.enableMyLocation()
        mLocOverlay.enableFollowLocation()
        mLocOverlay.runOnFirstFix {
            map.overlays.clear()
            map.overlays.add(mLocOverlay)
            map.overlays.add(pl)
        }
    }

    private fun registerPermission() { // Регистрация разрешений на получение геолокации
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (it[android.Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                initOSM()
                checkLocationEnabled()
            } else {
                showToast("Вы не дали разрешение на использование местоположения!")
            }
        }
    }

    private fun checkLocPermission() {  // Проверка разрешения на использование геолокации в зависимости от версии ОС
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkPermissionAfter10()
        } else {
            checkPermissionBefore10()
        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)  // Проверка разрешения на использование геолокации для версий ОС после Android 10
    private fun checkPermissionAfter10() {
        if (checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            && checkPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            initOSM()
            checkLocationEnabled()
        } else {
            pLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        }
    }

    private fun checkPermissionBefore10() {  // Проверка разрешения на использование геолокации для версий ОС до Android 10
        if (checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            initOSM()
            checkLocationEnabled()
        } else {
            pLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    private fun checkLocationEnabled() { // Проверка включена ли геолокация на устройстве
        val lManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isEnabled) {
            DialogManager.showLocEnableDialog(
                activity as AppCompatActivity,
                object : DialogManager.Listener {
                    override fun onClick() {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }

                }
            )

        } else {
            showToast("Location enabled")
        }
    }

    private val receiver = object : BroadcastReceiver() {  // BroadcastReceiver для получения обновленной информации о местоположении из службы
        override fun onReceive(context: Context?, i: Intent?) {
            if (i?.action == LocationService.LOC_MODEL_INTENT) {
                val locModel =
                    i.getSerializableExtra(LocationService.LOC_MODEL_INTENT) as LocationModel
                Log.d("MyLog", "Main Fragment Distance: ${locModel.distance}")
                model.locationUpdates.value = locModel
            }
        }
    }

    private fun registerLocReceiver() { // Регистрация приемника местоположения
        val locFilter = IntentFilter(LocationService.LOC_MODEL_INTENT)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity)
            .registerReceiver(receiver, locFilter)
    }

    private fun addPoint(list: List<GeoPoint>) {  // Добавление новой точки на карту
        pl?.addPoint(list[list.size - 1])
    }

    private fun fillPolyline(list: List<GeoPoint>) {  // Заполнение полилинии (маршрута) на карте
        list.forEach {
            pl?.addPoint(it)
        }
    }

    private fun updatePolyline(list: List<GeoPoint>) {  // Обновление полилинии на карте
        if (list.size > 1 && firstStart){
            fillPolyline(list)
        firstStart = false
    } else {
        addPoint(list)
    }
}

    override fun onDetach() {  // При откреплении фрагмента, отмена регистрации приемника местоположения
        super.onDetach()
        LocalBroadcastManager.getInstance(activity as AppCompatActivity)
            .unregisterReceiver(receiver)
    }

    companion object { // Функция для создания экземпляра MainFragment
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}
