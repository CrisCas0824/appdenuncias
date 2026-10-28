package com.castrobarrios.denunciasapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.castrobarrios.denunciasapp.data.model.Denuncia
import com.castrobarrios.denunciasapp.data.model.DenunciaRequest
import com.castrobarrios.denunciasapp.data.repository.DenunciaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DenunciaUiState(
    val denuncias: List<Denuncia> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userMessage: String? = null,
    val currentOffset: Int = 0,
    val isSearching: Boolean = false,
    val searchDni: String = "",
    val hasMorePages: Boolean = true,
    val showRegisterDialog: Boolean = false,
    val isRegistering: Boolean = false,
    val registerError: String? = null,
    val isAttending: Boolean = false,
    val selectedDenuncia: Denuncia? = null
)

class DenunciaViewModel(
    private val repository: DenunciaRepository = DenunciaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DenunciaUiState())
    val uiState: StateFlow<DenunciaUiState> = _uiState.asStateFlow()

    init {
        loadInitialDenuncias()
    }

    fun loadInitialDenuncias() {
        loadPage(0)
    }

    private fun loadPage(offset: Int) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    isSearching = false
                )
            }
            try {
                val response = repository.getDenunciasPaginadas(offset = offset, limit = 5)
                if (response.isSuccessful) {
                    val list = response.body() ?: emptyList()
                    _uiState.update {
                        if (list.isEmpty() && offset > 0) {
                            it.copy(isLoading = false, hasMorePages = false)
                        } else {
                            it.copy(
                                denuncias = list,
                                currentOffset = offset,
                                isLoading = false,
                                hasMorePages = list.size == 5
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error en el servidor: ${response.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error de conexión: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun loadMoreDenuncias() {
        val currentState = _uiState.value
        if (!currentState.isSearching && currentState.hasMorePages) {
            loadPage(currentState.currentOffset + 5)
        }
    }

    fun loadPreviousDenuncias() {
        val state = _uiState.value
        if (!state.isSearching && state.currentOffset > 0) {
            loadPage(state.currentOffset - 5)
        }
    }

    fun refreshDenuncias() {
        if (_uiState.value.isSearching) buscarPorDni()
        else loadPage(_uiState.value.currentOffset)
    }

    fun onSearchDniChange(dni: String) {
        if (dni.length <= 8 && dni.all { it.isDigit() }) {
            _uiState.update { it.copy(searchDni = dni) }
        }
    }

    fun buscarPorDni() {
        val dni = _uiState.value.searchDni.trim()
        if (dni.length != 8) {
            _uiState.update { it.copy(errorMessage = "El DNI debe contener exactamente 8 dígitos numéricos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSearching = true) }
            try {
                val response = repository.getDenunciasByDni(dni)
                if (response.isSuccessful) {
                    val list = response.body() ?: emptyList()
                    _uiState.update {
                        it.copy(
                            denuncias = list,
                            isLoading = false,
                            userMessage = if (list.isEmpty()) "No se encontraron denuncias para el DNI: $dni" else "Resultados para DNI: $dni"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al buscar por DNI (${response.code()})"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error de conexión al buscar por DNI: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun limpiarBusqueda() {
        _uiState.update { it.copy(searchDni = "") }
        loadInitialDenuncias()
    }

    fun registrarDenuncia(
        dni: String,
        tipo: String,
        direccion: String,
        descripcion: String
    ) {
        if (_uiState.value.isRegistering) return
        if (dni.trim().length != 8 || !dni.trim().all { it.isDigit() }) {
            _uiState.update { it.copy(registerError = "El DNI debe tener 8 dígitos numéricos") }
            return
        }
        if (tipo.isBlank()) {
            _uiState.update { it.copy(registerError = "Seleccione un tipo de denuncia") }
            return
        }
        if (direccion.isBlank()) {
            _uiState.update { it.copy(registerError = "La dirección no puede estar vacía") }
            return
        }
        if (descripcion.isBlank()) {
            _uiState.update { it.copy(registerError = "La descripción no puede estar vacía") }
            return
        }

        val request = DenunciaRequest(
            dni = dni.trim(),
            tipo = tipo.trim(),
            direccion = direccion.trim(),
            descripcion = descripcion.trim(),
            estado = 1
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isRegistering = true, registerError = null) }
            try {
                val response = repository.registrarDenuncia(request)
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            showRegisterDialog = false,
                            isRegistering = false,
                            userMessage = "Denuncia registrada correctamente"
                        )
                    }
                    loadInitialDenuncias()
                } else {
                    _uiState.update {
                        it.copy(
                            isRegistering = false,
                            registerError = "No se pudo registrar la denuncia (HTTP ${response.code()}). Revise los datos e intente de nuevo."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRegistering = false,
                        registerError = "Error de conexión al registrar: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun atenderDenuncia(id: Int) {
        if (_uiState.value.isAttending) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAttending = true, errorMessage = null) }
            try {
                val response = repository.atenderDenuncia(id)
                if (response.isSuccessful) {
                    val updatedList = _uiState.value.denuncias.map { d ->
                        if (d.id == id) d.copy(estado = 0) else d
                    }
                    val updatedSelected = _uiState.value.selectedDenuncia?.let {
                        if (it.id == id) it.copy(estado = 0) else it
                    }
                    _uiState.update {
                        it.copy(
                            denuncias = updatedList,
                            selectedDenuncia = updatedSelected,
                            isAttending = false,
                            userMessage = "La denuncia #$id ha sido marcada como ATENDIDA"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isAttending = false,
                            errorMessage = "Error al atender la denuncia (${response.code()})"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAttending = false,
                        errorMessage = "Error de red al atender denuncia: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun openDetail(denuncia: Denuncia) {
        _uiState.update { it.copy(selectedDenuncia = denuncia) }
        // Fetch fresh detail from GET denuncias/{id} if id is present
        denuncia.id?.let { id ->
            viewModelScope.launch {
                try {
                    val response = repository.getDenunciaById(id)
                    if (response.isSuccessful) {
                        response.body()?.let { freshDenuncia ->
                            _uiState.update { state ->
                                if (state.selectedDenuncia?.id == id) {
                                    state.copy(selectedDenuncia = freshDenuncia)
                                } else state
                            }
                        }
                    }
                } catch (_: Exception) { }
            }
        }
    }

    fun closeDetail() {
        if (!_uiState.value.isAttending) {
            _uiState.update { it.copy(selectedDenuncia = null) }
        }
    }

    fun openRegister() {
        _uiState.update { it.copy(showRegisterDialog = true, registerError = null) }
    }

    fun closeRegister() {
        if (!_uiState.value.isRegistering) {
            _uiState.update { it.copy(showRegisterDialog = false, registerError = null) }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
