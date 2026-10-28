package com.idat.citasya.model

class ResponseDoctor(
    var iddoctor: Int,
    var nombre: String,
    var idespecialidad: Int,
    var telefono: String,
    var estado: Int
) {
    constructor() : this(0, "", 0, "", 1)
}
