package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import us.brainstormz.examples.ExampleHardware
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.telemetryWizard.TelemetryConsole


//2 divisions of testing: fine tests (individual component) and gross tests
// Simplest test: check power pulled


@Autonomous(name="PaddieMatrickSelfCheck", group = "!")
class paddieMatrickSelfCheck/** Change Depending on robot */: LinearOpMode() {

    val hardware = PaddieMatrickHardware()
    val movement = MecanumDriveTrain(hardware)
    val fourBar = FourBar(telemetry)

    val funnel = Funnel()
    val collector = Collector()

    /** Change Depending on robot */

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        waitForStart()
        /** AUTONOMOUS  PHASE */
    }


}
