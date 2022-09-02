package com.rteslenko.android.map

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.maps.android.clustering.ClusterManager
import com.rteslenko.android.map.databinding.ActivityMapsBinding
import com.rteslenko.android.map.model.Item
import com.rteslenko.android.map.utils.GoogleMapHelper.getVisibleBounds
import com.rteslenko.android.map.utils.GoogleMapHelper.moveCameraToKyiv
import com.rteslenko.android.map.utils.GoogleMapHelper.setMapStyleFromRes
import com.rteslenko.android.map.utils.GoogleMapHelper.setupItemClusterManager

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val viewModel: MapViewModel by viewModels()
    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var clusterManager: ClusterManager<Item>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyEdgeToEdge()
        setupGoogleMap()
        loadMarkersData()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap.apply {
            viewModel.padding.observe(this@MapsActivity) { padding ->
                setPadding(0, padding.first, 0, padding.second)
            }
            uiSettings.isMapToolbarEnabled = false
            setMapStyleFromRes(this@MapsActivity, R.raw.retro_map_style)
            setMinZoomPreference(5f)
            moveCameraToKyiv()
            clusterManager = setupItemClusterManager(this@MapsActivity)
            setOnCameraIdleListener(clusterManager)
            setOnCameraMoveListener {
                viewModel.getDataForRegion(map.getVisibleBounds())
            }
        }
        observeMarkersData()
    }

    /**
     * Apply edge-to-edge content and collect insets to apply corresponding map padding
     */
    private fun applyEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val paddingTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val paddingBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            viewModel.notifyPaddingChanged(paddingTop, paddingBottom)
            insets
        }
    }

    /**
     * Obtain the SupportMapFragment and get notified when the map is ready to be used
     */
    private fun setupGoogleMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Load markers data from csv file
     */
    private fun loadMarkersData() {
        viewModel.loadItems(this)
    }

    /**
     * Get notified on data loading completion and
     * update cluster manager after markers data for visible map region received
     */
    private fun observeMarkersData() {
        viewModel.isDataReady.observe(this) { isDataReady ->
            if (isDataReady) {
                viewModel.getDataForRegion(map.getVisibleBounds())
            }
        }
        viewModel.regionData.observe(this) { items ->
            clusterManager.clearItems()
            clusterManager.addItems(items)
            clusterManager.cluster()
        }
    }
}