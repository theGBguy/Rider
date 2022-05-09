package com.yatra.yatraapp.ui.nav_fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.removeOnMapClickListener
import com.mapbox.search.*
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.yatra.yatraapp.R
import com.yatra.yatraapp.databinding.FragmentSelectLocationBinding
import com.yatra.yatraapp.utils.bitmapFromDrawableRes
import com.yatra.yatraapp.utils.showShortToast

class SelectLocationFragment private constructor() : DialogFragment(),
    LocationEngineCallback<LocationEngineResult>,
    PermissionsListener,
    OnMapClickListener {

    private var binding: FragmentSelectLocationBinding? = null
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var locationEngine: LocationEngine
    private lateinit var mapboxMap: MapboxMap
    private lateinit var currentPoint: Point
    private lateinit var currentPointAnnotation: PointAnnotation
    private lateinit var pointAnnotationManager: PointAnnotationManager

    private lateinit var searchEngine: SearchEngine
    private lateinit var reverseGeocoding: ReverseGeocodingSearchEngine
    private lateinit var searchRequestTask: SearchRequestTask

    // callback for reverse geocoding
    private val reverseSearchCallback = object : SearchCallback {
        override fun onResults(
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {
            if (results.isEmpty()) {
                Log.d("SelectionLocation", "No reverse geocoding results")
            } else {
                Log.d("SelectionLocation", "Reverse geocoding results: $results")
                if (requireArguments()[KEY_TYPE] == true) {
                    parentFragmentManager.setFragmentResult(
                        "departure",
                        bundleOf(
                            "departure_location" to getReadableSearchResult(results[0]),
                            "departure_lat" to currentPoint.latitude(),
                            "departure_long" to currentPoint.longitude()
                        )
                    )
                } else {
                    parentFragmentManager.setFragmentResult(
                        "arrival",
                        bundleOf(
                            "arrival_location" to getReadableSearchResult(results[0]),
                            "arrival_lat" to currentPoint.latitude(),
                            "arrival_long" to currentPoint.longitude()
                        )
                    )
                }
            }
            dismiss()
        }

        override fun onError(e: Exception) {
            Log.i("SelectionLocation", "Reverse geocoding error", e)
        }
    }

    // callback for forward geocoding
    private val forwardSearchCallback = object : SearchSelectionCallback {
        override fun onSuggestions(
            suggestions: List<SearchSuggestion>,
            responseInfo: ResponseInfo
        ) {
            if (suggestions.isEmpty()) {
                "No suggestions found".showShortToast(requireContext())
            } else {
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.select_dialog_item,
                    suggestions.map { it.name }
                )

                binding?.actvSelectLocation?.setAdapter(adapter)
                binding?.actvSelectLocation?.setOnItemClickListener { _, _, pos, _ ->
                    searchRequestTask = searchEngine.select(suggestions[pos], this)
                }
            }
        }

        override fun onResult(
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo
        ) {
//            location = result.name
//            latitude = result.coordinate?.latitude()!!
//            longitude = result.coordinate?.longitude()!!
//            "$location $latitude $longitude".showShortToast(this@RegisterActivity)
            onMapClick(
                Point.fromLngLat(
                    result.coordinate?.longitude()!!,
                    result.coordinate?.latitude()!!
                )
            )
        }

        override fun onCategoryResult(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {
            Log.i("SearchApiExample", "Category search results: $results")
        }

        override fun onError(e: Exception) {
            Log.i("SearchApiExample", "Search error", e)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectLocationBinding.inflate(
            layoutInflater,
            null,
            false
        )
        return binding?.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapboxMap = binding?.mapSelectLocation?.getMapboxMap()!!
        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
            mapboxMap.addOnMapClickListener(this)
            pointAnnotationManager =
                binding?.mapSelectLocation?.annotations?.createPointAnnotationManager()!!
        }

        if (requireArguments()[KEY_TYPE] == true) {
            // changing visibility of autocomplete textview based on whether this fragment is
            // loaded select departure or arrival location; making it visible only in the case of
            // selection of departure location
            binding?.actvSelectLocation?.visibility = View.VISIBLE

            searchEngine = MapboxSearchSdk.getSearchEngine()
            binding?.actvSelectLocation?.threshold = 3
            binding?.actvSelectLocation?.doOnTextChanged { text, _, _, _ ->
                searchRequestTask = searchEngine.search(
                    text?.trim().toString(),
                    SearchOptions(limit = 3),
                    forwardSearchCallback
                )
            }
        }

        reverseGeocoding = MapboxSearchSdk.getReverseGeocodingSearchEngine()
        binding?.btnSelectLocation?.setOnClickListener {
            if (this::currentPoint.isInitialized) {
                val options = ReverseGeoOptions(
                    center = currentPoint,
                    limit = 1
                )
                searchRequestTask = reverseGeocoding.search(options, reverseSearchCallback)
            } else {
                "Please select location first".showShortToast(requireContext())
            }
        }

        if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {
            locationEngine = LocationEngineProvider.getBestLocationEngine(requireContext())
            val request = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                .build()
            locationEngine.requestLocationUpdates(request, this, Looper.getMainLooper())
            locationEngine.getLastLocation(this)
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions((requireParentFragment() as? StudentHomeFragment)?.requireActivity())
        }
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        "Please grant location permission".showShortToast(requireContext())
    }

    @SuppressLint("MissingPermission")
    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            locationEngine = LocationEngineProvider.getBestLocationEngine(requireContext())
            val request = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                .build()
            locationEngine.requestLocationUpdates(request, this, Looper.getMainLooper())
            locationEngine.getLastLocation(this)
        } else {
            "Please grant the location permission to continue".showShortToast(requireContext())
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onResume() {
        dialog?.window?.attributes = dialog?.window?.attributes?.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
        super.onResume()
    }

    override fun onDestroyView() {
        if (this::searchRequestTask.isInitialized)
            searchRequestTask.cancel()
        binding?.mapSelectLocation?.getMapboxMap()?.removeOnMapClickListener(this)
        if (this::locationEngine.isInitialized)
            locationEngine.removeLocationUpdates(this)
        binding = null
        super.onDestroyView()
    }

    override fun onSuccess(result: LocationEngineResult?) {
        result?.lastLocation?.let {
            binding?.mapSelectLocation?.getMapboxMap()?.flyTo(
                CameraOptions.Builder()
                    .center(Point.fromLngLat(it.longitude, it.latitude))
                    .zoom(15.0)
                    .build(), null
            )
        }
    }

    override fun onFailure(e: Exception) {
        "Error occurred : ${e.message}".showShortToast(requireContext())
    }

    override fun onMapClick(point: Point): Boolean {
        binding?.mapSelectLocation?.getMapboxMap()?.flyTo(
            CameraOptions.Builder()
                .center(Point.fromLngLat(point.longitude(), point.latitude()))
                .zoom(15.0)
                .build(), null
        )
        currentPoint = point
        removeMarker()
        addMarker()
        return true
    }

    private fun removeMarker() {
        if (this::pointAnnotationManager.isInitialized && this::currentPointAnnotation.isInitialized) {
            pointAnnotationManager.delete(currentPointAnnotation)
        }
    }

    private fun addMarker() {
        if (this::pointAnnotationManager.isInitialized) {
            bitmapFromDrawableRes(
                requireContext(),
                R.drawable.icon_red_marker
            )?.let { marker ->
                currentPointAnnotation = pointAnnotationManager.create(
                    PointAnnotationOptions()
                        .withPoint(currentPoint)
                        .withIconImage(marker)
                )
            }
        }
    }

    companion object {
        const val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
        const val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
        const val KEY_TYPE = "KEY_TYPE"

        fun newInstance(isDeparture: Boolean): SelectLocationFragment {
            return SelectLocationFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(KEY_TYPE, isDeparture)
                }
            }
        }
    }
}

fun getReadableSearchResult(searchResult: SearchResult): String {
    return "${searchResult.name}, ${searchResult.address?.district}, ${searchResult.address?.country}"
}