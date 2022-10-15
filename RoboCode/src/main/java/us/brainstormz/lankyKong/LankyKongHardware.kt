//package us.brainstormz.lankyKong
//
//import com.qualcomm.hardware.lynx.LynxModule
//import com.qualcomm.robotcore.hardware.*
//import com.qualcomm.robotcore.hardware.configuration.LynxConstants
//import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
////import org.outoftheboxrobotics.neutrinoi2c.MB1242.AsyncMB1242
//import us.brainstormz.hardwareClasses.SmartLynxModule
//import us.brainstormz.hardwareClasses.HardwareClass
//import us.brainstormz.hardwareClasses.MecanumHardware
//
//class LankyKongHardware: HardwareClass, MecanumHardware {
//    override lateinit var hwMap: HardwareMap
//
//    var cachingMode = LynxModule.BulkCachingMode.MANUAL
//    lateinit var allHubs: List<LynxModule>
//
////    Drivetrain
//    override lateinit var lFDrive: DcMotor
//    override lateinit var rFDrive: DcMotor
//    override lateinit var lBDrive: DcMotor
//    override lateinit var rBDrive: DcMotor
//
//    lateinit var frontDistance: AsyncMB1242
//    lateinit var backDistance: AsyncMB1242
//
////    Depositor
//    lateinit var liftMotor: DcMotorEx
//    lateinit var horiMotor: DcMotorEx
//    lateinit var dropperServo: Servo
//    lateinit var dropperColor: ColorSensor
//
////    Collectors
//    val colorThreshold = 470
//    val collectorJamAmps = 7.5
//    lateinit var collector: DcMotorEx
//    lateinit var collector2: DcMotorEx
//
////    Ducc Spinners
//    lateinit var duccSpinner1: CRServo
////    lateinit var duccSpinner2: CRServo
//
//    val cameraName = "Webcam 1"
//
//    override fun init(ahwMap: HardwareMap) {
//        hwMap = ahwMap
//        allHubs = hwMap.getAll(LynxModule::class.java)
//
////        Drivetrain
//        lFDrive = hwMap["ctrl1"] as DcMotorEx // black
//        rFDrive = hwMap["ctrl0"] as DcMotorEx // pink
//        lBDrive = hwMap["ctrl2"] as DcMotorEx // blue
//        rBDrive = hwMap["ctrl3"] as DcMotorEx // green
//
//        rFDrive.direction = DcMotorSimple.Direction.FORWARD
//        lFDrive.direction = DcMotorSimple.Direction.REVERSE
//        rBDrive.direction = DcMotorSimple.Direction.FORWARD
//        lBDrive.direction = DcMotorSimple.Direction.REVERSE
//
//        rFDrive.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
//        lFDrive.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
//        rBDrive.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
//        lBDrive.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
//
//        rFDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
//        lFDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
//        rBDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
//        lBDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
//
//        rFDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
//        lFDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
//        rBDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
//        lBDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
//
//        frontDistance = hwMap["frontDistance"] as AsyncMB1242
//        frontDistance.enable()
//        backDistance = hwMap["backDistance"] as AsyncMB1242
//        backDistance.enable()
//
////        Depositor
//        liftMotor = hwMap["ex2"] as DcMotorEx
//        liftMotor.direction = DcMotorSimple.Direction.REVERSE
//        liftMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
//        liftMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
//        liftMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER
//
//        horiMotor = hwMap["ex3"] as DcMotorEx
//        horiMotor.direction = DcMotorSimple.Direction.REVERSE
//        horiMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
//        horiMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
//        horiMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
//        horiMotor.setCurrentAlert(4.0, CurrentUnit.AMPS)
//
//        dropperServo = hwMap["0"] as Servo
//        dropperServo.direction = Servo.Direction.REVERSE
//        dropperServo.position = DepositorLK.DropperPos.Closed.posValue
//
//        dropperColor = hwMap["dropperSensor"] as ColorSensor
//        dropperColor.enableLed(true)
//
//
////        Collectors
//        collector = hwMap["ex0"] as DcMotorEx
//        collector.direction = DcMotorSimple.Direction.FORWARD
//        collector.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
//        collector.setCurrentAlert(collectorJamAmps, CurrentUnit.AMPS)
//
//        collector2 = hwMap["ex1"] as DcMotorEx
//        collector2.direction = DcMotorSimple.Direction.FORWARD
//        collector2.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
//        collector2.setCurrentAlert(collectorJamAmps, CurrentUnit.AMPS)
//
////        Ducc Spiners
//        duccSpinner1 = hwMap["1"] as CRServo
//        duccSpinner1.direction = DcMotorSimple.Direction.FORWARD
//        duccSpinner1.power = 0.0
////
////        duccSpinner2 = hwMap["duccSpinner2"] as CRServo
////        duccSpinner2.direction = DcMotorSimple.Direction.FORWARD
//    }
//
//    fun clearHubCache() {
//        allHubs.forEach {
//            it.clearBulkCache()
//        }
//    }
//}