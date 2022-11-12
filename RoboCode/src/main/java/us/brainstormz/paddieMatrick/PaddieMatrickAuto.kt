package us.brainstormz.paddieMatrick

//2 ft = 1 Square

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.trajectory.Trajectory
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.pid.PID
import us.brainstormz.telemetryWizard.TelemetryConsole

@Autonomous(name= "PaddieMatrick Auto", group= "A")
class PaddieMatrickAuto: LinearOpMode() {

    val hardware = PaddieMatrickHardware()/** Change Depending on robot */
//    Drivetrain drive = new Drivetrain(hwMap);
    val movement = EncoderDriveMovement(hardware, TelemetryConsole(telemetry))

    var aprilTagGX = AprilTagEx()

    val fourBarPID = PID(kp= 0.03)
    val fourBar = FourBar(telemetry)

    val drivePower = 0.5
    val forwardDistance = 12.0
    val sideDistance = 12.0

    val trigonPower = 0.5
    val trigonTurnPower = 0.5
    val trigonStrafePower = 0.25



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
        /** AUTONOMOUS  PHASE */
        val aprilTagGXOutput = aprilTagGX.signalOrientation
        movement.driveRobotPosition(power = 1.0, inches = 20.0, smartAccel = true)
        movement.driveRobotStrafe(power = 1.0, inches = 20.0, smartAccel = true)
        movement.driveRobotTurn(power = 1.0, degree = 20.0, smartAccel = true)
        //Definitions of RoadRunner Trajectories
//        Trajectory RedSignalOne = drive.trajectoryBuilder(Pose2d(61.3, -35.3, Math.toRadians(180.0))) //coords of Red Alliance Start 2
//                .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
//                .lineTo(new Vector2d(43.2, -36.0))
//                .splineTo(new Vector2d(34.8, -59.5), Math.toRadians(180.0))
//                .build()
//        );
//
//        Trajectory RedSignalTwo = drive.trajectoryBuilder(new Pose2d(61.3, -35.3, Math.toRadians(180)))
//            .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
//            .lineTo(new Vector2d(42.8, -35.0))
//            .build()
//        );
//
//        Trajectory RedSignalThree =  drive.trajectoryBuilder(new Pose2d(61.3, -35.3, 180))
//            .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
//            .lineTo(new Vector2d(43.2, -36.0))
//            .splineTo(new Vector2d(34.8, -59.5), Math.toRadians(180.0))
//            .build()
//        );
//
//        Trajectory BlueSignalOne = drive.trajectoryBuilder(Pose2d(61.3, -35.3, Math.toRadians(0.0))) //coords of Red Alliance Start 2
//            .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
//            .lineTo(new Vector2d(43.2, -36.0))
//            .splineTo(new Vector2d(34.8, -59.5), Math.toRadians(180.0))
//            .build()
//        );
//
//        Trajectory BlueSignalTwo =  drive.trajectoryBuilder(new Pose2d(61.3, -35.3, Math.toRadians(0.0)))
//            .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
//            .lineTo(new Vector2d(42.8, -35.0))
//            .build()
//        );
//
//        Trajectory BlueSignalThree =  drive.trajectoryBuilder(new Pose2d(61.3, -35.3), Math.toRadians(0.0)))
//            .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
//            .lineTo(new Vector2d(43.2, -36.0))
//            .splineTo(new Vector2d(34.8, -59.5), Math.toRadians(180.0))
//            .build()
//        );

        //TriagonAuto (red only)
        //pull out & enter orientation (2 ft. per tile!)

        movement.driveRobotPosition(trigonPower, 72.0, true)
        movement.driveRobotTurn(power = trigonTurnPower, degree = 90.0, smartAccel = true)
        println("*drops thing*")

        for(i in 0..3) {
            //probably broken  -  moves to cone
            movement.driveRobotStrafe(trigonStrafePower, -12.0, true)
            //to pile
            println("*prepares collector*")
            movement.driveRobotPosition(trigonPower, -36.0, true)
            //to stop
            println("*rear-collects thing*")
            movement.driveRobotPosition(trigonPower, 36.0, true)
            movement.driveRobotStrafe(trigonStrafePower, -12.0, true)
            println("*drops thing*")
        }

        //basic drive forward
        when (aprilTagGXOutput) {
            SignalOrientation.one -> {
                movement.driveRobotStrafe(drivePower, sideDistance, true)
                movement.driveRobotPosition(drivePower, -forwardDistance, true)

            }
            SignalOrientation.two -> {
                movement.driveRobotStrafe(drivePower, sideDistance, true)
            }
            SignalOrientation.three -> {
                movement.driveRobotStrafe(drivePower, sideDistance, true)
                movement.driveRobotPosition(drivePower, forwardDistance, true)
            }
        }

        waitForStart();

        if(isStopRequested()) return;

    }

}