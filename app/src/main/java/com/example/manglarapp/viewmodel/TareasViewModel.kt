package com.example.manglarapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manglarapp.data.repository.TareasRepository
import com.example.manglarapp.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TareasViewModel(
    private val repository: TareasRepository = TareasRepository()
) : ViewModel() {

    val tareas: StateFlow<List<Tarea>> = repository.tareas

    private val _modoEdicion = MutableStateFlow(false)
    val modoEdicion: StateFlow<Boolean> = _modoEdicion.asStateFlow()

    private val _semanaActual = MutableStateFlow("Semana 06 oct - 12 oct")
    val semanaActual: StateFlow<String> = _semanaActual.asStateFlow()

    fun toggleModoEdicion() {
        _modoEdicion.value = !_modoEdicion.value
    }

    fun tomarTarea(tareaId: String, dia: DiaSemana, usuario: Usuario) {
        viewModelScope.launch {
            repository.tomarTarea(tareaId, dia, usuario)
        }
    }

    fun completarTarea(tareaId: String, dia: DiaSemana, fotoUri: String) {
        viewModelScope.launch {
            repository.completarTarea(tareaId, dia, fotoUri)
        }
    }

    fun aprobarTarea(tareaId: String, dia: DiaSemana) {
        viewModelScope.launch {
            repository.aprobarTarea(tareaId, dia)
        }
    }

    fun rechazarTarea(tareaId: String, dia: DiaSemana, comentario: String) {
        viewModelScope.launch {
            repository.rechazarTarea(tareaId, dia, comentario)
        }
    }

    fun liberarTarea(tareaId: String, dia: DiaSemana) {
        viewModelScope.launch {
            repository.liberarTarea(tareaId, dia)
        }
    }

    fun obtenerTareasPendientes(): List<Triple<Tarea, DiaSemana, AsignacionTarea>> {
        return repository.obtenerTareasPendientesAprobacion().map { (tarea, pair) ->
            Triple(tarea, pair.first, pair.second)
        }
    }
}