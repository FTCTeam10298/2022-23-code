package us.brainstormz.paddieMatrick

//2 ft = 1 Square

//import com.acmerobotics.roadrunner.geometry.Pose2d
//import com.acmerobotics.roadrunner.trajectory.Trajectory
import com.acmerobotics.dashboard.FtcDashboard
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.motion.RRLocalizer
import us.brainstormz.pid.PID
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.paddieMatrick.PaddieMatrickTeleOp.FourBarDegrees
import us.brainstormz.telemetryWizard.TelemetryWizard

@Autonomous(name= "Old PaddieMatrick Auto", group= "!")
class PaddieMatrickAuto: LinearOpMode() {
    val hardware = PaddieMatrickHardware()/** Change Depending on robot */
//    Drivetrain drive = new Drivetrain(hwMap);
//    val movement = EncoderDriveMovement(hardware, TelemetryConsole(telemetry))

    val console = TelemetryConsole(telemetry)
    val wizard = TelemetryWizard(console, this)

//    var aprilTagGX = AprilTagEx()
//
    val fourBarPID = PID(kp= 0.03)
    val fourBar = FourBar(telemetry)
//
    val lift = Lift(telemetry)
    val liftPID = PID(kp= 0.0065, ki= 0.00000001)

    enum class ParkPositions(val pos: PositionAndRotation) {
        One(PositionAndRotation(x = -20.0, y = -52.0, r = 0.0)),
        Two(PositionAndRotation(x = 0.0, y = -52.0, r = 0.0)),
        Three(PositionAndRotation(x = 20.0, y = -52.0, r = 0.0)),
    }

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
//        voltageHandler()

        val localizer = RRLocalizer(hardware)
        val movement = MecanumMovement(hardware = hardware, localizer = localizer, telemetry = dashboardTelemetry)

        lift.init(leftMotor = hardware.leftLift, rightMotor = hardware.rightLift, hardware.liftLimitSwitch)
        fourBar.init(leftServo = hardware.left4Bar, rightServo = hardware.right4Bar, encoder = hardware.encoder4Bar)
        val collector = Collector()
        val depositor = Depositor(hardware, fourBar, collector, telemetry)


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
        waitForStart()
        /** AUTONOMOUS  PHASE */
        val aprilTagGXOutput = SignalOrientation.Two// aprilTagGX.signalOrientation ?: SignalOrientation.Three

        var targetPosition = PositionAndRotation(x= 0.0, y= -49.0, r= 0.0)
        movement.goToPosition(targetPosition, this, 0.0..0.9) {
            movement.precisionInches = 3.0
            fourBar.goToPosition(FourBarDegrees.PreDeposit.degrees)
            lift(PaddieMatrickTeleOp.LiftCounts.MidJunction.counts)
        }

        val depositPosition = PositionAndRotation(x= -5.8, y= -58.2, r= 45.0) /** Deposit Position */

        targetPosition.r = depositPosition.r
        movement.goToPosition(targetPosition, this, 0.0..1.0) {
            movement.precisionInches = 3.0
            fourBar.goToPosition(FourBarDegrees.PreDeposit.degrees)
            lift(PaddieMatrickTeleOp.LiftCounts.HighJunction.counts)
        }

        targetPosition = depositPosition
        movement.goToPosition(targetPosition, this, 0.0..0.5) {
            fourBar.goToPosition(FourBarDegrees.PreDeposit.degrees)
            lift(PaddieMatrickTeleOp.LiftCounts.HighJunction.counts)
        }

        telemetry.addLine("Depositing done")
        telemetry.update()

        while (opModeIsActive()) {
            fourBar.goToPosition(FourBarDegrees.PreDeposit.degrees)
            val isLiftAtPosition = lift(PaddieMatrickTeleOp.LiftCounts.HighJunction.counts)

            if (isLiftAtPosition)
                break
        }

        while (opModeIsActive()) {
            val fourBarAtPosition = fourBar.goToPosition(FourBarDegrees.Deposit.degrees)
            lift(PaddieMatrickTeleOp.LiftCounts.HighJunction.counts)

            if (fourBarAtPosition)
                break
        }

        while (opModeIsActive()) {
            val fourBarAtPosition = fourBar.goToPosition(FourBarDegrees.Deposit.degrees)
            val liftAtPosition = lift(PaddieMatrickTeleOp.LiftCounts.HighJunction.counts)

            if (fourBarAtPosition && liftAtPosition)
                break
        }

        hardware.collector.power = -1.0
        sleep(700)
        hardware.collector.power = 0.0

        /** Cone deposited */

        while (opModeIsActive()) {
            val fourBarAtPosition = fourBar.goToPosition(FourBarDegrees.Vertical.degrees)
            lift(PaddieMatrickTeleOp.LiftCounts.HighJunction.counts)

            if (fourBarAtPosition)
                break
        }

        /** Driving to stack */

