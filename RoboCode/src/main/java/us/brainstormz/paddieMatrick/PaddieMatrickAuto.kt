package us.brainstormz.paddieMatrick

//2 ft = 1 Square

//import com.acmerobotics.roadrunner.geometry.Pose2d
//import com.acmerobotics.roadrunner.trajectory.Trajectory
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.pid.PID
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.paddieMatrick.PaddieMatrickTeleOp.FourBarDegrees

@Autonomous(name= "PaddieMatrick Auto", group= "A")
class PaddieMatrickAuto: LinearOpMode() {

    val hardware = PaddieMatrickHardware()/** Change Depending on robot */
//    Drivetrain drive = new Drivetrain(hwMap);
    val movement = EncoderDriveMovement(hardware, TelemetryConsole(telemetry))

    var aprilTagGX = AprilTagEx()

    val fourBarPID = PID(kp= 0.03)
    val fourBar = FourBar(telemetry)

    val lift = Lift(telemetry)
    val liftPID = PID(kp= 0.003, ki= 0.0)

    val drivePower = 0.5
    val forwardDistance = 12.0
    val sideDistance = 12.0

    val trigonPower = 0.5
    val trigonTurnPower = 0.5
    val trigonStrafePower = 0.25



    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
        lift.init(leftMotor = hardware.leftLift, rightMotor = hardware.rightLift, hardware.liftLimitSwitch)
        fourBar.init(leftServo = hardware.left4Bar, rightServo = hardware.right4Bar, encoder = hardware.encoder4Bar)

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
        val aprilTagGXOutput = aprilTagGX.signalOrientation ?: SignalOrientation.Three

        //TriagonAuto (blue only)
        hardware.collector.power = 1.0
        //pull out & enter orientation (2 ft. per tile!)

        movement.driveRobotPositionWithTask(0.7, -50.0, true) {
            fourBar.goToPosition(180.0)
        }

        movement.driveRobotTurnWithTask(power = trigonTurnPower, degree = 88.0, smartAccel = true) {
            fourBar.goToPosition(180.0)
        }

        sleep(500)
        movement.driveRobotStrafeWithTask(trigonStrafePower, -12.0, true) {
            fourBar.goToPosition(180.0)
        }

        println("*drops thing*")
        deposit(){
            movement.driveRobotPosition(trigonPower, -8.0, true)
        }

        for(i in 1..2) {
            //moves to stack
            movement.driveRobotStrafeWithTask(trigonStrafePower, 12.0, true) {
                fourBar.goToPosition(FourBarDegrees.PreCollection.degrees)
            }
            println("*prepares collector*")
            movement.driveRobotPositionWithTask(trigonPower, 20.0, true) {
                prepareToCollect()
            }
            //collect
            println("*rear-collects thing*")
            sleep(2000)
            //to pole
            movement.driveRobotPosition(trigonPower, -20.0, true)
            movement.driveRobotStrafe(trigonStrafePower, -12.0, true)
            //deposit
            println("*drops thing*")
            deposit(){
                movement.driveRobotPosition(trigonPower, -8.0, true)
            }
        }

        movement.driveRobotStrafe(trigonStrafePower, 14.0, true)

        //basic drive forward
        when (aprilTagGXOutput) {
            SignalOrientation.One -> {
                movement.driveRobotPosition(trigonPower, 23.0, true)
            }
            SignalOrientation.Two -> {

            }
            SignalOrientation.Three -> {
                movement.driveRobotPosition(trigonPower, -23.0, true)
            }
        }

    }

    fun prepareToCollect() {
            val liftAtPos = lift(0)
            val barAtPos = fourBar.goToPosition(FourBarDegrees.PreCollection.degrees)
    }

    fun deposit(preEjectTask: ()->Unit) {
        val preDepositDegrees = FourBarDegrees.Depositing.degrees
        while (opModeIsActive()) {
            val liftAtPos = lift(PaddieMatrickTeleOp.LiftCounts.HighJunction.counts)
            val barAtPos = fourBar.goToPosition(preDepositDegrees)

            if (liftAtPos && barAtPos)
                break
        }

        telemetry.addLine("depo move 1 done")

        preEjectTask()

        val depositDegrees = 30 + FourBarDegrees.Depositing.degrees
        while (opModeIsActive()) {
            val liftAtPos = lift(PaddieMatrickTeleOp.LiftCounts.HighJunction.counts)
            val barAtPos = fourBar.goToPosition(depositDegrees)

            if (liftAtPos && barAtPos) {
                hardware.collector.power = -1.0
                sleep(200)
                hardware.collector.power = 0.2
                sleep(200)
                hardware.collector.power = -1.0
                sleep(800)
                hardware.collector.power = 0.0

                break
            }
        }

        fourBar.setServoPower(0.0)
    }

    fun lift(targetCounts: Int): Boolean {
        val liftPos = if (!hardware.liftLimitSwitch.state) {
            hardware.rightLift.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            hardware.rightLift.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            targetCounts.coerceAtLeast(hardware.rightLift.currentPosition)
        } else {
            targetCounts
        }

        val error = liftPos-hardware.rightLift.currentPosition

        val liftPower = liftPID.calcPID(error.toDouble())

        hardware.leftLift.power = liftPower
        hardware.rightLift.power = liftPower

        return error in -300..300
    }

}