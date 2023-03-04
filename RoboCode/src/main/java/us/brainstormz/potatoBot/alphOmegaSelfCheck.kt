package us.brainstormz.potatoBot

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.hardwareClasses.MecanumDriveTrain


//2 divisions of testing: fine tests (individual component) and gross tests
// Simplest test: check power pulled


@Autonomous(name="potatoBotSelfCheck", group = "!")
class paddieMatrickSelfCheck/** Change Depending on robot */: LinearOpMode() {

    val hardware =  AlphOmegaHardware()
    val movement = MecanumDriveTrain(hardware)
    val motorTime: Long = 250 //in ms

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
        testMotor(hardware)

    }


}
