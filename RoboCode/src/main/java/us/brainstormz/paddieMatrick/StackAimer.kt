package us.brainstormz.paddieMatrick

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.StackDetector
import us.brainstormz.localizer.StackDetectorVars
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.motion.RRLocalizer
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.telemetryWizard.TelemetryConsole
import java.util.Stack

class StackAimer(private val telemetry: Telemetry, private val stackDetector: StackDetector) {

    fun getAngleFromStack(): Double {
        val centeredPosition: Double = 160.0
        val stackX = stackDetector.detectedPosition(1000) ?: centeredPosition
        val errorFromCenter = stackX - centeredPosition

        telemetry.addLine("centeredPosition: $centeredPosition")
        telemetry.addLine("stackX: $stackX")
        telemetry.addLine("errorFromCenter: $errorFromCenter")

        val turnSpeed = 0.20
        val angleToStack = (errorFromCenter * turnSpeed).coerceIn(-180.0..180.0)

        return angleToStack
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
    private val vars = StackDetectorVars(StackDetector.TargetHue.BLUE, StackDetector.Mode.MASK)
    private val stackDetector = StackDetector(vars, telemetry)
    private val stackAimer = StackAimer(telemetry, stackDetector)

    override fun init() {
        hardware.init(hardwareMap)
        val localizer= RRLocalizer(hardware)
        movement = MecanumMovement(localizer, hardware, telemetry)

        opencv.cameraName = "backCam"
        opencv.init(hardwareMap)
        opencv.onNewFrame(stackDetector::processFrame)
        opencv.start()
    }

    override fun loop() {

        val targetAngle = stackAimer.getAngleFromStack()
        val position = PositionAndRotation(r= -targetAngle) + movement.localizer.currentPositionAndRotation()

        val robotIsAtTarget = movement.moveTowardTarget(position, 0.0..1.0)

        multiTelemetry.addLine("\ntargetAngle: $targetAngle")
        multiTelemetry.addLine("position: $position")
        multiTelemetry.addLine("\nrobotIsAtTarget: $robotIsAtTarget")
        multiTelemetry.addLine("current time: ${System.currentTimeMillis()}")
        multiTelemetry.update()

        println("OpMode is running 69420")
    }

}
