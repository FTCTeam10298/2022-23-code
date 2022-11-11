package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import kotlin.math.asin
import kotlin.math.pow
import kotlin.math.sqrt

class Depositor(private val lift: Lift, private val fourBar: FourBar, private val collector: Collector, private val telemetry: Telemetry) {

    private val fourBarMountHeightIn = 10.5

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


        val targetAngle = calc4barAngle(constrainedInFromCenter)

        val fourBarAtTarget = fourBar.goToPosition(targetAngle)


        val liftOffset = calcLiftOffset(constrainedInFromCenter) * if (inchesFromGround > fourBarMountHeightIn) 1 else -1
        val targetInches = inchesFromGround - fourBarMountHeightIn - liftOffset + collector.heightInch

        val liftAtTarget = lift.setLiftPowerToward(targetInches)

        telemetry.addLine("inchesFromCenter: $inchesFromCenter\ninchesFromGround: $inchesFromGround\ntargetAngle: $targetAngle\nliftOffset: $liftOffset\ntargetInches: $targetInches\nfourBarAtTarget: $fourBarAtTarget\nliftAtTarget: $liftAtTarget")
        return when {
            fourBarAtTarget && liftAtTarget -> PositionalState.AtTarget
            fourBarAtTarget -> PositionalState.FourBarAtTarget
            liftAtTarget -> PositionalState.LiftAtTarget
            else -> PositionalState.NotAtTarget
        }
    }


    private fun calc4barAngle(inchesFromCenter: Double): Double = Math.toDegrees(asin(inchesFromCenter/fourBar.barLengthInch))
    private fun calcLiftOffset(inchesFromCenter: Double): Double = sqrt(fourBar.barLengthInch.pow(2) - inchesFromCenter.pow(2))




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
    var depositorZ = 0.0
    override fun loop() {
        telemetry.addLine("lift inches: ${lift.currentPosInches()}")
        telemetry.addLine("4bar degrees: ${fourBar.current4BarDegrees()}")

        depositorY += -gamepad2.right_stick_y.toDouble()
        depositorZ += -gamepad2.left_stick_y.toDouble()

        depositor.powerEndEffectorToward(depositorY, depositorZ)
    }

}