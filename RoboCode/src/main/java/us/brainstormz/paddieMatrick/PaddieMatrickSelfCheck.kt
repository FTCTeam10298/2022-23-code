package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
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
        fun testMotor(motor: DcMotorEx): Double {
            motor.power = 0.01
            sleep(motorTime.toLong())
            var consumedPower = motor.getCurrent(CurrentUnit.MILLIAMPS)
            motor.power = 0.0
            return(consumedPower)

        /** INIT PHASE */
        hardware.init(hardwareMap)

        waitForStart()
        /** AUTONOMOUS  PHASE */
        //Motor Evolution: DCMotorSimple --> DCMotor --> DCMotorEx
        }

    }


}
