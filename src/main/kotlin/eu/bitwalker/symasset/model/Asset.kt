package eu.bitwalker.symasset.model

import net.sympower.control.api.server.models.DeviceError
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

/**
 *
 * @param code Error code: * unavailable - The meter is unavailable, API implementation has lost connection to it; * unknown - The meter is in some unexpected error state (check the message for more information); Read-only.
 * @param updatedAt Timestamp (UTC) when the error status was last updated. Read-only.
 * @param message Error message. Read-only.
 */
data class DeviceError(
    /* Error code: * unavailable - The meter is unavailable, API implementation has lost connection to it; * unknown - The meter is in some unexpected error state (check the message for more information); Read-only.  */
    var code: DeviceError.Code,
    /* Timestamp (UTC) when the error status was last updated. Read-only. */
    var updatedAt: java.time.OffsetDateTime,
    /* Error message. Read-only. */
    var message: kotlin.String? = null
)
{
    /**
     * Error code: * unavailable - The meter is unavailable, API implementation has lost connection to it; * unknown - The meter is in some unexpected error state (check the message for more information); Read-only.
     * Values: unavailable,unknown
     */
    enum class Code(val value: kotlin.String){
        unavailable("unavailable"),
        unknown("unknown");
    }
}