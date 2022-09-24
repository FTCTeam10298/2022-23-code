package us.brainstormz.paddieMatrick

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import us.brainstormz.telemetryWizard.TelemetryConsole

class TseDetectorPM(private val console: TelemetryConsole) {
    enum class TSEPosition {
        One, Two, Three
    }

    private val blue = Scalar(0.0, 0.0, 255.0)
    private val red = Scalar(225.0, 0.0, 0.0)
    private val black = Scalar(255.0, 0.0, 255.0)
    private val transparent = Scalar(0.0, 255.0, 0.0)


    private val tseThreshold = 135


    private val bluePlaces = listOf(Rect(Point(100.0, 240.0), Point(0.0, 100.0)),
        Rect(Point(210.0, 100.0), Point(110.0, 240.0)),
        Rect(Point(220.0, 100.0), Point(300.0, 240.0)))



    private val regions = listOf(
        TSEPosition.One to bluePlaces[1],
        TSEPosition.Two to bluePlaces[2],
        TSEPosition.Three to bluePlaces[3]
    )

    private val colors = listOf(
        TSEPosition.One to blue,
        TSEPosition.Two to black,
        TSEPosition.Three to red
    )

    @Volatile // Volatile since accessed by OpMode thread w/o synchronization
    var position = TSEPosition.One

//    fun init(frame: Mat): Mat {
////        val cbFrame = inputToCb(frame)
//
//        return frame
//    }

    private lateinit var submats: List<Pair<TSEPosition, Mat>>
    private lateinit var cbFrame: Mat

    fun processFrame(frame: Mat): Mat {

        cbFrame = inputToCb(frame, 0)

        submats = regions.map {
            it.first to cbFrame.submat(it.second)
        }

        var result = TSEPosition.Three
        var prevColor = 0

// Todo: Finish this function.
//        submats.forEach {
//            val color = colorInRect(string foundColor, it.first)
//            if (color > prevColor) {
//                prevColor = color
//                result = it.first
//            }
//        }



        position = result

        colors.forEach {
            val rect = regions.toMap()[it.first]
            Imgproc.rectangle(frame, rect, it.second, 2)
        }

        console.display(8, "Position: $position")
        console.display(9, "Highest Color: $prevColor")

        return frame
    }

    private fun colorInRect(rect: Mat): Int {
        return Core.mean(rect).`val`[0].toInt()
    }

    /**
     * This function takes the RGB frame, converts to YCrCb,
     * and extracts the Cb channel to the cb variable
     */
    private var Channel = Mat()
    private fun inputToCb(RGBInput: Mat?, colorToExtract: Int): Mat {
        Core.extractChannel(RGBInput, Channel, colorToExtract)
        return Channel
    }
}