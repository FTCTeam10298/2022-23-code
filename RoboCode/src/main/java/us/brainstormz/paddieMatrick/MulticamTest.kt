package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.openftc.easyopencv.OpenCvCamera
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.localizer.PhoHardware
import us.brainstormz.localizer.StackDetector
import us.brainstormz.localizer.StackDetectorVars
import us.brainstormz.localizer.polePosition.creamsicleGoalDetection.CreamsicleGoalDetector
import us.brainstormz.openCvAbstraction.PipelineAbstraction
import us.brainstormz.telemetryWizard.TelemetryConsole

@Autonomous
class MulticamTest : LinearOpMode() {

    override fun runOpMode() {

        val dualCamAbstraction = DualCamAbstraction(hardwareMap)
        val viewportContainerIds = dualCamAbstraction.setupViewport()


        val liftCamName = "liftCam"

        val junctionDetector = CreamsicleGoalDetector(TelemetryConsole(PhoHardware.PhoTelemetry()))
        val liftPipeline = PipelineAbstraction()
        liftPipeline.userFun = junctionDetector::scoopFrame

        val liftCam: OpenCvCamera = dualCamAbstraction.startNewCamera(
            cameraName = liftCamName,
            cameraRotation = OpenCvCameraRotation.UPSIDE_DOWN,
            viewportContainerId = viewportContainerIds[0],
            pipeline = liftPipeline)


        val backCamName = "backCam"

        val stackDetectorVars =
            StackDetectorVars(StackDetector.TargetHue.BLUE, StackDetector.Mode.FRAME)
        val stackDetector = StackDetector(stackDetectorVars, telemetry)
        val backPipeline = PipelineAbstraction()
        backPipeline.userFun = stackDetector::processFrame

        val backCam: OpenCvCamera = dualCamAbstraction.startNewCamera(
            cameraName = backCamName,
            cameraRotation = OpenCvCameraRotation.UPSIDE_DOWN,
            viewportContainerId = viewportContainerIds[1],
            pipeline = backPipeline)

//        val webcamName = "Webcam 1"
//
////        val stackDetectorVars2 =
////            StackDetectorVars(StackDetector.TargetHue.BLUE, StackDetector.Mode.FRAME)
////        val stackDetector2 = StackDetector(stackDetectorVars2, telemetry)
//        val webcamPipeline = PipelineAbstraction()
////        webcamPipeline.userFun = stackDetector2::processFrame
//
//        val webcam: OpenCvCamera = dualCamAbstraction.startNewCamera(
//            cameraName = webcamName,
//            cameraRotation = OpenCvCameraRotation.UPSIDE_DOWN,
//            viewportContainerId = viewportContainerIds[1],
//            pipeline = webcamPipeline)
//
        waitForStart()


        while (opModeIsActive()) {
            telemetry.addLine("$liftCamName FPS: ${liftCam.fps}")
//            telemetry.addLine("$webcamName FPS: ${webcam.fps}")
            telemetry.addLine("$backCamName FPS: ${backCam.fps}")
            telemetry.update()
            sleep(100)
            if (isStopRequested) {
                liftCam.closeCameraDeviceAsync { liftCam.closeCameraDevice() }
                backCam.closeCameraDeviceAsync { backCam.closeCameraDevice() }
            }
        }
    }

}