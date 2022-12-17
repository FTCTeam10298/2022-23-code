package us.brainstormz.paddieMatrick

//lOdom neg


//2 ft = 1 Square

//import com.acmerobotics.roadrunner.geometry.Pose2d
//import com.acmerobotics.roadrunner.trajectory.Trajectory
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.pid.PID
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.paddieMatrick.PaddieMatrickTeleOp.FourBarDegrees
import us.brainstormz.telemetryWizard.TelemetryWizard

@Autonomous(name= "Local1zationTester", group= "!")
class PattieMatrickEncoderTest: OpMode() {
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
        println("rOdom: $rOdom, lOdom: $lOdom, cOdom: $cOdom")
    }



}