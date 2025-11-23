package com.codeleg.skycast.data.model

data class WeatherResponse(
    val coord:Coord,
    val weather:List<Weather>,
    val main:Main,
    val wind: Wind,
    val sys:Sys,
    val name : String
)
data class Coord(val lon: Double, val lat: Double)
data class Weather(val main: String, val description: String, val icon: String)
data class Main(val temp: Double, val humidity: Int, val pressure: Int)
data class Wind(val speed: Double)
data class Sys(val country: String)