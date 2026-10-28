package com.idat.citasya.model

// Un horario ya ocupado por otra cita activa (para deshabilitarlo en la pantalla de reserva)
class ResponseSlotOcupado(
    var fecha: String,
    var hora: String
) {
    constructor() : this("", "")
}
