package us.brainstormz.paddieMatrick

//2 ft = 1 Square

//import com.acmerobotics.roadrunner.geometry.Pose2d
//import com.acmerobotics.roadrunner.trajectory.Trajectory
import com.acmerobotics.dashboard.FtcDashboard
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.motion.RRLocalizer
import us.brainstormz.pid.PID
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.paddieMatrick.PaddieMatrickTeleOp.FourBarDegrees
import us.brainstormz.telemetryWizard.TelemetryWizard

@Autonomous(name= "PaddieMatrick Auto", group= "!")
class PaddieMatrickAuto: LinearOpMode() {
    val hardware = PaddieMatrickHardware()/** Change Depending on robot */
//    Drivetrain drive = new Drivetrain(hwMap);
//    val movement = EncoderDriveMovement(hardware, TelemetryConsole(telemetry))

    val console = TelemetryConsole(telemetry)
    val wizard = TelemetryWizard(console, this)

//    var aprilTagGX = AprilTagEx()
//
//    val fourBarPID = PID(kp= 0.03)
//    val fourBar = FourBar(telemetry)
//
//    val lift = Lift(telemetry)
//    val liftPID = PID(kp= 0.006, ki= 0.0)
//
//    val drivePower = 0.5
//    val forwardDistance = 12.0
//    val sideDistance = 12.0
//
//    val trigonPower = 0.5
//    val trigonTurnPower = 0.5
//    val trigonStrafePower = 0.25
//
//
//
    override fun runOpMode() {
        val dashboard = FtcDashboard.getInstance()
        val dashboardTelemetry = dashboard.telemetry
        dashboardTelemetry.addData("speedY: ", 0)
        dashboardTelemetry.addData("distanceErrorX: ", 0)
        dashboardTelemetry.addData("distanceErrorY: ", 0)
        dashboardTelemetry.addData("total distance error: ", 0)
        dashboardTelemetry.addData("angle error degrees: ", 0)
        dashboardTelemetry.addData("speedY: ", 0)
        dashboardTelemetry.addData("speedX: ", 0)
        dashboardTelemetry.addData("speedA: ", 0)
        dashboardTelemetry.update()

//        /** INIT PHASE */
        hardware.init(hardwareMap)

        val localizer = RRLocalizer(hardware)
        localizer.setPositionAndRotation(x= 65.0, y= -35.0, r= 90.0)
        val movement = MecanumMovement(hardware = hardware, localizer = localizer, telemetry = dashboardTelemetry)

        waitForStart()
        dashboardTelemetry.addData("current pos: ", localizer.currentPositionAndRotation())
        /** AUTONOMOUS  PHASE */
        movement.goToPosition(PositionAndRotation(x= 12.0, y= -35.0, r= 90.0), this)

//        lift.init(leftMotor = hardware.leftLift, rightMotor = hardware.rightLift, hardware.liftLimitSwitch)
//        fourBar.init(leftServo = hardware.left4Bar, rightServo = hardware.right4Bar, encoder = hardware.encoder4Bar)
//
//        aprilTagGX.initAprilTag(hardwareMap, telemetry, this)
//
//
//
////        val fourBarTarget = fourBar.current4BarDegrees() - 70
//
////        wizard.newMenu("alliance", "What alliance are we on?", listOf("Red", "Blue"),"program", firstMenu = true)
//        wizard.newMenu("program", "Which auto are we starting?", listOf("Park Auto" to null, "New Cycle Auto" to "startPos"), firstMenu = true)
////        wizard.newMenu("alliance", "Which alliance are we on?", listOf("Blue", "Red"), nextMenu = "startPos")
//        wizard.newMenu("startPos", "Which side are we starting?", listOf("Right", "Left"))
//
//        var hasWizardRun = false
//
//        while (!isStarted && !isStopRequested) {
//            println("i'm a runnin'")
//            aprilTagGX.runAprilTag(telemetry)
//
//            if (aprilTagGX.signalOrientation != null && !hasWizardRun) {
//                wizard.summonWizard(gamepad1)
//                hasWizardRun = true
//            }
////            val fourBarPower = fourBarPID.calcPID(fourBarTarget, fourBar.current4BarDegrees())
////            hardware.left4Bar.power = fourBarPower
////            hardware.right4Bar.power = fourBarPower
//        }
//
//        waitForStart()
//        /** AUTONOMOUS  PHASE */
//
//        val aprilTagGXOutput = aprilTagGX.signalOrientation ?: SignalOrientation.Three
////
////        when (wizard.wasItemChosen("alliance", "Red")) {
////            true -> {
//        when {
//            wizard.wasItemChosen("program", "New Cycle Auto") -> {
//                when {
//                    wizard.wasItemChosen("startPos", "Right") -> {
//                        blueTerminalsCycleAuto(aprilTagGXOutput)
//                    }
//                    wizard.wasItemChosen("startPos", "Left") -> {
//                        redTerminalsCycleAuto(aprilTagGXOutput)
//                    }
//                }
//            }
//            wizard.wasItemChosen("program", "Park Auto") -> {
//                val drivePower = 0.5
//                val forwardDistance = -25.0
//                val sideDistance = 30.0
//                when (aprilTagGXOutput) {
//                    SignalOrientation.One -> {
//                        movement.driveRobotPosition(drivePower, forwardDistance, true)
//                        movement.driveRobotStrafe(drivePower, -sideDistance, true)
//                    }
//                    SignalOrientation.Two -> {
//                        movement.driveRobotPosition(drivePower, forwardDistance, true)
//                    }
//                    SignalOrientation.Three -> {
//                        movement.driveRobotPosition(drivePower, forwardDistance, true)
//                        movement.driveRobotStrafe(drivePower, sideDistance, true)
//                    }
//                }
//            }
//        }
////            }
////            false -> {
////                if (wizard.wasItemChosen("program", "Cycle Auto")) {
////
////                }
////            }
////        }
//    }
//
//    fun redTerminalsCycleAuto(aprilTagGXOutput: SignalOrientation) {
//
//        val movementSpeed = 0.5
//
//        hardware.collector.power = 0.05
//
//        movement.driveRobotPositionWithTask(movementSpeed, -51.0, true) {
//            fourBar.goToPosition(FourBarDegrees.Depositing.degrees)
//            lift(PaddieMatrickTeleOp.LiftCounts.MidJunction.counts)
//            hardware.collector.power = 0.0
//        }
//
//        hardware.collector.power = 0.05
//
//        movement.driveRobotTurnWithTask(movementSpeed, 89.0, true) {
//            fourBar.goToPosition(FourBarDegrees.Depositing.degrees)
//            lift(PaddieMatrickTeleOp.LiftCounts.HighJunction.counts)
//        }
//
//        movement.driveRobotStrafeWithTask(movementSpeed, -16.4, true) {
//            fourBar.goToPosition(FourBarDegrees.Depositing.degrees)
//            lift(PaddieMatrickTeleOp.LiftCounts.HighJunction.counts)
//        }
//
//        hardware.collector.power = 0.0
//
//        sleep(100)
//        movement.driveRobotPositionWithTask(0.2, -4.1, true) {
//            fourBar.goToPosition(FourBarDegrees.Depositing.degrees)
//            lift(PaddieMatrickTeleOp.LiftCounts.HighJunction.counts)
//        }
//
//        while (!isStopRequested) {
//            if (fourBar.goToPosition(FourBarDegrees.Depositing.degrees + 35))
//                break
//        }
//
//        while (!isStopRequested) {
//            if (lift(PaddieMatrickTeleOp.LiftCounts.HighJunction.counts - 500)) {
//                hardware.leftLift.power = 0.0
//                hardware.rightLift.power = 0.0
//                break
//            }
//        }
//
//        hardware.collector.power = -1.0
//        sleep(1000)
//        hardware.collector.power = 0.0
//
//        while (!isStopRequested) {
//            if (fourBar.goToPosition(180.0))
//                break
//        }
//
//        movement.driveRobotPositionWithTask(movementSpeed, 4.0, true) {
//            fourBar.goToPosition(180.0)
//            lift(10)
//        }
//
//        movement.driveRobotStrafeWithTask(movementSpeed, 16.0, true) {
//            fourBar.goToPosition(180.0)
//            lift(10)
//        }
//
//        val parkDistance = 22.0
//        val parkDrivePhoInches = when (aprilTagGXOutput) {
//            SignalOrientation.One -> parkDistance
//            SignalOrientation.Two -> 0.0
//            SignalOrientation.Three -> -parkDistance
//        }
//
//        movement.driveRobotPositionWithTask(movementSpeed, parkDrivePhoInches, true) {
//            fourBar.goToPosition(180.0)
//            lift(10)
//        }
//
//        telemetry.addLine("opmode over")
//        telemetry.update()
//    }
//
//    fun blueTerminalsCycleAuto(aprilTagGXOutput: SignalOrientation) {
//
//        val movementSpeed = 0.5
//
//        hardware.collector.power = 0.05
//
//        movement.driveRobotPositionWithTask(movementSpeed, -50.5, true) {
//            fourBar.goToPosition(FourBarDegrees.Depositing.degrees)
//            lift(PaddieMatrickTeleOp.LiftCounts.MidJunction.counts)
//            hardware.collector.power = 0.0
//        }
//
//        hardware.collector.power = 0.05
//
//        movement.driveRobotTurnWithTask(movementSpeed, -89.0, true) {
//            fourBar.goToPosition(FourBarDegrees.Depositing.degrees)
//            lift(PaddieMatrickTeleOp.LiftCounts.HighJunction.counts)
//        }
//
//        movement.driveRobotStrafeWithTask(movementSpeed, 16.4, true) {
//            fourBar.goToPosition(FourBarDegrees.Depositing.degrees)
//            lift(PaddieMatrickTeleOp.LiftCounts.HighJunction.counts)
//        }
//
//        hardware.collector.power = 0.0
//
//        sleep(100)
//        movement.driveRobotPositionWithTask(0.2, -3.9, true) {
//            fourBar.goToPosition(FourBarDegrees.Depositing.degrees)
//            lift(PaddieMatrickTeleOp.LiftCounts.HighJunction.counts)
//        }
//
//        while (!isStopRequested) {
//            if (fourBar.goToPosition(FourBarDegrees.Depositing.degrees + 35))
//                break
//        }
//
//        while (!isStopRequested) {
//            if (lift(PaddieMatrickTeleOp.LiftCounts.HighJunction.counts - 500)) {
//                hardware.leftLift.power = 0.0
//                hardware.rightLift.power = 0.0
//                break
//            }
//        }
//
//        hardware.collector.power = -1.0
//        sleep(1000)
//        hardware.collector.power = 0.0
//
//        while (!isStopRequested) {
//            if (fourBar.goToPosition(180.0))
//                break
//        }
//
//        movement.driveRobotPositionWithTask(movementSpeed, 4.0, true) {
//            fourBar.goToPosition(180.0)
//            lift(10)
//        }
//
//        movement.driveRobotStrafeWithTask(movementSpeed, -16.0, true) {
//            fourBar.goToPosition(180.0)
//            lift(10)
//        }
//
//        val parkDistance = 22.0
//        val parkDrivePhoInches = when (aprilTagGXOutput) {
//            SignalOrientation.One -> -parkDistance
//            SignalOrientation.Two -> 0.0
//            SignalOrientation.Three -> parkDistance
//        }
//
//        movement.driveRobotPositionWithTask(movementSpeed, parkDrivePhoInches, true) {
//            fourBar.goToPosition(180.0)
//            lift(10)
//        }
//
//        telemetry.addLine("opmode over")
//        telemetry.update()
//    }
//
//    fun oldCycleAuto(aprilTagGXOutput: SignalOrientation) {
//
//        //TriagonAuto (blue only)
//        hardware.collector.power = 0.1
//        //pull out & enter orientation (2 ft. per tile!)
//
//        movement.driveRobotPositionWithTask(0.7, -50.5, false) {
//            fourBar.goToPosition(180.0)
//            lift(1500)
//        }
//
//        hardware.collector.power = 0.0
//
//        movement.driveRobotTurnWithTask(power = trigonTurnPower, degree = 88.0, smartAccel = true) {
//            fourBar.goToPosition(180.0)
//            lift(2000)
//        }
//
//
//        sleep(500)
//        movement.driveRobotStrafeWithTask(trigonStrafePower, -12.5, true) {
//            fourBar.goToPosition(180.0)
//            lift(2500)
//        }
//
//        println("*drops thing*")
//        deposit(){
//            movement.driveRobotPosition(trigonPower, -8.0, true)
//        }
//
////        for(i in 1..2) {
//        //moves to stack
//        movement.driveRobotStrafeWithTask(trigonStrafePower, 11.5, true) {
//            fourBar.goToPosition(180.0)
//        }
////            println("*prepares collector*")
////            movement.driveRobotPositionWithTask(trigonPower, 20.0, true) {
////                fourBar.goToPosition(FourBarDegrees.PreCollection.degrees)
////            }
////            //collect
////            println("*rear-collects thing*")
////            sleep(2000)
////            //to pole
////            movement.driveRobotPosition(trigonPower, -20.0, true)
////            movement.driveRobotStrafe(trigonStrafePower, -12.0, true)
////            //deposit
////            println("*drops thing*")
////            deposit(){
////                movement.driveRobotPosition(trigonPower, -8.0, true)
////            }
////        }
//
//
//        telemetry.addLine("hi there")
//        telemetry.update()
//
////        while (!fourBar.goToPosition(180.0)) {sleep(50)}
////        fourBar.setServoPower(0.0)
////        sleep(1000)
////        prepareToCollect()
////        while (opModeIsActive()) {
////            val liftAtPos = lift(0)
////            val barAtPos = fourBar.goToPosition(180.0)
////
////            if (liftAtPos)
////                break
////        }
//
//        hardware.leftLift.power = -0.6
//        hardware.rightLift.power = -0.6
//
////        hardware.leftLift.power = 0.0
////        hardware.rightLift.power = 0.0
////        fourBar.setServoPower(0.0)
////        movement.driveRobotStrafe(trigonStrafePower, 14.0, true)
//
//        //basic drive forward
//        when (aprilTagGXOutput) {
//            SignalOrientation.One -> {
//                movement.driveRobotPosition(trigonPower, 23.0, true)
//            }
//            SignalOrientation.Two -> {
//
//            }
//            SignalOrientation.Three -> {
//                movement.driveRobotPosition(trigonPower, -21.0, true)
//            }
//        }
//
//        hardware.leftLift.power = 0.0
//        hardware.rightLift.power = 0.0
//
//        telemetry.addLine("opmode over")
//        telemetry.update()
//    }
//
//    fun prepareToCollect() {
//        while (opModeIsActive()) {
//            val liftAtPos = lift(0)
//            val barAtPos = fourBar.goToPosition(180.0)
//
//            if (liftAtPos)
//                break
//        }
//
//        hardware.leftLift.power = 0.0
//        hardware.rightLift.power = 0.0
//        fourBar.setServoPower(0.0)
//    }
//
//    fun deposit(preEjectTask: ()->Unit) {
//        val preDepositDegrees = FourBarDegrees.Depositing.degrees
//        while (opModeIsActive()) {
//            val liftAtPos = lift(PaddieMatrickTeleOp.LiftCounts.HighJunction.counts)
//            val barAtPos = fourBar.goToPosition(preDepositDegrees)
//
//            if (liftAtPos && barAtPos)
//                break
//        }
//
//        telemetry.addLine("depo move 1 done")
//
//        preEjectTask()
//
//        val depositDegrees = 30 + FourBarDegrees.Depositing.degrees
//        while (opModeIsActive()) {
//            val liftAtPos = lift(PaddieMatrickTeleOp.LiftCounts.HighJunction.counts)
//            val barAtPos = fourBar.goToPosition(depositDegrees)
//
//            if (liftAtPos && barAtPos) {
//                sleep(500)
//                hardware.collector.power = -1.0
//                sleep(200)
//                hardware.collector.power = 0.0
//                sleep(200)
//                hardware.collector.power = -1.0
//                sleep(1000)
//                hardware.collector.power = 0.0
//
//                break
//            }
//        }
//
//        fourBar.setServoPower(0.0)
//    }
//
//    fun lift(targetCounts: Int): Boolean {
//        val adjustedTarget = if (!hardware.liftLimitSwitch.state) {
//            hardware.rightLift.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
//            hardware.rightLift.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
//            targetCounts.coerceAtLeast(hardware.rightLift.currentPosition)
//        } else {
//            targetCounts
//        }
//        telemetry.addLine("adjustedTarget: $adjustedTarget")
//
//        val error = adjustedTarget-hardware.rightLift.currentPosition
//        telemetry.addLine("error: $error")
//
//        val pidPower = liftPID.calcPID(error.toDouble())
//        telemetry.addLine("pidPower: $pidPower")
//
//        val liftPower = if (!hardware.liftLimitSwitch.state)
//            pidPower.coerceAtLeast(0.0)
//        else
//            pidPower
//        telemetry.addLine("liftPower: $liftPower")
//        telemetry.update()
//
//        hardware.leftLift.power = liftPower
//        hardware.rightLift.power = liftPower
//
//        val accuracy = 500
//        return error in -accuracy..accuracy
    }

}