package eu.bitwalker.symasset.service

import eu.bitwalker.symasset.model.Asset
import net.sympower.metering.api.server.models.ActivePower
import net.sympower.metering.api.server.models.Meter
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class MeteringService {

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