package us.brainstormz.paddieMatrick

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.HardwareMap
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.localizer.PhoHardware
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.polePosition.creamsicleGoalDetection.CreamsicleGoalDetector
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.motion.RRLocalizer
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.telemetryWizard.TelemetryConsole

class JunctionAimer {
    private val junctionDetector = CreamsicleGoalDetector(TelemetryConsole(PhoHardware.PhoTelemetry()))

    fun start(opencv: OpenCvAbstraction, hardwareMap: HardwareMap) {

        opencv.cameraName = "liftCam"
        opencv.cameraOrientation = OpenCvCameraRotation.UPSIDE_DOWN
        opencv.init(hardwareMap)
        opencv.start()
        opencv.onNewFrame(junctionDetector::scoopFrame)
    }

//    var lastPoleAngle: Double? = null
    fun getAngleFromPole(): Double {
        val centeredPosition: Double = 160.0
        val junctionX = junctionDetector.detectedPosition(staleThresholdAgeMillis = 100) ?: centeredPosition
        val junctionErrorFromCenter = junctionX - centeredPosition

        val turnSpeed = 0.38
        val angleToPole = (junctionErrorFromCenter *turnSpeed).coerceIn(-180.0..180.0)

//        if (lastPoleAngle == null)
//            lastPoleAngle = angleToPole

//        multiTelemetry.addLine("junctionX: $junctionX")
//        multiTelemetry.addLine("centeredPosition: $centeredPosition")
//        multiTelemetry.addLine("junctionErrorFromCenter: $junctionErrorFromCenter")

        return angleToPole
    }
}


@TeleOp
class JunctionAimbotTest: OpMode() {

    private val hardware = PaddieMatrickHardware()

    private lateinit var movement: MecanumMovement


    val console = TelemetryConsole(telemetry)
    val dashTelemetry = FtcDashboard.getInstance().telemetry
    val multiTelemetry = MultipleTelemetry(telemetry, dashTelemetry)

    val opencv = OpenCvAbstraction(this)
    private val junctionAimer = JunctionAimer()

    override fun init() {
        hardware.init(hardwareMap)
        val localizer= RRLocalizer(hardware)
        movement = MecanumMovement(localizer, hardware, telemetry)

        junctionAimer.start(opencv, hardwareMap)
    }

    override fun loop() {

        val targetAngle = junctionAimer.getAngleFromPole()
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