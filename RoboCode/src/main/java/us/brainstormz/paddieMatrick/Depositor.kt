package us.brainstormz.paddieMatrick

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import us.brainstormz.pid.PID
import us.brainstormz.telemetryWizard.GlobalConsole
import us.brainstormz.utils.MathHelps

class Depositor(private val hardware: PaddieMatrickHardware, private val fourBar: FourBar, private val collector: Collector, private val telemetry: Telemetry) {
    enum class FourBarDegrees(val degrees: Double) {
        Collecting(66.0),
        PreCollection(110.0),
        Vertical(180.0),
        PreDeposit(220.0),
        DepositOk(250.0),
        DepositTarget(259.0)
    }
    //Old tooth count: 30
    //New tooth count: 46
    object Tooth { const val oldToNewCountsConversion = 0.652173913043478}

    enum class LiftCounts(val counts: Int) {
        HighJunction((3900 * Tooth.oldToNewCountsConversion).toInt()),
        Detection((3100 * Tooth.oldToNewCountsConversion).toInt()),
        MidJunction((2200 * Tooth.oldToNewCountsConversion).toInt()),
        StackPreCollection((1160 * Tooth.oldToNewCountsConversion).toInt()),
        LowJunction((750 * Tooth.oldToNewCountsConversion).toInt()),
        SinglePreCollection((680 * Tooth.oldToNewCountsConversion).toInt()),
        Collection(0),
        Bottom(0)
    }

    //fix lift pid
    //dampen 4 bar more
    //dont go for 4th cone
    //dont drop early
    //wait for cone to fully leave collector

    fun automatedDeposit(targetJunction: LiftCounts) {
        if (isConeInCollector()) {
            moveLift(targetJunction.counts)

            if (hardware.rightLift.currentPosition >= targetJunction.counts - 300) {
                moveFourBar(FourBarDegrees.DepositTarget.degrees)

                if (fourBar.current4BarDegrees() >= FourBarDegrees.DepositTarget.degrees - 5) {
                    hardware.collector.power = -1.0
                }
            } else {
                moveFourBar(FourBarDegrees.PreDeposit.degrees)
            }
        } else {
            // once cone drops go back to home
            moveFourBar(FourBarDegrees.Vertical.degrees)

            if (fourBar.current4BarDegrees() <= FourBarDegrees.Vertical.degrees + 5) {
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

    var liftPID = PID(kp= 0.0045, ki= 0.00000003, kd= 0.075)
//    private val liftPID = PID(kp= 0.006, ki= 0.0)
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

    var accuracy = 100
    fun isLiftAtPosition(target: Int): Boolean {
        val targetRange = target - accuracy..target + accuracy
        return hardware.rightLift.currentPosition in targetRange
    }

    fun powerLift(power: Double) {
        hardware.leftLift.power = power
        hardware.rightLift.power = power // Direction reversed in hardware map
        hardware.extraLift.power = power
    }

    fun moveDepositer(fourBarPosition: FourBarDegrees, liftPosition: LiftCounts) {
        moveLift(liftPosition.counts)
        moveFourBar(fourBarPosition.degrees)
    }

    fun isConeInCollector(): Boolean {
        val minCollectedDistance = 45
        val collectedOpticalThreshold = 250 //doesnt work when cone is at an angle
        val collectedRedThreshold = 100

        val collectorDistance = hardware.collectorSensor.getDistance(DistanceUnit.MM)
        val collectorRawOptical = hardware.collectorSensor.rawOptical()
        val collectorRed = hardware.collectorSensor.red()

        return collectorDistance < minCollectedDistance// && collectorRawOptical > collectedOpticalThreshold// || collectorRed > collectedRedThreshold)
    }

    fun isConeInFunnel(): Boolean {
        val collectableDistance = 30
        val opticalThreshold = 2046
        val funnelBlueThreshold = 60
        val funnelRedThreshold = 60

        val red = hardware.funnelSensor.red()
        val blue = hardware.funnelSensor.blue()
        val optical = hardware.funnelSensor.rawOptical()
        val distance = hardware.funnelSensor.getDistance(DistanceUnit.MM)
//        telemetry.addLine("red: $red")
//        telemetry.addLine("blue: $blue")
//        telemetry.addLine("funnel distance: $distance")
        return distance < collectableDistance || optical >= opticalThreshold // && (blue > funnelBlueThreshold || red > funnelRedThreshold)
    }

}


@TeleOp(name= "Lift PID Tuning")
class LiftPIDTuning: LinearOpMode() {
    @Config
    object LiftPIDsAndTarget {
        @JvmField var kp = 0.005
        @JvmField var ki = 0.00000002
        @JvmField var kd = 0.075

        @JvmField var targetCounts = 0

        @JvmField var precisionCounts = 0
    }

    val hardware = PaddieMatrickHardware()
    val console = GlobalConsole.newConsole(telemetry)

    var targetCounts = LiftPIDsAndTarget.targetCounts

    override fun runOpMode() {
        hardware.init(hardwareMap)

        val dashboard = FtcDashboard.getInstance()
        val multipleTelemetry = MultipleTelemetry(telemetry, dashboard.telemetry)
        val collector = Collector()
        val fourBar = FourBar(telemetry)
        fourBar.init(leftServo = hardware.left4Bar, rightServo = hardware.right4Bar, encoder = hardware.encoder4Bar)
        val depositor = Depositor(collector = collector, fourBar = fourBar, hardware = hardware, telemetry = telemetry)

        depositor.liftPID = PID(LiftPIDsAndTarget.kp, LiftPIDsAndTarget.ki, LiftPIDsAndTarget.kp)
        depositor.accuracy = LiftPIDsAndTarget.precisionCounts

        multipleTelemetry.addData("isLiftAtPosition: ", 0)
        multipleTelemetry.addData("currentPosition: ", 0)
        multipleTelemetry.addData("power: ", 0)
        multipleTelemetry.addData("current: ", 0)
        multipleTelemetry.addData("targetCounts: ", 0)
        multipleTelemetry.update()
        waitForStart()

        while (opModeIsActive()) {
            depositor.moveLift(targetCounts)
            multipleTelemetry.addData("isLiftAtPosition: ", depositor.isLiftAtPosition(targetCounts))
            multipleTelemetry.addData("currentPosition: ", hardware.rightLift.currentPosition)
            multipleTelemetry.addData("power: ", hardware.rightLift.power)
            multipleTelemetry.addData("current: ", hardware.rightLift.getCurrent(CurrentUnit.AMPS))
            multipleTelemetry.addData("targetCounts: ", targetCounts)
            multipleTelemetry.update()
        }
    }
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