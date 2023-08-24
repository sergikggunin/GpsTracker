package com.example.gpstrackercurse.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.gpstrackercurse.MainActivity
import com.example.gpstrackercurse.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import org.osmdroid.util.GeoPoint

@Suppress("DEPRECATION") // подавляем устаревшие предупреждения
class LocationService: Service() { // инициализация необходимых переменных и объектов
    private  var distance = 0.0f // расстояние, которое прошёл пользователь
    private var lastLocation: Location? = null // последнее известное местоположение
    private lateinit var locProvider: FusedLocationProviderClient // клиент для взаимодействия
    private lateinit var locRequest: LocationRequest // запрос на обновление местоположения
    private lateinit var geoPointList: ArrayList<GeoPoint> // список координат точек

    override fun onBind(p0: Intent?): IBinder? { // Системный метод, который вызывается, когда другой компонент (как Activity) хочет связаться с сервисом через bindService()
        return null // этот сервис не предоставляет интерфейс связи, поэтому возвращается null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int { // Системный метод, вызываемый системой при старте сервиса через startService()
        startNotification() // запускаем уведомления
        startLocationUpdates() // начинаем получать обновления местоположения
        isRunning = true // сервис запущен
        return START_STICKY // если сервис убивается системой, система попытается воссоздать его
    }

    override fun onCreate() { // Системный метод, вызывается при первом создании сервиса
        super.onCreate()
        geoPointList = ArrayList() // инициализируем список геопоинтов
        initLocation() // инициализируем настройки местоположения
    }

    override fun onDestroy() { // Системный метод, вызывается при уничтожении сервиса
        super.onDestroy()
        isRunning = false // сервис больше не работает
        locProvider.removeLocationUpdates(locCallBack) // прекращаем получать обновления местоположения

    }

    private val locCallBack = object : LocationCallback() {     // Обратный вызов, получающий обновления местоположения
        override fun onLocationResult(lResult: LocationResult) {
            super.onLocationResult(lResult)
            val currentLocation = lResult.lastLocation // текущее местоположение
            if (lastLocation != null && currentLocation != null) { // если есть последнее и текущее местоположение
                distance += lastLocation?.distanceTo(currentLocation)!! // увеличиваем расстояние
                geoPointList.add(GeoPoint(currentLocation.latitude, currentLocation.longitude)) // добавляем геопоинт в список
                val locModel = LocationModel(  // создаём модель местоположения
                    currentLocation.speed,
                    distance,
                    geoPointList
                )
                sendLocData((locModel)) // отправляем данные модели
            }
            lastLocation = currentLocation // обновляем последнее местоположение
            Log.d("MyLog", "Distance $distance") // логируем расстояние
        }
    }

    private fun sendLocData(locModel: LocationModel) { // отправляем данные модели местоположения
        val i = Intent(LOC_MODEL_INTENT) // создаём интент
        i.putExtra(LOC_MODEL_INTENT, locModel) // кладём в интент модель
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(i) // широковещательное сообщение
    }

    private fun startNotification() { // запускаем уведомление
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // если версия ОС Oreo или выше
            val nChannel = NotificationChannel( // создаём канал уведомлений
                CHANNEL_ID,
                "Location Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val nManager = getSystemService(NotificationManager::class.java) as NotificationManager
            nManager.createNotificationChannel(nChannel) // создаём канал
        }
        val nIntent = Intent(this, MainActivity::class.java) // создаём интент
        val pIntent = PendingIntent.getActivity( // создаём задержанный интент
            this,
            10,
            nIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder( // создаём уведомление
            this,
            CHANNEL_ID
        ).setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Tracker Running!")
            .setContentIntent(pIntent).build()
        startForeground(9, notification) // старт уведомления
    }

    private fun initLocation() { // инициализируем настройки местоположения
        locRequest = LocationRequest.create() // создаем запрос
        locRequest.interval = 7000 // интервал обновлений
        locRequest.fastestInterval = 5000 // самый быстрый интервал
        locRequest.priority = PRIORITY_HIGH_ACCURACY // высокая точность
        locProvider = LocationServices.getFusedLocationProviderClient(baseContext) // получаем клиента
    }

    private fun startLocationUpdates() { // начинаем получать обновления местоположения
        if (ActivityCompat.checkSelfPermission( // если нет разрешения на доступ к местоположению, то выходим
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return // запрашиваем обновления местоположения

        locProvider.requestLocationUpdates(
            locRequest,
            locCallBack,
            Looper.myLooper()
        )
    }

    companion object {  // константы и переменные, которые могут использоваться другими классами
        const val LOC_MODEL_INTENT = "loc_intent"
        const val CHANNEL_ID = "channel_1"
        var isRunning = false // флаг работы сервиса
        var StartTime = 0L // время начала работы

    }
}