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
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
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

//        val airplaneBitmap =
//            bitmapFromDrawableRes(R.drawable.ic_airplanemode_active_black_24dp)
        // create a symbol
//        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
//            .withPoint(Point.fromLngLat(AIRPORT_LONGITUDE, AIRPORT_LATITUDE))
//            .withIconImage(airplaneBitmap)
//            .withTextField(ID_ICON_AIRPORT)
//            .withTextOffset(listOf(0.0, -2.0))
//            .withTextColor(Color.RED)
//            .withIconSize(1.3)
//            .withIconOffset(listOf(0.0, -5.0))
//            .withSymbolSortKey(10.0)
//            .withDraggable(true)
//        pointAnnotation = create(pointAnnotationOptions)

        drawMarkers(pointAnnotationManager)

        // random add symbols across the globe
//        val pointAnnotationOptionsList = List(25) {
//            PointAnnotationOptions()
//                .withPoint(AnnotationUtils.createRandomPoint())
//                .withIconImage(airplaneBitmap)
//                .withDraggable(true)
//        }
//        create(pointAnnotationOptionsList)
//        lifecycleScope.launch {
//            val featureCollection = withContext(Dispatchers.Default) {
//                FeatureCollection.fromJson(
//                    AnnotationUtils.loadStringFromAssets(
//                        this@PointAnnotationActivity,
//                        "annotations.json"
//                    )
//                )
//            }
//            create(featureCollection)
//        }
    }

    private fun showBusiness(business: Business?) {
        val businessBottomSheet = _binding?.businessBottomSheet
        val bottomSheetLayout: LinearLayout? = businessBottomSheet?.businessLayout
        val bottomSheetBehavior =
            bottomSheetLayout?.let { BottomSheetBehavior.from<LinearLayout?>(bottomSheetLayout) }
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
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
        val greenChangingTable =
            MapUtils().bitmapFromDrawableRes(
                requireContext(),
                R.drawable.baby_changing_station_green_24
            )
        val jjBeanCambie = Business().apply {
            name = "JJ Bean Cambie"
            type = "cafe"
            longitude = -123.1154027480853
            latitude = 49.2551275385386
            hasChangingTable = true
        }

        // create nearby symbols
        val nearbyOptions: PointAnnotationOptions = PointAnnotationOptions()
            .withPoint(Point.fromLngLat(jjBeanCambie.longitude, jjBeanCambie.latitude))
            .withIconImage(greenChangingTable)
            .withIconSize(1.0)
            .withSymbolSortKey(5.0)
            .withData(gson.toJsonTree(jjBeanCambie))
        //            .withDraggable(true)
        pointAnnotationManager.create(nearbyOptions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}