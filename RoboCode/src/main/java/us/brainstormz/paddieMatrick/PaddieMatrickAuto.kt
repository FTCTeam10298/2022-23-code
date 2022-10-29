package us.brainstormz.paddieMatrick

<<<<<<< HEAD
import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.trajectory.Trajectory
=======
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
>>>>>>> bbb69c901aa0af194b3448b51698d0a522dd6f38
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.pid.PID
import us.brainstormz.telemetryWizard.TelemetryConsole

@Autonomous
class PaddieMatrickAuto: LinearOpMode() {

    val hardware = PaddieMatrickHardware()/** Change Depending on robot */
    Drivetrain drive = new Drivetrain(hwMap);
    val movement = EncoderDriveMovement(hardware, TelemetryConsole(telemetry))

    var aprilTagGX = AprilTagEx()

    val fourBarPID = PID(kp= 0.03)
    val fourBar = FourBar(telemetry)

    val drivePower = 0.5
    val forwardDistance = 25.0
    val sideDistance = 30.0

//    val F4Coords


    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
//        fourBar.init(leftServo = hardware.left4Bar, rightServo = hardware.right4Bar, encoder = hardware.encoder4Bar)


        aprilTagGX.initAprilTag(hardwareMap, telemetry, this)


//        val fourBarTarget = fourBar.current4BarDegrees() - 70

        while (!isStarted && !isStopRequested) {
            aprilTagGX.runAprilTag(telemetry)

//            val fourBarPower = fourBarPID.calcPID(fourBarTarget, fourBar.current4BarDegrees())
//            hardware.left4Bar.power = fourBarPower
//            hardware.right4Bar.power = fourBarPower
        }

        waitForStart()
<<<<<<< HEAD
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

        //basic drive forward
        when (aprilTagGX.signalOrientation) {
            SignalOrientation.one -> {
                movement.driveRobotPosition(drivePower, forwardDistance, true)
                movement.driveRobotStrafe(drivePower, sideDistance, true)
            }
            SignalOrientation.two -> {
                movement.driveRobotPosition(drivePower, forwardDistance, true)
            }
            SignalOrientation.three -> {
                movement.driveRobotPosition(drivePower, forwardDistance, true)
                movement.driveRobotStrafe(drivePower, -sideDistance, true)
            }
        }

        waitForStart();

        if(isStopRequested()) return;

=======




>>>>>>> bbb69c901aa0af194b3448b51698d0a522dd6f38
    }
}

}

}