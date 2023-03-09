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

class StackAimer(private val telemetry: Telemetry, private val stackDetector: StackDetector?) {


//    fun calculateLateralOffsetInches(detectionPixelValue: Double, distanceFromWallToCameraInches: Double):Double {
//
//    }

    fun getStackInchesFromCenter(distanceFromStack: Double) = getStackInchesFromCenter(
        distanceFromStack = distanceFromStack,
        detectionPixelValue = stackDetector?.detectedPosition(1000)
    )


    fun getStackInchesFromCenter(distanceFromStack: Double, detectionPixelValue:Double?): Double {

        val angleToStack = getAngleToStackRad(distanceFromStack, detectionPixelValue)!!// ?: 0.0
        telemetry.addLine("angleToStack: $angleToStack")

        val stackInchesFromCenterOfCam = tan(angleToStack) * distanceFromStack
        val cameraOffsetFromCenterInches = 1.0
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

        override fun toString(): String = "Observation: \n   pixel $detectionPixelValue\n   distance $distanceFromWallToCameraInches\n   offset $sideOffsetFromCameraInches\n   angle $angleRad\n"
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
            detectionPixelValue= 166.2,
            distanceFromWallToCameraInches= 25.0,
            sideOffsetFromCameraInches= 0.5
        ),
        Observation(
            detectionPixelValue= 179.2,
            distanceFromWallToCameraInches= 25.0,
            sideOffsetFromCameraInches= -0.5
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
            detectionPixelValue= 161.7,
            distanceFromWallToCameraInches= 23.0,
            sideOffsetFromCameraInches= 0.5
        ),
        Observation(
            detectionPixelValue= 176.0,
            distanceFromWallToCameraInches= 23.0,
            sideOffsetFromCameraInches= -0.5
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
            detectionPixelValue= 161.2,
            distanceFromWallToCameraInches= 21.0,
            sideOffsetFromCameraInches= 0.5
        ),
        Observation(
            detectionPixelValue= 177.3,
            distanceFromWallToCameraInches= 21.0,
            sideOffsetFromCameraInches= -0.5
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

    // smth in here throws null in real auto
    private fun getAngleToStackRad(distanceFromStack: Double, detectionPixelValue:Double?): Double? {
        val centeredPixels = 120.0
        val pixels = detectionPixelValue

        telemetry.addLine("pixels: $pixels")
// regex filter: "Next task" | "[camera/" | "backCam" | "AllocSpace" | "Pixel in:"
        println("Pixel in: $detectionPixelValue")
//        val observationsSortedByPixels = angleByOffset.sortedByDescending { abs(abs(it.detectionPixelValue) - abs(pixels ?: centeredPixels)) }
//        val topThirtyPercentOfMatchingObservations = observationsSortedByPixels.take(angleByOffset.size / 3)
        val closestObservation = angleByOffset.minByOrNull { abs(abs(it.distanceFromWallToCameraInches) - abs(distanceFromStack)) + abs(abs(it.detectionPixelValue) - abs(pixels!!))}// ?: centeredPixels))}

        telemetry.addLine("distanceFromStack: $distanceFromStack")
        telemetry.addLine("closestObservation: $closestObservation")

        return if(pixels!=null && closestObservation!=null){
            val observationOffset: Double = centeredPixels - closestObservation.detectionPixelValue
            val offset: Double = centeredPixels - pixels
            val pixelDifferenceMultiplier = offset/observationOffset
            telemetry.addLine("pixelDifferenceMultiplier: $pixelDifferenceMultiplier")

            val angle = abs(pixelDifferenceMultiplier) * -closestObservation.angleRad
            telemetry.addLine("angle: $angle")

            return angle
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

        movement.precisionInches = 0.1
        targetPosition = movement.localizer.currentPositionAndRotation()
    }

    var targetPosition: PositionAndRotation = PositionAndRotation()
    var newPosition = PositionAndRotation()
    override fun loop() {
        movement.localizer.recalculatePositionAndRotation()
        movement.xTranslationPID = MecanumMovement.fineMoveXTranslation


        val distanceFromStack = 22.0 - movement.localizer.currentPositionAndRotation().y
        val stackInchesFromCentered = stackAimer.getStackInchesFromCenter(distanceFromStack)
        val stackInchesX = movement.localizer.currentPositionAndRotation().x - stackInchesFromCentered

        newPosition = if (gamepad1.x) targetPosition.copy(x=stackInchesX) else newPosition

        targetPosition = if (gamepad1.a) {
            newPosition
        } else movement.localizer.currentPositionAndRotation()

        multiTelemetry.addLine("\ntargetAngle: $stackInchesFromCentered")
        multiTelemetry.addLine("current time: ${System.currentTimeMillis()}")

        telemetry.addLine("newPosition: $newPosition")

        multiTelemetry.addLine("targetPosition: $targetPosition")
        multiTelemetry.addLine("actualPosition: ${movement.localizer.currentPositionAndRotation()}")
        val robotIsAtTarget = movement.moveTowardTarget(targetPosition, 0.0..1.0)
        multiTelemetry.addLine("\nrobotIsAtTarget: $robotIsAtTarget")

        multiTelemetry.update()
    }

}

