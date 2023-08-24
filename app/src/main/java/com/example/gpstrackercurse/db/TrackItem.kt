package com.example.gpstrackercurse.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey



@Entity (tableName = "track") // Аннотация @Entity указывает, что данный класс является сущностью в базе данных Room. Также указывается имя таблицы "track"
data class TrackItem(
    @PrimaryKey (autoGenerate = true) // Аннотация @PrimaryKey указывает, что следующее свойство является первичным ключом. autoGenerate = true означает, что Room будет автоматически генерировать значения для этого поля
    val id: Int?, // Первичный ключ, который может быть null и будет автоматически генерироваться
    @ColumnInfo (name = "time") // Аннотация @ColumnInfo указывает, что следующее свойство будет столбцом в таблице. Здесь устанавливается имя столбца "time"
    val time: String, // Свойство, которое будет сохранять время трека
    @ColumnInfo (name = "date") // Устанавливает имя столбца "date"
    val date: String, // Свойство, которое будет сохранять дату трека
    @ColumnInfo (name = "distance") // Устанавливает имя столбца "distance"
    val distance: String, // Свойство, которое будет сохранять пройденное расстояние
    @ColumnInfo (name = "velocity") // Устанавливает имя столбца "velocity"
    val velocity: String, // Свойство, которое будет сохранять скорость трека
    @ColumnInfo (name = "geo_points") // Устанавливает имя столбца "geo_points"
    val geoPoint: String // Свойство, которое будет сохранять геопозицию
)