        targetPosition = PositionAndRotation(x= -4.0, y= -52.0, r= 45.0)
        movement.goToPosition(targetPosition, this, 0.01..0.8) {
            movement.precisionInches = 1.0
            fourBar.goToPosition(FourBarDegrees.Vertical.degrees)
            lift(PaddieMatrickTeleOp.LiftCounts.Bottom.counts)
        }

        targetPosition.r = 90.0
        movement.goToPosition(targetPosition, this, 0.0..0.8) {
            movement.precisionDegrees = 1.0
            fourBar.goToPosition(FourBarDegrees.Vertical.degrees)
            lift(PaddieMatrickTeleOp.LiftCounts.Bottom.counts)
        }

        while (opModeIsActive()) {
            fourBar.goToPosition(FourBarDegrees.Vertical.degrees)
            val liftAtPosition = lift(PaddieMatrickTeleOp.LiftCounts.Bottom.counts)

            if (liftAtPosition)
                break
        }

        /** Cycles */
        for (i in 1..2) {
            val precollectionPosition = PositionAndRotation(x = 21.5, y = -51.0, r = 90.0)

            targetPosition = precollectionPosition
            movement.goToPosition(targetPosition, this, 0.0..0.5) {
                fourBar.goToPosition(FourBarDegrees.Vertical.degrees)
                lift(PaddieMatrickTeleOp.LiftCounts.Bottom.counts)
            }

            /** Collect */
            while (opModeIsActive() && !depositor.isConeInFunnel()) {
                targetPosition.x += 0.6
                movement.goToPosition(targetPosition, this, 0.0..0.6) {
                    depositor.automatedCollection(true)
                }
            }

            while (opModeIsActive() && !depositor.isConeInCollector()) {
                depositor.automatedCollection(true)
            }

            targetPosition = PositionAndRotation(x = 0.0, y = -52.0, r = 90.0)
            movement.goToPosition(targetPosition, this, 0.0..0.5) {
                fourBar.goToPosition(FourBarDegrees.Vertical.degrees)
                lift(PaddieMatrickTeleOp.LiftCounts.MidJunction.counts)
                hardware.collector.power = 0.0
            }

            /** Deposit */
            targetPosition = depositPosition
            movement.goToPosition(targetPosition, this, 0.0..0.5) {
                fourBar.goToPosition(FourBarDegrees.Vertical.degrees)
                lift(PaddieMatrickTeleOp.LiftCounts.HighJunction.counts)
            }

            while (opModeIsActive() && depositor.isConeInCollector()) {
                depositor.automatedDeposit(Depositor.LiftCounts.HighJunction)
            }

            while (opModeIsActive()) {
                val fourBarAtPosition = fourBar.goToPosition(FourBarDegrees.Vertical.degrees)
                lift(PaddieMatrickTeleOp.LiftCounts.HighJunction.counts)

                if (fourBarAtPosition)
                    break
            }


            targetPosition = PositionAndRotation(x = 0.0, y = -52.0, r = 90.0)
            movement.goToPosition(targetPosition, this, 0.0..0.5) {
                fourBar.goToPosition(FourBarDegrees.Vertical.degrees)
                lift(PaddieMatrickTeleOp.LiftCounts.Bottom.counts)
                hardware.collector.power = 0.0
            }
        }

        /** Park */

        when (aprilTagGXOutput) {
            SignalOrientation.One -> {
                movement.goToPosition(ParkPositions.One.pos, this, 0.0..0.5)
            }
            SignalOrientation.Two -> {
                movement.goToPosition(ParkPositions.Two.pos, this, 0.0..0.5)
            }
            SignalOrientation.Three -> {
                movement.goToPosition(ParkPositions.Three.pos, this, 0.0..0.5)
            }
        }

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

    }

    fun lift(targetCounts: Int): Boolean {
        val adjustedTarget = if (!hardware.liftLimitSwitch.state) {
            hardware.rightLift.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            hardware.rightLift.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            targetCounts.coerceAtLeast(hardware.rightLift.currentPosition)
        } else {
            targetCounts
        }
        telemetry.addLine("adjustedTarget: $adjustedTarget")

        val error = adjustedTarget - hardware.rightLift.currentPosition
        telemetry.addLine("error: $error")

        val pidPower = liftPID.calcPID(error.toDouble())
        telemetry.addLine("pidPower: $pidPower")

        val liftPower = if (!hardware.liftLimitSwitch.state)
            pidPower.coerceAtLeast(0.0)
        else
            pidPower
        telemetry.addLine("liftPower: $liftPower")
        telemetry.update()

        hardware.leftLift.power = liftPower
        hardware.rightLift.power = liftPower

        val accuracy = 500
        return error in -accuracy..accuracy
    }
    fun voltageHandler() {
        telemetry.addLine("Voltage: ${hardware.getVoltage()}")
        telemetry.update()
        val minVoltage = 13.3
        if (hardware.getVoltage() < minVoltage) {
            telemetry.addLine("VOLTAGE IS LESS THAN $minVoltage. \nHold X on gamepad1 when initializing to ignore")
            telemetry.update()
            if (!gamepad1.x) {
                this.requestOpModeStop()
            }

        }
    }

}