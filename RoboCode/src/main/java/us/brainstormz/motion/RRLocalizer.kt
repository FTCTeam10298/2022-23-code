package us.brainstormz.motion

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.hardwareClasses.MecanumHardware
import us.brainstormz.hardwareClasses.ThreeWheelOdometry
import us.brainstormz.localizer.Localizer
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.paddieMatrick.PaddieMatrickHardware
import us.brainstormz.roadRunner.drive.SampleMecanumDrive

class RRLocalizer(hardware: ThreeWheelOdometry): Localizer {
    val roadRunner = SampleMecanumDrive(hardware.hwMap)
    var positionOffset = PositionAndRotation()

    override fun currentPositionAndRotation(): PositionAndRotation {
        val (x, y, heading) = roadRunner.poseEstimate
        return PositionAndRotation(x= x, y= y, r= heading)
    }

    override fun recalculatePositionAndRotation() {
        roadRunner.update()
    }

    override fun setPositionAndRotation(x: Double?, y: Double?, r: Double?) {
        positionOffset = PositionAndRotation(x!!, y!!, r!!)
    }

    override fun startNewMovement() {
        print("startNewMovement does nothing")
    }
}

@Autonomous
class RRLocalizerTest: OpMode() {
    val hardware = PaddieMatrickHardware()
    val localizer = RRLocalizer(hardware)

    override fun init() {
        hardware.init(hardwareMap)
    }

    override fun loop() {
        localizer.recalculatePositionAndRotation()
        telemetry.addLine("current pos: ${localizer.currentPositionAndRotation()}")
        telemetry.update()
    }

}