package us.brainstormz.paddieMatrick

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DistanceSensor
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.pid.PID
import us.brainstormz.utils.MathHelps
import kotlin.math.IEEErem
import kotlin.math.abs

@TeleOp(name= "PaddieMatrick Tele-op", group= "!")
class PaddieMatrickTeleOp: OpMode() {

    val hardware = PaddieMatrickHardware()
    val movement = MecanumDriveTrain(hardware)
    val fourBar = FourBar(telemetry)

    val collector = Collector()

    var fourBarTarget = 0.0
    val allowedZone = 50.0..300.0
    val fourBarSpeed = 120.0
    enum class fourBarModes {
        FOURBAR_PID,
        FOURBAR_MANUAL
    }
    var fourBarMode = fourBarModes.FOURBAR_PID
    companion object {
        var centerPosition = 110.0
    }

    val liftPID = PID(kp= 0.003, ki= 0.0)
    var liftTarget = 0.0
    val liftSpeed = 1800.0

    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
        hardware.odomRaiser1.position = 1.0
        hardware.odomRaiser2.position = 1.0
        fourBar.init(leftServo = hardware.left4Bar, rightServo = hardware.right4Bar, encoder = hardware.encoder4Bar)
        fourBarTarget = fourBar.current4BarDegrees()
        fourBar.pid = PID(kp= 0.011, kd= 0.0001)//0000001)
    }

    override fun start() {
        fourBarTarget = fourBar.current4BarDegrees()
    }

    var prevTime = System.currentTimeMillis()
    override fun loop() {
        /** TELE-OP PHASE */
        val dt = System.currentTimeMillis() - prevTime
        prevTime = System.currentTimeMillis()
        telemetry.addLine("dt: $dt")

        val dashboard = FtcDashboard.getInstance()
        val dashboardTelemetry = dashboard.telemetry

        telemetry.addLine("limit switch state: ${hardware.liftLimitSwitch.state}")

        // DRONE DRIVE
        val antiTipModifier: Double = MathHelps.scaleBetween(hardware.rightLift.currentPosition.toDouble() + 1, 0.0..3000.0, 1.0..1.8)
        telemetry.addLine("antiTipModifier: $antiTipModifier")

        val slowSpeed = 0.3

//        val yInput = gamepad1.left_stick_y.toDouble()
//        val xInput =
        val gamepadSpeedR = gamepad1.right_stick_x.toDouble()
        val gamepadSpeedY = when {
            gamepad1.dpad_up -> {
                slowSpeed
            }
            gamepad1.dpad_down -> {
                -slowSpeed
            }
            else -> -gamepad1.left_stick_y.toDouble()
        }
        val gamepadSpeedX = when {
            gamepad1.dpad_left -> {
                slowSpeed
            }
            gamepad1.dpad_right -> {
                -slowSpeed
            }
            else -> -gamepad1.left_stick_x.toDouble()
        }

        val balanceAngle = hardware.getImuOrientation().x
        telemetry.addLine("balanceAngle: $balanceAngle")
        val offBalanceThresholdDegrees = 4
        val robotIsTippedTooFar = abs(balanceAngle) > offBalanceThresholdDegrees
        telemetry.addLine("robotIsTippedTooFar: $robotIsTippedTooFar")

        val balanceAngularVelocity = hardware.imu.getRobotAngularVelocity(AngleUnit.DEGREES).xRotationRate
        telemetry.addLine("balanceAngularVelocity: $balanceAngularVelocity")
        val offBalanceThresholdDegreesPerSecond = 40
        val robotIsTippingTooFast = (abs(balanceAngularVelocity) > offBalanceThresholdDegreesPerSecond)
        telemetry.addLine("robotIsTippingTooFast: $robotIsTippingTooFast")

        val robotIsOffBalance = robotIsTippedTooFar || robotIsTippingTooFast
        telemetry.addLine("robotIsOffBalance: $robotIsOffBalance")

        val cappedSpeed = if (robotIsOffBalance) {
            val degreesPerSecondToPowerMultiplier: Double = 0.1
            // if the requested power is in the right direction allow it, otherwise slow it down
            val powerMax: Double = if (balanceAngularVelocity != 0.0f) (1 / (balanceAngularVelocity * degreesPerSecondToPowerMultiplier)).coerceIn(-1.0..1.0) else 1.0
            telemetry.addLine("powerMax: $powerMax")
            dashboardTelemetry.addData("powerMax: ", powerMax)
            dashboardTelemetry.addData("balanceAngle: ", balanceAngle)
            dashboardTelemetry.addData("balanceAngularVelocity: ", balanceAngularVelocity)
            dashboardTelemetry.update()

            capGamepadSpeed(gamepadSpeedY, powerMax)
        } else gamepadSpeedY
        telemetry.addLine("cappedSpeed: $cappedSpeed")



        val y = -cappedSpeed
        val x = gamepadSpeedX
        val r = -gamepadSpeedR * abs(gamepadSpeedR) //square the number and keep the sign
        movement.driveSetPower((y + x - r),
                               (y - x + r),
                               (y - x - r),
                               (y + x + r))

        // Four bar
        telemetry.addLine("fourbar position: ${fourBar.current4BarDegrees()}")
        telemetry.addLine("fourBarTarget: $fourBarTarget")

        when {
            gamepad2.x || (gamepad1.b && !gamepad1.start) -> {
                fourBarMode = fourBarModes.FOURBAR_PID
                fourBarTarget = Depositor.FourBarDegrees.PreCollection.degrees
            }
            (gamepad1.a && !gamepad1.start) || gamepad2.y -> {} // dummy for preset elsewhere
            abs(gamepad2.right_stick_y) > 0.1 -> {
                fourBarMode = fourBarModes.FOURBAR_MANUAL
            }
            abs(gamepad2.right_stick_y) < 0.1 && fourBarMode == fourBarModes.FOURBAR_MANUAL -> {
                fourBarMode = fourBarModes.FOURBAR_PID
                fourBarTarget = fourBar.current4BarDegrees()
            }
        }

        //fourBarTarget += (gamepad2.right_stick_y.toDouble() * fourBarSpeed / dt)

        if (fourBarMode == fourBarModes.FOURBAR_PID) {
            fourBarTarget = MathHelps.wrap360(fourBarTarget).coerceIn(allowedZone)
            fourBar.goToPosition(fourBarTarget)
            telemetry.addLine("fourBarMode: PID")
        }
        else { // (fourBarMode == fourBarModes.FOURBAR_MANUAL
            fourBar.setServoPower(gamepad2.right_stick_y.toDouble())
            telemetry.addLine("fourBarMode: Manual")
        }

//        val fourBarPower = -gamepad2.right_stick_y.toDouble()
//                fourBarPID.calcPID(MathHelps.wrap360(fourBarTarget - (fourBar.current4BarDegrees())))

//        telemetry.addLine("fourBarPower: $fourBarPower")

//        hardware.left4Bar.power = fourBarPower
//        hardware.right4Bar.power = fourBarPower

        // Lift
        when {
            gamepad2.dpad_up-> {
                liftTarget = Depositor.LiftCounts.HighJunction.counts.toDouble()

                fourBarMode = fourBarModes.FOURBAR_PID
                fourBarTarget = Depositor.FourBarDegrees.PreDeposit.degrees
            }
            (gamepad1.a && !gamepad1.start) || gamepad2.y -> {
                automaticDeposit()
            }
            gamepad2.dpad_left -> {
                liftTarget = Depositor.LiftCounts.MidJunction.counts.toDouble()

                fourBarMode = fourBarModes.FOURBAR_PID
                fourBarTarget = Depositor.FourBarDegrees.PreDeposit.degrees
            }
            gamepad2.dpad_right -> {
                liftTarget = Depositor.LiftCounts.LowJunction.counts.toDouble()

                fourBarMode = fourBarModes.FOURBAR_PID
                fourBarTarget = Depositor.FourBarDegrees.PreDeposit.degrees
            }
            gamepad2.dpad_down -> {

                liftTarget = Depositor.LiftCounts.Bottom.counts.toDouble()

                fourBarMode = fourBarModes.FOURBAR_PID
                fourBarTarget = Depositor.FourBarDegrees.PreCollection.degrees
            }
            gamepad2.a -> {
                liftTarget += (-0.5 * liftSpeed / dt)
            }
        }

        liftTarget += (-gamepad2.left_stick_y.toDouble() * liftSpeed / dt)

        liftTarget = if (!hardware.liftLimitSwitch.state) {
            hardware.rightLift.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            hardware.rightLift.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            hardware.leftLift.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            liftTarget.coerceAtLeast(0.0)
        } else {
            liftTarget
        }

        val liftPower = liftPID.calcPID(liftTarget, hardware.rightLift.currentPosition.toDouble())

        telemetry.addLine("lift position: ${hardware.rightLift.currentPosition}")
        telemetry.addLine("liftTarget: $liftTarget")
        telemetry.addLine("liftPower: $liftPower")
        telemetry.addLine("Lift Amps:\n    Left: ${hardware.leftLift.getCurrent(CurrentUnit.AMPS)}\n   Right: ${hardware.rightLift.getCurrent(CurrentUnit.AMPS)}")

        powerLift(liftPower)

        // Collector
        when {
            gamepad1.right_trigger > 0 || gamepad2.right_trigger > 0 -> {
                hardware.collector.power = 1.0
            }
            gamepad1.left_trigger > 0 || gamepad2.left_trigger > 0 -> {
                hardware.collector.power = -1.0
            }
            (gamepad1.a && !gamepad1.start) || gamepad2.y -> {}
            else -> {
                hardware.collector.power = 0.05
            }
        }

        when {
            gamepad2.b -> {
                println("Auto Collection Disabled >:0 UwU OwO >::)")
                hardware.funnelLifter.position = collector.funnelDown
            }
            gamepad1.right_bumper || gamepad2.right_bumper -> {
                automatedCollection(multiCone = false)
            }
            gamepad1.left_bumper || gamepad2.left_bumper -> {
                automatedCollection(multiCone = true)
            }
            else -> {
                hardware.funnelLifter.position = collector.funnelUp
            }
        }

        telemetry.addLine("red: ${hardware.collectorSensor.red()}")
        telemetry.addLine("blue: ${hardware.collectorSensor.blue()}")
        telemetry.addLine("alpha: ${hardware.collectorSensor.alpha()}")
        telemetry.addLine("optical: ${hardware.collectorSensor.rawOptical()}")
        telemetry.addLine("distance: ${hardware.collectorSensor.getDistance(DistanceUnit.MM)}")
    }

    private fun capGamepadSpeed(gamepadPower: Double, powerMax: Double) =
            if (powerMax < 0)
                gamepadPower.coerceIn(powerMax..1.0)
            else if (powerMax > 0)
                gamepadPower.coerceIn(-1.0..powerMax)
            else
                gamepadPower

    private fun automaticDeposit() {
        if (isConeInCollector()) {
            hardware.collector.power = 0.05
            liftTarget = Depositor.LiftCounts.HighJunction.counts.toDouble()

            if (hardware.rightLift.currentPosition >= liftTarget - 300) {
                fourBarMode = fourBarModes.FOURBAR_PID
                fourBarTarget = Depositor.FourBarDegrees.Deposit.degrees

                if (fourBar.current4BarDegrees() >= Depositor.FourBarDegrees.Deposit.degrees - 5) {
                    hardware.collector.power = -1.0
                }
            } else {
                fourBarMode = fourBarModes.FOURBAR_PID
                fourBarTarget = Depositor.FourBarDegrees.PreDeposit.degrees
            }
        } else {
            hardware.collector.power = 0.0
            // once cone drops go back to home
            fourBarMode = fourBarModes.FOURBAR_PID
            fourBarTarget = Depositor.FourBarDegrees.Vertical.degrees

            if (fourBar.current4BarDegrees() <= Depositor.FourBarDegrees.Vertical.degrees + 5) {
                liftTarget = Depositor.LiftCounts.Bottom.counts.toDouble()
            }
        }
    }

    private val timeAfterCollectToKeepCollectingSeconds = 3.0
    var collectionTimeMilis:Long? = null
    fun automatedCollection(multiCone: Boolean) {
        val preCollectLiftTarget = if (multiCone) Depositor.LiftCounts.StackPreCollection else Depositor.LiftCounts.SinglePreCollection


        if (!isConeInCollector()) {
            telemetry.addLine("cone is not in collector")
            collectionTimeMilis = null
            hardware.funnelLifter.position = collector.funnelDown

            moveDepositer(fourBarPosition = Depositor.FourBarDegrees.Collecting, liftPosition = preCollectLiftTarget)

            if (isConeInFunnel()) {
                telemetry.addLine("Cone is In Funnel")
                hardware.collector.power = 1.0
                moveDepositer(fourBarPosition = Depositor.FourBarDegrees.Collecting, liftPosition = Depositor.LiftCounts.Collection)
            } else {
                hardware.collector.power = 0.0
                moveDepositer(fourBarPosition = Depositor.FourBarDegrees.Collecting, liftPosition = preCollectLiftTarget)
            }
        } else {
            if (collectionTimeMilis == null) {
                collectionTimeMilis = System.currentTimeMillis()
            }

            val coneIsStayingInCollector = (System.currentTimeMillis() - collectionTimeMilis!!) >= timeAfterCollectToKeepCollectingSeconds
            telemetry.addLine("coneIsStayingInCollector: $coneIsStayingInCollector")

            if (coneIsStayingInCollector) {
                fourBarMode = fourBarModes.FOURBAR_PID
                fourBarTarget = Depositor.FourBarDegrees.Vertical.degrees

                if (fourBar.is4BarAtPosition(Depositor.FourBarDegrees.Vertical.degrees)) {
                    hardware.funnelLifter.position = collector.funnelUp
                    liftTarget = if (multiCone) Depositor.LiftCounts.LowJunction.counts.toDouble() else Depositor.LiftCounts.Bottom.counts.toDouble()
                    hardware.collector.power = 1.0
                }

                if ((System.currentTimeMillis() - collectionTimeMilis!!) >= timeAfterCollectToKeepCollectingSeconds) {
                    hardware.collector.power = 0.0
                } else {
                    hardware.collector.power = 0.0
                }
            }
        }
    }

    fun moveDepositer(fourBarPosition: Depositor.FourBarDegrees, liftPosition: Depositor.LiftCounts ) {
        liftTarget = liftPosition.counts.toDouble()
        fourBarMode = fourBarModes.FOURBAR_PID
        fourBarTarget = fourBarPosition.degrees
    }

    fun isConeInCollector(): Boolean {
        val minCollectedDistance = 56
        val collectedOpticalThreshold = 250 //doesnt work when cone is at an angle
        val collectedRedThreshold = 100

        val collectorDistance = hardware.collectorSensor.getDistance(DistanceUnit.MM)
        val collectorRawOptical = hardware.collectorSensor.rawOptical()
        val collectorRed = hardware.collectorSensor.red()

        return collectorDistance < minCollectedDistance// && collectorRawOptical > collectedOpticalThreshold// || collectorRed > collectedRedThreshold)
    }

    fun isConeInFunnel(): Boolean {
        val collectableDistance = 75
        val funnelBlueThreshold = 60
        val funnelRedThreshold = 60

        val red = hardware.funnelSensor.red()
        val blue = hardware.funnelSensor.blue()
        val distance = (hardware.funnelSensor as DistanceSensor).getDistance(DistanceUnit.MM)
//        telemetry.addLine("red: $red")
//        telemetry.addLine("blue: $blue")
//        telemetry.addLine("funnel distance: $distance")
        return distance < collectableDistance && (blue > funnelBlueThreshold || red > funnelRedThreshold)
    }

    fun powerLift(power: Double) {
        hardware.leftLift.power = power
        hardware.rightLift.power = power // Direction reversed in hardware map
    }
}

//@TeleOp(name= "4bar Calibrator", group= "A")
class fourBarCalibrator: LinearOpMode() {

    val hardware = PaddieMatrickHardware()
    val fourBar = FourBar(telemetry)

    override fun runOpMode() {

        hardware.init(hardwareMap)
        fourBar.init(leftServo = hardware.left4Bar, rightServo = hardware.right4Bar, encoder = hardware.encoder4Bar)


        while (!isStarted && opModeInInit()) {
            telemetry.addLine("fourBar position: ${fourBar.current4BarDegrees()}")
        }

        PaddieMatrickTeleOp.centerPosition = fourBar.current4BarDegrees()
    }

}