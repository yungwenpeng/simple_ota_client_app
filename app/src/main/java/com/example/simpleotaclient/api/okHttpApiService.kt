package com.example.simpleotaclient.api

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.simpleotaclient.CustomExpandableListAdapter
import com.example.simpleotaclient.api.ServerConfig.API_URL
import com.example.simpleotaclient.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

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
                    Toast.makeText(context, "Failed to check for update", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()

                if (responseData != null) {
                    Log.d("OkHttpApiService", responseData)
                    if (responseData == "{}") {
                        // Response data is empty
                        (context as Activity).runOnUiThread {
                            Toast.makeText(context, "Failed to check for update. No data available.", Toast.LENGTH_SHORT).show()
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
                                val adapter = parent.expandableListAdapter as CustomExpandableListAdapter
                                val childData = adapter.getChild(groupPosition, childPosition) as Map<String, String>
                                val fileName = childData["line1"]
                                Log.d("OkHttpApiService", "Clicked fileName: $fileName")
                                if (fileName != null) {
                                    downloadOtaPackage(fileName)
                                }
                                true // Return true if the click is handled
                            }
                        }
                    }
                }
            }
        })
    }

    // /otaApi/download?filename=testing-0001.zip
    fun downloadOtaPackage(filename: String) {
        val request = Request.Builder()
            .url("$API_URL/otaApi/download?filename=$filename")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                (context as Activity).runOnUiThread {
                    Toast.makeText(context, "onFailure: Failed to download. No data available.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("OkHttpApiService", "onResponse: response:$response")
                if (response.isSuccessful) {
                    val responseBody = response.body
                    if (responseBody != null) {
                        saveOtaPackage(responseBody, filename)
                    }
                } else {
                    Log.d("OkHttpApiService", "onResponse: body:$response.body")
                    (context as Activity).runOnUiThread {
                        Toast.makeText(context, "response : Failed to download. No data available.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun saveOtaPackage(responseBody: ResponseBody, filename: String) {
        Log.d("OkHttpApiService", "saveOtaPackage: filename:$filename")
        // External Storage Directory
        //saveOtaPackageToExternal(responseBody, filename)

        // For Android Studio Emulator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveOtaPackageInMediaStore(responseBody, filename)
        } else {
            saveOtaPackageInExternalStorage(responseBody, filename)
        }
    }

    // External Storage Directory
    private fun saveOtaPackageToExternal(responseBody: ResponseBody, filename: String) {
        val directory = File(Environment.getExternalStorageDirectory().absolutePath)
        val file = File(directory, filename)

        try {
            val inputStream = responseBody.byteStream()
            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(4096)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            (context as Activity).runOnUiThread {
                Toast.makeText(context, "Download successfully.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            (context as Activity).runOnUiThread {
                Toast.makeText(context, "IOException : Failed to download. No data available.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // For Android Studio Emulator SDK >= 29
    private fun saveOtaPackageInMediaStore(responseBody: ResponseBody, filename: String) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        var outputStream: OutputStream? = null

        try {
            val contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            val uri = resolver.insert(contentUri, contentValues)

            if (uri != null) {
                outputStream = resolver.openOutputStream(uri)
                responseBody.byteStream().use { inputStream ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream?.write(buffer, 0, bytesRead)
                    }
                }

                (context as Activity).runOnUiThread {
                    Toast.makeText(context, "Download successfully.", Toast.LENGTH_SHORT).show()
                }
            } else {
                (context as Activity).runOnUiThread {
                    Toast.makeText(context, "Failed to insert.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: IOException) {
            (context as Activity).runOnUiThread {
                Toast.makeText(context, "Failed to download. No data available.", Toast.LENGTH_SHORT).show()
            }
        } finally {
            outputStream?.close()
        }
    }

    // For Android Studio Emulator SDK < 29
    private fun saveOtaPackageInExternalStorage(responseBody: ResponseBody, filename: String) {
        val downloadsDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDirectory, filename)

        try {
            val outputStream = FileOutputStream(file)
            responseBody.byteStream().use { inputStream ->
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
            }
            outputStream.close()

            (context as Activity).runOnUiThread {
                Toast.makeText(context, "Download successfully.", Toast.LENGTH_SHORT).show()
            }

        } catch (e: IOException) {
            e.printStackTrace()

            (context as Activity).runOnUiThread {
                Toast.makeText(context, "Failed to download. No data available.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}