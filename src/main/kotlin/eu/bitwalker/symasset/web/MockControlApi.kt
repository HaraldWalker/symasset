/**
 * Sympower Cloud-to-Cloud Control API
 * API implemented by Sympower partners so Sympower platform can control their resources (power consuming or producing devices).
 *
 * The version of the OpenAPI document: 2.1.0
 * Contact: support@sympower.net
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package eu.bitwalker.symasset.web

import eu.bitwalker.symasset.service.AssetService
import eu.bitwalker.symasset.service.MeteringService
import eu.bitwalker.symasset.service.ResourceGroupService
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import net.sympower.control.api.server.models.ResourceLevelChangeRequest
import net.sympower.control.api.server.models.ResourceLevelChangeResponse
import net.sympower.control.api.server.models.TimestampedIntegerValue
import java.time.OffsetDateTime

fun Route.mockControlApi(assetService: AssetService, resourceGroupService: ResourceGroupService, meteringService: MeteringService) {

    route("/control/bulk/resources/targetLevelChanges") {
        post {
            val resourceLevelChangeRequest = call.receive<Array<ResourceLevelChangeRequest>>()
            val result = ArrayList<ResourceLevelChangeResponse>(resourceLevelChangeRequest.size)
            for (changeRequest in resourceLevelChangeRequest) {
                val changeResponse =
                    changeTargetLevelOfAsset(
                        assetService,
                        meteringService,
                        changeRequest.resourceAddress.groupNumber,
                        changeRequest.resourceAddress.number,
                        changeRequest.targetLevel
                    )
                result.add(changeResponse)
            }
            call.respond(result)
        }
    }

    route("/control/resourceGroups/{resourceGroupNumber}/resources/{resourceNumber}") {
        get {
            val resourceGroupNumber = call.parameters["resourceGroupNumber"]!!.toInt()
            val resourceNumber = call.parameters["resourceNumber"]!!.toInt()
            val asset =
                assetService.getAssetByResourceGroupNumberAndWireNumber(resourceGroupNumber, resourceNumber)
            call.respond(asset.resource)
        }
    }

    route("/control/resourceGroups/{resourceGroupNumber}") {
        get {
            val resourceGroupNumber = call.parameters["resourceGroupNumber"]?.toInt()
            val resources = resourceGroupService.getByNumber(resourceGroupNumber)
            call.respond(resources)
        }
    }

    route("/control/resourceGroups") {
        get {
            call.respond(resourceGroupService.getAll())
        }
    }

    route("/control/resourceGroups/{resourceGroupNumber}/resources/{resourceNumber}/targetLevelChanges") {
        post {
            val resourceGroupNumber = call.parameters["resourceGroupNumber"]!!.toInt()
            val resourceNumber = call.parameters["resourceNumber"]!!.toInt()
            val formParameters = call.receiveParameters()
            val targetLevel = formParameters["targetLevel"]!!.toInt()
            val changeResponse =
                changeTargetLevelOfAsset(assetService, meteringService, resourceGroupNumber, resourceNumber, targetLevel)
            call.respond(changeResponse)
        }
    }

}

private fun changeTargetLevelOfAsset(
    assetService: AssetService,
    meteringService: MeteringService,
    resourceGroupNumber: Int,
    resourceNumber: Int,
    newTargetLevel: Int
): ResourceLevelChangeResponse {
    val asset = assetService.getAssetByResourceGroupNumberAndWireNumber(resourceGroupNumber, resourceNumber)
    val now = OffsetDateTime.now()
    if (asset.resource.targetLevel.value != newTargetLevel) {
        meteringService.simulateMeter(asset) // to get a latest power meter value
        asset.status.previousLevel = asset.resource.targetLevel.value
        asset.status.targetLevelChangedAt = now
        asset.resource.targetLevel = TimestampedIntegerValue(newTargetLevel, now)
        asset.resource.level = TimestampedIntegerValue(newTargetLevel, now)
    }
    return ResourceLevelChangeResponse(asset.resource, newTargetLevel, now, now, true)
}
