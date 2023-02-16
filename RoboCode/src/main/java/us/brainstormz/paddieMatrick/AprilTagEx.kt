package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.openftc.apriltag.AprilTagDetection
import org.openftc.easyopencv.OpenCvCamera
import org.openftc.easyopencv.OpenCvCamera.AsyncCameraOpenListener
import org.openftc.easyopencv.OpenCvCameraFactory
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.motion.aprilTag.AprilTagDetectionPipeline
import java.lang.Thread.sleep

enum class SignalOrientation {One, Two, Three}

class AprilTagEx() {
    var camera: OpenCvCamera? = null
    private var aprilTagDetectionPipeline: AprilTagDetectionPipeline? = null

    // Lens intrinsics
    // UNITS ARE PIXELS
    // NOTE: this calibration is for the C920 webcam at 800x448.
    // You will need to do your own calibration for other configurations!
    var fx = 578.272
    var fy = 578.272
    var cx = 402.145
    var cy = 221.506

    // UNITS ARE METERS
        var tagsize = 0.045
    var tagOfInterest: AprilTagDetection? = null
    public var signalOrientation: SignalOrientation? = null



    fun initAprilTag(hardwareMap: HardwareMap, telemetry: Telemetry, opmode: LinearOpMode?){
        val cameraMonitorViewId = hardwareMap.appContext.resources.getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.packageName)
        camera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName::class.java, "Webcam 1"), cameraMonitorViewId)
        aprilTagDetectionPipeline =
            AprilTagDetectionPipeline(
                tagsize,
                fx,
                fy,
                cx,
                cy
            )
        camera?.setPipeline(aprilTagDetectionPipeline)
        camera?.openCameraDeviceAsync(object : AsyncCameraOpenListener {
            override fun onOpened() {
                camera?.startStreaming(640, 480, OpenCvCameraRotation.UPRIGHT)
            }

            override fun onError(errorCode: Int) {}
        })
        telemetry.msTransmissionInterval = 50

        /*
     * The INIT-loop:
     * This REPLACES waitForStart!
     */
//        while (!opmode.isStarted && !opmode.isStopRequested) {
//
//        }
    }

    fun runAprilTag(telemetry: Telemetry) {
        telemetry.addLine("signalOrientation: ${signalOrientation}")
        val currentDetections: ArrayList<AprilTagDetection> = aprilTagDetectionPipeline!!.getLatestDetections()
        if (currentDetections.size != 0) {
            var tagFound = false
            for (tag in currentDetections) {
                when (tag.id) {
                    2 -> {
                        tagFound = true
                        tagOfInterest = tag
                        signalOrientation = SignalOrientation.One
                    }
                    1 -> {
                        tagFound = true
                        tagOfInterest = tag
                        signalOrientation = SignalOrientation.Two
                    }
                    0 -> {
                        tagFound = true
                        tagOfInterest = tag
                        signalOrientation = SignalOrientation.Three
                    }
                    //                            else -> null
                }
//                    if (tag.id == ID_TAG_OF_INTEREST) {
//                        tagOfInterest = tag
//                        tagFound = true
//                        break
//                    }
            }
            if (tagFound) {
                telemetry.addLine("Tag of interest is in sight!\n\nLocation data:")
//                tagToTelemetry(tagOfInterest, telemetry)
            } else {
                telemetry.addLine("Don't see tag of interest :(")
                if (tagOfInterest == null) {
                    telemetry.addLine("(The tag has never been seen)")
                } else {
                    telemetry.addLine("\nBut we HAVE seen the tag before; last seen at:")
//                    tagToTelemetry(tagOfInterest, telemetry)
                }
            }
        } else {
            telemetry.addLine("Don't see tag of interest :(")
            if (tagOfInterest == null) {
                telemetry.addLine("(The tag has never been seen)")
            } else {
                telemetry.addLine("\nBut we HAVE seen the tag before; last seen at:")
//                tagToTelemetry(tagOfInterest, telemetry)
            }
        }
        telemetry.update()
        sleep(20)
    }

    fun tagToTelemetry(detection: AprilTagDetection?, telemetry: Telemetry) {
        telemetry.addLine(String.format("\nDetected tag ID=%d", detection!!.id))
        telemetry.addLine(String.format("Translation X: %.2f feet", detection.pose.x * FEET_PER_METER))
        telemetry.addLine(String.format("Translation Y: %.2f feet", detection.pose.y * FEET_PER_METER))
        telemetry.addLine(String.format("Translation Z: %.2f feet", detection.pose.z * FEET_PER_METER))
        telemetry.addLine(String.format("Rotation Yaw: %.2f degrees", Math.toDegrees(detection.pose.yaw)))
        telemetry.addLine(String.format("Rotation Pitch: %.2f degrees", Math.toDegrees(detection.pose.pitch)))
        telemetry.addLine(String.format("Rotation Roll: %.2f degrees", Math.toDegrees(detection.pose.roll)))
    }

    companion object {
        const val FEET_PER_METER = 3.28084
    }
}

@Autonomous

//This tests AprilTag.
//ARDUCAM SPECS: Max resolution: 3264*2448 ; Compression format: MJPG / YUY2;
// High frame rates, MJPG 15fps@3264 x 2448, 30fps@1080P; YUY2 30fps@640x 480.
class monthOfApril: LinearOpMode(){

    var aprilTagGX = AprilTagEx()

    override fun runOpMode() {
        aprilTagGX.initAprilTag(hardwareMap, telemetry, this)
        while (!isStopRequested())
            aprilTagGX.runAprilTag(telemetry)
    }

}