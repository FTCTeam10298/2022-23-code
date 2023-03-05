package us.brainstormz.potatoBot

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.hardwareClasses.MecanumDriveTrain


//2 divisions of testing: fine tests (individual component) and gross tests
// Simplest test: check power pulled


@Autonomous(name="potatoBotSelfCheck", group = "!")
class paddieMatrickSelfCheck/** Change Depending on robot */: LinearOpMode() {

    val hardware =  AlphOmegaHardware()
    val motorTime: Long = 125 //in ms

    /** Change Depending on robot */

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        waitForStart()
        /** AUTONOMOUS  PHASE */
        //Note to Future Engineers: ALL MOTORS MUST BE DCMOTOREX TO BE SELF-TESTED  - @JAMES, THIS MEANS YOU
        fun testMotor(motor: DcMotorEx): String {
            motor.power = 0.01
            sleep(motorTime.toLong())
            var consumedPower = motor.getCurrent(CurrentUnit.MILLIAMPS)
            motor.power = 0.0
            //this is the  power draw without a motor, ambient voltage definition
            if (consumedPower > 30) {
                return ("M-PWR-OK")
            }  else {
                return ("M-PWR-FAIL")
            }
        }

        telemetry.addLine("Motor Current Draw: ${testMotor(hardware.someDrive as DcMotorEx)}")
        telemetry.update()
        sleep(5000)
    }


}
