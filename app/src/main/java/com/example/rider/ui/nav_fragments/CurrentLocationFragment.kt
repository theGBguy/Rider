package com.example.rider.ui.nav_fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.rider.R
import com.example.rider.databinding.AnnotationViewBinding
import com.example.rider.databinding.FragmentCurrentLocationBinding
import com.example.rider.utils.bitmapFromDrawableRes
import com.example.rider.utils.showShortToast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.RouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources

class CurrentLocationFragment private constructor() : DialogFragment() {

    private val navigationLocationProvider by lazy {
        NavigationLocationProvider()
    }

    private val mapboxMap: MapboxMap? by lazy {
        binding?.mapView?.getMapboxMap()
    }

    private val mapboxNavigation by lazy {
        if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            MapboxNavigationProvider.create(
                NavigationOptions.Builder(requireContext().applicationContext)
                    .accessToken(getString(R.string.mapbox_access_token))
                    .build()
            )
        }
    }

    private val routeLineResources by lazy {
        RouteLineResources.Builder()
            .routeLineColorResources(
                RouteLineColorResources.Builder()
                    .routeDefaultColor(Color.parseColor("#9A4057"))
                    .build()
            )
            .build()
    }

    private val options by lazy {
        MapboxRouteLineOptions.Builder(requireContext())
//            .withVanishingRouteLineEnabled(true)
            .withRouteLineResources(routeLineResources)
            .withRouteLineBelowLayerId("road-label")
            .build()
    }

    private val routeLineView by lazy {
        MapboxRouteLineView(options)
    }

    private val routeLineApi by lazy {
        MapboxRouteLineApi(options)
    }

    private val routesObserver = RoutesObserver { routeUpdateResult ->
        val routeLines = routeUpdateResult.routes.map { RouteLine(it, null) }
        routeLineApi.setRoutes(
            routeLines
        ) { value ->
            mapboxMap?.getStyle()?.apply {
                routeLineView.renderRouteDrawData(this, value)
            }
        }
    }

    private val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: Location) {
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            with(locationMatcherResult.enhancedLocation) {
                navigationLocationProvider.changePosition(
                    this,
                    locationMatcherResult.keyPoints,
                )
                if (moveCamToNextPersonLocation == false)
                    updateCamera(this.longitude, this.latitude)

                removeMarker(true)
                addMarker(true, Point.fromLngLat(this.longitude, this.latitude))

                Firebase.auth.currentUser?.uid?.let { uid ->
                    val userDocRef = Firebase.firestore.collection("users")
                        .document(uid)
                    userDocRef.update("latitude", this.latitude)
                        .continueWithTask {
                            userDocRef.update("longitude", this.longitude)
                        }.addOnCompleteListener {
                            if (it.isSuccessful) {
                                Log.d("Live Location", "User coordinates updated successfully")
                            } else {
                                "Error updating user coordinates : ${it.exception?.message}".showShortToast(
                                    requireContext()
                                )
                            }
                        }
                }
            }
        }
    }

    private val departurePoint by lazy {
        Point.fromLngLat(
            requireArguments()["dep_long"] as Double,
            requireArguments()["dep_lat"] as Double
        )
    }

    private val arrivalPoint by lazy {
        Point.fromLngLat(
            requireArguments()["arr_long"] as Double,
            requireArguments()["arr_lat"] as Double
        )
    }

    private lateinit var currentUserLocationMarker: PointAnnotation
    private lateinit var nextUserLocationMarker: PointAnnotation
    private val pointAnnotationManager by lazy {
        binding?.mapView?.annotations?.createPointAnnotationManager()
    }

    private var moveCamToNextPersonLocation: Boolean? = null

    private val isStudent by lazy {
        requireArguments()["is_student"] as Boolean
    }

