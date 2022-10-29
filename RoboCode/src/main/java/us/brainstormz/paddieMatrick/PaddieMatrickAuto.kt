package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.telemetryWizard.TelemetryConsole

@Autonomous
class PaddieMatrickAuto: LinearOpMode() {

    val hardware = PaddieMatrickHardware()
    val movement = EncoderDriveMovement(hardware, TelemetryConsole(telemetry))

    var aprilTagGX = AprilTagEx()

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)



        aprilTagGX.initAprilTag(hardwareMap, telemetry, this)

        waitForStart()

        when (aprilTagGX.signalOrientation) {
            SignalOrientation.one -> {
                movement.driveRobotPosition(0.8, 20.0, true)
            }
            SignalOrientation.two -> {
                movement.driveRobotPosition(0.8, 20.0, true)
            }
            SignalOrientation.three -> {
                movement.driveRobotPosition(0.8, 20.0, true)
            }
        }

    }

}