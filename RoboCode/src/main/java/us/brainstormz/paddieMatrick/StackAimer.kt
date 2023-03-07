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
import kotlin.math.sin
import kotlin.math.tan

class StackAimer(private val telemetry: Telemetry, private val stackDetector: StackDetector) {

    fun getStackInchesFromCenter(distanceFromStack: Double): Double {

        val angleToStack = getAngleToStackRad(distanceFromStack) ?: 0.0
        telemetry.addLine("angleToStack: $angleToStack")

        // tan(theta) = sin(theta)/cos(theta)
        // sin(theta) = y
        // cos(theta) = x
        // tan(theta) = y/x
        // tan(theta) * x = y/x * x = y
        val stackInchesFromCenter = tan(angleToStack) * distanceFromStack

        telemetry.addLine("stackInchesFromCenter: $stackInchesFromCenter")

        return stackInchesFromCenter
    }

    data class Observation(
        val pixels:Int,
        val angle:Double,
        val distance:Int
    )
    private val angleByOffset = listOf(
        Observation(97, 0.0665, 30),// 2" @ 30" distance
        Observation(83 , 0.0996, 20),// 2" @ 20" distance
        Observation(78 , 0.13255, 15),// 2" @ 15" distance
        Observation(74 , 0.197, 10)// 2" @ 10" distance
    )

    private fun getAngleToStackRad(distanceFromStack: Double): Double? {
        val centeredPixels = 100.0
        val pixels = stackDetector.detectedPosition(1000)

        telemetry.addLine("pixels: $pixels")

        val closestObservation = angleByOffset.minByOrNull { abs(abs(it.distance) - abs(distanceFromStack)) }

        return if(pixels!=null && closestObservation!=null){
            val observationOffset: Double = centeredPixels - closestObservation.pixels
            val offset: Double = centeredPixels - pixels

            val angle = (offset/observationOffset) * closestObservation.angle

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
        opencv.cameraOrientation = OpenCvCameraRotation.SIDEWAYS_LEFT
        opencv.init(hardwareMap)
        opencv.onNewFrame(stackDetector::processFrame)
        opencv.start()
    }

    override fun loop() {

        val distanceFromStack = 30.0 - movement.localizer.currentPositionAndRotation().y
        val targetAngle = stackAimer.getStackInchesFromCenter(distanceFromStack)
        val position = PositionAndRotation(x= -targetAngle) + movement.localizer.currentPositionAndRotation()

        val robotIsAtTarget = movement.moveTowardTarget(position, 0.0..1.0)

        multiTelemetry.addLine("\ntargetAngle: $targetAngle")
        multiTelemetry.addLine("position: $position")
//        multiTelemetry.addLine("\nrobotIsAtTarget: $robotIsAtTarget")
        multiTelemetry.addLine("current time: ${System.currentTimeMillis()}")
        multiTelemetry.update()

        println("OpMode is running 69420")
    }

}

