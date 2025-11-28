package com.tuempresa.PlanIt.util.validators

import org.junit.Assert.*
import org.junit.Test

class TaskValidatorTest {

    @Test
    fun `validateTask con título válido no devuelve error de título`() {
        // 1. Preparación (Arrange)
        val title = "Mi primera tarea"
        
        // 2. Acción (Act)
        val result = TaskValidator.validateTask(title = title, description = "", dueDate = null)
        
        // 3. Verificación (Assert)
        assertNull("No debería haber un error de título si el título es válido", result["title"])
    }

    @Test
    fun `validateTask con título vacío devuelve error de título`() {
        // 1. Preparación (Arrange)
        val title = "" // Título en blanco
        
        // 2. Acción (Act)
        val result = TaskValidator.validateTask(title = title, description = "", dueDate = null)
        
        // 3. Verificación (Assert)
        assertNotNull("Debería haber un error de título si el título está vacío", result["title"])
        assertEquals("El título no puede estar vacío", result["title"]?.errorMessage)
    }
}