package fr.vpm.changingtables.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.compass.compass
import fr.vpm.changingtables.R
import fr.vpm.changingtables.databinding.FragmentMapBinding
import fr.vpm.changingtables.location.UserLocationProvider
import fr.vpm.changingtables.models.Business
import fr.vpm.changingtables.viewmodels.BusinessViewModel
import kotlinx.coroutines.launch

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null

    private val mapViewModel: MapViewModel by activityViewModels()
    private val businessViewModel: BusinessViewModel by activityViewModels()

    private var mapManager = MapManager()
    private lateinit var businessDetailsBottomSheet: BusinessDetailsBottomSheet
    private lateinit var businessFormBottomSheet: BusinessFormBottomSheet

    private val userLocationProvider = UserLocationProvider()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) centerOnUserLocation()
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        businessDetailsBottomSheet = BusinessDetailsBottomSheet(
            binding.businessDetailsBottomSheet,
            mapManager
        )
        businessDetailsBottomSheet.setupBottomSheet()
        businessDetailsBottomSheet.onSaveListener = { business ->
            if (business.savedOnDevice != null) {
                business.savedOnDevice = null
                businessViewModel.addBusiness(business)
            } else {
                business.savedOnDevice = System.currentTimeMillis()
                businessViewModel.addBusiness(business)
            }
        }

        businessFormBottomSheet = BusinessFormBottomSheet(
            binding.businessFormBottomSheet,
            businessViewModel,
            mapManager
        )
        businessFormBottomSheet.setupBottomSheet()
        
        // Hide FAB when business form bottom sheet is shown
        val formBottomSheetBehavior = BottomSheetBehavior.from(binding.businessFormBottomSheet.businessFormLayout)
        formBottomSheetBehavior.addBottomSheetCallback(object : com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED,
                    BottomSheetBehavior.STATE_COLLAPSED,
                    BottomSheetBehavior.STATE_DRAGGING,
                    BottomSheetBehavior.STATE_SETTLING -> {
                        binding.fabAddBusiness.hide()
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.fabAddBusiness.show()
                    }
                    else -> {}
                }
            }
            
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        setupFilterChips()
        setupTopBarBelowStatusBar()
        setupCompassBelowTopBar()
        setupFab()
        mapManager.setupPointAnnotationManager(
            binding.mapView.annotations,
            binding.mapView
        ) { business ->
            businessFormBottomSheet.hide()
            businessDetailsBottomSheet.showBusiness(business)
        }
        mapManager.setupAddingBusiness(binding.mapView) { point ->
            businessDetailsBottomSheet.hide()
            businessFormBottomSheet.showNewBusiness(point)
        }
        mapManager.setupInteractionTracking(binding.mapView)

        mapViewModel.visibleBusinesses(businessViewModel.businesses)
            .observe(viewLifecycleOwner, ::onBusinesses)
    }

    override fun onResume() {
        super.onResume()
        centerOnUserLocation()
    }

    private fun centerOnUserLocation() {
        if (!userLocationProvider.hasLocationPermission(requireContext())) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                } else {
                    // Check if permanently denied (don't ask again)
                    val prefs = requireContext().getSharedPreferences("location_prefs", android.content.Context.MODE_PRIVATE)
                    val hasAskedBefore = prefs.getBoolean("location_permission_asked", false)
                    if (hasAskedBefore) {
                        // Permanently denied — don't ask again
                        mapManager.hideLocationPuck()
                        return
                    }
                    prefs.edit().putBoolean("location_permission_asked", true).apply()
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
            return
        }
        if (!userLocationProvider.isLocationEnabled(requireContext())) {
            mapManager.hideLocationPuck()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val location = userLocationProvider.getLocation(requireContext())
            if (location != null) {
                mapManager.showLocationPuck(requireContext(), location.latitude, location.longitude)
                if (!mapManager.isUserInteracting()) {
                    mapManager.centerOnLocation(binding.mapView, location.latitude, location.longitude)
                }
            } else {
                mapManager.hideLocationPuck()
            }
        }
    }

    private fun setupFilterChips() {
        val typeChips = mapOf(
            binding.chipCoffee to MapFilter.TYPE_COFFEE,
            binding.chipRestaurant to MapFilter.TYPE_RESTAURANT,
            binding.chipActivity to MapFilter.TYPE_ACTIVITY,
        )
        // The checkmark is drawn in the chip icon's slot, so the icon is hidden while checked
        typeChips.forEach { (chip, type) ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                chip.isChipIconVisible = !isChecked
                mapViewModel.setTypeSelected(type, isChecked)
            }
        }
        binding.chipSaved.setOnCheckedChangeListener { _, isChecked ->
            binding.chipSaved.isChipIconVisible = !isChecked
            mapViewModel.setSavedOnly(isChecked)
        }

        mapViewModel.filter.observe(viewLifecycleOwner) { filter ->
            typeChips.forEach { (chip, type) ->
                val isSelected = type in filter.types
                if (chip.isChecked != isSelected) chip.isChecked = isSelected
            }
            if (binding.chipSaved.isChecked != filter.savedOnly) {
                binding.chipSaved.isChecked = filter.savedOnly
            }
        }
    }

    /**
     * The map is drawn behind the status bar (edge-to-edge from Android 15), so the top bar
     * is padded to stay below it.
     */
    private fun setupTopBarBelowStatusBar() {
        val basePaddingTop = binding.mapTopBar.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(binding.mapTopBar) { topBar, insets ->
            val statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            topBar.updatePadding(top = basePaddingTop + statusBarTop)
            insets
        }
    }

    private fun setupCompassBelowTopBar() {
        val compassSpacing = resources.getDimensionPixelSize(R.dimen.margin_thin)
        binding.mapTopBar.addOnLayoutChangeListener { topBar, _, _, _, _, _, _, _, _ ->
            val marginTop = (topBar.bottom + compassSpacing).toFloat()
            if (binding.mapView.compass.marginTop != marginTop) {
                binding.mapView.compass.marginTop = marginTop
            }
        }
    }

    private fun setupFab() {
        binding.fabAddBusiness.setOnClickListener {
            // Clear any existing new business markers
            mapManager.clearNewBusinessMarker()
            
            // Create a draggable marker at the map center
            mapManager.createDraggableMarker(
                requireContext(),
                binding.mapView,
                onDrop = { point ->
                    // When the user drops the marker, show the business form
                    businessDetailsBottomSheet.hide()
                    businessFormBottomSheet.showNewBusiness(point)
                },
                onCancel = {
                    // When the user cancels (drags off-screen), just clean up
                    // The marker is already cleared by createDraggableMarker
                }
            )
            
            // Show a snackbar to instruct the user to drag and drop the marker
            Snackbar.make(binding.fabAddBusiness, "Drag and drop the marker to add a business at that location", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun onBusinesses(businesses: List<Business>?) {
        Log.d("businessViewModel", "all businesses to display are : $businesses")
        mapManager.showBusinesses(requireContext(), businesses)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapManager.onDestroyView()
        _binding = null
    }
}
