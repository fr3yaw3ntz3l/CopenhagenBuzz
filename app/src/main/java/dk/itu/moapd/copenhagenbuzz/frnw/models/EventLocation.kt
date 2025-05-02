package dk.itu.moapd.copenhagenbuzz.frnw.models

class EventLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = ""
) {
    constructor() : this(0.0, 0.0, "")
}