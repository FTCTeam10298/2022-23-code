package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.telemetryWizard.TelemetryConsole

//@Autonomous
class PaddieMatrickAuto/** Change Depending on robot */: LinearOpMode() {

    val hardware = PaddieMatrickHardware()/** Change Depending on robot */
    val movement = EncoderDriveMovement(hardware, TelemetryConsole(telemetry))

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        waitForStart()
        /** AUTONOMOUS  PHASE */
        movement.driveRobotPosition(power = 1.0, inches = 20.0, smartAccel = true)
        movement.driveRobotStrafe(power = 1.0, inches = 20.0, smartAccel = true)
        movement.driveRobotTurn(power = 1.0, degree = 20.0, smartAccel = true)
    }

}