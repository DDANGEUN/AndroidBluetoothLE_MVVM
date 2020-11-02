package com.lilly.ble

import android.app.Application
import android.content.Context

class MyApplication : Application() {

    init{
        instance = this
    }

    companion object {
        lateinit var instance: MyApplication
        fun applicationContext() : Context {
            return instance.applicationContext
        }

        fun getRepository(): Repository {
            return Repository.getInstance()
        }
    }

}