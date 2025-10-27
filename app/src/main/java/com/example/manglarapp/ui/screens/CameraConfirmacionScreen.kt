package com.example.manglarapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.manglarapp.domain.model.DiaSemana
import com.example.manglarapp.utils.CameraHelper

@Composable
fun CameraConfirmacionScreen(
    tareaId: String,
    tareaNombre: String,
    dia: DiaSemana,
    onConfirmar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var permissionDenied by remember { mutableStateOf(false) }
    var shouldLaunchCamera by remember { mutableStateOf(false) }

    // Verificar si ya tiene permiso
    val hasCameraPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // ⭐ IMPORTANTE: Definir launchers PRIMERO
    // Launcher para capturar foto
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            imageUri = tempImageUri
        }
        shouldLaunchCamera = false
    }

    // Launcher para solicitar permiso
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, preparar para abrir cámara
            shouldLaunchCamera = true
        } else {
            permissionDenied = true
        }
    }

    // Effect para lanzar cámara cuando se conceda el permiso
    LaunchedEffect(shouldLaunchCamera) {
        if (shouldLaunchCamera) {
            val uri = CameraHelper.createImageUri(context)
            tempImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Ícono de cámara
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Nombre de la tarea
        Text(
            text = tareaNombre.uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Confirma que hiciste tu tarea!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.tertiary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Mostrar imagen capturada
        imageUri?.let { uri ->
            Card(
                modifier = Modifier
                    .size(250.dp)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Foto de confirmación",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Botón para tomar/retomar foto
        Button(
            onClick = {
                when {
                    hasCameraPermission -> {
                        // Ya tiene permiso, abrir cámara directamente
                        val uri = CameraHelper.createImageUri(context)
                        tempImageUri = uri
                        cameraLauncher.launch(uri)
                    }
                    else -> {
                        // Solicitar permiso
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (imageUri == null) "TOMAR FOTO" else "VOLVER A TOMAR")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botón confirmar (solo si hay foto)
        if (imageUri != null) {
            Button(
                onClick = {
                    onConfirmar(imageUri.toString())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("CONFIRMAR TAREA")
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Botón cancelar
        TextButton(onClick = onCancelar) {
            Text("Cancelar")
        }

        // Mensaje si no hay permiso
        if (permissionDenied) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "Se requiere permiso de cámara para continuar. " +
                            "Por favor, habilítalo en Configuración.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}