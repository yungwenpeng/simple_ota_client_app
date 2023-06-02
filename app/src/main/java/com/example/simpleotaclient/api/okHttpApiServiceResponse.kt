package com.example.simpleotaclient.api

import com.google.gson.annotations.SerializedName

data class OtaPackageInfo(
    val fileName: String,
    @SerializedName("post-build")
    val postBuild: String,
    @SerializedName("post-build-incremental")
    val postBuildIncremental: String,
    @SerializedName("post-sdk-level")
    val postSdkLevel: String,
    @SerializedName("post-security-patch-level")
    val postSecurityPatchLevel: String,
    @SerializedName("post-timestamp")
    val postTimestamp: String,
    @SerializedName("pre-build")
    val preBuild: String
)