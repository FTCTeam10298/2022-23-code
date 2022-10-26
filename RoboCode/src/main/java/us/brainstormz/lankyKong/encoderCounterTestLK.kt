package us.brainstormz.lankyKong

import us.brainstormz.examples.ExampleHardware

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.HardwareMap
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.hardwareClasses.MecanumHardware


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

        // DRONE DRUNK
        var lF = hardware.lFDrive.currentPosition
        var lB = hardware.lBDrive.currentPosition
        var rf = hardware.rFDrive.currentPosition
        var rb = hardware.rBDrive.currentPosition

        telemetry.addLine(" lF: $lF, lB: $lB, rF: $rf, rB: $rb")
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