package project.alkautsar.simulasikredit.model

data class SimulasiCicilanModel(
    val bulan: Int,
    val cicilanPokok: Double,
    val bunga: Double,
    val totalCicilan: Double,
    val sisaCicilan: Double
)