package us.brainstormz.potatoBot

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import kotlinx.coroutines.channels.Channel
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.hardwareClasses.MecanumDriveTrain


//2 divisions of testing: fine tests (individual component) and gross tests
// Simplest test: check power pulled


@Autonomous(name="potatoBotSelfCheck", group = "!")
class paddieMatrickSelfCheck/** Change Depending on robot */: LinearOpMode() {
    enum class Status {
        OK,
        FAIL
    }
    class MotorAndStatus(val motor: DcMotorEx, val powerStatus: Status, val encoderStatus: Status)


    val hardware = AlphOmegaHardware()
    val motorTime: Long = 125 //in ms

    lateinit var motors: List<DcMotorEx>

    /** Change Depending on robot */

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
        motors = hardware.hwMap.getAll(DcMotorEx::class.java)


        waitForStart()
        telemetry.addLine("TESTING..........")
        /** AUTONOMOUS  PHASE */


        //Note to Future Engineers: ALL MOTORS MUST BE DcMotorEx TO BE SELF-TESTED  - @JAMES, THIS MEANS YOU
        fun motorPowerTest (motor: DcMotorEx): Status {
            motor.power = 0.01
            sleep(motorTime.toLong())
            var consumedPower = motor.getCurrent(CurrentUnit.MILLIAMPS)
            motor.power = 0.0
            //this is the  power draw without a motor, ambient voltage definition
            if (consumedPower > 30) {
                return Status.OK//("M-PWR-OK")
            }  else {
                return Status.FAIL//("M-PWR-FAIL")
            }
        }
        fun motorEncoderUpdateTest (motor: DcMotorEx): Status {
            var initialPos: Int = motor.currentPosition
            motor.power = 0.1
            sleep(motorTime.toLong() * 2)
            motor.power = 0.0
            var finalPos = motor.currentPosition
            //this is the  power draw without a motor, ambient voltage definition
            if (finalPos - initialPos > 30) {
                return Status.OK//("M-PWR-OK")
            }  else {
                return Status.FAIL//("M-PWR-FAIL")
            }
        }

        val listOfStatus = motors.map { motor ->
            val powerStatus = motorPowerTest(motor)
            val encoderStatus = motorEncoderUpdateTest(motor)
            MotorAndStatus(motor, powerStatus, encoderStatus)
        }
        listOfStatus.forEach{motorAndStatus ->
            val index = listOfStatus.indexOf(motorAndStatus)

            val motorName = hardwareMap.getNamesOf(motorAndStatus.motor)

            telemetry.addLine("${motorName}: PWR ${motorAndStatus.powerStatus} TX ${motorAndStatus.encoderStatus}")

        }
        telemetry.update()
        sleep(5000)
    }


}
