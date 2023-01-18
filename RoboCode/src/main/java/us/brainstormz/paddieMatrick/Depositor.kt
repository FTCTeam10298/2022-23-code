package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DistanceSensor
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.pid.PID
import us.brainstormz.utils.MathHelps

class Depositor(private val hardware: PaddieMatrickHardware, private val fourBar: FourBar, private val collector: Collector, private val telemetry: Telemetry) {
    enum class FourBarDegrees(val degrees: Double) {
        Collecting(72.0),
        PreCollection(110.0),
        Vertical(180.0),
        PreDeposit(210.0),
        Deposit(270.0)
    }
    enum class LiftCounts(val counts: Int) {
        HighJunction(3900),
        MidJunction(2200),
        StackPreCollection(1200),
        LowJunction(650),
        SinglePreCollection(400),
        Collection(0),
        Bottom(0)
    }

    fun automatedDeposit(targetJunction: LiftCounts) {
        if (isConeInCollector()) {
            moveLift(targetJunction.counts)

            if (hardware.rightLift.currentPosition >= targetJunction.counts - 300) {
                moveFourBar(FourBarDegrees.Deposit.degrees)

                if (fourBar.current4BarDegrees() >= PaddieMatrickTeleOp.FourBarDegrees.Deposit.degrees - 5) {
                    hardware.collector.power = -1.0
                }
            } else {
                moveFourBar(FourBarDegrees.PreDeposit.degrees)
            }
        } else {
            // once cone drops go back to home
            moveFourBar(FourBarDegrees.Vertical.degrees)

            if (fourBar.current4BarDegrees() <= PaddieMatrickTeleOp.FourBarDegrees.Vertical.degrees + 5) {
                moveLift(LiftCounts.Bottom.counts)
            }
        }
    }

    var timeOfCollection = 0.0
    fun automatedCollection(multiCone: Boolean) {
        val preCollectLiftTarget = if (multiCone) LiftCounts.StackPreCollection else LiftCounts.SinglePreCollection

        if (!isConeInCollector()) {
            hardware.funnelLifter.position = collector.funnelDown

            moveDepositer(fourBarPosition = FourBarDegrees.Collecting, liftPosition = preCollectLiftTarget)

            if (isConeInFunnel()) {
                hardware.collector.power = 1.0
                moveDepositer(fourBarPosition = FourBarDegrees.Collecting, liftPosition = LiftCounts.Collection)
            } else {
                moveDepositer(fourBarPosition = FourBarDegrees.Collecting, liftPosition = preCollectLiftTarget)
            }
        } else {
            moveFourBar(FourBarDegrees.Vertical.degrees)

            if (fourBar.is4BarAtPosition(FourBarDegrees.Vertical.degrees)) {
                hardware.funnelLifter.position = collector.funnelUp
                val liftTarget = if (multiCone) LiftCounts.LowJunction.counts else LiftCounts.Bottom.counts
                moveLift(liftTarget)
            }
        }
    }

    private val fourBarSafeDegrees = 50.0..300.0
    fun moveFourBar(targetDegrees: Double): Boolean {
        val fourBarTarget = MathHelps.wrap360(targetDegrees).coerceIn(fourBarSafeDegrees)
        fourBar.goToPosition(fourBarTarget)
        return fourBar.is4BarAtPosition(fourBarTarget)
    }

    private val liftPID = PID(kp= 0.003, ki= 0.0)
    fun moveLift(targetCounts: Int): Boolean {
        val liftTarget = if (!hardware.liftLimitSwitch.state) {
            hardware.rightLift.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            hardware.rightLift.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            targetCounts.coerceAtLeast(0)
        } else {
            targetCounts
        }

        val liftPower = liftPID.calcPID(liftTarget.toDouble(), hardware.rightLift.currentPosition.toDouble())

        powerLift(liftPower)
        return isLiftAtPosition(targetCounts)
    }

    fun isLiftAtPosition(target: Int): Boolean {
        val accuracy = 100
        val targetRange = target - accuracy..target + accuracy
        return hardware.rightLift.currentPosition in targetRange
    }

    fun powerLift(power: Double) {
        hardware.leftLift.power = power
        hardware.rightLift.power = power // Direction reversed in hardware map
    }

    fun moveDepositer(fourBarPosition: FourBarDegrees, liftPosition: LiftCounts) {
        moveLift(liftPosition.counts)
        moveFourBar(fourBarPosition.degrees)
    }

    fun isConeInCollector(): Boolean {
        val minCollectedDistance = 56
        val collectedOpticalThreshold = 250 //doesnt work when cone is at an angle
        val collectedRedThreshold = 100

        val collectorDistance = hardware.collectorSensor.getDistance(DistanceUnit.MM)
        val collectorRawOptical = hardware.collectorSensor.rawOptical()
        val collectorRed = hardware.collectorSensor.red()

        return collectorDistance < minCollectedDistance// && collectorRawOptical > collectedOpticalThreshold// || collectorRed > collectedRedThreshold)
    }

