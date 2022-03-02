package eu.bitwalker.symasset

import eu.bitwalker.symasset.converter.OffsetDateTimeConverter
import eu.bitwalker.symasset.service.AssetService
import eu.bitwalker.symasset.service.MeteringService
import eu.bitwalker.symasset.service.ResourceGroupService
import eu.bitwalker.symasset.web.assetApi
import eu.bitwalker.symasset.web.index
import eu.bitwalker.symasset.web.mockControlApi
import eu.bitwalker.symasset.web.mockMeteringApi
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.routing.*
import java.time.OffsetDateTime

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {

    val resourceGroupService = ResourceGroupService()
    val meteringService = MeteringService()
    val assetService = AssetService(resourceGroupService, meteringService)

    assetService.initializeAnalogWires(2)
    assetService.initializeRelayWires(2)

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
            registerTypeAdapter(OffsetDateTime::class.java, OffsetDateTimeConverter())
        }
    }

    install(Routing) {
        index(assetService)
        assetApi(assetService)
        mockMeteringApi(assetService)
        mockControlApi(assetService, resourceGroupService, meteringService)
    }
}
