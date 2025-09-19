package com.fatefulsupper.app.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.fatefulsupper.app.R
// Import Google Maps SDK components if you plan to use it, e.g.:
// import com.google.android.gms.maps.CameraUpdateFactory
// import com.google.android.gms.maps.GoogleMap
// import com.google.android.gms.maps.OnMapReadyCallback
// import com.google.android.gms.maps.SupportMapFragment
// import com.google.android.gms.maps.model.LatLng
// import com.google.android.gms.maps.model.MarkerOptions

class MapDirectionFragment : Fragment() /*, OnMapReadyCallback */ {

    private lateinit var viewModel: MapDirectionViewModel
    private val args: MapDirectionFragmentArgs by navArgs()
    // private var googleMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map_direction, container, false)
        viewModel = ViewModelProvider(this).get(MapDirectionViewModel::class.java)

        val destinationName = args.destinationName
        val latitude = args.latitude
        val longitude = args.longitude

        viewModel.setDestination(latitude, longitude, destinationName ?: "N/A")

        val textViewDestinationName = view.findViewById<TextView>(R.id.textView_destination_name_display)
        val textViewLatitude = view.findViewById<TextView>(R.id.textView_latitude_display)
        val textViewLongitude = view.findViewById<TextView>(R.id.textView_longitude_display)

        textViewDestinationName.text = "目的地: ${destinationName ?: "N/A"}"
        textViewLatitude.text = "緯度: $latitude"
        textViewLongitude.text = "經度: $longitude"

        // // Initialize the map (example for later)
        // val mapFragment = childFragmentManager.findFragmentById(R.id.map_view_placeholder) as? SupportMapFragment // map_view_placeholder should be a FragmentContainerView in XML
        // mapFragment?.getMapAsync(this)

        // TODO: Observe viewModel for route information and display it on the map.
        // viewModel.routeInfo.observe(viewLifecycleOwner) { route ->
        //     // Draw polyline, markers for start/end, etc.
        // }

        return view
    }

    // override fun onMapReady(map: GoogleMap) {
    //     googleMap = map
    //     // TODO: Request location permissions if not already granted.
    //     // TODO: Get current user location.
    //     // TODO: Add marker for destination, show route, etc.
    //     viewModel.destination.value?.let {
    //         // val destLatLng = LatLng(it.latitude, it.longitude) // Assuming DestinationInfo has these fields
    //         // googleMap?.addMarker(MarkerOptions().position(destLatLng).title(it.name))
    //         // googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(destLatLng, 15f))
    //         // // viewModel.fetchDirections(currentUserLocation, destLatLng) // Trigger fetching directions
    //     }
    // }
}
