package com.castrobarrios.denunciasapp.data.model

import com.google.gson.annotations.SerializedName

data class DenunciaRequest(
    @SerializedName("dni") val dni: String,
    @SerializedName("tipo") val tipo: String,
    @SerializedName("direccion") val direccion: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("estado") val estado: Int = 1
)
