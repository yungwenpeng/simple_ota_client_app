package com.example.simpleotaclient.api

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.simpleotaclient.CustomExpandableListAdapter
import com.example.simpleotaclient.api.ServerConfig.API_URL
import com.example.simpleotaclient.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException

class OkHttpApiService(val context: Context, val binding: ActivityMainBinding,
                       val configFunction: () -> CustomExpandableListAdapter) {
    private val client = OkHttpClient().newBuilder().build()

    // /otaApi/packages?filename=all
    fun getOtaPackageInfo(filename: String) {
        val request = Request.Builder()
            .url("$API_URL/otaApi/packages?filename=$filename")
            .build()
        val call = client.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                (context as Activity).runOnUiThread {
                    Toast.makeText(context, "AAAA Failed to check for update", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()

                if (responseData != null) {
                    Log.d("OkHttpApiService", responseData)
                    if (responseData == "{}") {
                        // Response data is empty
                        (context as Activity).runOnUiThread {
                            Toast.makeText(context, "BBBB Failed to check for update. No data available.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val otaPackages = Gson().fromJson<List<OtaPackageInfo>>(
                            responseData,
                            object : TypeToken<List<OtaPackageInfo>>() {}.type
                        )

                        // Update SoftwareUpdateChildData with the response data
                        val softwareUpdateChildData: MutableList<Map<String, String>> = mutableListOf()
                        for (otaPackage in otaPackages) {
                            val fileName = otaPackage.fileName
                            val postBuild = otaPackage.postBuild
                            Log.d("OkHttpApiService", "fileName:$fileName, postBuild:$postBuild")

                            if (fileName != null && postBuild != null) {
                                val data: MutableMap<String, String> = HashMap()
                                data["line1"] = fileName
                                data["line2"] = postBuild
                                softwareUpdateChildData.add(data)
                            }
                        }

                        val childDataList: List<List<Map<String, String>>> = mutableListOf(softwareUpdateChildData)
                        (context as Activity).runOnUiThread {
                            // Pass the updated softwareUpdateChildData to the adapter
                            val softwareUpdateExpandableListView = binding.listSoftwareUpdate
                            val adapter = configFunction()
                            adapter.setChildData(childDataList)
                            softwareUpdateExpandableListView.setAdapter(adapter)

                            // Set click listener here
                            softwareUpdateExpandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
                                // Handle child item click
                                Log.d("OkHttpApiService", "softwareUpdateExpandableListView.setOnChildClickListener")
                                true // Return true if the click is handled
                            }
                        }
                    }
                }
            }
        })
    }
}