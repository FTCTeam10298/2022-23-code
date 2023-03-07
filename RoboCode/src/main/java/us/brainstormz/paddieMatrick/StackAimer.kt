package us.brainstormz.paddieMatrick

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.StackDetector
import us.brainstormz.localizer.StackDetectorVars
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.motion.RRLocalizer
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.telemetryWizard.TelemetryConsole
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.tan

class StackAimer(private val telemetry: Telemetry, private val stackDetector: StackDetector) {

    fun getStackInchesFromCenter(distanceFromStack: Double): Double {

        val angleToStack = getAngleToStackRad(distanceFromStack) ?: 0.0
        telemetry.addLine("angleToStack: $angleToStack")

        val stackInchesFromCenterOfCam = tan(angleToStack) * distanceFromStack
        val cameraOffsetFromCenterInches = 0.0//0.75
        val stackInchesFromCenter = stackInchesFromCenterOfCam + cameraOffsetFromCenterInches

        telemetry.addLine("stackInchesFromCenter: $stackInchesFromCenter")

        return stackInchesFromCenter
    }

    data class Observation(
        val detectionPixelValue:Double,
        val distanceFromWallToCameraInches:Double,
        val sideOffsetFromCameraInches:Double // - is left
    ) {
        val angleRad:Double = atan2(y= sideOffsetFromCameraInches, x= distanceFromWallToCameraInches)
    }
    private val angleByOffset = listOf(
        Observation(
            detectionPixelValue= 140.0,
            distanceFromWallToCameraInches= 25.0,
            sideOffsetFromCameraInches= 3.0
        ),
        Observation(
            detectionPixelValue= 152.0,
            distanceFromWallToCameraInches= 25.0,
            sideOffsetFromCameraInches= 2.0
        ),
        Observation(
            detectionPixelValue= 161.6,
            distanceFromWallToCameraInches= 25.0,
            sideOffsetFromCameraInches= 1.0
        ),
        Observation(
            detectionPixelValue= 171.4,
            distanceFromWallToCameraInches= 25.0,
            sideOffsetFromCameraInches= 0.0
        ),
        Observation(
            detectionPixelValue= 180.5,
            distanceFromWallToCameraInches= 25.0,
            sideOffsetFromCameraInches= -1.0
        ),
        Observation(
            detectionPixelValue= 189.2,
            distanceFromWallToCameraInches= 25.0,
            sideOffsetFromCameraInches= -2.0
        ),
        Observation(
            detectionPixelValue= 201.2,
            distanceFromWallToCameraInches= 25.0,
            sideOffsetFromCameraInches= -3.0
        ),


        Observation(
            detectionPixelValue= 137.1,
            distanceFromWallToCameraInches= 23.0,
            sideOffsetFromCameraInches= 3.0
        ),
        Observation(
            detectionPixelValue= 144.8,
            distanceFromWallToCameraInches= 23.0,
            sideOffsetFromCameraInches= 2.0
        ),
        Observation(
            detectionPixelValue= 152.6,
            distanceFromWallToCameraInches= 23.0,
            sideOffsetFromCameraInches= 1.0
        ),
        Observation(
            detectionPixelValue= 171.5,
            distanceFromWallToCameraInches= 23.0,
            sideOffsetFromCameraInches= 0.0
        ),
        Observation(
            detectionPixelValue= 180.2,
            distanceFromWallToCameraInches= 23.0,
            sideOffsetFromCameraInches= -1.0
        ),
        Observation(
            detectionPixelValue= 187.3,
            distanceFromWallToCameraInches= 23.0,
            sideOffsetFromCameraInches= -2.0
        ),
        Observation(
            detectionPixelValue= 203.0,
            distanceFromWallToCameraInches= 23.0,
            sideOffsetFromCameraInches= -3.0
        ),


        Observation(
            detectionPixelValue= 133.5,
            distanceFromWallToCameraInches= 21.0,
            sideOffsetFromCameraInches= 3.0
        ),
        Observation(
            detectionPixelValue= 147.5,
            distanceFromWallToCameraInches= 21.0,
            sideOffsetFromCameraInches= 2.0
        ),
        Observation(
            detectionPixelValue= 158.0,
            distanceFromWallToCameraInches= 21.0,
            sideOffsetFromCameraInches= 1.0
        ),
        Observation(
            detectionPixelValue= 168.5,
            distanceFromWallToCameraInches= 21.0,
            sideOffsetFromCameraInches= 0.0
        ),
        Observation(
            detectionPixelValue= 184.1,
            distanceFromWallToCameraInches= 21.0,
            sideOffsetFromCameraInches= -1.0
        ),
        Observation(
            detectionPixelValue= 192.5,
            distanceFromWallToCameraInches= 21.0,
            sideOffsetFromCameraInches= -2.0
        ),
        Observation(
            detectionPixelValue= 205.3,
            distanceFromWallToCameraInches= 21.0,
            sideOffsetFromCameraInches= -3.0
        ),
    )

