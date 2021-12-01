package com.example.noteapp

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var appCon: Context;
        lateinit var instance: MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        appCon = applicationContext
        instance = this

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
    }
}