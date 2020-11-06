package edu.teco.earablecompanion.bluetooth

sealed class EarableType {
    object ESense : EarableType()
    object NotSupported : EarableType()

    data class Cosinuss(val accSupported: Boolean = false) : EarableType()
    data class Generic(val heartRateSupported: Boolean = false, val bodyTemperatureSupported: Boolean = false, val oximeterSupported: Boolean = false) : EarableType()
}