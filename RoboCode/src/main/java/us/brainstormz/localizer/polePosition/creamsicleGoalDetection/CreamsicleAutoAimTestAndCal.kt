package us.brainstormz.choivico.creamsicleGoalDetection

//import us.brainstormz.choivico.buttonHelper.ButtonHelper
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.HardwareDevice
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
//import us.brainstormz.choivico.openCvAbstraction.OpenCvAbstraction
import org.opencv.imgproc.Imgproc
import org.openftc.easyopencv.OpenCvCamera
import org.openftc.easyopencv.OpenCvCameraFactory
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.localizer.polePosition.creamsicleGoalDetection.CreamsicleGoalDetector
//import us.brainstormz.choivico.robotCode.ChoiVicoHardware
//import us.brainstormz.choivico.robotCode.hardwareClasses.EncoderDriveMovement
//import us.brainstormz.choivico.telemetryWizard.TelemetryConsole
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.telemetryWizard.TelemetryConsole


@Autonomous(name="Creamsicle Test and Calibration", group="Tests")
class CreamsicleAutoAimTestAndCal : OpMode() {
    //    val hardware = ChoiVicoHardware()
    val console = TelemetryConsole(telemetry)
    val opencv = OpenCvAbstraction(this)
////    val goalDetector = CreamsicleGoalDetector(console)
//
//    //    val aimer = UltimateGoalAimer(console, goalDetector, hardware)
////    val movement = EncoderDriveMovement(hardware, console)
//    val font = Imgproc.FONT_HERSHEY_COMPLEX
//    var camera: OpenCvCamera? = null
//

    private val cameraNameInMap = "Webcam 1"
    override fun init() {
        val creamsicleGoal = CreamsicleGoalDetector(console)
        opencv.cameraName = cameraNameInMap
        opencv.init(hardwareMap)
        opencv.start()
        opencv.onNewFrame(creamsicleGoal::scoopFrame)
    }
//
////    private var varBeingEdited: CreamsicleGoalDetector.NamedVar = goalDetector.goalColor.L_H
////    fun render() {
////        console.display(2, "Active Var; ${varBeingEdited.name}")
////        console.display(4, "${varBeingEdited.value}")
////    }
//
//    override fun init_loop() {
//        println("Hi!")
////        if (XbuttonHelper.stateChanged(gamepad1.x) && gamepad1.x) {
////            console.display(3, "TrainerMODE; ${goalDetector.displayMode}")
////            when (goalDetector.displayMode) {
////                "frame" -> goalDetector.displayMode = "mask"
////                "mask" -> goalDetector.displayMode = "kernel"
////                "kernel" -> goalDetector.displayMode = "frame"
////            }
////            render()
////        }
////
////
////        when {
////            DpadHelper.stateChanged(gamepad1.dpad_left) && gamepad1.dpad_left -> {
////                when (varBeingEdited) {
////                    goalDetector.goalColor.L_H -> varBeingEdited = goalDetector.goalColor.L_S
////                    goalDetector.goalColor.L_S -> varBeingEdited = goalDetector.goalColor.L_V
////                    goalDetector.goalColor.L_V -> varBeingEdited = goalDetector.goalColor.U_H
////                    goalDetector.goalColor.U_H -> varBeingEdited = goalDetector.goalColor.U_S
////                    goalDetector.goalColor.U_S -> varBeingEdited = goalDetector.goalColor.U_V
////                    goalDetector.goalColor.U_V -> varBeingEdited = goalDetector.goalColor.L_H
////                }
////                render()
////            }
////        }
////
////        if (YbuttonHelper.stateChanged(gamepad1.y) && gamepad1.y) {
////            console.display(1, "Vals Zeroed")
////            goalDetector.goalColor.L_H.value = 0.0
////            goalDetector.goalColor.L_S.value = 0.0
////            goalDetector.goalColor.L_V.value = 0.0
////            goalDetector.goalColor.U_H.value = 0.0
////            goalDetector.goalColor.U_S.value = 0.0
////            goalDetector.goalColor.U_V.value = 0.0
////            render()
////        }
////
////        if (RbumperHelper.stateChanged(gamepad1.right_bumper) && gamepad1.right_bumper) {
////            varBeingEdited.value += 5
////            render()
////        }
////        if (LbumperHelper.stateChanged(gamepad1.left_bumper) && gamepad1.left_bumper) {
////            varBeingEdited.value -= 5
////            render()
////        }
////
////        if (AbuttonHelper.stateChanged(gamepad1.a) && gamepad1.a) {
////            goalDetector.goalColor.L_H.value = 0.0
////            goalDetector.goalColor.L_S.value = 0.0
////            goalDetector.goalColor.L_V.value = 0.0
////            goalDetector.goalColor.U_H.value = 255.0
////            goalDetector.goalColor.U_S.value = 255.0
////            goalDetector.goalColor.U_V.value = 255.0
////            console.display(1, "Vals Squonked")
////            render()
//    }
//
//

    override fun loop() {
        //val theCam:HardwareDevice? = hardwareMap.get(cameraNameInMap)
        //println("oh joy, another iteration with our camera ?? $theCam")

//        aimer.updateAimAndAdjustRobot()
    }
}

