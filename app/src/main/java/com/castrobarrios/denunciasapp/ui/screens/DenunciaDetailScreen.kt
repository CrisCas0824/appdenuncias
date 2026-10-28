package com.castrobarrios.denunciasapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.castrobarrios.denunciasapp.data.model.Denuncia

@Composable
fun DenunciaDetailScreen(
    denuncia: Denuncia,
    isAttending: Boolean,
    onAtender: (id: Int) -> Unit
) {
    val isPendiente = denuncia.estado == 1
    var showConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Denuncia${denuncia.id?.let { " #$it" } ?: ""}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider()

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Estado: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = if (isPendiente) Color(0xFFE53935) else Color(0xFF4CAF50),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = if (isPendiente) "PENDIENTE" else "ATENDIDA",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        DetailRow(label = "DNI Ciudadano:", value = denuncia.dni)
        DetailRow(label = "Tipo de Delito:", value = denuncia.tipo)
        DetailRow(label = "Dirección:", value = denuncia.direccion)
        DetailRow(label = "Fecha:", value = denuncia.fecha.replace("T", " ").take(19))

        Column {
            Text(
                text = "Descripción:",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = denuncia.descripcion.ifBlank { "Sin descripción" },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        if (isPendiente && denuncia.id != null) {
            Button(
                onClick = { showConfirmation = true },
                enabled = !isAttending,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                )
            ) {
                if (isAttending) {
                    CircularProgressIndicator(modifier = Modifier.width(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Atender Denuncia", fontWeight = FontWeight.Bold)
                }
            }
        } else if (!isPendiente) {
            Surface(
                color = Color(0xFFE8F5E9),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "✓ Esta denuncia ya ha sido atendida.",
                    color = Color(0xFF2E7D32),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }

    if (showConfirmation && isPendiente && denuncia.id != null) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Confirmar atención") },
            text = { Text("¿Marcar la denuncia #${denuncia.id} como atendida?") },
            confirmButton = {
                Button(onClick = {
                    showConfirmation = false
                    onAtender(denuncia.id)
                }) { Text("Confirmar") }
            },
            dismissButton = {
                Button(onClick = { showConfirmation = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
