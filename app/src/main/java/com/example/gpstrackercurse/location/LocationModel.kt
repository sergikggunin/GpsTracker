package com.example.gpstrackercurse.location

import org.osmdroid.util.GeoPoint
import java.io.Serializable

data class LocationModel ( // Определяем класс LocationModel, который будет хранить данные о местоположении
    val velocity: Float = 0.0f, // значение скорости, по умолчанию 0.0f
    val distance: Float = 0.0f, // значение расстояния, по умолчанию 0.0f
    val geoPointList: ArrayList<GeoPoint> // список географических точек, в которых происходило местоположение
) : Serializable // наш класс реализует интерфейс Serializable, чтобы его экземпляры можно было преобразовывать в последовательность байтов и обратно