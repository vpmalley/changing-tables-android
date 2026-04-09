package fr.vpm.changingtables.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mapbox.maps.plugin.annotation.annotations
import fr.vpm.changingtables.databinding.FragmentHomeBinding
import fr.vpm.changingtables.models.Business
import fr.vpm.changingtables.viewmodels.BusinessViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val homeViewModel: HomeViewModel by activityViewModels()
    private val businessViewModel: BusinessViewModel by activityViewModels()

    private var mapManager = MapManager()
    private lateinit var businessBottomSheet: BusinessBottomSheet

    private var isFilterExpanded = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        businessBottomSheet = BusinessBottomSheet(
            binding.businessBottomSheet,
            businessViewModel,
            mapManager
        )
        businessBottomSheet.setupBottomSheet()

        setupFilterFab()
        mapManager.setupPointAnnotationManager(
            binding.mapView.annotations,
            businessBottomSheet::showBusiness
        )
        mapManager.setupAddingBusiness(binding.mapView, businessBottomSheet::showNewBusiness)

        businessViewModel.businesses.observe(viewLifecycleOwner, ::onBusinesses)
    }

    private fun setupFilterFab() {
        binding.fabFilter.setOnClickListener {
            toggleFilter()
        }
    }

    private fun toggleFilter() {
        isFilterExpanded = !isFilterExpanded
        if (isFilterExpanded) {
            expandFilter()
        } else {
            collapseFilter()
        }
    }

    private fun expandFilter() {
        binding.fabFilter.animate().rotation(45f).setDuration(300).start()
        binding.filterOptions.visibility = View.VISIBLE
        binding.filterOptions.alpha = 0f
        binding.filterOptions.translationX = 100f
        binding.filterOptions.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(300)
            .setListener(null)
    }

    private fun collapseFilter() {
        binding.fabFilter.animate().rotation(0f).setDuration(300).start()
        binding.filterOptions.animate()
            .alpha(0f)
            .translationX(100f)
            .setDuration(300)
            .withEndAction {
                binding.filterOptions.visibility = View.GONE
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