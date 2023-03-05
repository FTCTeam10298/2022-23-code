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

class StackAimer(private val telemetry: Telemetry, private val stackDetector: StackDetector) {

    fun getStackInchesFromCenter(distanceFromStack: Double): Double {

//        val stackInchesFromCenter =
//
//        return stackInchesFromCenter

        val angleToStack = getAngleToStackRad(distanceFromStack)
        telemetry.addLine("angleToStack: $angleToStack")


        return getPixelsFromCenter()
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
        val pixels = stackDetector.detectedPosition(1000)

        val closestObservation = angleByOffset.minByOrNull {Math.abs(Math.abs(it.distance) - Math.abs(distanceFromStack))}

       return if(pixels!=null && closestObservation!=null){
           val observationOffset = 120 - closestObservation.pixels
           val offset = 120 - pixels
           val angle = (offset.toDouble()/observationOffset.toDouble()) * closestObservation.angle

           return if(pixels > 120){
               angle
           }else{
               angle * -1
           }
        }else{
        null
       }
    }

    private fun getPixelsFromCenter(): Double {
        val centeredOnScreen: Double = 126.0
        val stackPixels = stackDetector.detectedPosition(1000) ?: centeredOnScreen
        val pixelsFromCenter = stackPixels - centeredOnScreen

        telemetry.addLine("centeredOnScreen: $centeredOnScreen")
        telemetry.addLine("stackPixels: $stackPixels")
        telemetry.addLine("errorFromCenterPixels: $pixelsFromCenter")

        return pixelsFromCenter
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

        val targetAngle = stackAimer.getStackInchesFromCenter(movement.localizer.currentPositionAndRotation().x)
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

