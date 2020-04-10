package com.droidhats.campuscompass.views

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Button
import android.widget.ToggleButton
import androidx.activity.addCallback
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.droidhats.mapprocessor.ProcessMap
import com.caverock.androidsvg.SVG
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.models.Building
import com.droidhats.campuscompass.models.IndoorLocation
import com.droidhats.campuscompass.models.OutdoorNavigationRoute
import com.droidhats.campuscompass.viewmodels.FloorViewModel
import com.droidhats.campuscompass.viewmodels.MapViewModel
import com.mancj.materialsearchbar.MaterialSearchBar
import com.otaliastudios.zoom.ZoomImageView
import kotlinx.android.synthetic.main.search_bar_layout.mapFragSearchBar
import java.io.InputStream


class FloorFragment : Fragment() {

    private lateinit var viewModel: FloorViewModel
    private lateinit var viewModelMapViewModel: MapViewModel
    private lateinit var root: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.floor_fragment, container, false)

        var floorNum: String? = arguments?.getString("floornum")
        var mapToDisplay: String = "hall8.svg" // default value
        val building : Building? = arguments?.getParcelable("building")
        var floormap : String? = arguments?.getString("floormap")
        if (floorNum == null && building != null) {
            floorNum = building.getIndoorInfo().second.keys.first()
        }
        if (floormap == null && building != null) {
            floormap = building.getIndoorInfo().second[floorNum]
        }

        viewModelMapViewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)

        var maps : MutableList<String> = mutableListOf()
        if (building != null) {
            for (ting in building?.getIndoorInfo()?.second!!.values) {
                maps.add(ting)
            }
        }

        if(!floormap.isNullOrBlank()) {
            mapToDisplay = floormap
        }

        var inputStream: InputStream = requireContext().assets.open(mapToDisplay)
        val mapProcessor: ProcessMap = ProcessMap()
        var file: String = inputStream.bufferedReader().use { it.readText() }
        file = mapProcessor.automateSVG(file, floorNum!!)

        val buildingToHighlight: String? = arguments?.getString("id")

        if (buildingToHighlight == null) {
            val svg: SVG = SVG.getFromString(file)
            setImage(svg)
        } else {
            val highlightedSVG = mapProcessor.highlightClassroom(file, buildingToHighlight)
            val svg: SVG = SVG.getFromString(highlightedSVG)
            setImage(svg)
        }

        val buttonPlus: ImageButton = root.findViewById(R.id.button_plus)
        var newSvg : SVG
        buttonPlus.setOnClickListener(View.OnClickListener {
            val indexOfCurrentMap = maps?.indexOf(mapToDisplay)
            if(indexOfCurrentMap != maps?.size?.minus(1) && indexOfCurrentMap !=null) {
                if (maps != null && indexOfCurrentMap != null) {
                    mapToDisplay = maps[indexOfCurrentMap.plus(1)]
                }
                inputStream = requireContext().assets.open(mapToDisplay)
                newSvg = SVG.getFromInputStream(inputStream)
                setImage(newSvg)
            }
        })

        val buttonMinus: ImageButton = root.findViewById(R.id.button_minus)
        buttonMinus.setOnClickListener(View.OnClickListener {
            val indexOfCurrentMap = maps?.indexOf(mapToDisplay)
            if(indexOfCurrentMap != 0 && indexOfCurrentMap !=null) {
                if (maps != null && indexOfCurrentMap != null) {
                    mapToDisplay = maps[indexOfCurrentMap.minus(1)]
                }
                inputStream = requireContext().assets.open(mapToDisplay)
                newSvg = SVG.getFromInputStream(inputStream)
                setImage(newSvg)
            }
        })

        return root
    }

    fun setImage(svg: SVG) {
        val imageView: ZoomImageView = root.findViewById(R.id.floormap)
        val displayMetrics = DisplayMetrics()
        (context as Activity?)!!.windowManager
            .defaultDisplay
            .getMetrics(displayMetrics)

        svg.setDocumentWidth(displayMetrics.widthPixels.toFloat())
        val bitmap = Bitmap.createBitmap(displayMetrics.widthPixels, displayMetrics.widthPixels, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawARGB(0, 255, 255, 255)
        svg.renderToCanvas(canvas)
        imageView.setImageDrawable(BitmapDrawable(getResources(), bitmap))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FloorViewModel::class.java)


        initSearchBar()

        var startAndEnd = viewModel.getDirections()
        if (startAndEnd?.first?.lID == "") {
            startAndEnd = Pair(startAndEnd.second, startAndEnd.second)
        }
        if (startAndEnd != null) {
            val inputStream: InputStream = requireContext().assets.open(startAndEnd.first.floorMap)
            val file: String = inputStream.bufferedReader().use { it.readText() }
            val mapProcessor: ProcessMap = ProcessMap()
            val newFile = mapProcessor.automateSVG(file, startAndEnd.first.floorNum)
            mapProcessor.readSVGFromString(newFile)
            val svg: SVG = SVG.getFromString(mapProcessor
                .getSVGStringFromDirections(Pair(startAndEnd.first.lID, startAndEnd.second.lID)))
            setImage(svg)
        }

        val doneButton: Button = requireActivity().findViewById(R.id.doneButtonFloor)
        doneButton.setOnClickListener {
            viewModel.consumeNavHandler()
            println("hiiii")
        }

        viewModel.navigationRepository?.getNavigationRoute()?.observe(viewLifecycleOwner, Observer {
            println("helloooo")
            println(it)
            println(it is OutdoorNavigationRoute)
            println(it?.origin is IndoorLocation)
            if (it is OutdoorNavigationRoute) {
                findNavController().navigate(R.id.map_fragment)
            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            viewModel.navigationRepository?.stepBack()
        }
    }

    private fun initSearchBar() {
        var toggleButton = root.findViewById<ToggleButton>(R.id.toggleButton)
        toggleButton.visibility = View.GONE

        mapFragSearchBar.setOnSearchActionListener(object : MaterialSearchBar.OnSearchActionListener{

            override fun onButtonClicked(buttonCode: Int) {
                when(buttonCode) {
                    //Open the Nav Bar
                    MaterialSearchBar.BUTTON_NAVIGATION -> requireActivity().
                        findViewById<DrawerLayout>(R.id.drawer_layout).openDrawer(GravityCompat.START)
                }
            }
            override fun onSearchStateChanged(enabled: Boolean) {
                if (enabled) {
                    findNavController().navigate(R.id.search_fragment)
                    mapFragSearchBar.closeSearch()
                }
            }
            override fun onSearchConfirmed(text: CharSequence?) {
            }
        })
    }
}
