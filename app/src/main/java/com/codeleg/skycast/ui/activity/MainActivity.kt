package com.codeleg.skycast.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import java.util.Locale
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.codeleg.skycast.R
import com.codeleg.skycast.data.local.PrefManager
import com.codeleg.skycast.databinding.ActivityMainBinding
import com.codeleg.skycast.ui.viewModel.MainViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    val locationPermissionLauncher by lazy {
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) getUserLocation()
            else Toast.makeText(this@MainActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }
    private  var stored: Pair<Double, Double>? = null

    lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        manageInsects()
        manageLocationPermission()
        binding.btnFetch.setOnClickListener { fetchWeather() }
        if(stored == null){
            lifecycleScope.launch {
                stored = PrefManager.getLocation(applicationContext)
            }
        }
    }

    private fun manageLocationPermission() {

        lifecycleScope.launch {
            if(PrefManager.isLocationSet(applicationContext)){
                val saved = PrefManager.getLocation(application)
                if(saved != null){
                    stored = saved
                    Toast.makeText(this@MainActivity , "Using saved lat: ${stored!!.first} , Lon : ${stored!! .second}" , Toast.LENGTH_SHORT).show()
                    return@launch
                }
            }

            if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getUserLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        }

    }

    private fun manageInsects() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun getUserLocation()  {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return
        }

        // Try last known location first
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    Toast.makeText(
                        this,
                        "Lat: ${location.latitude}, Lon: ${location.longitude}",
                        Toast.LENGTH_SHORT
                    ).show()
                    lifecycleScope.launch {
                        PrefManager.setLocation(application, location.latitude, location.longitude)
                        stored = location.latitude to location.longitude
                    }
                } else {
                    val cts = CancellationTokenSource()
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cts.token
                    )
                        .addOnSuccessListener { loc ->
                            if (loc != null) {
                                Toast.makeText(
                                    this,
                                    "Lat: ${loc.latitude}, Lon: ${loc.longitude}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                lifecycleScope.launch {
                                    PrefManager.setLocation(application , loc.latitude , loc.longitude)
                                    stored = loc.latitude to loc.longitude
                                }
                            } else {
                                Toast.makeText(this, "Location unavailable", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to get last location", Toast.LENGTH_SHORT).show()
            }
    }

    // Implemented: fetch weather using stored location if available, otherwise prompt for location and then fetch
    private fun fetchWeather() {
        lifecycleScope.launch {
            try {
                if (stored != null) {
                    val (lat, lon) = stored!!

                    // show loading state
                    binding.btnFetch.isEnabled = false
                    binding.btnFetch.text = "Refreshing..."

                    val weather = viewModel.fetchWeather(lat, lon)

                    // update UI
                    binding.tvTemp.text = String.format(Locale.getDefault(), "%.1f°", weather.main.temp)
                    val condition = weather.weather.firstOrNull()?.description ?: "-"
                    binding.tvCondition.text = condition.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    binding.tvLocation.text = "${weather.name}, ${weather.sys.country}"
                    binding.tvHumidity.text = "${weather.main.humidity}%"
                    binding.tvPressure.text = "${weather.main.pressure} hPa"
                    binding.tvWind.text = "${weather.wind.speed} m/s"

                    // load icon from OpenWeatherMap
                    val icon = weather.weather.firstOrNull()?.icon ?: "50d"
                    val iconUrl = "https://openweathermap.org/img/wn/${icon}@4x.png"
                    Glide.with(this@MainActivity)
                        .load(iconUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(binding.ivWeatherIcon)

                    // restore button
                    binding.btnFetch.isEnabled = true
                    binding.btnFetch.text = getString(R.string.refresh)

                    Log.d("codeleg" , weather.toString())
                    return@launch
                } else {
                    // No stored location - attempt to get it (user will have to press Refresh again after permission)
                    Toast.makeText(this@MainActivity, "Location not set. Requesting location...", Toast.LENGTH_SHORT).show()
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        getUserLocation()
                    } else {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            } catch (e: Exception) {
                binding.btnFetch.isEnabled = true
                binding.btnFetch.text = getString(R.string.refresh)
                Toast.makeText(this@MainActivity, "Failed to fetch weather: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

}
