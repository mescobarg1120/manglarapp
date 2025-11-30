package com.example.manglarapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.manglarapp.model.EstadoUsuario
import com.example.manglarapp.model.Usuario
import com.example.manglarapp.model.RolUsuario
import com.example.manglarapp.viewmodel.UsuariosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuariosScreen(
    viewModel: UsuariosViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    navController: NavHostController? = null,
    usuarioActual: Usuario? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2F6F6C),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    // ✅ NAVEGACIÓN: Tareas
                    IconButton(onClick = {
                        navController?.navigate("tareas")
                    }) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Tareas")
                    }

                    // ✅ NAVEGACIÓN: Finanzas
                    IconButton(onClick = {
                        navController?.navigate("finanzas")
                    }) {
                        Icon(Icons.Default.AttachMoney, contentDescription = "Finanzas")
                    }

                    // ✅ NAVEGACIÓN: Usuarios (destacado porque estás aquí)
                    IconButton(onClick = { /* Ya estás aquí */ }) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = "Usuarios",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.mostrarDialogoNuevoUsuario() },
                containerColor = Color(0xFF2F6F6C)
            ) {
                Icon(Icons.Default.Add, "Nuevo Usuario", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            // Barra de búsqueda
            SearchBar(
                busqueda = uiState.busqueda,
                onBusquedaChange = { viewModel.buscarUsuarios(it) },
                modifier = Modifier.padding(16.dp)
            )

            // Tabla de usuarios
            if (uiState.isLoading && uiState.usuarios.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF2F6F6C))
                }
            } else if (uiState.usuarios.isEmpty()) {
                EmptyState()
            } else {
                TablaUsuarios(
                    usuarios = uiState.usuarios,
                    onEditarClick = { viewModel.mostrarDialogoEditarUsuario(it) },
                    onEliminarClick = { viewModel.mostrarDialogoEliminar(it) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Diálogos
        if (uiState.mostrarDialogoNuevo) {
            DialogoUsuario(
                titulo = "Nuevo Usuario",
                usuario = null,
                onDismiss = { viewModel.cerrarDialogos() },
                onConfirm = { viewModel.crearUsuario(it) },
                errorMessage = uiState.errorMessage
            )
        }

        if (uiState.mostrarDialogoEditar && uiState.usuarioSeleccionado != null) {
            DialogoUsuario(
                titulo = "Editar Usuario",
                usuario = uiState.usuarioSeleccionado,
                onDismiss = { viewModel.cerrarDialogos() },
                onConfirm = { viewModel.actualizarUsuario(it) },
                errorMessage = uiState.errorMessage,
                esEdicion = true
            )
        }

        if (uiState.mostrarDialogoEliminar && uiState.usuarioSeleccionado != null) {
            DialogoConfirmacionEliminar(
                usuario = uiState.usuarioSeleccionado!!,
                onDismiss = { viewModel.cerrarDialogos() },
                onConfirm = { viewModel.eliminarUsuario(it) }
            )
        }

        // Snackbar de mensajes
        uiState.successMessage?.let { message ->
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(2000)
                viewModel.limpiarMensajes()
            }

            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = Color(0xFF4CAF50)
            ) {
                Text(message, color = Color.White)
            }
        }
    }
}

