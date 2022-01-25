package eu.bitwalker.symasset.web

import eu.bitwalker.symasset.service.AssetService
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.routing.*
import kotlinx.html.*

fun Route.index(assetService: AssetService) {

    static("static") {
        resources("files")
    }
    static("schema") {
        resources("openapi")
    }

    get("/") {
        call.respondHtml(HttpStatusCode.OK) {
            head {
                link(
                    href = "https://cdn.jsdelivr.net/npm/bootswatch@5.1.3/dist/darkly/bootstrap.min.css",
                    rel = "stylesheet",
                    type = "text/css"
                )
                styleLink("/static/styles.css")
                script(src = "/static/script.js") {}
            }
            body {
                nav("navbar navbar-expand-lg navbar-dark bg-primary") {
                    div(classes = "container-fluid") {
                        a(href = "#", classes = "navbar-brand") {
                            +"SymAssets"
                        }
                        div(classes = "navbar-nav ms-auto") {
                            form(
                                action = "/asset/addAnalogWire",
                                method = FormMethod.post,
                                classes = "form-inline px-2"
                            ) {
                                button(type = ButtonType.submit, classes = "btn btn-secondary") {
                                    +"Add analog wire"
                                }
                            }
                            form(action = "/asset/addRelayWire", method = FormMethod.post, classes = "form-inline") {
                                button(type = ButtonType.submit, classes = "btn btn-secondary") {
                                    +"Add relay wire"
                                }
                            }
                        }
                    }
                }
                div(classes = "container pt-3") {
                    h3 { +"Assets (Resources)" }
                    assetTable(assetService)

                    h3 { +"Meters" }
                    meterTable(assetService)
                }

                div(classes = "container pt-3") {
                    h4 { +"Endpoints" }
                    div {
                        a(
                            href = "/static/assetapi.html",
                            target = ATarget.blank
                        ) { strong { +"Asset API:" } }
                        span(classes = "px-2") { +" /assets" }
                    }
                    div {
                        a(
                            href = "/static/controlapi.html",
                            target = ATarget.blank
                        ) { strong { +"Control API:" } }
                        span(classes = "px-2") { +" /control" }
                    }
                    div {
                        a(
                            href = "/static/meteringapi.html",
                            target = ATarget.blank
                        ) { strong { +"Metering API:" } }
                        span(classes = "px-2") { +" /metering" }
                    }
                }
            }
        }
    }

}

private fun DIV.assetTable(assetService: AssetService) {
    table(classes = "table table-sm table-hover table-striped") {
        thead {
            tr {
                th { +"asset id" }
                th { +"group number" }
                th { +"wire number" }
                th { +"name" }
                th { +"type" }
                th { +"target level" }
                th { +"error" }
                th { +"is out of operation" }
                th { +"min. consumption" }
                th { +"max. consumption" }
                th { +"max. deviation" }
                th { +"ramp rate %" }
                th { +"meter number" }
            }
        }
        tbody {
            val assets = assetService.getWires()
            for ((index, asset) in assets.withIndex()) {
                tr {
                    td { +index.toString() }
                    td { +asset.resource.address.groupNumber.toString() }
                    td { +asset.resource.address.number.toString() }
                    td { +asset.resource.name!! }
                    td { +asset.resource.type.name }
                    td { +asset.resource.targetLevel.value.toString() }
                    td { +(asset.resource.error?.code?.value ?: "-") }
                    td { +asset.resource.isOutOfOperation.toString() }
                    td { +asset.settings.minPower.toKW() }
                    td { +asset.settings.maxPower.toKW() }
                    td { +asset.settings.maxDeviation.toKW() }
                    td { +asset.settings.rampRate.toString() }
                    td { +asset.meter.number.toString() }
                }
            }
        }
    }
}

private fun DIV.meterTable(assetService: AssetService) {
    table(classes = "table table-sm table-hover table-striped") {
        thead {
            tr {
                th { +"number" }
                th { +"name" }
                th { +"total" }
                th { +"phases" }
                th { +"unit" }
                th { +"error" }
            }
        }
        tbody {
            val meters = assetService.getPowerMeters()
            for (meter in meters) {
                tr {
                    td { +meter.number.toString() }
                    td { +meter.name!! }
                    td { +meter.activePower?.total.toKW()}
                    td { +meter.activePower?.inPhases.toKw() }
                    td { +(meter.activePower?.unit?.value ?:"-") }
                    td { +(meter.error?.code?.value ?: "-") }
                }
            }
        }
    }

}

private fun List<Int>?.toKw(): String {
    return this?.joinToString(separator = ", ") { it.toKW()} ?: "-"
}

private fun Int?.toKW(): String {
    if (this == null)
        return "-"
    return this.div(1000).toString() + " kW";
}


