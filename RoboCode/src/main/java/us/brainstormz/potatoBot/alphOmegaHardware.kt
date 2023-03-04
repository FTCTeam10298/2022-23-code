package us.brainstormz.potatoBot

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.hardware.rev.RevColorSensorV3
import com.qualcomm.robotcore.hardware.*
import org.firstinspires.ftc.robotcore.external.navigation.VoltageUnit
import us.brainstormz.hardwareClasses.EnhancedDCMotor
import us.brainstormz.hardwareClasses.MecanumHardware
import us.brainstormz.hardwareClasses.ThreeWheelOdometry

private const val s = "rEncoder"
private const val freeMove = false //for debugging. puts motors in float when stopped.

class AlphOmegaHardware : MecanumHardware {
    override lateinit var lFDrive: DcMotor
    override lateinit var rFDrive: DcMotor
    override lateinit var lBDrive: DcMotor
    override lateinit var rBDrive: DcMotor
    override lateinit var hwMap: HardwareMap

    override fun init(ahwMap: HardwareMap) {

        hwMap = ahwMap

        // Drivetrain
        lFDrive = hwMap["lFDrive"] as DcMotor
        rFDrive = hwMap["rFDrive"] as DcMotor
        lBDrive = hwMap["lBDrive"] as DcMotor
        rBDrive = hwMap["rBDrive"] as DcMotor
    }

}