package com.example.manglarapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.manglarapp.data.repository.TareasRepository
import com.example.manglarapp.domain.model.*
import kotlinx.coroutines.flow.StateFlow

class TareasViewModel(
    private val repository: TareasRepository = TareasRepository()
) : ViewModel() {

    // ⭐ Exponer el flow del repository
    val tareas: StateFlow<List<Tarea>> = repository.tareas

    private val _modoEdicion = kotlinx.coroutines.flow.MutableStateFlow(false)
    val modoEdicion: StateFlow<Boolean> = _modoEdicion

    private val _semanaActual = kotlinx.coroutines.flow.MutableStateFlow("Semana del 03 al 09 de junio")
    val semanaActual: StateFlow<String> = _semanaActual

    fun toggleModoEdicion() {
        _modoEdicion.value = !_modoEdicion.value
    }

    fun tomarTarea(tareaId: String, dia: DiaSemana, usuario: Usuario) {
        repository.tomarTarea(tareaId, dia, usuario)
    }

    fun completarTarea(tareaId: String, dia: DiaSemana, fotoUri: String) {
        repository.completarTarea(tareaId, dia, fotoUri)
    }

    fun aprobarTarea(tareaId: String, dia: DiaSemana) {
        repository.aprobarTarea(tareaId, dia)
    }

    fun rechazarTarea(tareaId: String, dia: DiaSemana, comentario: String) {
        repository.rechazarTarea(tareaId, dia, comentario)
    }

    fun liberarTarea(tareaId: String, dia: DiaSemana) {
        repository.liberarTarea(tareaId, dia)
    }

    fun obtenerTareasPendientes(): List<Triple<Tarea, DiaSemana, AsignacionTarea>> {
        return repository.obtenerTareasPendientesAprobacion()
    }
}


