/**
 * Sympower Cloud-to-Cloud Metering API
 * API implemented by Sympower partners so Sympower platform can get power metering data.
 *
 * The version of the OpenAPI document: 2.0.1
 * Contact: support@sympower.net
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package eu.bitwalker.symasset.web

import eu.bitwalker.symasset.service.AssetService
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.mockMeteringApi(assetService: AssetService) {

    route("/metering/meters/{meterNumber}") {
        get {
            val meterNumber = call.parameters["meterNumber"]!!.toInt()
            val meter = assetService.getPowerMeterById(meterNumber)
            call.respond(meter)
        }
    }

    route("/metering/meters") {
        get {
            call.respond(assetService.getPowerMeters())

        }
    }

}