@Composable
private fun SearchBar(
    busqueda: String,
    onBusquedaChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = busqueda,
        onValueChange = onBusquedaChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Buscar por RUT, nombre o email...") },
        leadingIcon = {
            Icon(Icons.Default.Search, "Buscar")
        },
        trailingIcon = {
            if (busqueda.isNotEmpty()) {
                IconButton(onClick = { onBusquedaChange("") }) {
                    Icon(Icons.Default.Clear, "Limpiar")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@Composable
private fun TablaUsuarios(
    usuarios: List<Usuario>,
    onEditarClick: (Usuario) -> Unit,
    onEliminarClick: (Usuario) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Encabezado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF546E7A))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "RUT",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    "Nombre",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    "Email",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(2.5f)
                )
                Text(
                    "Rol",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1.2f)
                )
                Text(
                    "Estado",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1.2f)
                )
                Text(
                    "Acciones",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1.5f)
                )
            }

            // Filas
            LazyColumn {
                items(usuarios) { usuario ->
                    FilaUsuario(
                        usuario = usuario,
                        onEditarClick = { onEditarClick(usuario) },
                        onEliminarClick = { onEliminarClick(usuario) }
                    )
                    if (usuario != usuarios.last()) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}


@Composable
private fun ChipRol(
    rol: RolUsuario,
    modifier: Modifier = Modifier
) {
    val (color, texto) = when (rol) {
        RolUsuario.ADMINISTRADOR -> Color(0xFF1976D2) to "Administrador"
        RolUsuario.ARRENDATARIO -> Color(0xFF7B1FA2) to "Arrendatario"
        RolUsuario.SUPERVISOR -> Color(0xFF388E3C) to "Supervisor"
    }

    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ChipEstado(
    estado: EstadoUsuario,
    modifier: Modifier = Modifier
) {
    val (color, texto) = when (estado) {
        EstadoUsuario.ACTIVO -> Color(0xFF4CAF50) to "Activo"
        EstadoUsuario.INACTIVO -> Color(0xFF9E9E9E) to "Inactivo"
        EstadoUsuario.BLOQUEADO -> Color(0xFFE53935) to "Bloqueado"
    }

    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun FilaUsuario(
    usuario: Usuario,
    onEditarClick: () -> Unit,
    onEliminarClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = usuario.rut,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1.5f)
        )

        Text(
            text = usuario.nombre,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f)
        )

        Text(
            text = usuario.email,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(2.5f)
        )

        ChipRol(
            rol = usuario.rol,
            modifier = Modifier.weight(1.2f)
        )

        ChipEstado(
            estado = usuario.estado,
            modifier = Modifier.weight(1.2f)
        )

        Row(
            modifier = Modifier.weight(1.5f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = onEditarClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Editar", style = MaterialTheme.typography.labelSmall)
            }

            Button(
                onClick = onEliminarClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Eliminar", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoUsuario(
    titulo: String,
    usuario: Usuario?,
    onDismiss: () -> Unit,
    onConfirm: (Usuario) -> Unit,
    errorMessage: String?,
    esEdicion: Boolean = false
) {
    var rut by remember { mutableStateOf(usuario?.rut ?: "") }
    var nombre by remember { mutableStateOf(usuario?.nombre ?: "") }
    var email by remember { mutableStateOf(usuario?.email ?: "") }
    var rol by remember { mutableStateOf(usuario?.rol ?: RolUsuario.ARRENDATARIO) }
    var password by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }

    var expandedRol by remember { mutableStateOf(false) }

    val roles = listOf(RolUsuario.ADMINISTRADOR, RolUsuario.ARRENDATARIO, RolUsuario.SUPERVISOR)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = rut,
                    onValueChange = { rut = it },
                    label = { Text("RUT") },
                    placeholder = { Text("12.345.678-9") },
                    enabled = !esEdicion,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre Completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    placeholder = { Text("usuario@ejemplo.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedRol,
                    onExpandedChange = { expandedRol = !expandedRol }
                ) {
                    OutlinedTextField(
                        value = when (rol) {
                            RolUsuario.ADMINISTRADOR -> "Administrador"
                            RolUsuario.ARRENDATARIO -> "Arrendatario"
                            RolUsuario.SUPERVISOR -> "Supervisor"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRol)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedRol,
                        onDismissRequest = { expandedRol = false }
                    ) {
                        roles.forEach { rolOpcion ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (rolOpcion) {
                                            RolUsuario.ADMINISTRADOR -> "Administrador"
                                            RolUsuario.ARRENDATARIO -> "Arrendatario"
                                            RolUsuario.SUPERVISOR -> "Supervisor"
                                        }
                                    )
                                },
                                onClick = {
                                    rol = rolOpcion
                                    expandedRol = false
                                }
                            )
                        }
                    }
                }

                if (!esEdicion) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        visualTransformation = if (mostrarPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                                Icon(
                                    if (mostrarPassword) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    "Toggle password"
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nuevoUsuario = Usuario(
                        rut = rut,
                        nombre = nombre,
                        email = email,
                        rol = rol,
                        estado = usuario?.estado ?: EstadoUsuario.ACTIVO,
                        password = password
                    )
                    onConfirm(nuevoUsuario)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2F6F6C)
                )
            ) {
                Text(if (esEdicion) "Actualizar" else "Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun DialogoConfirmacionEliminar(
    usuario: Usuario,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFE53935)
            )
        },
        title = { Text("Eliminar Usuario") },
        text = {
            Text("¿Está seguro que desea eliminar al usuario ${usuario.nombre} (${usuario.rut})? Esta acción no se puede deshacer.")
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(usuario.rut) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935)
                )
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.People,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Text(
                "No hay usuarios registrados",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            Text(
                "Presiona el botón + para agregar un usuario",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}