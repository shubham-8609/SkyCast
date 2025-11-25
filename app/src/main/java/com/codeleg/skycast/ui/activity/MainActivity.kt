package com.codeleg.skycast.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.codeleg.skycast.R
import com.codeleg.skycast.data.local.PrefManager
import com.codeleg.skycast.databinding.ActivityMainBinding
import com.codeleg.skycast.ui.fragment.Dashboard
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
        if(viewModel.stored == null){
            lifecycleScope.launch {
                viewModel.stored = PrefManager.getLocation(applicationContext)
            }
        }
        if(savedInstanceState == null){
            binding.mainContainer.apply {
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.main_container,
                        Dashboard(),
                        "DashboardFragment"
                    )
                    .commit()
        }
    }
        }

    private fun manageLocationPermission() {

        lifecycleScope.launch {
            if(PrefManager.isLocationSet(applicationContext)){
                val saved = PrefManager.getLocation(application)
                if(saved != null){
                    viewModel.stored = saved
                    Log.d("codeleg" , "Using saved lat: ${viewModel.stored!!.first} , Lon : ${viewModel.stored!!.second}")
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
                    Log.d("codeleg" , "Got location from lastLocation : Lat: ${location.latitude}, Lon: ${location.longitude}")
                    lifecycleScope.launch {
                        PrefManager.setLocation(application, location.latitude, location.longitude)
                        viewModel.stored = location.latitude to location.longitude
                    }
                } else {
                    val cts = CancellationTokenSource()
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cts.token
                    )
                        .addOnSuccessListener { loc ->
                            if (loc != null) {

                                Log.d("codeleg" , "Got location from getCurrentLocation : Lat: ${loc.latitude}, Lon: ${loc.longitude}")
                                lifecycleScope.launch {
                                    PrefManager.setLocation(application , loc.latitude , loc.longitude)
                                    viewModel.stored = loc.latitude to loc.longitude
                                }
                            } else {
                                Log.d("codeleg" , "Location unavailable from getCurrentLocation")
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


}
