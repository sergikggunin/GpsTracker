package com.example.gpstrackercurse.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Эта аннотация указывает Room создать базу данных,
// со всеми таблицами для каждого класса, указанного в массиве entities.
// Номер версии помогает управлять миграциями базы данных.
@Database(entities = [TrackItem::class], version = 1)

// Это говорит Room, что этот класс является базой данных.
abstract class MainDb: RoomDatabase() {
    abstract fun getDao(): Dao

    companion object {
        // Эта аннотация означает, что записи в это поле немедленно становятся видимыми для других потоков.
        @Volatile
        var INSTANCE: MainDb? = null

        fun getDatabase(context: Context): MainDb {
            // Здесь вы возвращаете экземпляр, если он уже существует,
            // в противном случае вы создаете новый в синхронизированном блоке,
            // чтобы предотвратить создание нескольких экземпляров базы данных.
            return INSTANCE ?: synchronized(this) {
                // Здесь вы создаете экземпляр базы данных с помощью конструктора Room.
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MainDb::class.java,
                    "GpsTracker.db"
                ).build()
                INSTANCE = instance
                // Вы возвращаете экземпляр в конце.
                return instance
            }
        }
    }
}
