package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import us.brainstormz.openCvAbstraction.OpenCvAbstraction

@Autonomous
class TwoCamerasAsyncTest: OpMode() {

    data class CameraAndDetector(val detector: Any, val cameraName: String, val opencv: OpenCvAbstraction)
    private val frontCameraAndDetector = CameraAndDetector(cameraName = "Webcam 1", detector = AprilTagEx(), opencv = OpenCvAbstraction(this))
    private val liftCameraAndDetector = CameraAndDetector(cameraName = "liftCam", detector = JunctionAimer(), opencv = OpenCvAbstraction(this))

    override fun init() {
        liftCameraAndDetector.detector as JunctionAimer
        liftCameraAndDetector.detector.start(liftCameraAndDetector.opencv, hardwareMap)

        frontCameraAndDetector.opencv.cameraName = frontCameraAndDetector.cameraName
        frontCameraAndDetector.detector as AprilTagEx
        frontCameraAndDetector.detector.initAprilTag(hardwareMap, telemetry, null)
    }

    override fun init_loop() {
        frontCameraAndDetector.detector as AprilTagEx
        frontCameraAndDetector.detector.runAprilTag(telemetry)
        val signalOrientation = frontCameraAndDetector.detector.signalOrientation
        telemetry.addLine("signalOrientation: $signalOrientation")

        liftCameraAndDetector.detector as JunctionAimer
        val angleFromPole = liftCameraAndDetector.detector.getAngleFromPole()
        telemetry.addLine("angleFromPole: $angleFromPole")

        telemetry.update()
    }

    override fun loop() {

    }
}