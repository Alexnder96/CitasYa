package com.idat.citasya.model

class ResponseEspecialidad(
    var idespecialidad: Int,
    var nombreespecialidad: String,
    var descripcion: String
) {
    constructor() : this(0, "", "")
}
