package com.example.manglarapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.manglarapp.domain.model.*
import com.example.manglarapp.viewmodel.TareasViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasScreen(
    viewModel: TareasViewModel,
    usuarioActual: Usuario,
    onNavigateToAgregar: () -> Unit,
    onConfirmarTarea: (String, DiaSemana) -> Unit,
    onRevisarTarea: (String, DiaSemana) -> Unit
) {
    val tareas by viewModel.tareas.collectAsState()
    val modoEdicion by viewModel.modoEdicion.collectAsState()
    val semana by viewModel.semanaActual.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tareas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Badge de tareas pendientes (solo admin)
                    if (usuarioActual.rol == RolUsuario.ADMIN) {
                        val pendientes = viewModel.obtenerTareasPendientes()
                        if (pendientes.isNotEmpty()) {
                            BadgedBox(
                                badge = {
                                    Badge { Text("${pendientes.size}") }
                                }
                            ) {
                                IconButton(onClick = { /* Navegar a pantalla de revisión */ }) {
                                    Icon(Icons.Default.Notifications, "Pendientes")
                                }
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (usuarioActual.rol == RolUsuario.ADMIN) {
                FloatingActionButton(
                    onClick = onNavigateToAgregar,
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.Add, "Agregar tarea")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = semana,
                    style = MaterialTheme.typography.titleMedium
                )

                if (usuarioActual.rol == RolUsuario.ADMIN) {
                    IconButton(onClick = { viewModel.toggleModoEdicion() }) {
                        Icon(
                            imageVector = if (modoEdicion) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = "Editar"
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tareas) { tarea ->
                    TareaCard(
                        tarea = tarea,
                        usuarioActual = usuarioActual,
                        modoEdicion = modoEdicion,
                        onTomarTarea = { dia ->
                            viewModel.tomarTarea(tarea.id, dia, usuarioActual)
                        },
                        onCompletarTarea = { dia ->
                            onConfirmarTarea(tarea.id, dia)
                        },
                        onRevisarTarea = { dia ->
                            onRevisarTarea(tarea.id, dia)
                        },
                        onLiberarTarea = { dia ->
                            viewModel.liberarTarea(tarea.id, dia)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TareaCard(
    tarea: Tarea,
    usuarioActual: Usuario,
    modoEdicion: Boolean,
    onTomarTarea: (DiaSemana) -> Unit,
    onCompletarTarea: (DiaSemana) -> Unit,
    onRevisarTarea: (DiaSemana) -> Unit,
    onLiberarTarea: (DiaSemana) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = tarea.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Row {
                    Text("${tarea.puntos} Pts", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("N° ${tarea.disponibilidad}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (modoEdicion) {
                DiaSemana.values().forEach { dia ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = tarea.asignaciones[dia] != null,
                            onCheckedChange = { /* lógica de selección */ }
                        )
                        Text(dia.name)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DiaSemana.values().forEach { dia ->
                        val asignacion = tarea.asignaciones[dia]

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dia.name,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(80.dp)
                            )

                            if (asignacion != null) {
                                TareaAsignacionChip(
                                    asignacion = asignacion,
                                    dia = dia,
                                    usuarioActual = usuarioActual,
                                    onCompletar = { onCompletarTarea(dia) },
                                    onRevisar = { onRevisarTarea(dia) },
                                    onLiberar = { onLiberarTarea(dia) }
                                )
                            } else {
                                // Botón para tomar tarea
                                val tareasTomadasCount = tarea.asignaciones.values.filterNotNull().size
                                if (tareasTomadasCount < tarea.disponibilidad &&
                                    usuarioActual.rol == RolUsuario.ARRENDATARIO) {
                                    FilledTonalButton(
                                        onClick = { onTomarTarea(dia) },
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Tomar", style = MaterialTheme.typography.bodySmall)
                                    }
                                } else {
                                    Text(
                                        text = "-",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TareaAsignacionChip(
    asignacion: AsignacionTarea,
    dia: DiaSemana,
    usuarioActual: Usuario,
    onCompletar: () -> Unit,
    onRevisar: () -> Unit,
    onLiberar: () -> Unit
) {
    // Determinar propiedades del chip según estado
    val chipColor: Color
    val chipIcon: ImageVector?
    val chipTexto: String
    val chipHabilitado: Boolean
    val chipAccion: () -> Unit

    when (asignacion.estado) {
        EstadoAsignacion.TOMADA -> {
            if (asignacion.usuarioId == usuarioActual.id) {
                chipColor = MaterialTheme.colorScheme.tertiary
                chipIcon = Icons.Default.CameraAlt
                chipTexto = asignacion.usuarioNombre
                chipHabilitado = true
                chipAccion = onCompletar
            } else {
                chipColor = MaterialTheme.colorScheme.surfaceVariant
                chipIcon = null
                chipTexto = asignacion.usuarioNombre
                chipHabilitado = false
                chipAccion = {}
            }
        }

        EstadoAsignacion.PENDIENTE_APROBACION -> {
            if (usuarioActual.rol == RolUsuario.ADMIN) {
                chipColor = Color(0xFFFFA726)
                chipIcon = Icons.Default.Visibility
                chipTexto = asignacion.usuarioNombre
                chipHabilitado = true
                chipAccion = onRevisar
            } else if (asignacion.usuarioId == usuarioActual.id) {
                chipColor = Color(0xFFFFA726)
                chipIcon = Icons.Default.HourglassEmpty
                chipTexto = "Esperando..."
                chipHabilitado = false
                chipAccion = {}
            } else {
                chipColor = Color(0xFFFFA726)
                chipIcon = Icons.Default.HourglassEmpty
                chipTexto = asignacion.usuarioNombre
                chipHabilitado = false
                chipAccion = {}
            }
        }

        EstadoAsignacion.APROBADA -> {
            chipColor = MaterialTheme.colorScheme.primary
            chipIcon = Icons.Default.CheckCircle
            chipTexto = asignacion.usuarioNombre
            chipHabilitado = false
            chipAccion = {}
        }

        EstadoAsignacion.RECHAZADA -> {
            if (asignacion.usuarioId == usuarioActual.id) {
                chipColor = MaterialTheme.colorScheme.error
                chipIcon = Icons.Default.Refresh
                chipTexto = "Reintentar"
                chipHabilitado = true
                chipAccion = onCompletar
            } else {
                chipColor = MaterialTheme.colorScheme.error
                chipIcon = Icons.Default.Cancel
                chipTexto = asignacion.usuarioNombre
                chipHabilitado = false
                chipAccion = {}
            }
        }
    }

    // Composable del chip
    AssistChip(
        onClick = { if (chipHabilitado) chipAccion() },
        label = {
            Text(chipTexto, style = MaterialTheme.typography.bodySmall)
        },
        leadingIcon = chipIcon?.let { icon ->
            { Icon(icon, null, Modifier.size(16.dp)) }
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = chipColor
        ),
        enabled = chipHabilitado,
        modifier = Modifier.height(32.dp)
    )
}