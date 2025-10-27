package com.example.manglarapp.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.manglarapp.domain.model.*
import com.example.manglarapp.ui.screens.*
import com.example.manglarapp.viewmodel.TareasViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Tareas : Screen("tareas")
    object RevisionTareas : Screen("revision_tareas")
    object CameraConfirmacion : Screen("camera/{tareaId}/{tareaNombre}/{dia}") {
        fun createRoute(tareaId: String, tareaNombre: String, dia: DiaSemana) =
            "camera/$tareaId/$tareaNombre/${dia.name}"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    tareasViewModel: TareasViewModel = viewModel()
) {
    var usuarioActual by remember { mutableStateOf<Usuario?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { nombreUsuario ->
                    usuarioActual = Usuario(
                        id = if (nombreUsuario.lowercase() == "admin") "0" else "1",
                        nombre = nombreUsuario,
                        rol = if (nombreUsuario.lowercase() == "admin") {
                            RolUsuario.ADMIN
                        } else {
                            RolUsuario.ARRENDATARIO
                        }
                    )
                    navController.navigate(Screen.Tareas.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Tareas.route) {
            usuarioActual?.let { usuario ->
                TareasScreen(
                    viewModel = tareasViewModel,
                    usuarioActual = usuario,
                    onNavigateToAgregar = { /* TODO */ },
                    onConfirmarTarea = { tareaId, dia ->
                        val tarea = tareasViewModel.tareas.value.find { it.id == tareaId }
                        if (tarea != null) {
                            navController.navigate(
                                Screen.CameraConfirmacion.createRoute(
                                    tareaId, tarea.nombre, dia
                                )
                            )
                        }
                    },
                    onRevisarTarea = {
                        navController.navigate(Screen.RevisionTareas.route)
                    },
                    onLogout = {
                        usuarioActual = null
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Screen.RevisionTareas.route) {
            RevisionTareasScreen(
                viewModel = tareasViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.CameraConfirmacion.route,
            arguments = listOf(
                navArgument("tareaId") { type = NavType.StringType },
                navArgument("tareaNombre") { type = NavType.StringType },
                navArgument("dia") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tareaId = backStackEntry.arguments?.getString("tareaId") ?: ""
            val tareaNombre = backStackEntry.arguments?.getString("tareaNombre") ?: ""
            val diaString = backStackEntry.arguments?.getString("dia") ?: ""
            val dia = DiaSemana.valueOf(diaString)

            CameraConfirmacionScreen(
                tareaId = tareaId,
                tareaNombre = tareaNombre,
                dia = dia,
                onConfirmar = { fotoUri ->
                    tareasViewModel.completarTarea(tareaId, dia, fotoUri)
                    navController.popBackStack()
                },
                onCancelar = {
                    navController.popBackStack()
                }
            )
        }
    }
}