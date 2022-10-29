package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.pid.PID
import us.brainstormz.telemetryWizard.TelemetryConsole

@Autonomous
class PaddieMatrickAuto: LinearOpMode() {

    val hardware = PaddieMatrickHardware()
    val movement = EncoderDriveMovement(hardware, TelemetryConsole(telemetry))

    var aprilTagGX = AprilTagEx()

    val fourBarPID = PID(kp= 0.03)
    val fourBar = FourBar(telemetry)

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
//        fourBar.init(leftServo = hardware.left4Bar, rightServo = hardware.right4Bar, encoder = hardware.encoder4Bar)


        aprilTagGX.initAprilTag(hardwareMap, telemetry, this)


//        val fourBarTarget = fourBar.current4BarDegrees() - 70

        while (!isStarted && !isStopRequested) {
            aprilTagGX.runAprilTag(telemetry)

//            val fourBarPower = fourBarPID.calcPID(fourBarTarget, fourBar.current4BarDegrees())
//            hardware.left4Bar.power = fourBarPower
//            hardware.right4Bar.power = fourBarPower
        }

        waitForStart()

        val drivePower = 0.5
        val forwardDistance = 25.0
        val sideDistance = 30.0
        when (aprilTagGX.signalOrientation) {
            SignalOrientation.one -> {
                movement.driveRobotPosition(drivePower, forwardDistance, true)
                movement.driveRobotStrafe(drivePower, sideDistance, true)
            }
            SignalOrientation.two -> {
                movement.driveRobotPosition(drivePower, forwardDistance, true)
            }
            SignalOrientation.three -> {
                movement.driveRobotPosition(drivePower, forwardDistance, true)
                movement.driveRobotStrafe(drivePower, -sideDistance, true)
            }
        }

    }

}