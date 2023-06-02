package com.example.simpleotaclient

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.simpleotaclient.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Fetch current build information
        val currentExpandableListView = binding.listCurrentMain
        currentExpandableListView.setAdapter(configCurrentExpandableListView())
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
}