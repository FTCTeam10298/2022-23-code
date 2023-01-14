package us.brainstormz.paddieMatrick

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.hardware.rev.RevColorSensorV3
import com.qualcomm.robotcore.hardware.*
import org.firstinspires.ftc.robotcore.external.navigation.VoltageUnit
import us.brainstormz.hardwareClasses.EnhancedDCMotor
import us.brainstormz.hardwareClasses.MecanumHardware
import us.brainstormz.hardwareClasses.ThreeWheelOdometry

private const val s = "rEncoder"
private const val freeMove = false //for debugging. puts motors in float when stopped.

class PaddieMatrickHardware: MecanumHardware, ThreeWheelOdometry {
    override lateinit var lFDrive: DcMotor
    override lateinit var rFDrive: DcMotor
    override lateinit var lBDrive: DcMotor
    override lateinit var rBDrive: DcMotor

    lateinit var liftLimitSwitch: DigitalChannel
    lateinit var leftLift: DcMotorEx
    lateinit var rightLift: DcMotorEx

    lateinit var encoder4Bar: AnalogInput
    lateinit var left4Bar: CRServo
    lateinit var right4Bar: CRServo

    lateinit var rightOdomEncoder: DcMotor
    lateinit var leftOdomEncoder: DcMotor
    lateinit var centerOdomEncoder: DcMotor
    override lateinit var lOdom: EnhancedDCMotor
    override lateinit var rOdom: EnhancedDCMotor
    override lateinit var cOdom: EnhancedDCMotor
    lateinit var odomRaiser1: Servo
    lateinit var odomRaiser2: Servo
//    val odomRaiser1Up = 1.0
//    val odomRaiser1Up = 0.0

    lateinit var collectorSensor: RevColorSensorV3
    lateinit var collector: CRServo

    lateinit var allHubs: List<LynxModule>

    override lateinit var hwMap: HardwareMap

    override fun init(ahwMap: HardwareMap) {
        hwMap = ahwMap

        allHubs = hwMap.getAll(LynxModule::class.java)

        // Collector
        collector = hwMap["collector"] as CRServo
        collector.direction = DcMotorSimple.Direction.REVERSE
//        collectorSensor = hwMap["color"] as RevColorSensorV3

        // 4 Bar
        left4Bar = hwMap["left4Bar"] as CRServo
        right4Bar = hwMap["right4Bar"] as CRServo
        encoder4Bar = hwMap["encoder"] as AnalogInput

        left4Bar.direction = DcMotorSimple.Direction.REVERSE
        right4Bar.direction = DcMotorSimple.Direction.FORWARD


        // Lift
        leftLift = hwMap["leftLift"] as DcMotorEx
        rightLift = hwMap["rightLift"] as DcMotorEx
        liftLimitSwitch = hwMap["limitSwitch"] as DigitalChannelImpl

        rightLift.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        leftLift.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        rightLift.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        leftLift.direction = DcMotorSimple.Direction.FORWARD
        rightLift.direction = DcMotorSimple.Direction.REVERSE

        //Encoders/Deadwheels
        //3 = R; 2 = C; 1 = L
        //on D4-5 (Digital 4-5)
        leftOdomEncoder = leftLift// plugged in to the encoder port for the motor input that we are also using for the lift
        centerOdomEncoder = hwMap["cEncoder"] as DcMotor // empty motor port used for encoder/deadwheel
        rightOdomEncoder = hwMap["rEncoder"] as DcMotor

        rightOdomEncoder.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        leftOdomEncoder.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        centerOdomEncoder.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER

        lOdom = EnhancedDCMotor(leftOdomEncoder)
        rOdom = EnhancedDCMotor(rightOdomEncoder)
        cOdom = EnhancedDCMotor(centerOdomEncoder)

//        odomRaiser1 = hwMap["odomRaiser1"] as Servo
//        odomRaiser2 = hwMap["odomRaiser2"] as Servo


        // Drivetrain
        lFDrive = hwMap["lFDrive"] as DcMotor
        rFDrive = hwMap["rFDrive"] as DcMotor
        lBDrive = hwMap["lBDrive"] as DcMotor
        rBDrive = hwMap["rBDrive"] as DcMotor
        if(freeMove){
            lFDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            rFDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            lBDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            rBDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        }
        else{
            lFDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            rFDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            lBDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            rBDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        }
        rFDrive.direction = DcMotorSimple.Direction.REVERSE
        rBDrive.direction = DcMotorSimple.Direction.REVERSE
    }
     fun getVoltage(): Double = allHubs[0].getInputVoltage(VoltageUnit.VOLTS)

}