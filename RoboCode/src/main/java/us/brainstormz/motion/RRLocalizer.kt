package us.brainstormz.motion

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
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
        //rr switches x and y
        return PositionAndRotation(x= y, y= x, r= heading)
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

class Foo(private val hardware: PaddieMatrickHardware, private val telemetry:Telemetry){
    val localizer = RRLocalizer(hardware)

    fun loop(){
        localizer.recalculatePositionAndRotation()
        telemetry.addLine("current pos: ${localizer.currentPositionAndRotation()}")
        telemetry.update()
    }
}

@Autonomous
class RRLocalizerTest: OpMode() {
    var foo:Foo? = null
    val hardware = PaddieMatrickHardware()
    val movement = MecanumDriveTrain(hardware)

    override fun init() {
        hardware.init(hardwareMap)
        hardware.lFDrive.zeroPowerBehavior = ZeroPowerBehavior.FLOAT
        hardware.rFDrive.zeroPowerBehavior =  ZeroPowerBehavior.FLOAT
        hardware.lBDrive.zeroPowerBehavior =  ZeroPowerBehavior.FLOAT
        hardware.rBDrive.zeroPowerBehavior =  ZeroPowerBehavior.FLOAT
        foo = Foo(hardware, telemetry)
    }

    override fun loop() {
        foo?.loop()
    }

}