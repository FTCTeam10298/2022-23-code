package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.openftc.easyopencv.*
import org.openftc.easyopencv.OpenCvCamera.AsyncCameraCloseListener
import org.openftc.easyopencv.OpenCvCamera.AsyncCameraOpenListener
import us.brainstormz.localizer.PhoHardware
import us.brainstormz.localizer.StackDetector
import us.brainstormz.localizer.StackDetectorVars
import us.brainstormz.localizer.polePosition.creamsicleGoalDetection.CreamsicleGoalDetector
import us.brainstormz.openCvAbstraction.PipelineAbstraction
import us.brainstormz.telemetryWizard.TelemetryConsole
import java.util.Stack

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

        val stackDetectorVars = StackDetectorVars(StackDetector.TargetHue.BLUE, StackDetector.Mode.FRAME)
        val stackDetector = StackDetector(stackDetectorVars, telemetry)
        val backPipeline = PipelineAbstraction()
        backPipeline.userFun = stackDetector::processFrame

        val backCam: OpenCvCamera = dualCamAbstraction.startNewCamera(
                cameraName = backCamName,
                cameraRotation = OpenCvCameraRotation.UPRIGHT,
                viewportContainerId = viewportContainerIds[1],
                pipeline = backPipeline)

        waitForStart()


        while (opModeIsActive()) {
            telemetry.addLine("$liftCamName FPS: ${liftCam.fps}")
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

class DualCamAbstraction(private val hardwareMap: HardwareMap) {

    fun setupViewport(): IntArray {
        val cameraMonitorViewId = hardwareMap.appContext.resources.getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.packageName)

        val viewportContainerIds = OpenCvCameraFactory.getInstance()
                .splitLayoutForMultipleViewports(
                        cameraMonitorViewId,  //The container we're splitting
                        2,  //The number of sub-containers to create
                        OpenCvCameraFactory.ViewportSplitMethod.HORIZONTALLY) //Whether to split the container vertically or horizontally

        return viewportContainerIds
    }

    fun startNewCamera(cameraName: String, pipeline: OpenCvPipeline, cameraRotation: OpenCvCameraRotation, viewportContainerId: Int): OpenCvCamera {
        val newCamera: OpenCvCamera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName::class.java, cameraName), viewportContainerId)

        newCamera.showFpsMeterOnViewport(false)

        newCamera.openCameraDeviceAsync(object : AsyncCameraOpenListener {

            override fun onOpened() {
                println("[camera/$cameraName] opened ")
                newCamera.setPipeline(pipeline)
                newCamera.startStreaming(320, 240, cameraRotation)
            }

            override fun onError(errorCode: Int) {
                println("[camera/$cameraName] not opened - error code $errorCode")
            }
        })

        return newCamera
    }
}