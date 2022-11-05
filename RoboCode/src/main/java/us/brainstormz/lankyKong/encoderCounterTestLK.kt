package us.brainstormz.lankyKong

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import us.brainstormz.examples.ExampleHardware

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.hardwareClasses.MecanumHardware
import java.lang.Thread.sleep

@TeleOp
class quackQuackQuackQuack /** Change Depending on robot */: LinearOpMode() {

    val hardware = MainWheelEncoders()

    /** Change Depending on robot */
    val movement = MecanumDriveTrain(hardware)

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
        telemetry.addLine("Are You Still There?")
        telemetry.update()

        waitForStart()

        /** TELE-OP PHASE */

        var lF: Int = 0
        var lB: Int = 0
        var rF: Int = 0
        var rB: Int = 0

        // DRONE DRUNK
        fun reportVals() {
            lF = hardware.lFDrive.currentPosition
            lB = hardware.lBDrive.currentPosition
            rF = hardware.rFDrive.currentPosition
            rB = hardware.rBDrive.currentPosition
            telemetry.addLine("LF: $lF, LB: $lB, RF: $rF, RB: $rB")
            telemetry.update()
        }

        val motoring = listOf<DcMotor>(hardware.lFDrive, hardware.rFDrive, hardware.lBDrive, hardware.rBDrive)
        for (i in motoring) {
            i.power = 0.5
            telemetry.addLine("${i.portNumber} Moving")
            sleep(5000)
            i.power = 0.0
            reportVals()
            sleep(3000)
        }





//        hardware.lFDrive.power = 0.5
//        telemetry.addLine("lFDrive Moving")
//        sleep(5000)
//        hardware.lFDrive.power = 0.0
//        reportVals()
//        sleep(5000)

//
//
//        hardware.rBDrive.power = 0.5
//        telemetry.addLine("rBDrive Moving")
//        telemetry.update()
//        reportVals()
//        telemetry.update()
//        sleep(5000)
//        hardware.rBDrive.power = 0.0
//
//
//        hardware.lBDrive.power = 0.5
//        telemetry.addLine("lBDrive moving")
//        telemetry.update()
//        hardware.lBDrive.power = 0.0
//        reportVals()
//        telemetry.update()
//
//        reportVals()
        sleep(5000)
    }


    class MainWheelEncoders : MecanumHardware {
        override lateinit var lFDrive: DcMotor
        override lateinit var rFDrive: DcMotor
        override lateinit var lBDrive: DcMotor
        override lateinit var rBDrive: DcMotor
        override lateinit var hwMap: HardwareMap

        override fun init(ahwMap: HardwareMap) {
            hwMap = ahwMap

            //Drivetrain
            lFDrive = hwMap["ctrl1"] as DcMotorEx // black
            rFDrive = hwMap["ctrl0"] as DcMotorEx // pink
            lBDrive = hwMap["ctrl2"] as DcMotorEx // blue
            rBDrive = hwMap["ctrl3"] as DcMotorEx // green

            lFDrive.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            lBDrive.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            rFDrive.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            rBDrive.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER

            lFDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            lBDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            rFDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            rBDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

            lFDrive.direction  =  DcMotorSimple.Direction.REVERSE
            lBDrive.direction = DcMotorSimple.Direction.REVERSE
        }
    }
}