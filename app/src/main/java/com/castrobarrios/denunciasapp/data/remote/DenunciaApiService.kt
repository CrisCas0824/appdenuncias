package com.castrobarrios.denunciasapp.data.remote

import com.castrobarrios.denunciasapp.data.model.Denuncia
import com.castrobarrios.denunciasapp.data.model.DenunciaRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DenunciaApiService {

    @POST("denuncias/")
    suspend fun registrarDenuncia(
        @Body denuncia: DenunciaRequest
    ): Response<Denuncia>

    @GET("denuncias/")
    suspend fun getDenunciasPaginadas(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int = 5
    ): Response<List<Denuncia>>

    @GET("denuncias/{id}")
    suspend fun getDenunciaById(
        @Path("id") id: Int
    ): Response<Denuncia>

    @GET("denuncias/ciudadano/{dni}")
    suspend fun getDenunciasByDni(
        @Path("dni") dni: String
    ): Response<List<Denuncia>>

    @PUT("denuncias/{id}")
    suspend fun atenderDenuncia(
        @Path("id") id: Int,
        @Body body: Map<String, Int> = mapOf("estado" to 0)
    ): Response<Denuncia>
}
