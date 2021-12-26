package eu.bitwalker.symasset.web

import eu.bitwalker.symasset.model.Error
import eu.bitwalker.symasset.model.Settings
import eu.bitwalker.symasset.service.AssetService
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.assetApi(assetService: AssetService) {

    route("/asset") {

        get {
            call.respond(assetService.getWires())
        }

        post("/addAnalogWire") {
            val newWire = assetService.addAnalogWire()
            when (call.request.accept()) {
                "application/json" -> call.respond(newWire)
                else -> call.respondRedirect("/")
            }
        }
        post("/addRelayWire") {
            val newWire = assetService.addRelayWire()
            when (call.request.accept()) {
                "application/json" -> call.respond(newWire)
                else -> call.respondRedirect("/")
            }
        }

        put("/{assetId}/settings") {
            val assetId = call.parameters["assetId"]!!.toInt()
            val settings = call.receive<Settings>()
            call.respond(assetService.updateAssetSettings(assetId, settings))
        }

        put("/{assetId}/wire_error") {
            val assetId = call.parameters["assetId"]!!.toInt()
            val error = call.receive<Error>()
            call.respond(assetService.setWireError(assetId, error))
        }

        delete("/{assetId}/wire_error") {
            val assetId = call.parameters["assetId"]!!.toInt()
            call.respond(assetService.clearWireError(assetId))
        }

        put("/{assetId}/meter_error") {
            val assetId = call.parameters["assetId"]!!.toInt()
            val error = call.receive<Error>()
            call.respond(assetService.setMeterError(assetId, error))
        }

        delete("/{assetId}/meter_error") {
            val assetId = call.parameters["assetId"]!!.toInt()
            call.respond(assetService.clearMeterError(assetId))
        }

    }
}
