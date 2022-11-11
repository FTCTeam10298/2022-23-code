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

    val fourBarPID = PID(kp= 0.02)//, ki= 0.000035)
    var fourBarTarget = 0.0
    val fourBarSpeed = 200.0
    companion object {
        var centerPosition = 110.0
    }
    val fourBarRange = 180.0

    val liftPID = PID(kp= 0.003, ki= 0.0)
    var liftTarget = 0.0
    val liftSpeed = 1200.0
    enum class LiftCounts(val counts: Int) {
        PreCollection(480),
        Collection(30),
        HighJunction(4200),
        MidJunction(2400),
        LowJunction(800)
    }

    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
        fourBar.init(leftServo = hardware.left4Bar, rightServo = hardware.right4Bar, encoder = hardware.encoder4Bar)
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

        val yInput = gamepad1.left_stick_y.toDouble()
        val xInput = gamepad1.left_stick_x.toDouble()
        val rInput = gamepad1.right_stick_x.toDouble()

        val y = -yInput / antiTipModifier
        val x = xInput / antiTipModifier
        val r = -rInput * abs(rInput)
        movement.driveSetPower((y + x - r),
                               (y - x + r),
                               (y - x - r),
                               (y + x + r))

        // Four bar
        telemetry.addLine("fourbar position: ${fourBar.current4BarDegrees()}")
//        fourBarTarget += (gamepad2.right_stick_y.toDouble() * fourBarSpeed / dt )
//        fourBarTarget = MathHelps.wrap360(fourBarTarget)
//        telemetry.addLine("fourBarTarget: $fourBarTarget")


        val fourBarPower = -gamepad2.right_stick_y.toDouble()
//                fourBarPID.calcPID(MathHelps.wrap360(fourBarTarget - (fourBar.current4BarDegrees())))

        telemetry.addLine("fourBarPower: $fourBarPower")


        hardware.left4Bar.power = fourBarPower
        hardware.right4Bar.power = fourBarPower

        // Lift
        liftTarget += (-gamepad2.left_stick_y.toDouble() * liftSpeed / dt)

        when {
            gamepad1.dpad_up || gamepad2.dpad_up -> {
                liftTarget = LiftCounts.HighJunction.counts.toDouble()
            }
            gamepad1.dpad_left || gamepad2.dpad_left -> {
                liftTarget = LiftCounts.MidJunction.counts.toDouble()
            }
            gamepad1.dpad_down || gamepad2.dpad_down -> {
                liftTarget = LiftCounts.LowJunction.counts.toDouble()
            }
//            gamepad1.a || gamepad2.a -> {
//                liftTarget = LiftCounts.PreCollection.counts.toDouble()
//            }
            gamepad1.x || gamepad2.x -> {
                liftTarget = LiftCounts.Collection.counts.toDouble()
            }
        }

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