package com.codeleg.skycast.data.repository

import com.codeleg.skycast.data.model.WeatherResponse
import com.codeleg.skycast.data.remote.WeatherApiService

class WeatherRepository(private val api: WeatherApiService) {
    suspend fun fetchWeather(lat:Double , lon:Double): WeatherResponse{
        return api.getCurrentWeather(lat,lon,"2c49ba6e46f708e3b38a7e3d0218522b")
    }
}