package fr.vpm.changingtables.ui.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import fr.vpm.changingtables.BusinessToPointAnnotation
import fr.vpm.changingtables.R
import fr.vpm.changingtables.databinding.FragmentHomeBinding
import fr.vpm.changingtables.models.Business
import fr.vpm.changingtables.ui.tools.MapUtils

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val gson = Gson().newBuilder().create()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val annotationApi = binding.mapView.annotations
        val pointAnnotationManager = annotationApi.createPointAnnotationManager()
        pointAnnotationManager.addClickListener { annotation ->
            val annotationData = annotation.getData()
            if (annotationData != null) {
                val business = gson.fromJson(annotationData, Business::class.java)
                showBusiness(business)
            }
            annotationData != null
        }

        drawMarkers(pointAnnotationManager)
    }

    private fun showBusiness(business: Business?) {
        val businessBottomSheet = _binding?.businessBottomSheet
        businessBottomSheet?.businessLayout?.visibility = View.VISIBLE
        val bottomSheetLayout: LinearLayout? = businessBottomSheet?.businessLayout
        val bottomSheetBehavior =
            bottomSheetLayout?.let { BottomSheetBehavior.from<LinearLayout?>(bottomSheetLayout) }
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        businessBottomSheet?.businessTitle?.text = business?.name
        businessBottomSheet?.businessDescription?.text = business?.description ?: "Coffee shop"
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
    }

    private fun drawMarkers(pointAnnotationManager: PointAnnotationManager) {
        val greenBusinessMarker =
            MapUtils().bitmapFromDrawableRes(
                requireContext(),
                R.drawable.pin_ok_teal_32dp
            )

        val allBusinessAnnotationOptions = listOf(Business().apply {
            name = "JJ Bean Cambie"
            type = "cafe"
            longitude = -123.1154027480853
            latitude = 49.2551275385386
            hasChangingTable = true
        }).map { business ->
            BusinessToPointAnnotation().toPointAnnotation(business)
                .withIconImage(greenBusinessMarker)
        }
        pointAnnotationManager.create(allBusinessAnnotationOptions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}