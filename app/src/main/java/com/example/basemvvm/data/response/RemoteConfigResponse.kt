package com.example.basemvvm.data.response

import com.google.gson.annotations.SerializedName

data class RemoteConfigResponse(
    @SerializedName("server")
    val server: String,
    @SerializedName("storage")
    val storage: String
) {

}