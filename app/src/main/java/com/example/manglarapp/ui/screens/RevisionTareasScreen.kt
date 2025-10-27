package com.example.manglarapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.manglarapp.domain.model.*
import com.example.manglarapp.viewmodel.TareasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionTareasScreen(
    viewModel: TareasViewModel,
    onNavigateBack: () -> Unit
) {
    val tareasPendientes = viewModel.obtenerTareasPendientes()
    var tareaSeleccionada by remember { mutableStateOf<Triple<Tarea, DiaSemana, AsignacionTarea>?>(null) }
    var showDialogRechazo by remember { mutableStateOf(false) }
    var comentarioRechazo by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tareas Pendientes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (tareasPendientes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No hay tareas pendientes de revisión")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tareasPendientes) { (tarea, dia, asignacion) ->
                    TareaPendienteCard(
                        tarea = tarea,
                        dia = dia,
                        asignacion = asignacion,
                        onAprobar = {
                            viewModel.aprobarTarea(tarea.id, dia)
                        },
                        onRechazar = {
                            tareaSeleccionada = Triple(tarea, dia, asignacion)
                            showDialogRechazo = true
                        }
                    )
                }
            }
        }
    }

    // Dialog de rechazo
    if (showDialogRechazo && tareaSeleccionada != null) {
        AlertDialog(
            onDismissRequest = { showDialogRechazo = false },
            title = { Text("Rechazar Tarea") },
            text = {
                Column {
                    Text("¿Por qué rechazas esta tarea?")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = comentarioRechazo,
                        onValueChange = { comentarioRechazo = it },
                        label = { Text("Comentario") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        tareaSeleccionada?.let { (tarea, dia, _) ->
                            viewModel.rechazarTarea(tarea.id, dia, comentarioRechazo)
                        }
                        showDialogRechazo = false
                        comentarioRechazo = ""
                        tareaSeleccionada = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Rechazar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialogRechazo = false
                    comentarioRechazo = ""
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun TareaPendienteCard(
    tarea: Tarea,
    dia: DiaSemana,
    asignacion: AsignacionTarea,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = tarea.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${dia.name} - ${asignacion.usuarioNombre}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = "${tarea.puntos} pts",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Foto
            asignacion.fotoConfirmacion?.let { fotoUri ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(fotoUri),
                        contentDescription = "Foto de confirmación",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAprobar,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aprobar")
                }

                OutlinedButton(
                    onClick = onRechazar,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rechazar")
                }
            }
        }
    }
}