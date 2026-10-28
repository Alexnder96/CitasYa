package com.idat.citasya.model

class ResponseSede(
    var idsede: Int,
    var nombre: String,
    var direccion: String,
    var telefono: String,
    var estado: Int
) {
    constructor() : this(0, "", "", "", 1)
}
