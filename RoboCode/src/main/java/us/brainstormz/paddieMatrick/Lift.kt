package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.DcMotorEx
import us.brainstormz.pid.PID

class Lift(private val motor1: DcMotorEx, private val motor2: DcMotorEx, private val limitSwitch: AnalogInput) {

    private val maxInches = 33.0
    private val minInches = 0.0
    private val outOfBoundsPower = 0.5
    private val positionAccuracyIn = 0.5

    private val pid = PID()

    fun goToPositionBlocking(targetInches: Double, linearOpMode: LinearOpMode) {
        while (linearOpMode.opModeIsActive()) {

            val atTarget = setLiftPowerToward(targetInches)

            if (atTarget)
                break
        }
    }

    fun setLiftPowerToward(targetInches: Double): Boolean {
        val currentPosInches = currentPosInches()

        val powerPID = pid.calcPID(targetInches, currentPosInches)
        val constrainedPower = constrainPower(powerPID, targetInches)

        setLiftPower(constrainedPower)

        return currentPosInches in -positionAccuracyIn..positionAccuracyIn
    }

    fun currentPosInches(): Double {
        val averagedPosition = (motor1.currentPosition + motor2.currentPosition) / 2
        val correctedPosition = correctPosition(averagedPosition)
        return encoderCountsToInches(correctedPosition)
    }

    private fun constrainPower(powerIn: Double, targetInches: Double): Double {
        val travelRange = minInches .. maxInches

        return if (targetInches !in travelRange) {
                powerIn.coerceIn(-outOfBoundsPower..outOfBoundsPower)
        } else {
            powerIn
        }
    }

    private var zeroCount = 0
    private var limitSwitchWasPressed = false
    private fun correctPosition(currentCounts: Int): Int {
        if (isLimitSwitchPressed() && !limitSwitchWasPressed) {
            zeroCount = currentCounts
        }
        limitSwitchWasPressed = isLimitSwitchPressed()

        return currentCounts - zeroCount
    }

    private fun setLiftPower(powerIn: Double) {
        val powerOut = if (isLimitSwitchPressed())
            powerIn.coerceAtLeast(0.0)
        else
            powerIn

        motor1.power = powerOut
        motor2.power = powerOut
    }

    private fun encoderCountsToInches(counts: Int): Double = (counts / 150).toDouble()

    fun isLimitSwitchPressed(): Boolean {
        return limitSwitch.voltage > 10
    }
}