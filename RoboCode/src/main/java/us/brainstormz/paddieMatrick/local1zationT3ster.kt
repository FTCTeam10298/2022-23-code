package us.brainstormz.paddieMatrick

//lOdom neg


//2 ft = 1 Square

//import com.acmerobotics.roadrunner.geometry.Pose2d
//import com.acmerobotics.roadrunner.trajectory.Trajectory
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.telemetryWizard.TelemetryConsole

@Autonomous(name= "Local1zationTe3ter", group= "!")
//GET THE ENCODER NUMB3RS

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
        telemetry.addData("rOdom", rOdom)
        telemetry.addData("lOdom", lOdom)
        telemetry.addData("cOdom", cOdom)
        telemetry.update()
        println("rOdom: $rOdom, lOdom: $lOdom, cOdom: $cOdom")
    }



}