    private fun getAngleToStackRad(distanceFromStack: Double): Double? {
        val centeredPixels = 120.0
        val pixels = stackDetector.detectedPosition(1000)

        telemetry.addLine("pixels: $pixels")

        val closestObservation = angleByOffset.minByOrNull {
            abs(abs(it.distanceFromWallToCameraInches) - abs(distanceFromStack)) +
                    abs(abs(it.detectionPixelValue) - abs(pixels ?: centeredPixels))}

        telemetry.addLine("closestObservation: ${closestObservation}")

        return if(pixels!=null && closestObservation!=null){
            val observationOffset: Double = centeredPixels - closestObservation.detectionPixelValue
            val offset: Double = centeredPixels - pixels

            val angle = (offset/observationOffset) * closestObservation.angleRad
            telemetry.addLine("angle: $angle")

            return if(offset > centeredPixels){
                angle
            } else {
                angle * -1
            }
        }else{
            null
        }
    }
}

@Autonomous
class StackAimerTest: OpMode() {
    private val hardware = PaddieMatrickHardware()

    private lateinit var movement: MecanumMovement

    val console = TelemetryConsole(telemetry)
    val dashTelemetry = FtcDashboard.getInstance().telemetry
    val multiTelemetry = MultipleTelemetry(telemetry, dashTelemetry)

    val opencv = OpenCvAbstraction(this)
    private val vars = StackDetectorVars(StackDetector.TargetHue.RED, StackDetector.Mode.FRAME)
    private val stackDetector = StackDetector(vars, telemetry)
    private val stackAimer = StackAimer(telemetry, stackDetector)

    override fun init() {
        hardware.init(hardwareMap)
        val localizer= RRLocalizer(hardware)
        movement = MecanumMovement(localizer, hardware, telemetry)

        opencv.cameraName = "backCam"
        opencv.cameraOrientation = OpenCvCameraRotation.UPSIDE_DOWN
        opencv.init(hardwareMap)
        opencv.onNewFrame(stackDetector::processFrame)
        opencv.start()
    }

    override fun loop() {

        val distanceFromStack = 30.0 - movement.localizer.currentPositionAndRotation().y
        val targetAngle = stackAimer.getStackInchesFromCenter(distanceFromStack)
        val position = PositionAndRotation(x= -targetAngle) + movement.localizer.currentPositionAndRotation()

//        val robotIsAtTarget = movement.moveTowardTarget(position, 0.0..1.0)

        multiTelemetry.addLine("\ntargetAngle: $targetAngle")
        multiTelemetry.addLine("position: $position")
//        multiTelemetry.addLine("\nrobotIsAtTarget: $robotIsAtTarget")
        multiTelemetry.addLine("current time: ${System.currentTimeMillis()}")
        multiTelemetry.update()

        println("OpMode is running 69420")
    }

}

