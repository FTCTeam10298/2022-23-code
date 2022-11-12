package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.pid.PID
import us.brainstormz.utils.MathHelps
import kotlin.math.abs

@TeleOp(name= "PaddieMatrick Tele-op", group= "!")
class PaddieMatrickTeleOp: OpMode() {

    val hardware = PaddieMatrickHardware()
    val movement = MecanumDriveTrain(hardware)
    val fourBar = FourBar(telemetry)

    var fourBarTarget = 0.0
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
        Horizontal(100.0),
        Depositing(220.0),
        Collecting(90.0)
    }


    val liftPID = PID(kp= 0.003, ki= 0.0)
    var liftTarget = 0.0
    val liftSpeed = 1200.0
    enum class LiftCounts(val counts: Int) {
        PreCollection(0),
        Collection(0),
        HighJunction(3900),
        MidJunction(2400),
        LowJunction(0)
    }

    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
        fourBar.init(leftServo = hardware.left4Bar, rightServo = hardware.right4Bar, encoder = hardware.encoder4Bar)
        fourBarTarget = fourBar.current4BarDegrees()
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
        val r = -rInput * abs(rInput)
        movement.driveSetPower((y + x - r),
                               (y - x + r),
                               (y - x - r),
                               (y + x + r))

        // Four bar
        telemetry.addLine("fourbar position: ${fourBar.current4BarDegrees()}")
        telemetry.addLine("fourBarTarget: $fourBarTarget")

        when {
            gamepad2.x -> {
                fourBarMode = fourBarModes.FOURBAR_PID
                fourBarTarget = FourBarDegrees.Horizontal.degrees
            }
            gamepad2.y -> {
                fourBarMode = fourBarModes.FOURBAR_PID
                fourBarTarget = FourBarDegrees.Depositing.degrees
            }
            gamepad2.right_bumper -> {
                fourBarMode = fourBarModes.FOURBAR_PID
                fourBarTarget = FourBarDegrees.Collecting.degrees
            }
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
            fourBarTarget = MathHelps.wrap360(fourBarTarget).coerceIn(50.0..300.0)
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
            gamepad2.dpad_up -> {
                liftTarget = LiftCounts.HighJunction.counts.toDouble()
            }
            gamepad2.dpad_left -> {
                liftTarget = LiftCounts.MidJunction.counts.toDouble()
            }
            gamepad2.dpad_down -> {
                liftTarget = LiftCounts.LowJunction.counts.toDouble()
            }
            gamepad2.left_bumper -> {
                liftTarget += (-0.5 * liftSpeed / dt)
            }
        }

        liftTarget += (-gamepad2.left_stick_y.toDouble() * liftSpeed / dt)

        liftTarget = if (!hardware.liftLimitSwitch.state) {
            hardware.rightLift.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            hardware.rightLift.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            liftTarget.coerceAtLeast(hardware.rightLift.currentPosition.toDouble())
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
            else -> {
                hardware.collector.power = 0.0
            }
        }


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