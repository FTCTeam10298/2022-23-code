package us.brainstormz.paddieMatrick

//lOdom neg


//2 ft = 1 Square

//import com.acmerobotics.roadrunner.geometry.Pose2d
//import com.acmerobotics.roadrunner.trajectory.Trajectory
import com.acmerobotics.roadrunner.localization.ThreeTrackingWheelLocalizer
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.localizer.OdometryFacts
import us.brainstormz.localizer.OdometryLocalizer
import us.brainstormz.telemetryWizard.TelemetryConsole
import java.math.BigDecimal

@Autonomous(name= "LocalizationTester", group= "!")
class LocalizationTester: OpMode() {
    val hardware = PaddieMatrickHardware()/** Change Depending on robot */
//    Drivetrain drive = new Drivetrain(hwMap);
    val movement = EncoderDriveMovement(hardware, TelemetryConsole(telemetry))

    val console = TelemetryConsole(telemetry)
    var rOdom = 0
    var lOdom = 0
    var cOdom = 0


     override fun init() {
        hardware.init(hardwareMap)
     }
//
     override fun loop() {
        rOdom = hardware.rightOdomEncoder.currentPosition
        lOdom = hardware.leftOdomEncoder.currentPosition
        cOdom = hardware.centerOdomEncoder.currentPosition
        val delta = Math.abs(rOdom) - Math.abs(lOdom)
        telemetry.addData("lr-delta", delta)
        telemetry.addData("lr-delta rotations",
            BigDecimal(delta).setScale(5) / BigDecimal(OdometryFacts.ticksPerRotation).setScale(5))
        telemetry.addData("rOdom", rOdom)
        telemetry.addData("lOdom", lOdom)
        telemetry.addData("cOdom", cOdom)
        telemetry.update()
        println("rOdom: $rOdom, lOdom: $lOdom, cOdom: $cOdom")
    }



}