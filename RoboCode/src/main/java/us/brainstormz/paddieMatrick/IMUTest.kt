package us.brainstormz.paddieMatrick

import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp

@TeleOp
class IMUTest: LinearOpMode() {

    override fun runOpMode() {
        // define initialization values for IMU, and then initialize it.
        val parameters = BNO055IMU.Parameters()
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES
//        parameters.mode = BNO055IMU.SensorMode.
        parameters.loggingEnabled = true
        parameters.loggingTag     = "IMU"
        parameters.accelUnit      = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC
//        parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode


        val imu:IMU = hardwareMap["imu"] as IMU
        imu.initialize(parameters)

        val calibrationData = imu.readCalibrationData()

//        imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES) To get the gyro in all 3 axis
//        imu.calibrationStatus
//        imu.readCalibrationData()
//        imu.writeCalibrationData(BNO055IMU.CalibrationData.deserialize("a"))

        while (opModeInInit() || opModeIsActive()) {
            telemetry.addLine("Current Time Millis: ${System.currentTimeMillis()}")
            telemetry.addLine(
                    "Imu: $imu" +
                    "Data:\n" +
                    "   Acceleration:\n" +
                    "       ${imu.acceleration}\n" +
                    "   Position:\n" +
                    "       ${imu.position}")
            telemetry.update()

        }

    }
}