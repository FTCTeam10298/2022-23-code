package us.brainstormz.paddieMatrick

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.IMU
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference

@TeleOp
class IMUTest: LinearOpMode() {

    override fun runOpMode() {
        val imuOrientationOnRobot = RevHubOrientationOnRobot(RevHubOrientationOnRobot.LogoFacingDirection.RIGHT, RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD)
        val imuParameters = IMU.Parameters(imuOrientationOnRobot)

        val imu: IMU = hardwareMap["imu"] as IMU
        imu.initialize(imuParameters)
        imu.resetYaw()

        while (opModeInInit() || opModeIsActive()) {
            telemetry.addLine("Current Time Millis: ${System.currentTimeMillis()}")
            val orientation = imu.getRobotOrientation(AxesReference.INTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES)
            val x = orientation.firstAngle
            val y = orientation.secondAngle
            val z = orientation.thirdAngle
            val angularVelocity = imu.getRobotAngularVelocity(AngleUnit.DEGREES)

            telemetry.addLine(
                    "Imu Data:\n" +
                    "   Acceleration:\n" +
                    "       ${angularVelocity}\n" +
                    "   Position:\n" +
                    "       x= $x, y= $y, z= $z")
            telemetry.update()

        }

    }
}
