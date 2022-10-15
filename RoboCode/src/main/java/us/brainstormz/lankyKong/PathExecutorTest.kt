//package us.brainstormz.lankyKong
//
//import com.qualcomm.hardware.lynx.LynxModule
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp
//import us.brainstormz.path.BezierCurve
////import us.brainstormz.path.BezierCurve
////import posePlanner.Point3D
//import us.brainstormz.localizer.EncoderLocalizer
//import us.brainstormz.localizer.PositionAndRotation
//import us.brainstormz.motion.MecanumMovement
//import us.brainstormz.path.BezierPath
//import us.brainstormz.pathexecution.MecanumPathExecutor
//import us.brainstormz.telemetryWizard.GlobalConsole
//
//@TeleOp
//class PathExecutorTest: LinearOpMode() {
//
//    val console = GlobalConsole.newConsole(telemetry)
//
//    val hardware = LankyKongHardware()
//    val localizer = EncoderLocalizer(hardware)
//    val movement = MecanumMovement(localizer, hardware)
//    val pathExecutor = MecanumPathExecutor(movement)
//
//    override fun runOpMode() {
//        hardware.cachingMode = LynxModule.BulkCachingMode.OFF
//        hardware.init(hardwareMap)
//
//        hardwareMap.allDeviceMappings.forEach { m ->
//            println("HW: ${m.deviceTypeClass} ${m.entrySet().map{it.key}.joinToString(",")}")
//        }
//
//        waitForStart()
//
//        val path = BezierPath(
//            BezierCurve(listOf(
//            PositionAndRotation(),
//            PositionAndRotation(10.0, 0.0, 0.0),
//            PositionAndRotation(10.0, 10.0, 0.0),
//            PositionAndRotation(10.0, 10.0, 10.0)
//        ))
//        )
//
//        pathExecutor.doPath(path)
//    }
//}