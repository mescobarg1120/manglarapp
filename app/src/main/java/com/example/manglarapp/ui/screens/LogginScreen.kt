package com.example.manglarapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.manglarapp.domain.model.DiaSemana
import com.example.manglarapp.utils.CameraHelper


@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var modoFoto by remember { mutableStateOf(false) }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var tempFotoUri by remember { mutableStateOf<Uri?>(null) }
    var shouldLaunchCamera by remember { mutableStateOf(false) }

    // ⭐ Launchers definidos PRIMERO
    // Launcher para cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempFotoUri != null) {
            fotoUri = tempFotoUri
            // Simular reconocimiento facial
            onLoginSuccess("Felipe")
        }
        shouldLaunchCamera = false
    }

    // Launcher para permiso
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            shouldLaunchCamera = true
        }
    }

    // Effect para lanzar cámara
    LaunchedEffect(shouldLaunchCamera) {
        if (shouldLaunchCamera) {
            val uri = CameraHelper.createImageUri(context)
            tempFotoUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo
            Text(
                text = "M",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "MANGLAR",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (modoFoto) {
                // Modo login con foto
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Reconocimiento Facial",
                        style = MaterialTheme.typography.titleMedium
                    )

                    fotoUri?.let { uri ->
                        Card(
                            modifier = Modifier.size(200.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Foto capturada",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                val uri = CameraHelper.createImageUri(context)
                                tempFotoUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tomar Foto para Ingresar")
                    }

                    TextButton(onClick = {
                        modoFoto = false
                        fotoUri = null
                    }) {
                        Text("Usar contraseña")
                    }
                }
            } else {
                // Modo login tradicional
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Default.Visibility
                                } else {
                                    Icons.Default.VisibilityOff
                                },
                                contentDescription = null
                            )
                        }
                    },
                    singleLine = true
                )

                Text(
                    text = "¿Olvidaste tu contraseña?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (nombre.isNotEmpty() && contrasena == "12345") {
                            onLoginSuccess(nombre)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("INICIAR SESIÓN")
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                OutlinedButton(
                    onClick = { modoFoto = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Face, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ingresar con Foto")
                }
            }
        }

        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    }
}