package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.openftc.easyopencv.*
import org.openftc.easyopencv.OpenCvCamera.AsyncCameraOpenListener
import us.brainstormz.localizer.PhoHardware
import us.brainstormz.localizer.StackDetector
import us.brainstormz.localizer.StackDetectorVars
import us.brainstormz.localizer.polePosition.creamsicleGoalDetection.CreamsicleGoalDetector
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.openCvAbstraction.PipelineAbstraction
import us.brainstormz.telemetryWizard.TelemetryConsole

/**
 * In this sample, we demonstrate how to use the [OpenCvCameraFactory.splitLayoutForMultipleViewports]
 * method in order to concurrently display the preview of two cameras, using
 * OpenCV on both.
 */
@TeleOp
class MulticamTest : LinearOpMode() {
    override fun runOpMode() {
        val cameraMonitorViewId = hardwareMap.appContext.resources.getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.packageName)

        val viewportContainerIds = OpenCvCameraFactory.getInstance()
                .splitLayoutForMultipleViewports(
                        cameraMonitorViewId,  //The container we're splitting
                        2,  //The number of sub-containers to create
                        OpenCvCameraFactory.ViewportSplitMethod.HORIZONTALLY) //Whether to split the container vertically or horizontally

        val liftCamName = "liftCam"
        val backCamName = "backCam"
        val liftCam: OpenCvCamera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName::class.java, liftCamName), viewportContainerIds[0])
        val backCam: OpenCvCamera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName::class.java, backCamName), viewportContainerIds[1])
        liftCam.openCameraDeviceAsync(object : AsyncCameraOpenListener {
            override fun onOpened() {

                val junctionDetector = CreamsicleGoalDetector(TelemetryConsole(PhoHardware.PhoTelemetry()))
                val pipelineAbstraction = PipelineAbstraction()
                pipelineAbstraction.userFun = junctionDetector::scoopFrame

                liftCam.setPipeline(pipelineAbstraction)
                liftCam.startStreaming(320, 240, OpenCvCameraRotation.UPSIDE_DOWN)
            }

            override fun onError(errorCode: Int) {
                /*
                 * This will be called if the camera could not be opened
                 */
            }
        })
        backCam.openCameraDeviceAsync(object : AsyncCameraOpenListener {
            override fun onOpened() {

                val stackDetectorVars = StackDetectorVars(StackDetector.TargetHue.RED, StackDetector.Mode.FRAME)
                val stackDetector = StackDetector(stackDetectorVars, telemetry)
                val pipelineAbstraction = PipelineAbstraction()
                pipelineAbstraction.userFun = stackDetector::processFrame

                backCam.setPipeline(pipelineAbstraction)
                backCam.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT)
            }

            override fun onError(errorCode: Int) {
                /*
                 * This will be called if the camera could not be opened
                 */
            }
        })


        waitForStart()


        while (opModeIsActive()) {
            telemetry.addData("Internal cam FPS", liftCam.fps)
            telemetry.addData("Webcam FPS", backCam.fps)
            telemetry.update()
            sleep(100)
        }
    }
}