package us.brainstormz.paddieMatrick

import androidx.annotation.FloatRange
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.utils.MathHelps
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.hypot

class Depositor(private val lift: Lift, private val fourBar: FourBar, private val collector: Collector, private val telemetry: Telemetry) {

    val angleLiftDownHightInch = fourBar.mountHeightInch + fourBar.barLengthInch

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

//    fun moveEndEffectorBlocking(inchesFromCenter: Double, inchesFromGround: Double, linearOpMode: LinearOpMode, angleBarUp: Boolean = true) {
//        while (linearOpMode.opModeIsActive()) {
//            val depositorState = powerEndEffectorToward(inchesFromCenter, inchesFromGround, angleBarUp)
//
//            if (depositorState == PositionalState.AtTarget)
//                break
//        }
//    }

    fun powerEndEffectorToward(inchesFromCenter: Double, inchesFromGround: Double): PositionalState {
        val constrainedInFromCenter = inchesFromCenter.coerceIn(-fourBar.barLengthInch..fourBar.barLengthInch)

        val barIsAngledUp = inchesFromGround > angleLiftDownHightInch

        val targetAngle = calc4barAngle(constrainedInFromCenter) + (if (!barIsAngledUp) 90 else 0 * if (inchesFromCenter > 0) 1 else -1)

        val fourBarAtTarget = fourBar.goToPosition(targetAngle)

        val fourBarCompensation = calcLiftOffset(constrainedInFromCenter) * if (barIsAngledUp) 1 else -1
        val targetInches = inchesFromGround - fourBar.mountHeightInch - fourBarCompensation + collector.heightInch

        val liftAtTarget = lift.setLiftPowerToward(targetInches)

        telemetry.addLine("inchesFromCenter: $inchesFromCenter\ninchesFromGround: $inchesFromGround\n\nbarAngledUpSign: $barIsAngledUp\ntargetAngle: $targetAngle\n\nliftOffset: $fourBarCompensation\ntargetInches: $targetInches\nfourBarAtTarget: $fourBarAtTarget\nliftAtTarget: $liftAtTarget")
        return when {
            fourBarAtTarget && liftAtTarget -> PositionalState.AtTarget
            fourBarAtTarget -> PositionalState.FourBarAtTarget
            liftAtTarget -> PositionalState.LiftAtTarget
            else -> PositionalState.NotAtTarget
        }
    }


    private fun calc4barAngle(inchesFromCenter: Double): Double = Math.toDegrees(asin(inchesFromCenter/fourBar.barLengthInch))
    private fun calcLiftOffset(inchesFromCenter: Double): Double = hypot(fourBar.barLengthInch, inchesFromCenter)// sqrt(fourBar.barLengthInch.pow(2) - inchesFromCenter.pow(2))




//    fun does4barInterfere(fourBarTargetDeg: Double, fourBarCurrentDeg: Double, liftTargetInch: Double, liftCurrentInch: Double): Boolean {
//
//        return true
//    }
}

@TeleOp(name="Depositor Test", group="!")
class DepositorTest: OpMode() {

    val hardware = PaddieMatrickHardware()
    val movement = MecanumDriveTrain(hardware)

    val collector = Collector()
    val fourBar = FourBar(telemetry)
    val lift = Lift(telemetry)
    val depositor = Depositor(lift, fourBar, collector, telemetry)

    override fun init() {
        hardware.init(hardwareMap)

        collector.init(hardware.collector)
        fourBar.init(hardware.left4Bar, hardware.right4Bar, hardware.encoder4Bar)
        lift.init(hardware.leftLift, hardware.rightLift, hardware.liftLimitSwitch)
    }


    var depositorY = 0.0
    var depositorZ = depositor.angleLiftDownHightInch + 1
    override fun loop() {
        telemetry.addLine("lift inches: ${lift.currentPosInches()}")
        telemetry.addLine("\n4bar degrees: ${fourBar.current4BarDegrees()}\n")

        depositorY += -gamepad2.right_stick_y.toDouble()
        depositorZ += -gamepad2.left_stick_y.toDouble()

        depositorY = depositorY.coerceIn(-fourBar.barLengthInch..fourBar.barLengthInch)
        depositor.powerEndEffectorToward(depositorY, depositorZ)

//        depositor.powerEndEffectorToward(-5.0, 1.0)
    }

}