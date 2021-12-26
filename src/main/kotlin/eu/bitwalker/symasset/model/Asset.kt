package eu.bitwalker.symasset.model

import net.sympower.control.api.server.models.Resource
import net.sympower.metering.api.server.models.Meter

data class Asset(
    val resource: Resource,
    val meter: Meter,
    var settings: Settings,
    val status: Status,
)

data class Settings(
    val minPower: Int,
    val maxPower: Int,
    val maxDeviation: Int,
    val rampRate: Int,
)

data class Status(
    var previousLevel: Int,
    var targetLevelChangedAt: java.time.OffsetDateTime,
)
