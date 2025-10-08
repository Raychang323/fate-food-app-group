package com.fatefulsupper.app.ui.luckyfood

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.fatefulsupper.app.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class LuckyFoodFragment : Fragment(), OnMapReadyCallback {

    private lateinit var viewModel: LuckyFoodViewModel
    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private lateinit var findButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocationAndFetchFood()
        } else {
            Toast.makeText(requireContext(), "需要定位權限才能尋找附近的幸運餐廳", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lucky_food, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(LuckyFoodViewModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        mapView = view.findViewById(R.id.mapView)
        findButton = view.findViewById(R.id.find_lucky_food_button)
        progressBar = view.findViewById(R.id.progressBar)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        findButton.setOnClickListener {
            checkLocationPermission()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            findButton.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.luckyFoodResult.observe(viewLifecycleOwner) { response ->
            response?.places?.let { places ->
                googleMap?.clear()
                if (places.isNotEmpty()) {
                    val boundsBuilder = LatLngBounds.Builder()
                    places.forEach { place ->
                        val latLng = LatLng(place.apiLocation.latitude, place.apiLocation.longitude)
                        googleMap?.addMarker(
                            MarkerOptions().position(latLng).title(place.displayName.text)
                        )
                        boundsBuilder.include(latLng)
                    }
                    val bounds = boundsBuilder.build()
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                } else {
                    Toast.makeText(requireContext(), "附近沒有找到推薦的餐廳", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocationAndFetchFood()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // 可在此顯示對話框向用戶解釋為何需要此權限
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getCurrentLocationAndFetchFood() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // TODO: 判斷使用者是否為會員
                val isMember = true // 暫時寫死為 true
                viewModel.fetchLuckyFood(location.latitude, location.longitude, isMember)

                // 將地圖中心移到目前位置
                val currentLatLng = LatLng(location.latitude, location.longitude)
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            } else {
                Toast.makeText(requireContext(), "無法取得目前位置，請確認 GPS 已開啟", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        // 預設將地圖中心移到台北車站
        val defaultLocation = LatLng(25.0479, 121.5173)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}