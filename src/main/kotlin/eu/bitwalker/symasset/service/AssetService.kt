package eu.bitwalker.symasset.service

import eu.bitwalker.symasset.model.Asset
import eu.bitwalker.symasset.model.Settings
import eu.bitwalker.symasset.model.Status
import net.sympower.control.api.server.models.Resource
import net.sympower.control.api.server.models.ResourceAddress
import net.sympower.control.api.server.models.TimestampedIntegerValue
import net.sympower.metering.api.server.models.ActivePower
import net.sympower.metering.api.server.models.DeviceError
import net.sympower.metering.api.server.models.Meter
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class AssetService {

    private val assets = mutableListOf<Asset>()

    fun initializeAnalogWires(amount: Int) {
        repeat(amount) { addAnalogWire() }
    }

    fun initializeRelayWires(amount: Int) {
        repeat(amount) { addRelayWire() }
    }

    fun addAnalogWire(): Asset {
        return addWire(
            1,
            assets.filter { it.resource.type.equals(Resource.Type.analog) }.size + 1,
            type = Resource.Type.analog
        )
    }

    fun addRelayWire(): Asset {
        return addWire(
            2,
            assets.filter { it.resource.type.equals(Resource.Type.relay) }.size + 1,
            type = Resource.Type.relay
        )
    }

    private fun addWire(groupNumber: Int, number: Int, type: Resource.Type): Asset {
        val wireNumber = groupNumber * 100 + number
        val now = OffsetDateTime.now()
        val resource = Resource(
            ResourceAddress(groupNumber, wireNumber),
            type,
            TimestampedIntegerValue(0, now),
            TimestampedIntegerValue(0, now),
            "Wire $wireNumber",
            false
        )
        val meterNumber = getPowerMeters().size + 1
        val meter = Meter(
            meterNumber,
            "${resource.name} Meter $meterNumber",
            ActivePower(now, 1000, null, ActivePower.Unit.w),
            null
        )
        val wire = Asset(resource, meter, Settings(0, 10000000, 10000, 51), Status(0, now))
        assets.add(wire)
        return wire
    }

    fun getWires(): MutableList<Asset> {
        return assets
    }

    fun getAssetById(id: Int): Asset {
        return assets[id]
    }

    fun updateAssetSettings(id: Int, settings: Settings): Asset {
        assets[id].settings = settings
        return assets[id]
    }

    fun setMeterError(id: Int, error: eu.bitwalker.symasset.model.Error): Asset {
        assets[id].meter.error = DeviceError(DeviceError.Code.valueOf(error.code.value), OffsetDateTime.now(), error.message)
        return assets[id]
    }

    fun clearMeterError(id: Int): Asset {
        assets[id].meter.error = null
        return assets[id]
    }

    fun setWireError(id: Int, error: eu.bitwalker.symasset.model.Error): Asset {
        assets[id].resource.error = net.sympower.control.api.server.models.DeviceError(net.sympower.control.api.server.models.DeviceError.Code.valueOf(error.code.value), OffsetDateTime.now(), error.message)
        return assets[id]
    }

    fun clearWireError(id: Int): Asset {
        assets[id].resource.error = null
        return assets[id]
    }

    fun getResourceByResourceGroupNumber(id: Int?): List<Resource> {
        return (assets.filter { it.resource.address.groupNumber == id }.map { it.resource })
    }

    fun getAssetByResourceGroupNumberAndWireNumber(groupNumber: Int, wireNumber: Int): Asset {
        return assets.filter { it.resource.address.groupNumber == groupNumber && it.resource.address.number == wireNumber }
                .first()
    }

    fun getPowerMeterById(id: Int): Meter {
        val wire = assets.filter { it.meter.number == id }.first()
        return simulateMeter(wire)
    }

    fun getPowerMeters(): List<Meter> {
        return (assets.map { simulateMeter(it) })
    }

    fun simulateMeter(asset: Asset): Meter {
        val meter = asset.meter

        if (meter.error != null) {
            meter.activePower = null;
            return meter
        }

        var deviation = (0..asset.settings.maxDeviation).random()
        val availableCapacity = asset.settings.maxPower - asset.settings.minPower
        val targetLevelChange = asset.resource.targetLevel.value - asset.status.previousLevel

        // default consumption when we are not ramping up or down
        var expectedLevel = asset.resource.targetLevel.value

        if(targetLevelChange > 0) {
            // ramping up
            // val ratePerSecondInWatts = (availableCapacity * asset.settings.rampRate) / (100F * 60)
            val ratePerSecondInLevelSteps = (1000 * asset.settings.rampRate) / (100F * 60)
            val secondsSinceLevelChange = asset.status.targetLevelChangedAt.until(OffsetDateTime.now(), ChronoUnit.SECONDS)
            val rampingLevel = (secondsSinceLevelChange * ratePerSecondInLevelSteps + asset.status.previousLevel).toInt()
            if (rampingLevel < asset.resource.targetLevel.value) {
                expectedLevel = rampingLevel
            }
        }
        if(targetLevelChange < 0) {
            // ramping down
            val ratePerSecondInLevelSteps = (1000 * asset.settings.rampRate) / (100F * 60)
            val secondsSinceLevelChange = asset.status.targetLevelChangedAt.until(OffsetDateTime.now(), ChronoUnit.SECONDS)
            val rampingLevel = (asset.status.previousLevel - secondsSinceLevelChange * ratePerSecondInLevelSteps).toInt()
            if (rampingLevel > asset.resource.targetLevel.value) {
                expectedLevel = rampingLevel
            }
        }

        var consumption = (availableCapacity / 1000) * expectedLevel + asset.settings.minPower


        if (consumption - deviation > 0) {
            deviation *= intArrayOf(-1,1)[Random.nextInt(2)]
        }
        consumption += deviation
        val onePhase = consumption / 3
        val inPhases = mutableListOf<Int>(onePhase, onePhase, consumption - (onePhase * 2))
        meter.activePower = ActivePower(OffsetDateTime.now(), consumption, inPhases, ActivePower.Unit.w)
        return meter
    }

}