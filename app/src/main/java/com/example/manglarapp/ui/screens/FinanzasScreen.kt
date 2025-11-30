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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.manglarapp.model.Finanza
import com.example.manglarapp.model.TipoFinanza
import com.example.manglarapp.model.UsuarioFinanza
import com.example.manglarapp.model.Usuario
import com.example.manglarapp.model.RolUsuario
import com.example.manglarapp.viewmodel.FinanzasViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanzasScreen(
    viewModel: FinanzasViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    navController: NavHostController? = null,
    usuarioActual: Usuario? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Finanzas"
                        )
                        Text("Finanzas hogar")
                    }
                },
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

                    // ✅ NAVEGACIÓN: Finanzas (destacado porque estás aquí)
                    IconButton(onClick = { /* Ya estás aquí */ }) {
                        Icon(
                            Icons.Default.AttachMoney,
                            contentDescription = "Finanzas",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // ✅ NAVEGACIÓN: Usuarios (SOLO ADMINISTRADOR)
                    if (usuarioActual?.rol == RolUsuario.ADMINISTRADOR) {
                        IconButton(onClick = {
                            navController?.navigate("usuarios")
                        }) {
                            Icon(Icons.Default.People, contentDescription = "Usuarios")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selector de mes
            SelectorMes(
                mesSeleccionado = uiState.mesSeleccionado,
                onMesChange = { viewModel.cambiarMes(it) }
            )

            // Tablas en fila
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tabla de usuarios
                Box(modifier = Modifier.weight(1f)) {
                    TablaUsuarios(usuarios = uiState.usuariosFinanzas)
                }

                // Tabla de gastos
                Box(modifier = Modifier.weight(1f)) {
                    TablaGastos(
                        gastos = uiState.gastos,
                        onAgregarGasto = { viewModel.mostrarDialogoGasto() }
                    )
                }
            }

            // Resumen financiero
            ResumenFinanciero(
                totalIngresos = uiState.totalIngresos,
                totalGastos = uiState.totalGastos,
                saldo = uiState.saldo,
                onExportarExcel = { viewModel.exportarAExcel() }
            )
        }

        // Diálogo para agregar gasto
        if (uiState.mostrarDialogoGasto) {
            DialogoGasto(
                onDismiss = { viewModel.cerrarDialogos() },
                onConfirm = { gasto -> viewModel.crearGasto(gasto) },
                errorMessage = uiState.errorMessage
            )
        }

        // Snackbar de mensajes
        uiState.successMessage?.let { message ->
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(2000)
                viewModel.limpiarMensajes()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Snackbar(
                    containerColor = Color(0xFF4CAF50)
                ) {
                    Text(message, color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorMes(
    mesSeleccionado: String,
    onMesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val meses = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    Column(modifier = modifier) {
        Text(
            text = "Selecciona el mes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = mesSeleccionado,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .width(250.dp)
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                meses.forEach { mes ->
                    DropdownMenuItem(
                        text = { Text(mes) },
                        onClick = {
                            onMesChange(mes)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TablaUsuarios(
    usuarios: List<UsuarioFinanza>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            // Encabezado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF546E7A))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Usuario",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    "Puntos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Abono",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.2f)
                )
                Text(
                    "Deuda",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.2f)
                )
            }

            // Filas
            usuarios.forEach { usuario ->
                FilaUsuario(usuario)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun FilaUsuario(usuario: UsuarioFinanza) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = usuario.nombre,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1.5f)
        )

        Surface(
            modifier = Modifier.weight(1f),
            color = Color(0xFF00BCD4),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = usuario.puntos.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Text(
            text = formatearMoneda(usuario.totalAbono),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.2f)
        )

        Text(
            text = formatearMoneda(usuario.totalDeuda),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            color = if (usuario.totalDeuda > 0) Color.Red else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.2f)
        )
    }
}

@Composable
private fun TablaGastos(
    gastos: List<Finanza>,
    onAgregarGasto: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Encabezado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF546E7A), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Gasto",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    "Fecha",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    "Valor",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.5f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filas
            gastos.forEach { gasto ->
                FilaGasto(gasto)
                HorizontalDivider()
            }

            // Botón agregar gasto
            Button(
                onClick = onAgregarGasto,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Icon(Icons.Default.Add, "Agregar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar Gasto")
            }
        }
    }
}

@Composable
private fun FilaGasto(gasto: Finanza) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = gasto.descripcion,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f)
        )

        Text(
            text = gasto.fecha,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1.5f)
        )

        Text(
            text = formatearMoneda(gasto.monto),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.5f)
        )
    }
}

@Composable
private fun ResumenFinanciero(
    totalIngresos: Double,
    totalGastos: Double,
    saldo: Double,
    onExportarExcel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilaResumen("Total ingresos casa", totalIngresos, Color(0xFF4CAF50))
            HorizontalDivider()
            FilaResumen("Total gastos", totalGastos, Color(0xFFF44336))
            HorizontalDivider(thickness = 2.dp)
            FilaResumen(
                "Saldo",
                saldo,
                if (saldo >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                isSaldo = true
            )

            Button(
                onClick = onExportarExcel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Icon(Icons.Default.Download, "Exportar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exportar a Excel")
            }
        }
    }
}

@Composable
private fun FilaResumen(
    label: String,
    valor: Double,
    color: Color,
    isSaldo: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isSaldo) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSaldo) FontWeight.Bold else FontWeight.Normal
        )

        Text(
            text = formatearMoneda(valor),
            style = if (isSaldo) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoGasto(
    onDismiss: () -> Unit,
    onConfirm: (Finanza) -> Unit,
    errorMessage: String?
) {
    var descripcion by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Alimentación") }

    var expandedCategoria by remember { mutableStateOf(false) }
    val categorias = listOf(
        "Alimentación", "Limpieza", "Mantención",
        "Servicios", "Transporte", "Otros"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Gasto") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    placeholder = { Text("Ej: Feria, Luz, etc.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = monto,
                    onValueChange = { if (it.all { char -> char.isDigit() }) monto = it },
                    label = { Text("Monto") },
                    placeholder = { Text("40000") },
                    prefix = { Text("$") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text("Fecha") },
                    placeholder = { Text("25/11/25") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCategoria,
                    onExpandedChange = { expandedCategoria = !expandedCategoria }
                ) {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoria)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCategoria,
                        onDismissRequest = { expandedCategoria = false }
                    ) {
                        categorias.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    categoria = cat
                                    expandedCategoria = false
                                }
                            )
                        }
                    }
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
                    val gasto = Finanza(
                        descripcion = descripcion,
                        monto = monto.toDoubleOrNull() ?: 0.0,
                        fecha = fecha,
                        categoria = categoria,
                        tipo = TipoFinanza.EGRESO
                    )
                    onConfirm(gasto)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2F6F6C)
                )
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Función auxiliar para formatear moneda en formato chileno
 */
private fun formatearMoneda(valor: Double): String {
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    return formato.format(valor)
}