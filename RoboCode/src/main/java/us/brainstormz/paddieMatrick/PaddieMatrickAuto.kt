package us.brainstormz.paddieMatrick

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.trajectory.Trajectory
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.telemetryWizard.TelemetryConsole

//@Autonomous
class PaddieMatrickAuto/** Change Depending on robot */: LinearOpMode() {

    val hardware = PaddieMatrickHardware()/** Change Depending on robot */
    Drivetrain drive = new Drivetrain(hwMap);
    val movement = EncoderDriveMovement(hardware, TelemetryConsole(telemetry))

    val F4Coords

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        waitForStart()
        /** AUTONOMOUS  PHASE */
        movement.driveRobotPosition(power = 1.0, inches = 20.0, smartAccel = true)
        movement.driveRobotStrafe(power = 1.0, inches = 20.0, smartAccel = true)
        movement.driveRobotTurn(power = 1.0, degree = 20.0, smartAccel = true)

        Trajectory RedF2SignalOne = drive.trajectoryBuilder(Pose2d(61.3, -35.3, Math.toRadians(180.0))) //coords of Red Alliance Start 2
                .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
                .lineTo(new Vector2d(43.2, -36.0))
                .splineTo(new Vector2d(34.8, -59.5), Math.toRadians(180.0))
                .build()
        );

        Trajectory RedF2SignalTwo =  drive.trajectoryBuilder(new Pose2d(61.3, -35.3, Math.toRadians(180)))
            .lineTo(new Vector2d(42.8, -35.0))
            .build()
        );

        Trajectory RedF2SignalThree =  drive.trajectoryBuilder(new Pose2d(61.3, -35.3, 180))
            .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
            .lineTo(new Vector2d(43.2, -36.0))
            .splineTo(new Vector2d(34.8, -59.5), Math.toRadians(180.0))
            .build()
        );

        Trajectory RedF4SignalOne = drive.trajectoryBuilder(Pose2d(59.0, 35.0, Math.toRadians(180.0))) //coords of Red Alliance Start 2
            .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
            .lineTo(new Vector2d(42.3, 35.0))
            .splineTo(new Vector2d(34.8, -59.5), Math.toRadians(180.0))
            .build()
        );

        Trajectory RedF2SignalTwo =  drive.trajectoryBuilder(new Pose2d(61.3, -35.3, Math.toRadians(180)))
        .lineTo(new Vector2d(42.8, -35.0))
        .build()
        );

        Trajectory RedF2SignalThree =  drive.trajectoryBuilder(new Pose2d(61.3, -35.3, 180))
        .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
                .lineTo(new Vector2d(43.2, -36.0))
        .splineTo(new Vector2d(34.8, -59.5), Math.toRadians(180.0))
        .build()
        );







        waitForStart();

        if(isStopRequested()) return;

        drivetrain.followTrajectory(myTrajectory);
    }
}

}

}