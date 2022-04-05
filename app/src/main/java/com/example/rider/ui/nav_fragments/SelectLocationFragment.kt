package com.example.rider.ui.nav_fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.example.rider.R
import com.example.rider.databinding.FragmentSelectLocationBinding
import com.example.rider.utils.showShortToast
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.removeOnMapClickListener
import com.mapbox.search.*
import com.mapbox.search.result.SearchResult

class SelectLocationFragment() : DialogFragment(),
    LocationEngineCallback<LocationEngineResult>,
    PermissionsListener,
    OnMapClickListener {

    private var binding: FragmentSelectLocationBinding? = null
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var locationEngine: LocationEngine
    private lateinit var currentPoint: Point

    private lateinit var reverseGeocoding: ReverseGeocodingSearchEngine
    private lateinit var searchRequestTask: SearchRequestTask

    private val searchCallback = object : SearchCallback {
        override fun onResults(
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {
            if (results.isEmpty()) {
                Log.d("SelectionLocation", "No reverse geocoding results")
            } else {
                Log.d("SelectionLocation", "Reverse geocoding results: $results")
                if (requireArguments()["KEY_TYPE"] == true) {
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
                            "arrival_location" to (getReadableSearchResult(results[0])),
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

        binding?.mapSelectLocation?.getMapboxMap()?.loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            binding?.mapSelectLocation?.getMapboxMap()?.addOnMapClickListener(this)
        }

        reverseGeocoding = MapboxSearchSdk.getReverseGeocodingSearchEngine()
        binding?.btnSelectLocation?.setOnClickListener {
            if (this::currentPoint.isInitialized) {
                val options = ReverseGeoOptions(
                    center = currentPoint,
                    limit = 1
                )
                searchRequestTask = reverseGeocoding.search(options, searchCallback)
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
        binding?.tvSelectedLocation?.text =
            "Selected Location\nLatitude ${point.latitude()}, Longitude ${point.longitude()}"
//        addMarker(point.latitude(), point.longitude())
        currentPoint = point
        return true
    }

    private fun removeMarker() {
        val annotationApi = binding?.mapSelectLocation?.annotations
        annotationApi?.cleanup()
    }

    private fun addMarker(lat: Double, long: Double) {
        bitmapFromDrawableRes(
            requireContext(),
            R.drawable.icon_red_marker
        )?.let {
            val annotationApi = binding?.mapSelectLocation?.annotations
            val pointAnnotationManager =
                annotationApi?.createPointAnnotationManager(binding?.mapSelectLocation!!)
            val pointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(lat, long))
                .withIconImage(it)
                .withIconSize(8.0)
            pointAnnotationManager?.create(pointAnnotationOptions)
        }
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    companion object {
        const val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
        const val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5

        fun newInstance(isDeparture: Boolean): SelectLocationFragment {
            return SelectLocationFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("KEY_TYPE", isDeparture)
                }
            }
        }
    }
}

fun getReadableSearchResult(searchResult: SearchResult): String {
    return "${searchResult.name}, ${searchResult.address?.district}, ${searchResult.address?.country}"
}