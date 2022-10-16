package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import us.brainstormz.examples.ExampleHardware
import us.brainstormz.telemetryWizard.TelemetryConsole
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.hardwareClasses.HardwareClass
import us.brainstormz.hardwareClasses.MecanumHardware
import us.brainstormz.lankyKong.TseDetectorLK
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.telemetryWizard.GlobalConsole


class TseDetectorPM(private val console: TelemetryConsole) {
    enum class TSESides {
        One, Two, Three
    }

    //For UI only.
    private val blue = Scalar(0.0, 0.0, 255.0)
    private val red = Scalar(225.0, 0.0, 0.0)
    private val black = Scalar(255.0, 0.0, 255.0)
    private val transparent = Scalar(0.0, 255.0, 0.0)


    enum class Hues {
        Cyan, Magenta, Yellow
    }

    private val signalPlace = Rect(Point(100.0, 240.0), Point(0.0, 100.0))

    private val tseToColors = listOf(
        TSESides.One to Hues.Cyan,
        TSESides.Two to Hues.Magenta,
        TSESides.Three to Hues.Yellow
    )

    private val uiColors = listOf(
        TSESides.One to blue,
        TSESides.Two to black,
        TSESides.Three to red
    )

    @Volatile // Volatile since accessed by OpMode thread w/o synchronization
    var rotation = TSESides.One

//    fun init(frame: Mat): Mat {
////        val cbFrame = inputToCb(frame)
//
//        return frame
//    }

    private lateinit var submat: Mat
    private lateinit var cbFrame: Mat


    fun processFrame(frame: Mat): Mat {
        //just like the big city- where stuff happens

        cbFrame = inputToCb(frame, 0)

        submat = cbFrame.submat(signalPlace)
//                creates submat: cbFrame.submat(rectangle)
//                tseToColors.map {
//            it.first to cbFrame.submat(it.second)
//        }

        var result = TSESides.Three

        val cyanPercent = quantifyCyan(submat)
        val magentaPercent = quantifyMagenta(submat)
        val yellowPercent = quantifyYellow(submat)

        console.display(1, "cyanPercent: $cyanPercent \nmagentaPercent: $magentaPercent \nyellowPercent: $yellowPercent")

        result = when {
            cyanPercent > magentaPercent && cyanPercent > yellowPercent -> TSESides.One
//                tseToColors.first { it.second == Hues.Cyan }.first
            magentaPercent > cyanPercent && magentaPercent > yellowPercent -> TSESides.Two
            yellowPercent > magentaPercent && yellowPercent > cyanPercent -> TSESides.Three
            else -> TSESides.Three
        }

        rotation = result


        Imgproc.rectangle(frame, signalPlace,  blue, 2)

        console.display(8, "Position: $rotation")

        return frame
    }


//    fun name(input: Type): OutputType {
//        stuff()
//        return output
//    }
//    val name: Type = value

    private val lower_yellow_bounds: Scalar? = Scalar(200.0, 200.0, 0.0, 255.0)
    private val upper_yellow_bounds = Scalar(255.0, 255.0, 130.0, 255.0)
    private val lower_cyan_bounds = Scalar(0.0, 200.0, 200.0, 255.0)
    private val upper_cyan_bounds = Scalar(150.0, 255.0, 255.0, 255.0)
    private val lower_magenta_bounds = Scalar(170.0, 0.0, 170.0, 255.0)
    private val upper_magenta_bounds = Scalar(255.0, 60.0, 255.0, 255.0)

    private var yellowPicture = Mat()
    private fun quantifyYellow(frame: Mat): Int {
        Core.inRange(frame, lower_yellow_bounds, upper_yellow_bounds, yellowPicture);
        return Core.countNonZero(yellowPicture);
    }
    private var cyanPicture = Mat()
    private fun quantifyCyan(frame: Mat): Int {
        Core.inRange(frame, lower_cyan_bounds, upper_cyan_bounds, cyanPicture);
        return Core.countNonZero(cyanPicture);
    }

    private var magentaPicture = Mat()
    private fun quantifyMagenta(frame: Mat): Int {
        Core.inRange(frame, lower_magenta_bounds, upper_magenta_bounds, magentaPicture);
        return Core.countNonZero(magentaPicture);
    }

    private var Channel = Mat()
    private fun inputToCb(RGBInput: Mat?, colorToExtract: Int): Mat {
        Core.extractChannel(RGBInput, Channel, colorToExtract)
        return Channel
    }
}



@Autonomous
class SignalCVTest: LinearOpMode() {

    val hardware = CvTestHardware()
    val console = GlobalConsole.newConsole(telemetry)
    val opencv = OpenCvAbstraction(this)
    val tseDetector = TseDetectorPM(console)

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)



        opencv.internalCamera = false
        opencv.init(hardwareMap)
//        opencv.cameraName = hardware.cameraName
////        opencv.cameraOrientation = OpenCvCameraRotation.SIDEWAYS_LEFT
//
        opencv.onNewFrame(tseDetector::processFrame)

        waitForStart()
        /** START PHASE */

        val tsePosition = tseDetector.rotation
//        opencv.stop()
    }

}

class CvTestHardware: HardwareClass {
    override lateinit var hwMap: HardwareMap

    val cameraName = "Webcam 1"
    override fun init(ahwMap: HardwareMap) {

        hwMap = ahwMap

        // Drivetrain
    }

}
