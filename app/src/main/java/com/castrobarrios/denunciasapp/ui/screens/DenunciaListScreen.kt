package com.castrobarrios.denunciasapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.castrobarrios.denunciasapp.data.model.Denuncia
import com.castrobarrios.denunciasapp.ui.theme.DenunciasAppTheme
import com.castrobarrios.denunciasapp.ui.viewmodel.DenunciaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DenunciaListScreen(
    viewModel: DenunciaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    BackHandler(enabled = uiState.selectedDenuncia != null) {
        viewModel.closeDetail()
    }

    LaunchedEffect(uiState.currentOffset) {
        listState.scrollToItem(0)
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.selectedDenuncia != null) "Detalle de denuncia" else "Sistema de Denuncias",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    if (uiState.selectedDenuncia != null) {
                        IconButton(onClick = { viewModel.closeDetail() }, enabled = !uiState.isAttending) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver a la lista")
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshDenuncias() },
                        enabled = !uiState.isLoading && uiState.selectedDenuncia == null
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar denuncias")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (uiState.selectedDenuncia == null) FloatingActionButton(
                onClick = { viewModel.openRegister() },
                modifier = Modifier.offset(y = (-80).dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Registrar Denuncia")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.selectedDenuncia != null) Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            DenunciaDetailScreen(
                denuncia = uiState.selectedDenuncia!!,
                isAttending = uiState.isAttending,
                onAtender = { id -> viewModel.atenderDenuncia(id) }
            )
        } else Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar Section
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.searchDni,
                            onValueChange = { viewModel.onSearchDniChange(it) },
                            label = { Text("Buscar DNI") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    keyboardController?.hide()
                                    viewModel.buscarPorDni()
                                }
                            ),
                            trailingIcon = {
                                if (uiState.searchDni.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchDniChange("") }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar campo")
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                viewModel.buscarPorDni()
                            },
                            modifier = Modifier.height(56.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buscar")
                        }
                    }

                    if (uiState.isSearching) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filtro activo: DNI ${uiState.searchDni}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            OutlinedButton(
                                onClick = { viewModel.limpiarBusqueda() }
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Limpiar búsqueda")
                            }
                        }
                    }
                }
            }

            // Main Content Area
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (!uiState.isLoading && uiState.denuncias.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (uiState.isSearching) "No se encontraron denuncias para este DNI" else "No hay denuncias registradas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.isSearching) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { viewModel.limpiarBusqueda() }) {
                                Text("Ver todas las denuncias")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.denuncias,
                            key = { it.id ?: it.hashCode() }
                        ) { denuncia ->
                            DenunciaCard(
                                denuncia = denuncia,
                                onClick = { viewModel.openDetail(denuncia) }
                            )
                        }
                    }
                }
            }
            if (!uiState.isSearching) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.loadPreviousDenuncias() },
                        enabled = uiState.currentOffset > 0 && !uiState.isLoading
                    ) { Text("Anterior") }
                    Text("Página ${uiState.currentOffset / 5 + 1}")
                    Button(
                        onClick = { viewModel.loadMoreDenuncias() },
                        enabled = uiState.hasMorePages && !uiState.isLoading
                    ) { Text("Siguiente") }
                }
            }
        }
    }
    // Dialogs
    if (uiState.showRegisterDialog) {
        DenunciaRegisterDialog(
            onDismissRequest = { viewModel.closeRegister() },
            isRegistering = uiState.isRegistering,
            errorMessage = uiState.registerError,
            onRegistrar = { dni, tipo, direccion, descripcion ->
                viewModel.registrarDenuncia(dni, tipo, direccion, descripcion)
            }
        )
    }

}

@Composable
fun DenunciaCard(
    denuncia: Denuncia,
    onClick: () -> Unit
) {
    val isPendiente = denuncia.estado == 1

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = denuncia.tipo.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // State Badge / Chip
                Surface(
                    color = if (isPendiente) Color(0xFFE53935) else Color(0xFF4CAF50),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (isPendiente) "PENDIENTE" else "ATENDIDA",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = denuncia.direccion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DNI: ${denuncia.dni}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Fecha: ${denuncia.fecha.replace("T", " ").take(19)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DenunciaCardPreview() {
    DenunciasAppTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DenunciaCard(
                denuncia = Denuncia(
                    id = 1,
                    dni = "12345678",
                    fecha = "2025-02-23",
                    tipo = "ROBO",
                    direccion = "Av. Principal 123",
                    descripcion = "Robo de pertenencias",
                    estado = 1
                ),
                onClick = {}
            )
            DenunciaCard(
                denuncia = Denuncia(
                    id = 2,
                    dni = "87654321",
                    fecha = "2025-02-22",
                    tipo = "ASALTO",
                    direccion = "Calle Las Flores 456",
                    descripcion = "Asalto en la vía pública",
                    estado = 0
                ),
                onClick = {}
            )
        }
    }
}