//    private lateinit var currentRoute: DirectionsRoute

    private var binding: FragmentCurrentLocationBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCurrentLocationBinding.inflate(layoutInflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {
            initializeMap()
        } else {
            "Please grant location permission".showShortToast(requireContext())
        }
    }

    override fun onResume() {
        dialog?.window?.attributes = dialog?.window?.attributes?.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
        super.onResume()
    }

    private fun initializeMap() {
        initStyle()
        initListeners()
    }

    private fun initStyle() {

        mapboxMap?.loadStyleUri(Style.MAPBOX_STREETS) { style ->
            updateCamera(departurePoint.longitude(), departurePoint.latitude())

            binding?.mapView?.location?.apply {
                setLocationProvider(navigationLocationProvider)
//                locationPuck = LocationPuck2D(
//                    bearingImage = ContextCompat.getDrawable(
//                        requireContext(),
//                        R.drawable.icon_red_marker
//                    )
//                )
                enabled = false
//                pulsingEnabled = true
//                pulsingColor = resources.getColor(R.color.md_theme_light_primary)
//                pulsingMaxRadius = 24f
            }
            fetchRoute()
            routeLineView.hideAlternativeRoutes(style)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initNavigation(route: DirectionsRoute) {
        mapboxNavigation.apply {
            setRoutes(listOf(route))
            startTripSession()
            registerLocationObserver(locationObserver)
            registerRoutesObserver(routesObserver)
        }
    }

    private fun initListeners() {
        (requireArguments()["next_person_uid"] as String).let { uid ->
            Firebase.firestore.collection("users")
                .document(uid)
                .addSnapshotListener { value, error ->
                    value?.let { snapshot ->
                        val latitude = snapshot.get("latitude") as Double
                        val longitude = snapshot.get("longitude") as Double
                        removeMarker(false)
                        addMarker(false, Point.fromLngLat(longitude, latitude))
                    }
                    error?.let { e ->
                        Log.d(
                            "LiveLocation",
                            "Error occurred while reading live location : ${e.message}"
                        )
                    }
                }
        }

        pointAnnotationManager?.addClickListener { clickedAnnotation ->
            if (clickedAnnotation == currentUserLocationMarker) {
//                "Current Student Location".showShortToast(requireContext())
                addViewAnnotation(true, currentUserLocationMarker.point)
            } else {
//                "Current Volunteer Location".showShortToast(requireContext())
                addViewAnnotation(false, nextUserLocationMarker.point)
            }
            true
        }

        moveCamToNextPersonLocation = requireArguments()["is_student"] as Boolean?

        binding?.btnSeeNextPersonLocation?.apply {
            text = if (isStudent) "See Volunteer's Location" else "See Student's Location"
        }?.setOnClickListener {
            moveCamToNextPersonLocation = true
            updateCamera(
                nextUserLocationMarker.point.longitude(),
                nextUserLocationMarker.point.latitude()
            )
        }

        binding?.btnSeeMyLocation?.setOnClickListener {
            moveCamToNextPersonLocation = false
        }
    }

    @OptIn(MapboxExperimental::class)
    private fun addViewAnnotation(isCurrentUser: Boolean, point: Point) {
        try {
            val view = binding?.mapView?.viewAnnotationManager?.addViewAnnotation(
                resId = R.layout.annotation_view,
                options = viewAnnotationOptions {
                    geometry(point)
//                    associatedFeatureId(
//                        if (isCurrentUser)
//                            currentUserLocationMarker.featureIdentifier
//                        else
//                            nextUserLocationMarker.featureIdentifier
//                    )
//                    anchor(ViewAnnotationAnchor.BOTTOM)
//                    offsetY((currentUserLocationMarker.iconImageBitmap?.height!!).toInt())
                }
            )
            if (view != null) {
                val annotationViewBinding = AnnotationViewBinding.bind(view)
                annotationViewBinding.btnClose.setOnClickListener {
                    binding?.mapView?.viewAnnotationManager?.removeViewAnnotation(view)
                }
                annotationViewBinding.tvMsg.text = if (isCurrentUser) "Your location" else {
                    if (isStudent) "Volunteer's location" else "Student's location"
                }
            }
        } catch (e: MapboxViewAnnotationException) {
            Log.e("CurrentLocationFragment", "Error occurred : ${e.message}")
            "Close the dialog first".showShortToast(requireContext())
        } catch (e: Exception) {
            Log.e("CurrentLocationFragment", "Error occurred : ${e.message}")
        }
    }

    private fun updateCamera(longitude: Double, latitude: Double) {
        val mapAnimationOptions = MapAnimationOptions.Builder().duration(1500L).build()
        binding?.mapView?.camera?.easeTo(
            CameraOptions.Builder()
                .center(Point.fromLngLat(longitude, latitude))
                .zoom(14.0)
                .padding(EdgeInsets(500.0, 0.0, 0.0, 0.0))
                .build(),
            mapAnimationOptions
        )
    }

    @SuppressLint("SetTextI18n")
    private fun fetchRoute() {
        "Fetching route...".showShortToast(requireContext())

        val routeOptions = RouteOptions.builder()
            .applyDefaultNavigationOptions()
            .applyLanguageAndVoiceUnitOptions(requireContext())
            .coordinatesList(listOf(departurePoint, arrivalPoint))
            .alternatives(false)
            .bearingsList(
                listOf(
                    Bearing.builder()
                        .angle(10.0)
                        .degrees(45.0)
                        .build(),
                    null
                )
            )
            .build()

        mapboxNavigation.requestRoutes(
            routeOptions,
            object : RouterCallback {
                override fun onRoutesReady(
                    routes: List<DirectionsRoute>,
                    routerOrigin: RouterOrigin
                ) {
//                    val routeLines = routes.map { route ->
//                        RouteLine(route, null)
//                    }
//                    routeLineApi.setRoutes(routeLines) { value ->
//                        routeLineView.renderRouteDrawData(style, value)
//                    }
                    initNavigation(routes[0])
                }

                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
                    "Route request canceled".showShortToast(requireContext())
                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    "route request failed with $reasons".showShortToast(requireContext())
                }
            }
        )
    }

    private fun removeMarker(isCurrentUser: Boolean) {
        if (isCurrentUser) {
            if (this::currentUserLocationMarker.isInitialized) {
                pointAnnotationManager?.delete(currentUserLocationMarker)
            }
        } else {
            if (this::nextUserLocationMarker.isInitialized) {
                pointAnnotationManager?.delete(nextUserLocationMarker)
            }
        }
    }

    private fun addMarker(isCurrentUser: Boolean, point: Point) {
        bitmapFromDrawableRes(
            requireContext(),
            R.drawable.icon_red_marker
        )?.let { marker ->
            if (isCurrentUser) {
                currentUserLocationMarker = pointAnnotationManager?.create(
                    PointAnnotationOptions()
                        .withPoint(point)
                        .withIconImage(marker)
                )!!
            } else {
                nextUserLocationMarker = pointAnnotationManager?.create(
                    PointAnnotationOptions()
                        .withPoint(point)
                        .withIconImage(marker)
                )!!
            }
        }
    }


    override fun onDestroyView() {
        binding = null
        mapboxNavigation.stopTripSession()
        mapboxNavigation.unregisterLocationObserver(locationObserver)
        mapboxNavigation.unregisterRoutesObserver(routesObserver)
        routeLineView.cancel()
        routeLineApi.cancel()
        mapboxNavigation.onDestroy()
        super.onDestroyView()
    }

    companion object {
//        val pointKtm = Point.fromLngLat(85.300140, 27.700769)
//        val pointPkr = Point.fromLngLat(83.959518, 28.209499)

        fun newInstance(
            isStudent: Boolean,
            nextPersonUId: String,
            vararg latLongs: Double
        ): CurrentLocationFragment {
            return CurrentLocationFragment().apply {
                arguments = Bundle().apply {
                    putDouble("arr_lat", latLongs[0])
                    putDouble("arr_long", latLongs[1])
                    putDouble("dep_lat", latLongs[2])
                    putDouble("dep_long", latLongs[3])
                    putBoolean("is_student", isStudent)
                    putString("next_person_uid", nextPersonUId)
                }
            }
        }
    }
}