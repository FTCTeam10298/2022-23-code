package us.brainstormz.lankyKong

import us.brainstormz.examples.ExampleHardware

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.HardwareMap
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.hardwareClasses.MecanumHardware
import java.lang.Thread.sleep


@TeleOp
class encoderCounterTestLK /** Change Depending on robot */: OpMode() {

    val hardware = MainWheelEncoders() /** Change Depending on robot */
    val movement = MecanumDriveTrain(hardware)

    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
    }

    override fun loop() {
        /** TELE-OP PHASE */

        var lF: Int
        var lB: Int
        var rF: Int
        var rB: Int

        // DRONE DRUNK
        fun reportVals(){
             lF = hardware.lFDrive.currentPosition
             lB = hardware.lBDrive.currentPosition
             rF = hardware.rFDrive.currentPosition
             rB = hardware.rBDrive.currentPosition
        }


        hardware.rFDrive.power = 0.5
        telemetry.addLine("rFDrive Moving")
        sleep(5000)
        reportVals()
        hardware.rFDrive.power = 0.0
        hardware.lFDrive.power = 0.5
        telemetry.addLine("lFDrive Moving")
        sleep(5000)
        reportVals()
        hardware.lFDrive.power = 0.0
        hardware.rBDrive.power = 0.5
        telemetry.addLine("rBDrive Moving")
        sleep(5000)
        reportVals()
        hardware.rBDrive.power = 0.0
        hardware.lBDrive.power = 0.5
        telemetry.addLine("lBDrive moving")
        sleep(5000)
        reportVals()
        hardware.lBDrive.power = 0.0
    }
}

class MainWheelEncoders: MecanumHardware {
    override lateinit var lFDrive: DcMotor
    override lateinit var rFDrive: DcMotor
    override lateinit var lBDrive: DcMotor
    override lateinit var rBDrive: DcMotor
    override lateinit var hwMap: HardwareMap

    override fun init(ahwMap: HardwareMap){
        hwMap = ahwMap

        //Drivetrain
        lFDrive = hwMap["ctrl1"] as DcMotorEx // black
        rFDrive = hwMap["ctrl0"] as DcMotorEx // pink
        lBDrive = hwMap["ctrl2"] as DcMotorEx // blue
        rBDrive = hwMap["ctrl3"] as DcMotorEx // green
    }
}