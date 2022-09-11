package us.brainstormz.rataTony

import com.qualcomm.robotcore.hardware.*
import us.brainstormz.hardwareClasses.EnhancedDCMotor
import us.brainstormz.hardwareClasses.MecOdometry

class RataTonyHardware: MecOdometry {
    override lateinit var lFDrive: DcMotor
    override lateinit var rFDrive: DcMotor
    override lateinit var lBDrive: DcMotor
    override lateinit var rBDrive: DcMotor

    override lateinit var lOdom: EnhancedDCMotor
    override lateinit var rOdom: EnhancedDCMotor
    override lateinit var cOdom: EnhancedDCMotor

    override lateinit var hwMap: HardwareMap

    override fun init(ahwMap: HardwareMap) {
        hwMap = ahwMap

//        DRIVE TRAIN
        lFDrive = hwMap["lFDrive"] as DcMotorEx
        rFDrive = hwMap["rFDrive"] as DcMotorEx
        lBDrive = hwMap["lBDrive"] as DcMotorEx
        rBDrive = hwMap["rBDrive"] as DcMotorEx

        rFDrive.direction = DcMotorSimple.Direction.FORWARD
        lFDrive.direction = DcMotorSimple.Direction.REVERSE
        rBDrive.direction = DcMotorSimple.Direction.FORWARD
        lBDrive.direction = DcMotorSimple.Direction.REVERSE


        rFDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
        lFDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
        rBDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
        lBDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT

//        Odom
        cOdom = EnhancedDCMotor(hwMap["lFDrive"] as DcMotor)
        rOdom = EnhancedDCMotor(hwMap["rFDrive"] as DcMotor)
        lOdom = EnhancedDCMotor(hwMap["lBDrive"] as DcMotor)

//        lOdom.reversed = false
        rOdom.reversed = true
//        cOdom.reversed = false

        cOdom.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        rOdom.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        lOdom.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER

        rBDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        lBDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        rFDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        lFDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

////        Depositor
//        liftMotor = hwMap["liftMotor"] as DcMotorEx
//        liftMotor.direction = DcMotorSimple.Direction.REVERSE
//        liftMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
//        liftMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
//        liftMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER
//
//        horiMotor = hwMap["horiMotor"] as DcMotorEx
//        horiMotor.direction = DcMotorSimple.Direction.FORWARD
//        horiMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
//        horiMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
//        horiMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
//
//        dropperServo = hwMap["dropper"] as Servo
//        dropperServo.direction = Servo.Direction.REVERSE
//        dropperServo.position = 0.0
//
//        xInnerLimit = hwMap["innerLimit"] as RevTouchSensor
//        yLowerLimit = hwMap["lowerLimit"] as RevTouchSensor
//
////        Collector
//        collector = hwMap["collector"] as DcMotor
//        collector.direction = DcMotorSimple.Direction.FORWARD
//        collector.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
//
//        collector2 = hwMap["collector2"] as DcMotor
//        collector2.direction = DcMotorSimple.Direction.FORWARD
//        collector2.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
//
////        Ducc
//        carouselSpinner = hwMap["duccSpinner"] as CRServo
//        carouselSpinner.direction = DcMotorSimple.Direction.FORWARD
//
//        rangeSensor = hwMap["ultrasonic"] as AsyncMB1242
//        rangeSensor.enable()
    }
}
