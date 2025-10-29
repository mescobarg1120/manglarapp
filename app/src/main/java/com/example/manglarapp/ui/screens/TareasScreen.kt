package com.example.manglarapp.ui.screens

import androidx.activity.compose.BackHandler
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
    onRevisarTarea: () -> Unit,
    onLogout: () -> Unit
) {
    val tareas by viewModel.tareas.collectAsState()
    val modoEdicion by viewModel.modoEdicion.collectAsState()
    val semana by viewModel.semanaActual.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showMenuExpanded by remember { mutableStateOf(false) }

    // ⭐ CONTADOR NUCLEAR
    var recompositionTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(tareas) {
        recompositionTrigger++
        println("🔴 RECOMPOSICIÓN FORZADA: $recompositionTrigger")
        println("🔴 TAREAS: ${tareas.map { "${it.id}: ${it.asignaciones.size} asignaciones" }}")
    }

    BackHandler {
        showExitDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Tareas")
                        Text(
                            text = "Hola, ${usuarioActual.nombre}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    if (usuarioActual.rol == RolUsuario.ADMIN) {
                        val pendientes = viewModel.obtenerTareasPendientes()
                        if (pendientes.isNotEmpty()) {
                            BadgedBox(
                                badge = {
                                    Badge { Text("${pendientes.size}") }
                                }
                            ) {
                                IconButton(onClick = onRevisarTarea) {
                                    Icon(Icons.Default.Notifications, "Pendientes")
                                }
                            }
                        }
                    }

                    Box {
                        IconButton(onClick = { showMenuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, "Menú")
                        }

                        DropdownMenu(
                            expanded = showMenuExpanded,
                            onDismissRequest = { showMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Perfil") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null)
                                },
                                onClick = {
                                    showMenuExpanded = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Configuración") },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, contentDescription = null)
                                },
                                onClick = {
                                    showMenuExpanded = false
                                }
                            )

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("Cerrar sesión") },
                                leadingIcon = {
                                    Icon(Icons.Default.Logout, contentDescription = null)
                                },
                                onClick = {
                                    showMenuExpanded = false
                                    showLogoutDialog = true
                                }
                            )
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

            // ⭐ LazyColumn sin key
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = tareas,
                    key = { tarea ->
                        // ⭐ KEY incluye el trigger para forzar recomposición
                        "${tarea.id}_${tarea.asignaciones.hashCode()}_$recompositionTrigger"
                    }
                ) { tarea ->
                    TareaCard(
                        tarea = tarea,
                        usuarioActual = usuarioActual,
                        modoEdicion = modoEdicion,
                        recompositionTrigger = recompositionTrigger,
                        onTomarTarea = { dia ->
                            println("🟢 CLICK TOMAR: ${tarea.id} - $dia")
                            viewModel.tomarTarea(tarea.id, dia, usuarioActual)
                        },
                        onCompletarTarea = { dia ->
                            onConfirmarTarea(tarea.id, dia)
                        },
                        onRevisarTarea = { dia ->
                            onRevisarTarea()
                        },
                        onLiberarTarea = { dia ->
                            viewModel.liberarTarea(tarea.id, dia)
                        }
                    )
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, contentDescription = null) },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
            title = { Text("Salir") },
            text = { Text("¿Deseas cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        onLogout()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun TareaCard(
    tarea: Tarea,
    usuarioActual: Usuario,
    modoEdicion: Boolean,
    recompositionTrigger: Int,
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
                    // ⭐ Mostrar cuántas quedan disponibles
                    val tareasRestantes = tarea.disponibilidad - tarea.asignaciones.size
                    Text(
                        text = "N° $tareasRestantes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (tareasRestantes > 0) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DiaSemana.values().forEach { dia ->
                        val asignacion = tarea.asignaciones[dia]

                        key("${tarea.id}_${dia.name}_${asignacion?.hashCode() ?: 0}_$recompositionTrigger") {
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
                                    // ⭐ CORRECCIÓN: Contar correctamente las tareas tomadas
                                    val tareasTomadasCount = tarea.asignaciones.size

                                    if (tareasTomadasCount < tarea.disponibilidad &&
                                        usuarioActual.rol == RolUsuario.ARRENDATARIO) {
                                        FilledTonalButton(
                                            onClick = {
                                                println("🟢 BOTÓN TOMAR PRESIONADO: ${tarea.id} - $dia (${tareasTomadasCount}/${tarea.disponibilidad})")
                                                onTomarTarea(dia)
                                            },
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Tomar", style = MaterialTheme.typography.bodySmall)
                                        }
                                    } else {
                                        Text(
                                            text = if (tareasTomadasCount >= tarea.disponibilidad) "Completo" else "-",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (tareasTomadasCount >= tarea.disponibilidad) {
                                                MaterialTheme.colorScheme.error
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
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

    AssistChip(
        onClick = { if (chipHabilitado) chipAccion() },
        label = {
            Text(chipTexto, style = MaterialTheme.typography.bodySmall)
        },
        leadingIcon = if (chipIcon != null) {
            { Icon(chipIcon, null, Modifier.size(16.dp)) }
        } else {
            null
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = chipColor
        ),
        enabled = chipHabilitado,
        modifier = Modifier.height(32.dp)
    )
}
