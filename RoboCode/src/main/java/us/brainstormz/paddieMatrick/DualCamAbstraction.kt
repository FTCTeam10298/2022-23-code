package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.openftc.easyopencv.*
import org.openftc.easyopencv.OpenCvCamera.AsyncCameraOpenListener

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


