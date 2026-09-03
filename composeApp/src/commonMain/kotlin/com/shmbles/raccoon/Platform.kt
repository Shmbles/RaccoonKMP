package com.shmbles.raccoon

/**
 * Clase esperada que representa la plataforma en la que se está ejecutando la aplicación.
 * Contiene propiedades específicas de la plataforma, como su nombre.
 */
expect class Platform() {
    /**
     * El nombre de la plataforma actual (ej. "Android", "iOS", "Desktop").
     */
    val name: String
}

/**
 * Función esperada que proporciona una instancia de [Platform] para la plataforma actual.
 * La implementación real se define en los módulos específicos de cada plataforma.
 *
 * @return Una instancia de [Platform] con información sobre la plataforma actual.
 */
expect fun getPlatform(): Platform