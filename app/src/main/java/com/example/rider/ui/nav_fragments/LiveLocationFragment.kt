package com.example.rider.ui.nav_fragments

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.rider.R
import com.example.rider.databinding.FragmentLiveLocationBinding
import com.example.rider.utils.showShortToast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.RouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.RouteLayerConstants.TOP_LEVEL_ROUTE_LINE_LAYER_ID
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources
import com.mapbox.navigation.ui.tripprogress.api.MapboxTripProgressApi
import com.mapbox.navigation.ui.tripprogress.model.DistanceRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.EstimatedTimeToArrivalFormatter
import com.mapbox.navigation.ui.tripprogress.model.TimeRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.TripProgressUpdateFormatter

class LiveLocationFragment() : DialogFragment() {
    private var binding: FragmentLiveLocationBinding? = null

    private val mapboxNavigation by lazy {
        if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            MapboxNavigationProvider.create(
                NavigationOptions.Builder(requireContext().applicationContext)
                    .accessToken(getString(R.string.mapbox_access_token))
                    .locationEngine(replayLocationEngine)
                    .build()
            )
        }
    }

    private lateinit var hardCodedRoute: DirectionsRoute
    private var moveCamToStudent = false

    private val mapboxReplayer = MapboxReplayer()
    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)
    private val replayLocationEngine = ReplayLocationEngine(mapboxReplayer)

    private val mapboxMap: MapboxMap? by lazy {
        binding?.mapSelectLocation?.getMapboxMap()
    }

    private val navigationLocationProvider by lazy {
        NavigationLocationProvider()
    }

    private val locationComponent by lazy {
        binding?.mapSelectLocation?.location?.apply {
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }
    }

    private val routeLineResources: RouteLineResources by lazy {
        RouteLineResources.Builder()
            .routeLineColorResources(RouteLineColorResources.Builder().build())
            .build()
    }

    private val options: MapboxRouteLineOptions by lazy {
        MapboxRouteLineOptions.Builder(requireContext())
            .withVanishingRouteLineEnabled(true)
            .withRouteLineResources(routeLineResources)
            .withRouteLineBelowLayerId("road-label")
            .build()
    }

    private val routeLineView by lazy {
        MapboxRouteLineView(options)
    }

    private val routeLineApi: MapboxRouteLineApi by lazy {
        MapboxRouteLineApi(options)
    }

    private val routeArrowApi: MapboxRouteArrowApi by lazy {
        MapboxRouteArrowApi()
    }

    private val routeArrowOptions by lazy {
        RouteArrowOptions.Builder(requireContext())
            .withAboveLayerId(TOP_LEVEL_ROUTE_LINE_LAYER_ID)
            .build()
    }

    private val routeArrowView: MapboxRouteArrowView by lazy {
        MapboxRouteArrowView(routeArrowOptions)
    }

    private val routesObserver: RoutesObserver = RoutesObserver { routeUpdateResult ->
        val routeLines = routeUpdateResult.routes.map { RouteLine(it, null) }
        routeLineApi.setRoutes(
            routeLines
        ) { value ->
            mapboxMap?.getStyle()?.apply {
                routeLineView.renderRouteDrawData(this, value)
            }
        }
    }

    private val onPositionChangedListener = OnIndicatorPositionChangedListener { point ->
        val result = routeLineApi.updateTraveledRouteLine(point)
        mapboxMap?.getStyle()?.apply {
            routeLineView.renderRouteLineUpdate(this, result)
        }
    }

    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        routeLineApi.updateWithRouteProgress(routeProgress) { result ->
            mapboxMap?.getStyle()?.apply {
                routeLineView.renderRouteLineUpdate(this, result)
            }
        }
        val arrowUpdate = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
        mapboxMap?.getStyle()?.apply {
            routeArrowView.renderManeuverUpdate(this, arrowUpdate)
        }
        val tripProgress = tripProgressApi.getTripProgress(routeProgress)
        binding?.tripProgressView?.render(tripProgress)
    }

    private val tripProgressFormatter: TripProgressUpdateFormatter by lazy {
        val distanceFormatterOptions =
            DistanceFormatterOptions.Builder(requireContext().applicationContext).build()
        TripProgressUpdateFormatter.Builder(requireContext())
            .distanceRemainingFormatter(DistanceRemainingFormatter(distanceFormatterOptions))
            .timeRemainingFormatter(TimeRemainingFormatter(requireContext()))
            .estimatedTimeToArrivalFormatter(EstimatedTimeToArrivalFormatter(requireContext()))
            .build()
    }

    private val tripProgressApi: MapboxTripProgressApi by lazy {
        MapboxTripProgressApi(tripProgressFormatter)
    }

    private val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: Location) {}
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            navigationLocationProvider.changePosition(
                enhancedLocation,
                locationMatcherResult.keyPoints,
            )

            if (!moveCamToStudent)
                updateCamera(
                    Point.fromLngLat(enhancedLocation.longitude, enhancedLocation.latitude),
                    enhancedLocation.bearing.toDouble()
                )

            if (requireArguments()["is_student"] == false) {
                Firebase.auth.currentUser?.uid?.let { uid ->
                    val userDocRef = Firebase.firestore.collection("users")
                        .document(uid)
                    userDocRef.update("latitude", enhancedLocation.latitude)
                        .continueWithTask {
                            userDocRef.update("longitude", enhancedLocation.longitude)
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLiveLocationBinding.inflate(
            layoutInflater,
            null,
            false
        )
        return binding?.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {
            "Playing mock navigation from departure to arrival location".showShortToast(
                requireContext()
            )

            fetchARoute()

            if (requireArguments()["is_student"] == true) {
                moveCamToStudent = true
            }

            binding?.btnMoveCamToStudent?.setOnClickListener {
                moveCamToStudent = true
                updateCamera(arrivalPoint, 10.0)
            }

            binding?.btnMoveCamToVolunteer?.setOnClickListener {
                moveCamToStudent = false
            }

        } else {
            "Please grant location permission".showShortToast(requireContext())
        }
    }

    @SuppressLint("MissingPermission")
    private fun initStyle() {
        mapboxMap?.loadStyleUri(
            Style.MAPBOX_STREETS,
            {
                updateCamera(departurePoint, 10.0)
                initNavigation()
                initListeners()
            },
            object : OnMapLoadErrorListener {
                override fun onMapLoadError(eventData: MapLoadingErrorEventData) {
                    Log.e(
                        LiveLocationFragment::class.java.simpleName,
                        "Error loading map: " + eventData.message
                    )
                }
            }
        )
    }

    @SuppressLint("MissingPermission")
    private fun initNavigation() {
        mapboxNavigation.run {
            setRoutes(listOf(hardCodedRoute))
            registerRoutesObserver(routesObserver)
            registerLocationObserver(locationObserver)
            registerRouteProgressObserver(routeProgressObserver)
            registerRouteProgressObserver(replayProgressObserver)
            startTripSession()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initListeners() {
        locationComponent?.addOnIndicatorPositionChangedListener(onPositionChangedListener)
        mapboxMap?.getStyle()?.apply {
            routeLineView.hideAlternativeRoutes(this)
        }
        startSimulation(hardCodedRoute)
    }

    private fun fetchARoute() {
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
                    hardCodedRoute = routes[0]
                    initStyle()
                }

                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
                    // todo
                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    // todo
                }
            }
        )
    }

    private fun updateCamera(point: Point, bearing: Double?) {
        val mapAnimationOptionsBuilder = MapAnimationOptions.Builder()
        binding?.mapSelectLocation?.camera?.easeTo(
            CameraOptions.Builder()
                .center(point)
                .bearing(bearing)
                .pitch(45.0)
                .zoom(16.0)
                .padding(EdgeInsets(800.0, 0.0, 0.0, 0.0))
                .build(),
            mapAnimationOptionsBuilder.build()
        )
    }

    @SuppressLint("MissingPermission")
    private fun startSimulation(route: DirectionsRoute) {
        mapboxReplayer.run {
            stop()
            clearEvents()
            pushRealLocation(requireContext(), 0.0)
            val replayEvents = ReplayRouteMapper().mapDirectionsRouteGeometry(route)
            pushEvents(replayEvents)
            seekTo(replayEvents.first())
            play()
        }
    }

    override fun onResume() {
        dialog?.window?.attributes = dialog?.window?.attributes?.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
        super.onResume()
    }

    override fun onDestroyView() {
        binding = null
        locationComponent?.removeOnIndicatorPositionChangedListener(onPositionChangedListener)
        mapboxNavigation.run {
            stopTripSession()
            unregisterRoutesObserver(routesObserver)
            unregisterLocationObserver(locationObserver)
            unregisterRouteProgressObserver(routeProgressObserver)
            unregisterRouteProgressObserver(replayProgressObserver)
        }
        mapboxReplayer.finish()
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
            vararg latLongs: Double
        ): LiveLocationFragment {
            return LiveLocationFragment().apply {
                arguments = Bundle().apply {
                    putDouble("arr_lat", latLongs[0])
                    putDouble("arr_long", latLongs[1])
                    putDouble("dep_lat", latLongs[2])
                    putDouble("dep_long", latLongs[3])
                    putBoolean("is_student", isStudent)
                }
            }
        }
    }
}