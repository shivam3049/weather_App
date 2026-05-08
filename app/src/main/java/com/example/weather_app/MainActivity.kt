package com.example.weather_app

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.weather_app.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private val TAG = "WeatherApp"

    private lateinit var binding: ActivityMainBinding

    private val api by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fetchWeatherData("Mumbai")
        setupSearch()
    }

    private fun setupSearch() {
        val searchEditText = binding.searchView.findViewById<EditText>(
            androidx.appcompat.R.id.search_src_text
        )
        searchEditText.hint = "Search City"
        searchEditText.setHintTextColor(Color.GRAY)
        searchEditText.setTextColor(Color.BLACK)
        searchEditText.isCursorVisible = true
        binding.searchView.setOnClickListener {
            binding.searchView.isIconified = false  
            searchEditText.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    fetchWeatherData(query.trim())
                    binding.searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?) = false
        })
    }
    private fun fetchWeatherData(city: String) {
        api.getWeatherData(city, apiKey = "99c74442541d6d66f227702e4dc6a67e", units = "metric")
            .enqueue(object : Callback<WeatherApp> {
                override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!

                        binding.cityName.text = data.name
                        binding.temp.text = "${data.main.temp} °C"
                        binding.maxtemp.text = "Max ${data.main.temp_max} °C"
                        binding.mintemp.text = "Min ${data.main.temp_min} °C"
                        binding.Humidity.text = "${data.main.humidity} %"
                        binding.WindSpeed.text = "${data.wind.speed} m/s"
                        binding.Sea.text = "${data.main.pressure} hPa"

                        val condition = data.weather[0].main
                        binding.Condition.text = condition
                        binding.textView4.text = condition.uppercase()

                        binding.Sunrise.text = formatTime(data.sys.sunrise, data.timezone)
                        binding.Sunset.text = formatTime(data.sys.sunset, data.timezone)

                        setDateAndDay(data.timezone)
                        changeImageAccordingToWeather(condition)
                    } else {
                        Log.e(TAG, "City not found")
                    }
                }

                override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                    Log.e(TAG, "API Error: ${t.message}")
                }
            })
    }

    private fun changeImageAccordingToWeather(condition: String) {
        when (condition.lowercase()) {
            "clear" -> {
                binding.main.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sunclouds)
            }
            "clouds", "mist", "fog", "haze" -> {
                binding.main.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "rain", "drizzle", "thunderstorm" -> {
                binding.main.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "snow" -> {
                binding.main.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }
    private fun formatTime(time: Long, timezone: Int): String {
        val date = Date((time + timezone) * 1000L)
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(date)
    }
    private fun setDateAndDay(timezone: Int) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis() + (timezone * 1000L)
        binding.day.text = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
        binding.date.text =
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
    }
}
