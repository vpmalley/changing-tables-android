package fr.vpm.changingtables.ui.map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.compass.compass
import fr.vpm.changingtables.R
import fr.vpm.changingtables.databinding.FragmentMapBinding
import fr.vpm.changingtables.models.Business
import fr.vpm.changingtables.viewmodels.BusinessViewModel

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null

    private val mapViewModel: MapViewModel by activityViewModels()
    private val businessViewModel: BusinessViewModel by activityViewModels()

    private var mapManager = MapManager()
    private lateinit var businessDetailsBottomSheet: BusinessDetailsBottomSheet
    private lateinit var businessFormBottomSheet: BusinessFormBottomSheet

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

        setupFilterChips()
        setupTopBarBelowStatusBar()
        setupCompassBelowTopBar()
        mapManager.setupPointAnnotationManager(
            binding.mapView.annotations
        ) { business ->
            businessFormBottomSheet.hide()
            businessDetailsBottomSheet.showBusiness(business)
        }
        mapManager.setupAddingBusiness(binding.mapView) { point ->
            businessDetailsBottomSheet.hide()
            businessFormBottomSheet.showNewBusiness(point)
        }

        mapViewModel.visibleBusinesses(businessViewModel.businesses)
            .observe(viewLifecycleOwner, ::onBusinesses)
    }

    private fun setupFilterChips() {
        val typeChips = mapOf(
            binding.chipCoffee to MapFilter.TYPE_COFFEE,
            binding.chipRestaurant to MapFilter.TYPE_RESTAURANT,
            binding.chipActivity to MapFilter.TYPE_ACTIVITY,
        )
        typeChips.forEach { (chip, type) ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                mapViewModel.setTypeSelected(type, isChecked)
            }
        }
        binding.chipSaved.setOnCheckedChangeListener { _, isChecked ->
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
