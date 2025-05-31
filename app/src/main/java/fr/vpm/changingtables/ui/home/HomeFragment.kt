package fr.vpm.changingtables.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import fr.vpm.changingtables.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val annotationApi = binding.mapView.annotations
        val pointAnnotationManager = annotationApi.createPointAnnotationManager()
        pointAnnotationManager.addClickListener(
            OnPointAnnotationClickListener {
                Toast.makeText(
                    requireContext(),
                    "Clicked on ${it.id}",
                    Toast.LENGTH_SHORT
                ).show()
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

//        blueBitmap = bitmapFromDrawableRes(R.drawable.mapbox_user_icon)
        // create nearby symbols
        val nearbyOptions: PointAnnotationOptions = PointAnnotationOptions()
            .withPoint(Point.fromLngLat(-123.1154027480853, 49.2551275385386))
//            .withIconImage(blueBitmap)
            .withIconSize(2.5)
            .withTextField("coffee")
            .withSymbolSortKey(5.0)
//            .withDraggable(true)
        pointAnnotationManager.create(nearbyOptions)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}