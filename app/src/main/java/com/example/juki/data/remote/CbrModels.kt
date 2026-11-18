package com.example.juki.data.remote

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "Metall", strict = false)
data class CbrMetallResponse(
    @field:ElementList(entry = "Record", inline = true, required = false)
    var records: List<CbrRecord>? = null
)

@Root(name = "Record", strict = false)
data class CbrRecord(
    @field:Attribute(name = "Code", required = false)
    var code: String? = null,

    @field:Element(name = "Buy", required = false)
    var buy: String? = null
)