package us.brainstormz.openCvAbstraction

import android.os.SystemClock.sleep
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.opencv.core.Mat
import org.openftc.easyopencv.*
import org.openftc.easyopencv.OpenCvCameraRotation

import org.openftc.easyopencv.OpenCvCamera
import org.openftc.easyopencv.OpenCvCamera.AsyncCameraOpenListener


//class CameraMaker {
//
//    lateinit var camera: OpenCvInternalCamera
//    private val cameraMonitorViewId: Int = hardwareMap.appContext.resources.getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.packageName)
//
//    fun create(direction: OpenCvInternalCamera.CameraDirection) {
//        camera = OpenCvCameraFactory.getInstance().createInternalCamera(direction, cameraMonitorViewId)
//        camera.openCameraDevice()
//    }
//
//    fun setPipeline(pipeline: OpenCvPipeline) {
//        camera.setPipeline(pipeline)
//    }
//}

class PipelineAbstraction: OpenCvPipeline() {
    var isFirstFrame = true
    private val frame: Mat = Mat()
    
    var userFun: (Mat) -> Mat = {it}
    var onFirstFrame: ((Mat) -> Unit)? = null

    override fun processFrame(input: Mat): Mat {
        input.copyTo(frame)

        if (frame.empty()) {
            return input
        }



        return if (onFirstFrame != null && isFirstFrame) {
            isFirstFrame = false
            onFirstFrame?.invoke(frame)
            input
        } else {
            userFun(frame)
        }

    }

}

class OpenCvAbstraction(private val opmode: OpMode) {

    private val pipeline = PipelineAbstraction()

    private lateinit var camera: OpenCvCamera

    var optimizeView = false
    var openCameraDeviceAsync = false
    var cameraOrientation = OpenCvCameraRotation.UPRIGHT
    var internalCamera = true
    var cameraName: String = "Webcam 1"

//TODO: Add blur support from this init according to blurType:String, blurRadius:Float
    fun init(hardwareMap: HardwareMap) {
        val cameraMonitorViewId: Int = opmode.hardwareMap.appContext.resources.getIdentifier(
            "cameraMonitorViewId",
            "id",
            opmode.hardwareMap.appContext.packageName
        )
       camera = if (internalCamera)
           OpenCvCameraFactory.getInstance().createInternalCamera(OpenCvInternalCamera.CameraDirection.BACK, cameraMonitorViewId)
       else {
           val webcamName = hardwareMap.get(WebcamName::class.java, cameraName)
           OpenCvCameraFactory.getInstance().createWebcam(webcamName, cameraMonitorViewId)
       }

        camera.setPipeline(pipeline)

        val cameraListener = object: AsyncCameraOpenListener {
            override fun onOpened() {
                /*
                 * Tell the webcam to start streaming images to us! Note that you must make sure
                 * the resolution you specify is supported by the camera. If it is not, an exception
                 * will be thrown.
                 *
                 * Keep in mind that the SDK's UVC driver (what OpenCvWebcam uses under the hood) only
                 * supports streaming from the webcam in the uncompressed YUV image format. This means
                 * that the maximum resolution you can stream at and still get up to 30FPS is 480p (640x480).
                 * Streaming at e.g. 720p will limit you to up to 10FPS and so on and so forth.
                 *
                 * Also, we specify the rotation that the webcam is used in. This is so that the image
                 * from the camera sensor can be rotated such that it is always displayed with the image upright.
                 * For a front facing camera, rotation is defined assuming the user is looking at the screen.
                 * For a rear facing camera or a webcam, rotation is defined assuming the camera is facing
                 * away from the user.
                 */

                camera.startStreaming(320, 240, cameraOrientation)
//                webcam.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT)
            }

            override fun onError(errorCode: Int) {
            }
        }

        camera.openCameraDeviceAsync(cameraListener)
    }

    fun start() {
//
//        camera.startStreaming(432, 240, cameraOrientation)
//        camera.startStreaming(1920, 1080, cameraOrientation)
//        camera.startStreaming(1920, 1080, OpenCvCameraRotation.UPRIGHT)
        sleep(100)

//        if (optimizeView)
//            camera.setViewportRenderingPolicy(OpenCvCamera.ViewportRenderingPolicy.OPTIMIZE_VIEW)

//        if (openCameraDeviceAsync)
//            camera.openCameraDeviceAsync{ camera.startStreaming(320, 240, OpenCvCameraRotation.SIDEWAYS_LEFT) }
    }

    fun stop() {
        camera.stopStreaming()
    }

    fun onFirstFrame(function: (Mat) -> Unit) {
        pipeline.onFirstFrame = function
    }

    fun onNewFrame(function: (Mat) -> Mat) {
        pipeline.userFun = function
    }
}

//@Autonomous
//class CameraTest(): LinearOpMode() {
//
//    val hardware = RataTonyHardware()
//    val console = TelemetryConsole(telemetry)
//    val opencv = OpenCvAbstraction(this)
//    val tseDetector = TeamScoringElementDetector()
//    var tsePosition = TeamScoringElementDetector.TSEPosition.One
//
//    override fun runOpMode() {
//        opencv.stop()
//        opencv.init(hardwareMap)
//        opencv.cameraName = hardware.cameraName
//        opencv.cameraOrientation = OpenCvCameraRotation.SIDEWAYS_RIGHT
//        opencv.start()
////        opencv.onFirstFrame(tseDetector::init)
//        opencv.onNewFrame(tseDetector::processFrame)
//
//        waitForStart()
//
//    }
//}