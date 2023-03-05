package us.brainstormz.potatoBot.demos

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.potatoBot.AlphOmegaHardware


//2 divisions of testing: fine tests (individual component) and gross tests
// Simplest test: check power pulled


@Autonomous(name="potatoBotEncoderMeter", group = "!")
class encoderPosDemo/** Change Depending on robot */: LinearOpMode() {

    val hardware =  AlphOmegaHardware()
    val motorTime: Long = 125 //in ms

    /** Change Depending on robot */

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        waitForStart()
        /** AUTONOMOUS  PHASE */
        //This thing
        fun encoderUpdateTest (motor: DcMotorEx): Int {
            var initialPos: Int = motor.currentPosition
            motor.power = 0.1
            sleep(motorTime.toLong() * 2)
            motor.power = 0.0
            var finalPos: Int = motor.currentPosition
            //this is the change in encoder position
            return(finalPos - initialPos)
        }

        telemetry.addLine("Change in Encoder Position: ${encoderUpdateTest(hardware.someDrive as DcMotorEx)}")
        telemetry.update()
        sleep(5000)
    }


}
