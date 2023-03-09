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

//2 divisions of testing: fine tests (individual component) and gross tests
// Simplest test: check power pulled


@Autonomous(name="potatoBotMotorMeter", group = "!")
class motorCurrentGetter/** Change Depending on robot */: LinearOpMode() {

    val hardware =  AlphOmegaHardware()
    val motorTime: Long = 125 //in ms

    /** Change Depending on robot */

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        waitForStart()
        /** AUTONOMOUS  PHASE */
        //Note to Future Engineers: ALL MOTORS MUST BE DCMOTOREX TO BE SELF-TESTED  - @JAMES, THIS MEANS YOU
        fun testMotor(motor: DcMotorEx): Double {
            var passivePower = motor.getCurrent(CurrentUnit.MILLIAMPS)
            motor.power = 0.01
            sleep(motorTime.toLong())
            var consumedPower = motor.getCurrent(CurrentUnit.MILLIAMPS)
            motor.power = 0.0
            return (consumedPower - passivePower)  //calculating DELTAPower, change in power - final - initial
        }

        telemetry.addLine("someDrive Motor Current Draw: ${testMotor(hardware.someDrive as DcMotorEx)} mA")
        telemetry.addLine("anotherDrive Motor Current Draw: ${testMotor(hardware.anotherDrive as DcMotorEx)} mA")
        telemetry.addLine("impDrive Motor Current Draw: ${testMotor(hardware.improbabilityDrive as DcMotorEx)} mA")
        telemetry.addLine("infDrive Motor Current Draw: ${testMotor(hardware.infinityDrive as DcMotorEx)} mA")
        telemetry.update()
        sleep(5000)
    }


}

@Autonomous(name="potatoBotMotorMeter")
class motorCurrentDemo/** Change Depending on robot */: LinearOpMode() {

    val hardware =  AlphOmegaHardware()
    val motorTime: Long = 125 //in ms

    /** Change Depending on robot */

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        waitForStart()
        /** AUTONOMOUS  PHASE */
        //Note to Future Engineers: ALL MOTORS MUST BE DCMOTOREX TO BE SELF-TESTED  - @JAMES, THIS MEANS YOU
        fun testMotor(motor: DcMotorEx): Double {
            motor.power = 0.01
            sleep(motorTime)
            var consumedPower = motor.getCurrent(CurrentUnit.MILLIAMPS)
            motor.power = 0.0
            return (consumedPower)
        }

        telemetry.addLine("Motor Current Draw: ${testMotor(hardware.anotherDrive as DcMotorEx)} mA")
        telemetry.update()
        sleep(5000)
    }


}
