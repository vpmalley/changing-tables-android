package fr.vpm.changingtables.ui.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.mapbox.maps.plugin.annotation.annotations
import fr.vpm.changingtables.R
import fr.vpm.changingtables.databinding.FragmentHomeBinding
import fr.vpm.changingtables.models.Business
import fr.vpm.changingtables.viewmodels.BusinessViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val homeViewModel: HomeViewModel by activityViewModels()
    private val businessViewModel: BusinessViewModel by activityViewModels()

    private var mapManager = MapManager()

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

        mapManager.setupPointAnnotationManager(binding.mapView.annotations, ::showBusiness)

        businessViewModel.businesses.observe(viewLifecycleOwner, ::onBusinesses)
    }

    private fun showBusiness(business: Business?) {
        val businessBottomSheet = _binding?.businessBottomSheet
        businessBottomSheet?.businessLayout?.visibility = View.VISIBLE
        val bottomSheetLayout: ConstraintLayout? = businessBottomSheet?.businessLayout
        val bottomSheetBehavior =
            bottomSheetLayout?.let { BottomSheetBehavior.from<ConstraintLayout?>(bottomSheetLayout) }
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        businessBottomSheet?.businessTitle?.text = business?.name
        businessBottomSheet?.businessDescription?.text = business?.description ?: "Coffee shop"
        businessBottomSheet?.businessRating?.rating = business?.ratingAsFloat ?: 0f
        businessBottomSheet?.businessRating?.numStars = 5
        if (business?.hasChangingTable == true) {
            businessBottomSheet?.changingTableDescription?.let {
                it.text = "There is a changing table here"
                TextViewCompat.setCompoundDrawableTintList(
                    it, ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.green)
                    )
                )
            }
        } else {
            businessBottomSheet?.changingTableDescription?.text = "No changing table"
        }
        businessBottomSheet?.changingTableOptions?.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedChipId = checkedIds.first() // Get the first and only ID
                val selectedChip = group.findViewById<Chip>(selectedChipId)
                if (selectedChip.text.toString() == getString(R.string.all_yes)) {
                    businessBottomSheet.changingTableLocationQuestion.visibility = View.VISIBLE
                    businessBottomSheet.changingTableLocationOptions.visibility = View.VISIBLE
                } else {
                    businessBottomSheet.changingTableLocationQuestion.visibility = View.GONE
                    businessBottomSheet.changingTableLocationOptions.visibility = View.GONE
                }
            }
        }
    }


    private fun onBusinesses(businesses: List<Business>?) {
        mapManager.showBusinesses(requireContext(), businesses)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}