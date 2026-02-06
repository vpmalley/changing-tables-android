package fr.vpm.changingtables.ui.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.annotations
import fr.vpm.changingtables.R
import fr.vpm.changingtables.databinding.FragmentHomeBinding
import fr.vpm.changingtables.models.Business
import fr.vpm.changingtables.models.Option
import fr.vpm.changingtables.models.Question
import fr.vpm.changingtables.viewmodels.BusinessViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val homeViewModel: HomeViewModel by activityViewModels()
    private val businessViewModel: BusinessViewModel by activityViewModels()

    private var mapManager = MapManager()

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

        setupBottomSheet()
        setupFilterFab()
        mapManager.setupPointAnnotationManager(binding.mapView.annotations, ::showBusiness)
        mapManager.setupAddingBusiness(binding.mapView, ::showNewBusiness)

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

    private fun setupBottomSheet() {
        val businessBottomSheet = binding.businessBottomSheet
        val bottomSheetLayout = businessBottomSheet.businessLayout
        val behavior = BottomSheetBehavior.from(bottomSheetLayout)
        val initialPeekHeight = behavior.peekHeight

        val cornerRadius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            28f,
            resources.displayMetrics
        )

        val shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setTopLeftCorner(CornerFamily.ROUNDED, cornerRadius)
            .setTopRightCorner(CornerFamily.ROUNDED, cornerRadius)
            .build()

        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true)
        val colorSurface = typedValue.data

        val backgroundDrawable = MaterialShapeDrawable(shapeAppearanceModel).apply {
            fillColor = ColorStateList.valueOf(colorSurface)
        }
        bottomSheetLayout.background = backgroundDrawable

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    mapManager.clearNewBusinessMarker()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val parentHeight = bottomSheet.parent.let { (it as View).height }.toFloat()
                if (parentHeight > 0) {
                    // Interpolation: 0 at the top (square), 1 when far from top (rounded)
                    // Animation happens when the sheet is within 2 * cornerRadius from the top
                    val threshold = cornerRadius * 2
                    val interpolation = (bottomSheet.top.toFloat() / threshold).coerceIn(0f, 1f)
                    backgroundDrawable.interpolation = interpolation
                }

                if (behavior.state == BottomSheetBehavior.STATE_DRAGGING) {
                    val currentHeight = parentHeight.toInt() - bottomSheet.top
                    behavior.peekHeight = currentHeight.coerceAtLeast(initialPeekHeight)
                }
            }
        })
    }

    private fun showBusiness(business: Business?) {
        mapManager.clearNewBusinessMarker()
        val businessBottomSheet = _binding?.businessBottomSheet
        businessBottomSheet?.businessLayout?.visibility = View.VISIBLE
        val bottomSheetLayout: ConstraintLayout? = businessBottomSheet?.businessLayout
        val bottomSheetBehavior =
            bottomSheetLayout?.let { BottomSheetBehavior.from<ConstraintLayout?>(bottomSheetLayout) }
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED

        businessBottomSheet?.businessAddPrompt?.visibility = View.GONE
        businessBottomSheet?.businessTitleNew?.visibility = View.INVISIBLE
        businessBottomSheet?.businessTitle?.visibility = View.VISIBLE
        businessBottomSheet?.changingTableDescription?.visibility = View.VISIBLE
        businessBottomSheet?.addBusinessButton?.visibility = View.GONE
        businessBottomSheet?.addBusinessButton?.isEnabled = false

        businessBottomSheet?.businessTitle?.text = business?.name
        businessBottomSheet?.businessDescription?.text = business?.description ?: "Coffee shop"
        businessBottomSheet?.businessRating?.rating = business?.ratingAsFloat ?: 0f
        businessBottomSheet?.businessRating?.numStars = 5
        if (business?.hasChangingTable == "Yes") {
            businessBottomSheet?.changingTableDescription?.let {
                it.visibility = View.VISIBLE
                it.text = "There is a changing table here"
                TextViewCompat.setCompoundDrawableTintList(
                    it, ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.green)
                    )
                )
            }
        } else if (business?.hasChangingTable == "OutOfService") {
            businessBottomSheet?.changingTableDescription?.let {
                it.visibility = View.VISIBLE
                it.text = "Changing table is out of service"
                TextViewCompat.setCompoundDrawableTintList(
                    it, ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.purple_500)
                    )
                )
            }
        } else {
            businessBottomSheet?.changingTableDescription?.visibility = View.VISIBLE
            businessBottomSheet?.changingTableDescription?.text = "No changing table"
            businessBottomSheet?.changingTableDescription?.let {
                TextViewCompat.setCompoundDrawableTintList(
                    it, ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.black)
                    )
                )
            }
        }
    }

    private fun showNewBusiness(point: Point) {
        mapManager.clearNewBusinessMarker()
        mapManager.showNewBusinessMarker(requireContext(), point)
        val businessBottomSheet = _binding?.businessBottomSheet
        businessBottomSheet?.businessLayout?.visibility = View.VISIBLE
        val bottomSheetLayout: ConstraintLayout? = businessBottomSheet?.businessLayout
        val bottomSheetBehavior =
            bottomSheetLayout?.let { BottomSheetBehavior.from<ConstraintLayout?>(bottomSheetLayout) }
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED

        businessBottomSheet?.businessAddPrompt?.visibility = View.VISIBLE
        businessBottomSheet?.businessTitleNew?.visibility = View.VISIBLE
        businessBottomSheet?.businessTitle?.visibility = View.GONE
        businessBottomSheet?.changingTableDescription?.visibility = View.GONE
        businessBottomSheet?.addBusinessButton?.visibility = View.VISIBLE
        businessBottomSheet?.addBusinessButton?.isEnabled = false

        val changingTableQuestion = Question().apply {
            titleResId = R.string.changing_table_question
            options = listOf(
                Option("yes", R.string.all_yes),
                Option("no", R.string.all_no),
                Option("out_of_service", R.string.changing_table_out_of_service)
            )
            singleChoice = true
        }
        businessBottomSheet?.changingTableQuestionWithChips?.setQuestion(
            changingTableQuestion,
            com.google.android.material.R.style.Widget_Material3_Chip_Suggestion
        )
        businessBottomSheet?.changingTableQuestionWithChips?.setOnChipSelectedListener { selectedChipIds: List<Int>, selectedChipTexts: List<String>, selectedChipTags: List<String?> ->
            businessBottomSheet.addBusinessButton.isEnabled = true
            if (selectedChipTags.any { it == "yes" }) {
                val changingTableLocationQuestion = Question().apply {
                    titleResId = R.string.changing_table_location_question
                    options = listOf(
                        Option("unisex", R.string.changing_table_unisex_toilet),
                        Option("male", R.string.changing_table_male_toilet),
                        Option("female", R.string.changing_table_female_toilet),
                        Option("accessible", R.string.changing_table_accessible_toilet),
                        Option("other_room", R.string.changing_table_other_place)
                    )
                    singleChoice = false
                }
                businessBottomSheet.changingTableLocationQuestionWithChips.setQuestion(
                    changingTableLocationQuestion,
                    com.google.android.material.R.style.Widget_Material3_Chip_Filter
                )
                businessBottomSheet.changingTableLocationQuestionWithChips.visibility = View.VISIBLE
            } else {
                businessBottomSheet.changingTableLocationQuestionWithChips.visibility = View.GONE
            }
        }
        businessBottomSheet?.addBusinessButton?.setOnClickListener {
            val newBusiness = Business().apply {
                name = businessBottomSheet.businessTitleNew.editText?.text?.toString()
//                description = business?.description
                // find the location
                longitude = point.longitude()
                latitude = point.latitude()
                rating = -1
//                type = business?.type

                hasChangingTable =
                    businessBottomSheet.changingTableQuestionWithChips.getSelectedChipTexts()
                        .firstOrNull()
                changingTableLocation =
                    businessBottomSheet.changingTableLocationQuestionWithChips.getSelectedChipTexts()
                        .firstOrNull()
            }
            businessViewModel.addBusiness(newBusiness)
            val businessName = newBusiness.name ?: "New business"
            Snackbar.make(binding.root, "$businessName added", Snackbar.LENGTH_LONG).show()

            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            mapManager.clearNewBusinessMarker()
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