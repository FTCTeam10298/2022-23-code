package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.hardwareClasses.MecanumDriveTrain


//2 divisions of testing: fine tests (individual component) and gross tests
// Simplest test: check power pulled


@Autonomous(name="PaddieMatrickSelfCheck", group = "!")
class paddieMatrickSelfCheck/** Change Depending on robot */: LinearOpMode() {

    val hardware = PaddieMatrickHardware()
    val movement = MecanumDriveTrain(hardware)
    val fourBar = FourBar(telemetry)
    val motorTime: Long = 250 //in ms

    val funnel = Funnel()
    val collector = Collector()

    /** Change Depending on robot */

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        waitForStart()
        /** AUTONOMOUS  PHASE */
        fun testMotor(motor: DcMotor) {
            motor.power = 0.01
           sleep(motorTime.toLong())
            motor.power = 0.0
        }

    }


}
