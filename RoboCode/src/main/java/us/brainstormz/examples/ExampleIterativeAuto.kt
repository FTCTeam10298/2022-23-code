package us.brainstormz.examples
//
//import com.qualcomm.robotcore.eventloop.opmode.OpMode
//import us.brainstormz.localizer.EncoderLocalizer
//import us.brainstormz.localizer.PositionAndRotation
//import us.brainstormz.motion.MecanumMovement
//import us.brainstormz.rataTony.Depositor
//import us.brainstormz.telemetryWizard.TelemetryConsole
//import us.brainstormz.rataTony.Depositor.LiftPos
//import us.brainstormz.rataTony.RataTonyHardware
//
////@Autonomous
//class ExampleIterativeAuto: OpMode() {
//
//    val console = TelemetryConsole(telemetry)
//    val hardware = RataTonyHardware()
//    val movement = MecanumMovement(EncoderLocalizer(hardware, console), hardware, console)
//    val depositor = Depositor(hardware, console)
//
//    override fun init() {
//        /** INIT PHASE */
//        hardware.init(hardwareMap)
//    }
//
//    var currentPhase = Phases.Move
//
//    enum class Phases {
//        Move,
//        RaiseLift
//    }
//
//    override fun loop() {
//        /** Autonomous */
//
//        when(currentPhase) {
//            Phases.Move -> {
//                val done = movement.moveTowardTarget(PositionAndRotation())
//                if (done)
//                    currentPhase = Phases.RaiseLift
//            }
//            Phases.RaiseLift -> {
//                val done = depositor.yTowardPosition(LiftPos.HighGoal.counts)
//            }
//        }
//
//
////        depositor.updateYPosition()
//    }
//}