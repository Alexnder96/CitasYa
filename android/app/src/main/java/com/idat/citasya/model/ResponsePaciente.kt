package com.idat.citasya.model

class ResponsePaciente(
    var idpaciente: Int,
    var dni: String,
    var nombre: String,
    var fecha_nacimiento: String,
    var edad: Int,
    var sexo: String,
    var telefono: String,
    var correo: String,
    var estado: Int,
    var idsede: Int,
    var nombre_sede: String
) {

    constructor() : this(0, "", "", "", 0, "", "", "", 1, 0, "")
}