package fr.vpm.changingtables.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
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

        val bottomSheetLayout: LinearLayout? = _binding?.businessBottomSheet?.businessLayout
        val bottomSheetBehavior =
            bottomSheetLayout?.let { BottomSheetBehavior.from<LinearLayout?>(bottomSheetLayout) }

        val annotationApi = binding.mapView.annotations
        val pointAnnotationManager = annotationApi.createPointAnnotationManager()
        pointAnnotationManager.addClickListener(
            OnPointAnnotationClickListener {
                it.getData()?.let {
                    val business = gson.fromJson(it, Business::class.java)

                    bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                    _binding?.businessBottomSheet?.bottomSheetTitle?.text = business?.name
                    _binding?.businessBottomSheet?.changingTableDescription?.text =
                        if (business?.hasChangingTable == true) {
                            "There is a changing table at this business"
                        } else {
                            "No changing table"
                        }
                }
                true
            }
        )

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

    private fun drawMarkers(pointAnnotationManager: PointAnnotationManager) {
        val greenChangingTable =
            MapUtils().bitmapFromDrawableRes(
                requireContext(),
                R.drawable.baby_changing_station_green_24
            )
        val jjBeanCambie = Business().apply {
            name = "JJ Bean Cambie"
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