package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlin.math.asin
import kotlin.math.pow
import kotlin.math.sqrt

class Depositor(private val lift: Lift, private val fourBar: FourBar, private val collector: Collector) {

    enum class PositionalState {
        AtTarget,
        FourBarAtTarget,
        LiftAtTarget,
        NotAtTarget
    }

    enum class EndEffectorXPresets(val inchesFromCenter: Double) {
        CollectFront(4.0),
        DepositFront(5.0)
    }

    enum class EndEffectorYPresets(val inchesFromGround: Double) {
        PreCollection(4.0),
        Collection(2.0),
        HighJunc(33.0),
        MidJunc(20.0),
        LowJunc(5.0),
        GroundJunc(33.0),
        Terminal(33.0)
    }

    fun moveEndEffectorBlocking(inchesFromCenter: Double, inchesFromGround: Double, linearOpMode: LinearOpMode, angleBarUp: Boolean = true) {
        while (linearOpMode.opModeIsActive()) {
            val depositorState = powerEndEffectorToward(inchesFromCenter, inchesFromGround, angleBarUp)

            if (depositorState == PositionalState.AtTarget)
                break
        }
    }

    fun powerEndEffectorToward(inchesFromCenter: Double, inchesFromGround: Double, angleBarUp: Boolean = true /*whether the bar should be angled up or down*/): PositionalState {
        val constrainedInFromCenter = inchesFromCenter.coerceIn(-fourBar.barLengthInch..fourBar.barLengthInch)


        val targetAngle = calc4barAngle(constrainedInFromCenter)

        val fourBarAtTarget = fourBar.goToPosition(targetAngle)


        val liftOffset = calcLiftOffset(constrainedInFromCenter) * if (angleBarUp) 1 else -1
        val targetInches = inchesFromGround - liftOffset + collector.heightInch

        val liftAtTarget = lift.setLiftPowerToward(targetInches)


        return when {
            fourBarAtTarget && liftAtTarget -> PositionalState.AtTarget
            fourBarAtTarget -> PositionalState.FourBarAtTarget
            liftAtTarget -> PositionalState.LiftAtTarget
            else -> PositionalState.NotAtTarget
        }
    }


    private fun calc4barAngle(inchesFromCenter: Double): Double = asin(inchesFromCenter/fourBar.barLengthInch)
    private fun calcLiftOffset(inchesFromCenter: Double): Double = sqrt(fourBar.barLengthInch.pow(2) - inchesFromCenter.pow(2))




//    fun does4barInterfere(fourBarTargetDeg: Double, fourBarCurrentDeg: Double, liftTargetInch: Double, liftCurrentInch: Double): Boolean {
//
//        return true
//    }
}