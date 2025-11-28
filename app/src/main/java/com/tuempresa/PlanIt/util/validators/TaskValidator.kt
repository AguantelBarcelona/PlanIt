package com.tuempresa.PlanIt.util.validators

object TaskValidator {

    fun validateTask(title: String, description: String, dueDate: Long?): Map<String, ValidationResult> {
        val results = mutableMapOf<String, ValidationResult>()

        if (title.isBlank()) {
            results["title"] = ValidationResult(false, "El título no puede estar vacío")
        }

        // Example of another validation
        // if (description.length > 100) {
        //    results["description"] = ValidationResult(false, "La descripción no puede exceder los 100 caracteres")
        // }

        return results
    }

    fun isFormValid(results: Map<String, ValidationResult>): Boolean {
        return results.values.all { it.isValid }
    }
}

data class ValidationResult(val isValid: Boolean, val errorMessage: String? = null)
