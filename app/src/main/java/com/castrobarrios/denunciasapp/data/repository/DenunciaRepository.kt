package com.castrobarrios.denunciasapp.data.repository

import com.castrobarrios.denunciasapp.data.model.Denuncia
import com.castrobarrios.denunciasapp.data.model.DenunciaRequest
import com.castrobarrios.denunciasapp.data.remote.DenunciaApiService
import com.castrobarrios.denunciasapp.data.remote.RetrofitClient
import retrofit2.Response

class DenunciaRepository(
    private val apiService: DenunciaApiService = RetrofitClient.apiService
) {
    suspend fun getDenunciasPaginadas(offset: Int, limit: Int = 5): Response<List<Denuncia>> {
        return apiService.getDenunciasPaginadas(offset = offset, limit = limit)
    }

    suspend fun getDenunciasByDni(dni: String): Response<List<Denuncia>> {
        return apiService.getDenunciasByDni(dni = dni)
    }

    suspend fun getDenunciaById(id: Int): Response<Denuncia> {
        return apiService.getDenunciaById(id = id)
    }

    suspend fun registrarDenuncia(request: DenunciaRequest): Response<Denuncia> {
        return apiService.registrarDenuncia(request)
    }

    suspend fun atenderDenuncia(id: Int): Response<Denuncia> {
        return apiService.atenderDenuncia(id = id)
    }
}
