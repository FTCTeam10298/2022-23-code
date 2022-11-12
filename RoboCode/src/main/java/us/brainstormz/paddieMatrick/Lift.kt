package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DigitalChannel
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.pid.PID
import kotlin.math.PI

class Lift(private val telemetry: Telemetry) {

    private lateinit var leftMotor: DcMotorEx
    private lateinit var rightMotor: DcMotorEx
    private lateinit var limitSwitch: DigitalChannel

    private val maxInches = 31.0
    private val minInches = 0.0
    private val maxTravelRange = minInches .. maxInches
    private val outOfBoundsPower = 0.5
    private val positionAccuracyIn = 2.0

    private val countsPerMotorRotation = 537.7
    private val rotationsPerMM = 114 / PI
    private val countsPerInch = countsPerMotorRotation * (rotationsPerMM * 25.4)

    private val pid = PID(kp= 0.005, ki= 0.0)//PID(kp= 0.02, ki= 0.0)

    fun init(leftMotor: DcMotorEx, rightMotor: DcMotorEx, limitSwitch: DigitalChannel) {
        this.leftMotor = leftMotor
        this.rightMotor = rightMotor
        this.limitSwitch = limitSwitch
    }

//    fun goToPositionBlocking(targetInches: Double, linearOpMode: LinearOpMode) {
//        while (linearOpMode.opModeIsActive()) {
//
//            val atTarget = setLiftPowerToward(targetInches)
//
//            if (atTarget)
//                break
//        }
//    }


    fun setLiftPowerToward(targetInches: Double): Boolean {
        val currentPosInches = currentPosInches()

        val error = targetInches - currentPosInches

        val pidPower = pid.calcPID(error)

        val constrainedPower = limitPowerForOutOfBounds(pidPower, targetInches)

        setLiftPower(constrainedPower)

        return error in -positionAccuracyIn..positionAccuracyIn
    }

    fun currentPosInches(): Double {
        val averagedPosition = rightMotor.currentPosition
        telemetry.addLine("liftEncoderCounts: $averagedPosition")
        return encoderCountsToInches(averagedPosition)
//        val correctedPosition = correctPosition(averagedPosition)
//        return encoderCountsToInches(correctedPosition)
    }

    private fun limitPowerForOutOfBounds(powerIn: Double, targetInches: Double): Double {
        return if (targetInches !in maxTravelRange) {
            powerIn.coerceIn(-outOfBoundsPower..outOfBoundsPower)
        } else {
            powerIn
        }
    }

//    private var zeroCount = 0
    private var limitSwitchWasPressed = false
    private fun correctPosition(/*currentCounts: Int*/) {
        if (isLimitSwitchPressed() && !limitSwitchWasPressed) {
            rightMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            rightMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER
//            zeroCount = currentCounts
        }
        limitSwitchWasPressed = isLimitSwitchPressed()

//        return currentCounts - zeroCount
    }

    private fun setLiftPower(powerIn: Double) {
        val powerOut = if (isLimitSwitchPressed())
            powerIn.coerceAtLeast(0.0)
        else
            powerIn

        correctPosition()

        telemetry.addLine("powerOut: $powerOut")

        leftMotor.power = powerOut
        rightMotor.power = powerOut
    }

    private fun encoderCountsToInches(counts: Int): Double {
        telemetry.addLine("countsPerMotorRotation: $countsPerMotorRotation\nrotationsPerMM: $rotationsPerMM\ncountsPerInch: $countsPerInch")
        return counts * 0.006//* countsPerInch
    }

    fun isLimitSwitchPressed(): Boolean {
        return !limitSwitch.state
    }
}

//fun main() {
//    val lift = Lift()
//
//    val targetInches = 20.0
//
//    lift.setLiftPowerToward(targetInches)
//
//}