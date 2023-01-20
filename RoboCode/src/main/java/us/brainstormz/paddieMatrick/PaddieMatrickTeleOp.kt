package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DistanceSensor
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.pid.PID
import us.brainstormz.utils.MathHelps
import kotlin.math.abs

@TeleOp(name= "PaddieMatrick Tele-op", group= "!")
class PaddieMatrickTeleOp: OpMode() {

    val hardware = PaddieMatrickHardware()
    val movement = MecanumDriveTrain(hardware)
    val fourBar = FourBar(telemetry)

    val collector = Collector()

    var fourBarTarget = 0.0
    val allowedZone = 50.0..300.0
    val fourBarSpeed = 120.0
    enum class fourBarModes {
        FOURBAR_PID,
        FOURBAR_MANUAL
    }
    var fourBarMode = fourBarModes.FOURBAR_PID
    companion object {
        var centerPosition = 110.0
    }
    enum class FourBarDegrees(val degrees: Double) {
        Collecting(72.0),
        PreCollection(110.0),
        Vertical(180.0),
        PreDeposit(210.0),
        Deposit(270.0)
    }

    val liftPID = PID(kp= 0.003, ki= 0.0)
    var liftTarget = 0.0
    val liftSpeed = 1200.0
    enum class LiftCounts(val counts: Int) {
        HighJunction(3900),
        MidJunction(2200),
        StackPreCollection(1200),
        LowJunction(650),
        SinglePreCollection(400),
        Collection(0),
        Bottom(0)
    }

    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
        hardware.odomRaiser1.position = 1.0
        hardware.odomRaiser2.position = 1.0
        fourBar.init(leftServo = hardware.left4Bar, rightServo = hardware.right4Bar, encoder = hardware.encoder4Bar)
        fourBarTarget = fourBar.current4BarDegrees()
        fourBar.pid = PID(kp= 0.011, kd= 0.0001)//0000001)
    }

    override fun start() {
        fourBarTarget = fourBar.current4BarDegrees()
    }

    var prevTime = System.currentTimeMillis()
    override fun loop() {
        /** TELE-OP PHASE */
        val dt = System.currentTimeMillis() - prevTime
        prevTime = System.currentTimeMillis()
        telemetry.addLine("dt: $dt")

        telemetry.addLine("limit switch state: ${hardware.liftLimitSwitch.state}")

        // DRONE DRIVE
        val antiTipModifier: Double = MathHelps.scaleBetween(hardware.rightLift.currentPosition.toDouble() + 1, 0.0..3000.0, 1.0..1.8)
        telemetry.addLine("antiTipModifier: $antiTipModifier")

        val slowSpeed = 0.3

//        val yInput = gamepad1.left_stick_y.toDouble()
//        val xInput =
        val rInput = gamepad1.right_stick_x.toDouble()
        val yInput = when {
            gamepad1.dpad_up -> {
                slowSpeed
            }
            gamepad1.dpad_down -> {
                -slowSpeed
            }
            else -> -gamepad1.left_stick_y.toDouble()
        }
        val xInput = when {
            gamepad1.dpad_left -> {
                slowSpeed
            }
            gamepad1.dpad_right -> {
                -slowSpeed
            }
            else -> -gamepad1.left_stick_x.toDouble()
        }


        val y = -yInput / antiTipModifier
        val x = xInput / antiTipModifier
        val r = -rInput * abs(rInput) //square the number and keep the sign
        movement.driveSetPower((y + x - r),
                               (y - x + r),
                               (y - x - r),
                               (y + x + r))

        // Four bar
        telemetry.addLine("fourbar position: ${fourBar.current4BarDegrees()}")
        telemetry.addLine("fourBarTarget: $fourBarTarget")

        when {
            gamepad2.x || (gamepad1.b && !gamepad1.start) -> {
                fourBarMode = fourBarModes.FOURBAR_PID
                fourBarTarget = FourBarDegrees.PreCollection.degrees
            }
            gamepad1.a || gamepad2.y && !gamepad1.start -> {} // dummy for preset elsewhere
            abs(gamepad2.right_stick_y) > 0.1 -> {
                fourBarMode = fourBarModes.FOURBAR_MANUAL
            }
            abs(gamepad2.right_stick_y) < 0.1 && fourBarMode == fourBarModes.FOURBAR_MANUAL -> {
                fourBarMode = fourBarModes.FOURBAR_PID
                fourBarTarget = fourBar.current4BarDegrees()
            }
        }

        //fourBarTarget += (gamepad2.right_stick_y.toDouble() * fourBarSpeed / dt)

        if (fourBarMode == fourBarModes.FOURBAR_PID) {
            fourBarTarget = MathHelps.wrap360(fourBarTarget).coerceIn(allowedZone)
            fourBar.goToPosition(fourBarTarget)
            telemetry.addLine("fourBarMode: PID")
        }
        else { // (fourBarMode == fourBarModes.FOURBAR_MANUAL
            fourBar.setServoPower(gamepad2.right_stick_y.toDouble())
            telemetry.addLine("fourBarMode: Manual")
        }

//        val fourBarPower = -gamepad2.right_stick_y.toDouble()
//                fourBarPID.calcPID(MathHelps.wrap360(fourBarTarget - (fourBar.current4BarDegrees())))

//        telemetry.addLine("fourBarPower: $fourBarPower")

//        hardware.left4Bar.power = fourBarPower
//        hardware.right4Bar.power = fourBarPower

        // Lift
        when {
            gamepad2.dpad_up-> {
                liftTarget = LiftCounts.HighJunction.counts.toDouble()

                fourBarMode = fourBarModes.FOURBAR_PID
                fourBarTarget = FourBarDegrees.PreDeposit.degrees
            }
            gamepad1.a || gamepad2.y && !gamepad1.start-> {
                if (isConeInCollector()) {
                    liftTarget = LiftCounts.HighJunction.counts.toDouble()

                    if (hardware.rightLift.currentPosition >= liftTarget - 300) {
                        fourBarMode = fourBarModes.FOURBAR_PID
                        fourBarTarget = FourBarDegrees.Deposit.degrees

                        if (fourBar.current4BarDegrees() >= FourBarDegrees.Deposit.degrees - 5) {
                            hardware.collector.power = -1.0
                        }
                    } else {
                        fourBarMode = fourBarModes.FOURBAR_PID
                        fourBarTarget = FourBarDegrees.PreDeposit.degrees
                    }
                } else {
                    // once cone drops go back to home
                    fourBarMode = fourBarModes.FOURBAR_PID
                    fourBarTarget = FourBarDegrees.Vertical.degrees

                    if (fourBar.current4BarDegrees() <= FourBarDegrees.Vertical.degrees + 5) {
                        liftTarget = LiftCounts.Bottom.counts.toDouble()
                    }
                }
            }
            gamepad2.dpad_left -> {
                liftTarget = LiftCounts.MidJunction.counts.toDouble()

                fourBarMode = fourBarModes.FOURBAR_PID
                fourBarTarget = FourBarDegrees.PreDeposit.degrees
            }
            gamepad2.dpad_right -> {
                liftTarget = LiftCounts.LowJunction.counts.toDouble()

                fourBarMode = fourBarModes.FOURBAR_PID
                fourBarTarget = FourBarDegrees.PreDeposit.degrees
            }
            gamepad2.dpad_down -> {

                liftTarget = LiftCounts.Bottom.counts.toDouble()

                fourBarMode = fourBarModes.FOURBAR_PID
                fourBarTarget = FourBarDegrees.PreCollection.degrees
            }
            gamepad2.a -> {
                liftTarget += (-0.5 * liftSpeed / dt)
            }
        }

        liftTarget += (-gamepad2.left_stick_y.toDouble() * liftSpeed / dt)

        liftTarget = if (!hardware.liftLimitSwitch.state) {
            hardware.rightLift.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            hardware.rightLift.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            liftTarget.coerceAtLeast(0.0)
        } else {
            liftTarget
        }

        val liftPower = liftPID.calcPID(liftTarget, hardware.rightLift.currentPosition.toDouble())

        telemetry.addLine("lift position: ${hardware.rightLift.currentPosition}")
        telemetry.addLine("liftTarget: $liftTarget")
        telemetry.addLine("liftPower: $liftPower")

        powerLift(liftPower)

        // Collector
        when {
            gamepad1.right_trigger > 0 || gamepad2.right_trigger > 0 -> {
                hardware.collector.power = 1.0
            }
            gamepad1.left_trigger > 0 || gamepad2.left_trigger > 0 -> {
                hardware.collector.power = -1.0
            }
            gamepad1.a || gamepad2.y -> {}
            else -> {
                hardware.collector.power = 0.0
            }
        }

        when {
            gamepad2.b -> {
                println("Auto Collection Disabled >:0 UwU OwO >::)")
                hardware.funnelLifter.position = collector.funnelDown
            }
            gamepad1.right_bumper || gamepad2.right_bumper -> {
                automatedCollection(multiCone = false)
            }
            gamepad1.left_bumper || gamepad2.left_bumper -> {
                automatedCollection(multiCone = true)
            }
            else -> {
                hardware.funnelLifter.position = collector.funnelUp
            }
        }

        telemetry.addLine("red: ${hardware.collectorSensor.red()}")
        telemetry.addLine("blue: ${hardware.collectorSensor.blue()}")
        telemetry.addLine("alpha: ${hardware.collectorSensor.alpha()}")
        telemetry.addLine("optical: ${hardware.collectorSensor.rawOptical()}")
        telemetry.addLine("distance: ${hardware.collectorSensor.getDistance(DistanceUnit.MM)}")
    }

    fun doTheAutoCollect(){

            fourBarMode = fourBarModes.FOURBAR_MANUAL
    }
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
            fourBarMode = fourBarModes.FOURBAR_PID
            fourBarTarget = FourBarDegrees.Vertical.degrees

            if (fourBar.is4BarAtPosition(FourBarDegrees.Vertical.degrees)) {
                hardware.funnelLifter.position = collector.funnelUp
                liftTarget = if (multiCone) LiftCounts.LowJunction.counts.toDouble() else LiftCounts.Bottom.counts.toDouble()
            }
        }
    }

    fun moveDepositer(fourBarPosition: FourBarDegrees, liftPosition: LiftCounts) {
        liftTarget = liftPosition.counts.toDouble()
        fourBarMode = fourBarModes.FOURBAR_PID
        fourBarTarget = fourBarPosition.degrees
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

    fun powerLift(power: Double) {
        hardware.leftLift.power = power
        hardware.rightLift.power = power // Direction reversed in hardware map
    }
}

//@TeleOp(name= "4bar Calibrator", group= "A")
class fourBarCalibrator: LinearOpMode() {

    val hardware = PaddieMatrickHardware()
    val fourBar = FourBar(telemetry)

    override fun runOpMode() {

        hardware.init(hardwareMap)
        fourBar.init(leftServo = hardware.left4Bar, rightServo = hardware.right4Bar, encoder = hardware.encoder4Bar)


        while (!isStarted && opModeInInit()) {
            telemetry.addLine("fourBar position: ${fourBar.current4BarDegrees()}")
        }

        PaddieMatrickTeleOp.centerPosition = fourBar.current4BarDegrees()
    }

}