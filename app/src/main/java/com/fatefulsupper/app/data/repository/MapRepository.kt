package com.fatefulsupper.app.data.repository

// You might need to define these data classes or use library-specific ones (e.g., from Google Maps SDK)
// data class LatLngPoint(val latitude: Double, val longitude: Double)
// data class RouteDetails(val polyline: String, val duration: String, val distance: String)

interface MapRepository {

    /**
     * Fetches route details between an origin and a destination.
     * This would typically involve calling a directions API (e.g., Google Directions API).
     *
     * @param origin The starting point (latitude, longitude).
     * @param destination The ending point (latitude, longitude).
     * @return RouteDetails containing information like polyline for drawing, duration, distance, etc.
     *         Returns null if the route cannot be fetched.
     */
    // suspend fun getDirections(origin: LatLngPoint, destination: LatLngPoint): RouteDetails?

    // You might also add other map-related functionalities, such as:
    // suspend fun geocodeAddress(address: String): LatLngPoint?
    // suspend fun reverseGeocode(point: LatLngPoint): String? // Get address from coordinates
    // suspend fun searchNearbyPlaces(location: LatLngPoint, type: String, radius: Int): List<Place>?
}
