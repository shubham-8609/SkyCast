package com.codeleg.skycast.ui.viewModel

import androidx.lifecycle.ViewModel
import com.codeleg.skycast.data.model.WeatherResponse
import com.codeleg.skycast.data.remote.RetrofitClient
import com.codeleg.skycast.data.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {

      var stored: Pair<Double, Double>? = null


    // create repository internally to keep activity/ViewModel wiring simple
    private val weatherRepo = WeatherRepository(RetrofitClient.instance)

    suspend fun fetchWeather(lat: Double, lon: Double): WeatherResponse =
        withContext(Dispatchers.IO) {
            weatherRepo.fetchWeather(lat, lon)
        }
}