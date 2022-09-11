package us.brainstormz.lankyKong

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import us.brainstormz.telemetryWizard.TelemetryConsole

class TseDetectorLK(private val console: TelemetryConsole) {
    enum class TSEPosition {
        One, Two, Three
    }

    private val blue = Scalar(0.0, 0.0, 255.0)
    private val red = Scalar(225.0, 0.0, 0.0)
    private val black = Scalar(255.0, 0.0, 255.0)
    private val transparent = Scalar(0.0, 255.0, 0.0)


    private val tseThreshold = 135

    lateinit var alliance: AutoTeleopTransitionLK.Alliance
    private val redPlaces = listOf(Rect(Point(100.0, 240.0), Point(0.0, 100.0)),
        Rect(Point(210.0, 100.0), Point(110.0, 240.0)),
        Rect(Point(220.0, 100.0), Point(300.0, 240.0)))

    private val bluePlaces = listOf(Rect(Point(100.0, 240.0), Point(0.0, 100.0)),
        Rect(Point(210.0, 100.0), Point(110.0, 240.0)),
        Rect(Point(220.0, 100.0), Point(300.0, 240.0)))

    private val actualPlaces = when (alliance) {
        AutoTeleopTransitionLK.Alliance.Red -> redPlaces
        AutoTeleopTransitionLK.Alliance.Blue -> bluePlaces
    }

    private val regions = listOf(
        TSEPosition.One to actualPlaces[1],
        TSEPosition.Two to actualPlaces[2],
        TSEPosition.Three to actualPlaces[3]
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

        cbFrame = inputToCb(frame)

        submats = regions.map {
            it.first to cbFrame.submat(it.second)
        }

        var result = TSEPosition.Three
        var prevColor = 0
        when (alliance){
            AutoTeleopTransitionLK.Alliance.Blue -> {
                submats.forEach {
                    if (it.first != TSEPosition.Three) {
                        val color = colorInRect(it.second)
                        if (color >= tseThreshold)
                            result = it.first
                    }
                }
            }
            AutoTeleopTransitionLK.Alliance.Red -> {
                submats.forEach {
                    val color = colorInRect(it.second)
                    if (color > prevColor) {
                        prevColor = color
                        result = it.first
                    }
                }
            }
        }

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
    private var yCrCb = Mat()
    private var cb = Mat()
    private fun inputToCb(input: Mat?): Mat {
        Imgproc.cvtColor(input, yCrCb, Imgproc.COLOR_RGB2YCrCb)
        Core.extractChannel(yCrCb, cb, 1)
        return cb
    }
}