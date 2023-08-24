package com.example.gpstrackercurse

import android.app.Application
import com.example.gpstrackercurse.db.MainDb

class MainApp : Application() {
    val database by lazy { MainDb.getDatabase(this) }

}