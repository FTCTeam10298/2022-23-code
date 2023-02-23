package us.brainstormz.paddieMatrick

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.hardwareClasses.MecOdometry
import us.brainstormz.localizer.OdometryLocalizer
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.polePosition.creamsicleGoalDetection.CreamsicleGoalDetector
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.motion.RRLocalizer
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.telemetryWizard.TelemetryConsole

@TeleOp
class JunctionAimbotTest: OpMode() {

    private val hardware = PaddieMatrickHardware()

    private lateinit var movement: MecanumMovement


    val console = TelemetryConsole(telemetry)
    val dashTelemetry = FtcDashboard.getInstance().telemetry
    val multiTelemetry = MultipleTelemetry(telemetry, dashTelemetry)

    val opencv = OpenCvAbstraction(this)
    private val junctionDetector = CreamsicleGoalDetector(console)

    override fun init() {
        hardware.init(hardwareMap)
        val localizer= RRLocalizer(hardware)
        movement = MecanumMovement(localizer, hardware, telemetry)

        opencv.cameraName = "liftCam"
        opencv.cameraOrientation = OpenCvCameraRotation.UPSIDE_DOWN
        opencv.init(hardwareMap)
        opencv.start()
        opencv.onNewFrame(junctionDetector::scoopFrame)
    }

//    var initAngle = 0.0
//    override fun start() {
//        initAngle = movement.localizer.currentPositionAndRotation().r
//    }

    override fun loop() {

        val centeredPosition: Double = 165.0
        val junctionX = junctionDetector.xPositionOfJunction() ?: centeredPosition
        val junctionErrorFromCenter = junctionX - centeredPosition

        val turnSpeed = 0.4
        val targetAngle = (junctionErrorFromCenter *turnSpeed).coerceIn(-180.0..180.0)
        val position = PositionAndRotation(r= -targetAngle) + movement.localizer.currentPositionAndRotation()

        val robotIsAtTarget = movement.moveTowardTarget(position, 0.0..0.2)

        multiTelemetry.addLine("junctionX: $junctionX")
        multiTelemetry.addLine("centeredPosition: $centeredPosition")
        multiTelemetry.addLine("junctionErrorFromCenter: $junctionErrorFromCenter")
        multiTelemetry.addLine("\ntargetAngle: $targetAngle")
        multiTelemetry.addLine("position: $position")
        multiTelemetry.addLine("\nrobotIsAtTarget: $robotIsAtTarget")
        multiTelemetry.addLine("current time: ${System.currentTimeMillis()}")
        multiTelemetry.update()

        println("OpMode is running 69420")
    }

}