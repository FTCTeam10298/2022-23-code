//package us.brainstormz.lankyKong
//
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
//import com.qualcomm.robotcore.eventloop.opmode.OpMode
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp
//import us.brainstormz.localizer.EncoderLocalizer
//import us.brainstormz.localizer.PositionAndRotation
//import us.brainstormz.motion.MecanumMovement
//import us.brainstormz.pid.PID
//import us.brainstormz.telemetryWizard.GlobalConsole
//
//@TeleOp
//class MovementTests: LinearOpMode() {
//
//    val console = GlobalConsole.newConsole(telemetry)
//
//    val hardware = LankyKongHardware()
//    val localizer = EncoderLocalizer(hardware)
//    val movement = MecanumMovement(localizer, hardware)
//
//    val target = PositionAndRotation(y = 20.0, x = 10.0)
//
//    override fun runOpMode() {
//        hardware.init(hardwareMap)
//
//        waitForStart()
//
//        movement.movementPID = PID(0.15, 0.000006, 0.0)
//        movement.goToPosition(target, this)
//    }
//
////    override fun loop() {
////        localizer.recalculatePositionAndRotation()
//////        console.display(2, "current pos: \n${localizer.currentPositionAndRotation()}")
////        movement.moveTowardTarget(target)
////    }
//}