    fun isConeInFunnel(): Boolean {
        val collectableDistance = 75
        val funnelBlueThreshold = 60
        val funnelRedThreshold = 60

        val red = hardware.funnelSensor.red()
        val blue = hardware.funnelSensor.blue()
        val distance = (hardware.funnelSensor as DistanceSensor).getDistance(DistanceUnit.MM)
//        telemetry.addLine("red: $red")
//        telemetry.addLine("blue: $blue")
//        telemetry.addLine("funnel distance: $distance")
        return distance < collectableDistance && (blue > funnelBlueThreshold || red > funnelRedThreshold)
    }




//    val angleLiftDownHightInch = fourBar.mountHeightInch + fourBar.barLengthInch

//    enum class PositionalState {
//        AtTarget,
//        FourBarAtTarget,
//        LiftAtTarget,
//        NotAtTarget
//    }

//    enum class EndEffectorXPresets(val inchesFromCenter: Double) {
//        CollectFront(4.0),
//        DepositFront(5.0)
//    }
//
//    enum class EndEffectorYPresets(val inchesFromGround: Double) {
//        PreCollection(4.0),
//        Collection(2.0),
//        HighJunc(33.0),
//        MidJunc(20.0),
//        LowJunc(5.0),
//        GroundJunc(33.0),
//        Terminal(33.0)
//    }

//    fun moveEndEffectorBlocking(inchesFromCenter: Double, inchesFromGround: Double, linearOpMode: LinearOpMode, angleBarUp: Boolean = true) {
//        while (linearOpMode.opModeIsActive()) {
//            val depositorState = powerEndEffectorToward(inchesFromCenter, inchesFromGround, angleBarUp)
//
//            if (depositorState == PositionalState.AtTarget)
//                break
//        }
//    }

//    fun powerEndEffectorToward(inchesFromCenter: Double, inchesFromGround: Double): PositionalState {
//        val constrainedInFromCenter = inchesFromCenter.coerceIn(-fourBar.barLengthInch..fourBar.barLengthInch)
//
//        val barIsAngledUp = inchesFromGround > angleLiftDownHightInch
//
//        val targetAngle = calc4barAngle(constrainedInFromCenter) + (if (!barIsAngledUp) 90 else 0 * if (inchesFromCenter > 0) 1 else -1)
//
//        val fourBarAtTarget = fourBar.goToPosition(targetAngle)
//
//        val fourBarCompensation = calcLiftOffset(constrainedInFromCenter) * if (barIsAngledUp) 1 else -1
//        val targetInches = inchesFromGround - fourBar.mountHeightInch - fourBarCompensation + collector.heightInch
//
//        val liftAtTarget = lift.setLiftPowerToward(targetInches)
//
//        telemetry.addLine("inchesFromCenter: $inchesFromCenter\ninchesFromGround: $inchesFromGround\n\nbarAngledUpSign: $barIsAngledUp\ntargetAngle: $targetAngle\n\nliftOffset: $fourBarCompensation\ntargetInches: $targetInches\nfourBarAtTarget: $fourBarAtTarget\nliftAtTarget: $liftAtTarget")
//        return when {
//            fourBarAtTarget && liftAtTarget -> PositionalState.AtTarget
//            fourBarAtTarget -> PositionalState.FourBarAtTarget
//            liftAtTarget -> PositionalState.LiftAtTarget
//            else -> PositionalState.NotAtTarget
//        }
//    }
//
//
//    private fun calc4barAngle(inchesFromCenter: Double): Double = Math.toDegrees(asin(inchesFromCenter/fourBar.barLengthInch))
//    private fun calcLiftOffset(inchesFromCenter: Double): Double = hypot(fourBar.barLengthInch, inchesFromCenter)// sqrt(fourBar.barLengthInch.pow(2) - inchesFromCenter.pow(2))




//    fun does4barInterfere(fourBarTargetDeg: Double, fourBarCurrentDeg: Double, liftTargetInch: Double, liftCurrentInch: Double): Boolean {
//
//        return true
//    }
}

//@TeleOp(name="Depositor Test", group="!")
//class DepositorTest: OpMode() {
//
//    val hardware = PaddieMatrickHardware()
//    val movement = MecanumDriveTrain(hardware)
//
//    val collector = Collector()
//    val fourBar = FourBar(telemetry)
//    val lift = Lift(telemetry)
//    val depositor = Depositor(lift, fourBar, collector, telemetry)
//
//    override fun init() {
//        hardware.init(hardwareMap)
//
//        collector.init(hardware.collector)
//        fourBar.init(hardware.left4Bar, hardware.right4Bar, hardware.encoder4Bar)
//        lift.init(hardware.leftLift, hardware.rightLift, hardware.liftLimitSwitch)
//    }
//
//
//    var depositorY = 0.0
//    var depositorZ = depositor.angleLiftDownHightInch + 1
//    override fun loop() {
//        telemetry.addLine("lift inches: ${lift.currentPosInches()}")
//        telemetry.addLine("\n4bar degrees: ${fourBar.current4BarDegrees()}\n")
//
//        depositorY += -gamepad2.right_stick_y.toDouble()
//        depositorZ += -gamepad2.left_stick_y.toDouble()
//
//        depositorY = depositorY.coerceIn(-fourBar.barLengthInch..fourBar.barLengthInch)
//        depositor.powerEndEffectorToward(depositorY, depositorZ)
//
////        depositor.powerEndEffectorToward(-5.0, 1.0)
//    }
//
//}