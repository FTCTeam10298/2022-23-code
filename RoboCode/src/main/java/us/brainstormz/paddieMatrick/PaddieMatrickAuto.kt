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
import us.brainstormz.telemetryWizard.TelemetryWizard

@Autonomous(name= "PaddieMatrick Auto", group= "!")
class PaddieMatrickAuto: LinearOpMode() {

    val hardware = PaddieMatrickHardware()/** Change Depending on robot */
//    Drivetrain drive = new Drivetrain(hwMap);
    val movement = EncoderDriveMovement(hardware, TelemetryConsole(telemetry))

    val console = TelemetryConsole(telemetry)
    val wizard = TelemetryWizard(console, this)

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

//        wizard.newMenu("alliance", "What alliance are we on?", listOf("Red", "Blue"),"program", firstMenu = true)
        wizard.newMenu("program", "Which auto are we starting?", listOf("Cycle Auto", "Park Auto"), firstMenu = true)

        var hasWizardRun = false

        while (!isStarted && !isStopRequested) {
            aprilTagGX.runAprilTag(telemetry)

            if (aprilTagGX.signalOrientation != null && !hasWizardRun) {
                wizard.summonWizard(gamepad1)
                hasWizardRun = true
            }
//            val fourBarPower = fourBarPID.calcPID(fourBarTarget, fourBar.current4BarDegrees())
//            hardware.left4Bar.power = fourBarPower
//            hardware.right4Bar.power = fourBarPower
        }

        waitForStart()
        /** AUTONOMOUS  PHASE */
        val aprilTagGXOutput = aprilTagGX.signalOrientation ?: SignalOrientation.Three
//
//        when (wizard.wasItemChosen("alliance", "Red")) {
//            true -> {
        if (wizard.wasItemChosen("program", "Cycle Auto")) {
            cycleAuto(aprilTagGXOutput)
        }
        if (wizard.wasItemChosen("program", "Park Auto")) {
            val drivePower = 0.5
            val forwardDistance = 25.0
            val sideDistance = 30.0
            when (aprilTagGXOutput) {
                SignalOrientation.One -> {
                    movement.driveRobotPosition(0.8, 20.0, true)
                    movement.driveRobotPosition(drivePower, forwardDistance, true)
                    movement.driveRobotStrafe(drivePower, sideDistance, true)
                }
                SignalOrientation.Two -> {
                    movement.driveRobotPosition(0.8, 20.0, true)
                    movement.driveRobotPosition(drivePower, forwardDistance, true)
                }
                SignalOrientation.Three -> {
                    movement.driveRobotPosition(0.8, 20.0, true)
                    movement.driveRobotPosition(drivePower, forwardDistance, true)
                    movement.driveRobotStrafe(drivePower, -sideDistance, true)
                }
            }
        }
//            }
//            false -> {
//                if (wizard.wasItemChosen("program", "Cycle Auto")) {
//
//                }
//            }
//        }
    }

    fun cycleAuto(aprilTagGXOutput: SignalOrientation) {

        //TriagonAuto (blue only)
        hardware.collector.power = 0.1
        //pull out & enter orientation (2 ft. per tile!)

        movement.driveRobotPositionWithTask(0.7, -50.5, false) {
            fourBar.goToPosition(180.0)
            lift(1500)
        }

        hardware.collector.power = 0.0

        movement.driveRobotTurnWithTask(power = trigonTurnPower, degree = 88.0, smartAccel = true) {
            fourBar.goToPosition(180.0)
            lift(2000)
        }


        sleep(500)
        movement.driveRobotStrafeWithTask(trigonStrafePower, -12.5, true) {
            fourBar.goToPosition(180.0)
            lift(2500)
        }

        println("*drops thing*")
        deposit(){
            movement.driveRobotPosition(trigonPower, -8.0, true)
        }

//        for(i in 1..2) {
        //moves to stack
        movement.driveRobotStrafeWithTask(trigonStrafePower, 11.5, true) {
            fourBar.goToPosition(180.0)
        }
//            println("*prepares collector*")
//            movement.driveRobotPositionWithTask(trigonPower, 20.0, true) {
//                fourBar.goToPosition(FourBarDegrees.PreCollection.degrees)
//            }
//            //collect
//            println("*rear-collects thing*")
//            sleep(2000)
//            //to pole
//            movement.driveRobotPosition(trigonPower, -20.0, true)
//            movement.driveRobotStrafe(trigonStrafePower, -12.0, true)
//            //deposit
//            println("*drops thing*")
//            deposit(){
//                movement.driveRobotPosition(trigonPower, -8.0, true)
//            }
//        }


        telemetry.addLine("hi there")
        telemetry.update()

//        while (!fourBar.goToPosition(180.0)) {sleep(50)}
//        fourBar.setServoPower(0.0)
//        sleep(1000)
//        prepareToCollect()
//        while (opModeIsActive()) {
//            val liftAtPos = lift(0)
//            val barAtPos = fourBar.goToPosition(180.0)
//
//            if (liftAtPos)
//                break
//        }

        hardware.leftLift.power = -0.6
        hardware.rightLift.power = -0.6

//        hardware.leftLift.power = 0.0
//        hardware.rightLift.power = 0.0
//        fourBar.setServoPower(0.0)
//        movement.driveRobotStrafe(trigonStrafePower, 14.0, true)

        //basic drive forward
        when (aprilTagGXOutput) {
            SignalOrientation.One -> {
                movement.driveRobotPosition(trigonPower, 23.0, true)
            }
            SignalOrientation.Two -> {

            }
            SignalOrientation.Three -> {
                movement.driveRobotPosition(trigonPower, -21.0, true)
            }
        }

        hardware.leftLift.power = 0.0
        hardware.rightLift.power = 0.0

        telemetry.addLine("opmode over")
        telemetry.update()
    }

    fun prepareToCollect() {
        while (opModeIsActive()) {
            val liftAtPos = lift(0)
            val barAtPos = fourBar.goToPosition(180.0)

            if (liftAtPos)
                break
        }

        hardware.leftLift.power = 0.0
        hardware.rightLift.power = 0.0
        fourBar.setServoPower(0.0)
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
                sleep(500)
                hardware.collector.power = -1.0
                sleep(200)
                hardware.collector.power = 0.0
                sleep(200)
                hardware.collector.power = -1.0
                sleep(1000)
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

        val pidPower = liftPID.calcPID(error.toDouble())

        val liftPower = if (hardware.liftLimitSwitch.state)
            pidPower.coerceAtLeast(0.0)
        else
            pidPower

        hardware.leftLift.power = liftPower
        hardware.rightLift.power = liftPower

        val accuracy = 500
        return error in -accuracy..accuracy
    }

}