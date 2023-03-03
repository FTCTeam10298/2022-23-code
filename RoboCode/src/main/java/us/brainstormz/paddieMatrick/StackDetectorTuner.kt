package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import org.openftc.easyopencv.OpenCvCamera
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.localizer.StackDetector
import us.brainstormz.localizer.StackDetectorVars
import us.brainstormz.localizer.polePosition.creamsicleGoalDetection.CreamsicleConfig
import us.brainstormz.localizer.polePosition.creamsicleGoalDetection.CreamsicleGoalDetector
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.telemetryWizard.TelemetryConsole


@Autonomous(name = "Stack Detector Tuner", group = "Tests")
class StackDetectorTuner: OpMode() {
    data class ControllerStatus(val yPressed: Boolean,
                                val aPressed: Boolean,
                                val xPressed: Boolean,
                                val dpadLeftPressed: Boolean,
                                val rBumperPressed: Boolean,
                                val lBumperPressed: Boolean)

    val console = TelemetryConsole(telemetry)
    val opencv = OpenCvAbstraction(this)

    private val stackDetectorVars = StackDetectorVars(StackDetector.TargetHue.RED, StackDetector.Mode.FRAME)
    private val detector = StackDetector(stackDetectorVars, telemetry)

    private val cameraNameInMap = "backCam"
    override fun init() {
        opencv.cameraName = cameraNameInMap
        opencv.cameraOrientation = OpenCvCameraRotation.UPRIGHT
        opencv.init(hardwareMap)
        opencv.start()
        opencv.onNewFrame(detector::processFrame)
    }

    private var previousLoopControllerStatus: ControllerStatus = ControllerStatus(
            yPressed = false,
            aPressed = false,
            xPressed = false,
            dpadLeftPressed = false,
            rBumperPressed = false,
            lBumperPressed = false)
    private var varBeingEdited: StackDetector.NamedVar = detector.goalColor.L_H
    override fun init_loop() {
        telemetry.addLine( "Detected Position: ${detector.detectedPosition(1000)}")

        if (gamepad1.x && !previousLoopControllerStatus.xPressed) {
            telemetry.addLine("TrainerMODE; ${stackDetectorVars.displayMode}")
            when (stackDetectorVars.displayMode) {
                StackDetector.Mode.FRAME -> stackDetectorVars.displayMode = StackDetector.Mode.MASK
                StackDetector.Mode.MASK -> stackDetectorVars.displayMode = StackDetector.Mode.FRAME
            }
        }

        telemetry.addLine("varBeingEdited: \n   name ${varBeingEdited.name}\n   value ${varBeingEdited.value}")

        telemetry.addLine("All values: \nL_S ${detector.goalColor.L_S.value}\nL_H ${detector.goalColor.L_H.value}\nL_V ${detector.goalColor.L_V.value}\nU_H ${detector.goalColor.U_H.value}\nU_S ${detector.goalColor.U_S.value}\n U_V${detector.goalColor.U_V.value}")
        if (gamepad1.dpad_left && !previousLoopControllerStatus.dpadLeftPressed) {
            varBeingEdited = when (varBeingEdited) {
                detector.goalColor.L_H -> detector.goalColor.L_S
                detector.goalColor.L_S -> detector.goalColor.L_V
                detector.goalColor.L_V -> detector.goalColor.U_H
                detector.goalColor.U_H -> detector.goalColor.U_S
                detector.goalColor.U_S -> detector.goalColor.U_V
                detector.goalColor.U_V -> detector.goalColor.L_H
                else-> detector.goalColor.L_H
            }
        }

        if (gamepad1.y && !previousLoopControllerStatus.yPressed) {
            telemetry.addLine( "Vals Zeroed")
            console.queueToTelemetry()
            detector.goalColor.L_S.value = 0.0
            detector.goalColor.L_H.value = 0.0
            detector.goalColor.L_V.value = 0.0
            detector.goalColor.U_H.value = 0.0
            detector.goalColor.U_S.value = 0.0
            detector.goalColor.U_V.value = 0.0
        }

        if (gamepad1.right_bumper && !previousLoopControllerStatus.rBumperPressed) {
            varBeingEdited.value += 5
        }

        if (gamepad1.left_bumper && !previousLoopControllerStatus.lBumperPressed) {
            varBeingEdited.value -= 5
        }

        if (gamepad1.a && !previousLoopControllerStatus.aPressed) {
            telemetry.addLine("Vals set to max range")
            detector.goalColor.L_H.value = 0.0
            detector.goalColor.L_S.value = 0.0
            detector.goalColor.L_V.value = 0.0
            detector.goalColor.U_H.value = 255.0
            detector.goalColor.U_S.value = 255.0
            detector.goalColor.U_V.value = 255.0
            print("Reality: ${detector.goalColor.L_H.value}, ${detector.goalColor.L_S.value},  ${detector.goalColor.L_V.value},  ${detector.goalColor.U_H.value}, ${detector.goalColor.U_S.value}, ${detector.goalColor.U_V.value}")
        }

        telemetry.update()
        previousLoopControllerStatus = ControllerStatus(
                yPressed = gamepad1.y,
                aPressed = gamepad1.a,
                xPressed = gamepad1.x,
                dpadLeftPressed = gamepad1.dpad_left,
                rBumperPressed = gamepad1.right_bumper,
                lBumperPressed = gamepad1.left_bumper)
    }

    override fun loop() {
        telemetry.addLine("Only works during init so that we can see the camera output.")
        telemetry.update()
    }
}