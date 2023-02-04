package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import kotlin.math.*


@TeleOp(name= "TestFieldCentric", group= "!")
class TestFieldCentric: OpMode() {

    val hardware = PaddieMatrickHardware()
    val movement = MecanumDriveTrain(hardware)

    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
        hardware.odomRaiser1.position = 1.0
        hardware.odomRaiser2.position = 1.0
    }

    override fun loop() {
        val y = gamepad1.left_stick_y.toDouble()

        val x = -gamepad1.left_stick_x * 1.1 // Counteract imperfect strafing

        val rx = -gamepad1.right_stick_x.toDouble()
        val ry = -gamepad1.right_stick_y.toDouble()

        val botHeading: Double = hardware.imu.robotYawPitchRollAngles.getYaw(AngleUnit.RADIANS)

        val atan = atan2(rx, ry)
        val head = botHeading
        fun mod(x: Double, y: Double) = (x%y) * (y/y)
        val rad = if (gamepad1.right_stick_x != 0.0f || gamepad1.right_stick_y != 0.0f) (head-atan)%(2*PI) else 0.0

        movement.driveFieldCentric(x, y, rad, botHeading)

        telemetry.addLine("atan: $atan")
        telemetry.addLine("head: $head")
        telemetry.addLine("rad: $rad")
//
//        // Rotate the movement direction counter to the bot's rotation
//        val rotX = x * cos(-botHeading) - y * sin(-botHeading)
//        val rotY = x * sin(-botHeading) + y * cos(-botHeading)
//
//
//        // Denominator is the largest motor power (absolute value) or 1
//        // This ensures all the powers maintain the same ratio, but only when
//        // at least one is out of the range [-1, 1]
//        val denominator = 1//(abs(rotY) + abs(rotX) + abs(r)).coerceAtLeast(1.0)
//
//        movement.driveSetPower((rotY + rotX + r) / denominator,
//                (rotY - rotX - r) / denominator,
//                (rotY - rotX + r) / denominator,
//                (rotY + rotX - r) / denominator)
        telemetry.update()
    }
}