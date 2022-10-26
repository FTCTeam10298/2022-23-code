package us.brainstormz.paddieMatrick

import com.qualcomm.hardware.rev.RevColorSensorV3
import com.qualcomm.robotcore.hardware.*
import us.brainstormz.hardwareClasses.MecanumHardware

class PaddieMatrickHardware: MecanumHardware {
    override lateinit var lFDrive: DcMotor
    override lateinit var rFDrive: DcMotor
    override lateinit var lBDrive: DcMotor
    override lateinit var rBDrive: DcMotor

    lateinit var liftLimitSwitch: AnalogInput
    lateinit var leftLift: DcMotorEx
    lateinit var rightLift: DcMotorEx

    lateinit var encoder4Bar: AnalogInput
    lateinit var left4Bar: CRServo
    lateinit var right4Bar: CRServo

    lateinit var collectorSensor: RevColorSensorV3
    lateinit var collectorFront: CRServo
    lateinit var collectorBack: CRServo

    override lateinit var hwMap: HardwareMap

    override fun init(ahwMap: HardwareMap) {
        hwMap = ahwMap

        // Collector
//        collectorFront = hwMap["collector1"] as CRServo
//        collectorBack = hwMap["collector2"] as CRServo
//        collectorSensor = hwMap["color"] as RevColorSensorV3

        // 4 Bar
        left4Bar = hwMap["left4Bar"] as CRServo
        right4Bar = hwMap["right4Bar"] as CRServo
        encoder4Bar = hwMap["encoder"] as AnalogInput

        left4Bar.direction = DcMotorSimple.Direction.FORWARD
        right4Bar.direction = DcMotorSimple.Direction.REVERSE

        // Lift
        leftLift = hwMap["leftLift"] as DcMotorEx
        rightLift = hwMap["rightLift"] as DcMotorEx
//        liftLimitSwitch = hwMap["limitSwitch"] as AnalogInput

        leftLift.direction = DcMotorSimple.Direction.FORWARD
        rightLift.direction = DcMotorSimple.Direction.REVERSE

        // Drivetrain
//        lFDrive = hwMap["lFDrive"] as DcMotor
//        rFDrive = hwMap["rFDrive"] as DcMotor
//        lBDrive = hwMap["lBDrive"] as DcMotor
//        rBDrive = hwMap["rBDrive"] as DcMotor
//        lFDrive.direction = DcMotorSimple.Direction.REVERSE
//        lBDrive.direction = DcMotorSimple.Direction.REVERSE
    }

}