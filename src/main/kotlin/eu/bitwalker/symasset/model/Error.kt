package eu.bitwalker.symasset.model

/**
 *
 * @param code Error code: * unavailable - The meter is unavailable, API implementation has lost connection to it; * unknown - The meter is in some unexpected error state (check the message for more information); Read-only.
 * @param message Error message. Read-only.
 */
data class Error(
    /* Error code: * unavailable - The meter is unavailable, API implementation has lost connection to it; * unknown - The meter is in some unexpected error state (check the message for more information); Read-only.  */
    var code: DeviceError.Code,

    var message: String? = null
)
{
    /**
     * Error code: * unavailable - The meter is unavailable, API implementation has lost connection to it; * unknown - The meter is in some unexpected error state (check the message for more information); Read-only.
     * Values: unavailable,unknown
     */
    enum class Code(val value: String){
        unavailable("unavailable"),
        unknown("unknown");
    }
}
