package com.example.simpleotaclient

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ExpandableListView
import com.example.simpleotaclient.api.OkHttpApiService
import com.example.simpleotaclient.databinding.ActivityMainBinding
import com.example.simpleotaclient.websocket.MyWebSocketClient
import com.example.simpleotaclient.websocket.WebScoketServerConfig.WEBSOCKET_SERVER_URL
import java.net.URI

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var apiService: OkHttpApiService
    private lateinit var softwareUpdateExpandableListView: ExpandableListView
    private lateinit var softwareUpdateAdapter: CustomExpandableListAdapter
    private lateinit var webSocketClient: MyWebSocketClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Fetch current build information
        val currentExpandableListView = binding.listCurrentMain
        currentExpandableListView.setAdapter(configCurrentExpandableListView())

        // Fetch software update list
        apiService = OkHttpApiService(this, binding, ::configSoftwareUpdateExpandableListView)
        softwareUpdateExpandableListView = binding.listSoftwareUpdate
        softwareUpdateAdapter = configSoftwareUpdateExpandableListView()
        softwareUpdateExpandableListView.setAdapter(softwareUpdateAdapter)

        apiService.getOtaPackageInfo("all")

        val serverUri = URI(WEBSOCKET_SERVER_URL)
        webSocketClient = MyWebSocketClient(this, binding, apiService, serverUri)
        webSocketClient.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient.close()
    }

    private fun buildData(key: String, value: String): Map<String, String> {
        return mapOf("line1" to key, "line2" to value)
    }

    private fun configCurrentExpandableListView(): CustomExpandableListAdapter {
        val currentGroupData: List<Map<String, String>> = listOf(
            mapOf("groupTitle" to getString(R.string.about_build_info_title))
        )
        val currentChildData: List<List<Map<String, String>>> = listOf(
            listOf(
                buildData(getString(R.string.build_number_title), Build.DISPLAY),
                buildData(getString(R.string.android_version_title), Build.VERSION.RELEASE),
                buildData(getString(R.string.model_title), Build.MODEL),
                buildData(getString(R.string.incremental_title), Build.VERSION.INCREMENTAL),
                buildData(getString(R.string.fingerprint_title), Build.FINGERPRINT)
            )
        )
        return CustomExpandableListAdapter(currentGroupData, currentChildData)
    }

    private fun configSoftwareUpdateExpandableListView(): CustomExpandableListAdapter {
        val softwareUpdateGroupData: List<Map<String, String>> = listOf(
            mapOf("groupTitle" to getString(R.string.software_update_title))
        )
        val softwareUpdateChildData: MutableList<List<Map<String, String>>> = mutableListOf()
        Log.d("MainActivity", "softwareUpdateChildData: $softwareUpdateChildData")

        return CustomExpandableListAdapter(softwareUpdateGroupData, softwareUpdateChildData)
    }
}