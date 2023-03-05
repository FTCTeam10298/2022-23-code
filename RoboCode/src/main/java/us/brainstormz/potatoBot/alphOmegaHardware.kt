package us.brainstormz.potatoBot

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.hardware.rev.RevColorSensorV3
import com.qualcomm.robotcore.hardware.*
import org.firstinspires.ftc.robotcore.external.navigation.VoltageUnit
import us.brainstormz.hardwareClasses.EnhancedDCMotor
import us.brainstormz.hardwareClasses.HardwareClass
import us.brainstormz.hardwareClasses.MecanumHardware
import us.brainstormz.hardwareClasses.ThreeWheelOdometry

private const val s = "rEncoder"
private const val freeMove = false //for debugging. puts motors in float when stopped.

class AlphOmegaHardware: HardwareClass {
    lateinit var someDrive: DcMotor
    lateinit var anotherDrive: DcMotor
    lateinit var improbabilityDrive: DcMotorEx
    override lateinit var hwMap: HardwareMap

    override fun init(ahwMap: HardwareMap) {

        hwMap = ahwMap

        // Drivetrain
        someDrive = hwMap["someDrive"] as DcMotorEx
        anotherDrive = hwMap["anotherDrive"]  as DcMotorEx
        improbabilityDrive = hwMap["impDrive"] as DcMotorEx
    }

}