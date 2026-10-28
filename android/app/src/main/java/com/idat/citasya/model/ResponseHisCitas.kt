package com.idat.citasya.model

class ResponseHisCitas(
    var idcita: Int,
    var fecha: String,
    var hora: String,
    var especialidad: Int,
    var idpaciente: Int,
    var iddoctor: Int,
    var idsede: Int,
    var nombre_doctor: String,
    var nombreespecialidad: String,
    var nombre_sede: String,
    var estado: String
) {
    constructor() : this(0, "", "", 0, 0, 0, 0, "", "", "", "")
}