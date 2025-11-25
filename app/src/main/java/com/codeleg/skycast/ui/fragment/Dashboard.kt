package com.codeleg.skycast.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.codeleg.skycast.R
import com.codeleg.skycast.data.model.WeatherResponse
import com.codeleg.skycast.databinding.FragmentDashboardBinding
import com.codeleg.skycast.ui.viewModel.MainViewModel
import kotlinx.coroutines.launch
import java.util.Locale

class Dashboard : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnRefresh.setOnClickListener { fetchWeather() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun fetchWeather() {
        lifecycleScope.launch {
            try {
                val stored = mainViewModel.stored
                if (stored != null) {
                    val (lat, lon) = stored

                    // show loading state
                    binding.btnRefresh.isEnabled = false
                    binding.btnRefresh.text = "Refreshing..."

                    val weather = mainViewModel.fetchWeather(lat, lon)

                    // update UI
                    binding.tvTemperature.text = String.format(Locale.getDefault(), "%.1f°", weather.main.temp)
                    val condition = weather.weather.firstOrNull()?.description ?: "-"
                    binding.tvLocation.text = "${weather.name}, ${weather.sys.country}"
                    binding.tvHumidity.text = "${weather.main.humidity}%"
                    binding.tvPressure.text = "${weather.main.pressure} hPa"
                    binding.tvWeatherStatus.text = "${weather.weather.firstOrNull()?.main}"
                    binding.tvWind.text = "${weather.wind.speed} m/s"
                    updateIndicators(weather)

                    // load icon from OpenWeatherMap
                    val icon = weather.weather.firstOrNull()?.icon ?: "50d"
                    val iconUrl = "https://openweathermap.org/img/wn/${icon}@4x.png"
                    Glide.with(requireContext())
                        .load(iconUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(binding.ivWeatherIcon)

                    // restore button
                    binding.btnRefresh.isEnabled = true
                    binding.btnRefresh.text = getString(R.string.refresh)

                    Log.d("codeleg" , weather.toString())
                    return@launch
                } else {
                    Toast.makeText(requireContext(), "Location not set.", Toast.LENGTH_SHORT).show()

                }
            } catch (e: Exception) {
                binding.btnRefresh.isEnabled = true
                binding.btnRefresh.text = getString(R.string.refresh)
                Toast.makeText(requireContext(), "Failed to fetch weather: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateIndicators(weather: WeatherResponse) {

        // Humidity
        val humidity = weather.main.humidity
        binding.progressHumidity.setProgress(humidity, true)

        // Wind Speed
        val windSpeed = weather.wind.speed
        val kmh = windSpeed * 3.6
        val windPercent = ((kmh / 20f) * 100).toInt().coerceIn(0, 100)

        binding.progressWind.setProgress(windPercent, true)

        // Pressure
        val pressure = weather.main.pressure
        val pressurePercent = ((pressure - 950f) / 100f * 100).toInt().coerceIn(0, 100)

        binding.progressPressure.setProgress(pressurePercent, true)
    }

}