package us.brainstormz.localizer

import locationTracking.PosAndRot
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.abs

data class StackDetection(
    val xPosition:Double,
    val whenDetected:Long
)

class StackDetectorVars(
    var targetHue:StackDetector.TargetHue,
    var displayMode:StackDetector.Mode
)

class StackDetector(private val vars:StackDetectorVars){

    enum class Mode {
        FRAME,
        MASK
    }

    enum class TargetHue {
        RED,
        BLUE,
        absRED
    }

    class NamedVar(val name: String, var value: Double)


    class ColorRange(val L_H: NamedVar, val L_S: NamedVar, val L_V: NamedVar, val U_H: NamedVar, val U_S: NamedVar, val U_V: NamedVar)

    private val redSolarizedColor = ColorRange(
            L_H = NamedVar("Low Hue", 120.0),
            L_S = NamedVar("Low Saturation", 150.0),
            L_V = NamedVar("Low Vanity/Variance/VolumentricVibacity", 0.0),
            U_H = NamedVar("Uppper Hue", 130.0),
            U_S = NamedVar("Upper Saturation", 255.0),
            U_V = NamedVar("Upper Vanity/Variance/VolumentricVibracity", 200.0)
    )

    private val redAbsoluteColor = ColorRange(
            L_H = NamedVar("Low Hue", 120.0),
            L_S = NamedVar("Low Saturation", 150.0),
            L_V = NamedVar("Low Vanity/Variance/VolumentricVibacity", 0.0),
            U_H = NamedVar("Uppper Hue", 255.0),
            U_S = NamedVar("Upper Saturation", 255.0),
            U_V = NamedVar("Upper Vanity/Variance/VolumentricVibracity", 255.0)
    )

    val redColor = ColorRange(
            L_H = NamedVar("Low Hue", 0.0),
            L_S = NamedVar("Low Saturation", 0.0),
            L_V = NamedVar("Low Vanity/Variance/VolumentricVibacity", 0.0),
            U_H = NamedVar("Uppper Hue", 255.0),
            U_S = NamedVar("Upper Saturation", 255.0),
            U_V = NamedVar("Upper Vanity/Variance/VolumentricVibracity", 255.0))

    //trained for yellow
    val blueColor = ColorRange(
            L_S = NamedVar("Low Saturation", 20.0),
            L_H = NamedVar("Low Hue", 0.0),
            L_V = NamedVar("Low Vanity/Variance/VolumentricVibacity", 55.0),
            U_H = NamedVar("Upper Hue", 35.0),
            U_S = NamedVar("Upper Saturation", 255.0),
            U_V = NamedVar("Upper Vanity/Variance/VolumentricVibracity", 255.0))

    val goalColor: ColorRange = when(vars.targetHue){
        TargetHue.RED -> redColor
        TargetHue.BLUE -> blueColor
        TargetHue.absRED -> redAbsoluteColor
    }

//    var x = 0.0
//    var y = 0.0

    private val hsv = Mat()
    private val maskA = Mat()
    private val maskB = Mat()
    private val kernel = Mat(5, 5, CvType.CV_8U)

    private var mostRecentDetection:StackDetection? = null

    fun detectedPosition(staleThresholdAgeMillis:Long):Double? {
        return mostRecentDetection?.let{detection ->
            val t = System.currentTimeMillis() - staleThresholdAgeMillis
            if(detection.whenDetected >= t) detection.xPosition else null
        }
    }

    fun convert(matOfPoint2f: MatOfPoint2f): MatOfPoint {
        val foo = MatOfPoint()
        matOfPoint2f.convertTo(foo, CvType.CV_32S)
        return foo
    }

    private fun heightOfLineOnScreen(line:Pair<Point, Point>):Double {
        return line.second.y - line.first.y

    }

    fun processFrame(frame: Mat): Mat {

        // Pre-process the image to make contour detection easier
        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV)
        val lower = Scalar(blueColor.L_H.value, blueColor.L_S.value, blueColor.L_V.value)
        val upper = Scalar(blueColor.U_H.value, blueColor.U_S.value, blueColor.U_V.value)
        Core.inRange(hsv, lower, upper, maskA)
        Imgproc.erode(maskA, maskB, kernel)

        // Find the contours
        val contours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(maskB, contours, Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

        // Dig through them
        val shapesByArea = contours.map{ cnt: MatOfPoint ->
            val area = Imgproc.contourArea(cnt)

            fun convert(src: MatOfPoint): MatOfPoint2f {
                val dst = MatOfPoint2f()
                src.convertTo(dst, CvType.CV_32F)
                return dst
            }

            val cnt2f = convert(cnt)
            val points = MatOfPoint2f()
            Imgproc.approxPolyDP(cnt2f, points, 0.02 * Imgproc.arcLength(cnt2f, true), true)
//
//            drawLines(points, frame)

            val pointsList = points.toList()

            val pointWeLike = pointsList.firstOrNull()
            if(pointWeLike!=null){
                mostRecentDetection = StackDetection(
                        xPosition = pointWeLike.x,
                        whenDetected = System.currentTimeMillis()
                )
            }
            area to pointsList
        }

        val largestShape = shapesByArea.maxByOrNull { it.first }?.second

        if(largestShape!=null){
            drawLinesFromPoints(largestShape, frame)

            val center = centroid(largestShape)

            val topY = -240.0
            val bottomY = 240.0

            val pointAtTheTop = Point(center.x, topY)
            val pointAtBottom = Point(center.x, bottomY)

            Imgproc.line(frame, center, pointAtTheTop, lower, 10)
            Imgproc.line(frame, center, pointAtBottom, upper, 10)

        }

        return when (vars.displayMode) {
            Mode.FRAME -> frame
            Mode.MASK -> maskB
        }
    }


    private fun centroid(points: List<Point>): Point {
        val result = points.fold(Point()){ acc, it ->
            Point(acc.x + it.x, acc.y + it.y)
        }
        result.x /= points.size
        result.y /= points.size

        return result
    }

    private fun areRoughlyInSameXPlane(a: Point, b: Point): Boolean {
        val fudge = 42
        return abs(b.x - a.x) <= fudge
    }


    fun drawLinesFromPoints(points: List<Point>, toFrame:Mat) {
        Imgproc.drawContours(
                toFrame,
                listOf(convert(MatOfPoint2f(*(points.toTypedArray())))),
                0,
                Scalar(0.0, 255.0, 0.0),
                2)
    }

    fun drawLines(points: MatOfPoint2f, toFrame:Mat) {

        Imgproc.drawContours(
                toFrame,
                listOf(convert(points)),
                0,
                Scalar(0.0, 255.0, 0.0),
                2)
    }

}