package com.diavolo.gogroceriesapp

import android.app.Application
import android.util.Log
import com.diavolo.gogroceriesapp.data.local.DefaultCategorySeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class GoGroceriesApp : Application() {

    @Inject
    lateinit var defaultCategorySeeder: DefaultCategorySeeder

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            runCatching { defaultCategorySeeder.seed() }
                .onFailure { error ->
                    Log.e("GoGroceriesApp", "Unable to seed default categories", error)
                }
        }
    }
}
