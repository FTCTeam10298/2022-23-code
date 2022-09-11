package us.brainstormz.lankyKong

import android.os.Build
import androidx.annotation.RequiresApi
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import us.brainstormz.buttonEx.ButtonExpanded
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.telemetryWizard.GlobalConsole

@TeleOp(name= "Lanky Kong Teleop", group= "A")
class LankyKongTeleop: OpMode() {

    val console = GlobalConsole.newConsole(telemetry)

    val hardware = LankyKongHardware() /** Change Depending on robot */
    val movement = MecanumDriveTrain(hardware)

    var isStickButtonDown = false
    var driveReversed = if (AutoTeleopTransitionLK.alliance == AutoTeleopTransitionLK.Alliance.Red) 1 else -1

//    var aDown = false
    var speedMode = false
    val a1Ex = ButtonExpanded()
    var blockRejectCooldown = 1000
    var timeWhenBlockIn = System.currentTimeMillis()
    var eject = false
    var blockIsNew = false
    var timeSinceBlockIn = 0L


    lateinit var depo: DepositorLK

    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
        depo = DepositorLK(hardware)
    }

    override fun loop() {
        /** TELE-OP PHASE */
        hardware.clearHubCache()
        a1Ex.update(gamepad1.a)
        speedMode = a1Ex.isToggled()

//        DRONE DRIVE
        val adjustPower = 0.2
        val yAdjust = when {
            gamepad2.right_bumper -> adjustPower
            gamepad2.left_bumper -> -adjustPower
            else -> 0.0
        }

        fun isPressed(v:Float):Boolean = v > 0
        val wallRiding = if (isPressed(gamepad1.right_trigger)) 0.5 else 0.0

        if (gamepad1.left_stick_button || gamepad1.right_stick_button && !isStickButtonDown) {
            isStickButtonDown = true
            driveReversed = -driveReversed
        } else if (!gamepad1.left_stick_button && !gamepad1.right_stick_button) {
            isStickButtonDown = false
        }

        val yInput = -gamepad1.left_stick_y.toDouble()
        val xInput = gamepad1.left_stick_x.toDouble()
        val rInput = -gamepad1.right_stick_x.toDouble()

        val y = (yInput + yAdjust) * driveReversed
        val x = (xInput * driveReversed) + wallRiding
        val r = rInput * .9

        movement.driveSetPower((y + x - r),
                               (y - x + r),
                               (y - x - r),
                               (y + x + r))


//        COLLECTOR
        val forwardPower = 1.0
        val reversePower = 0.9

        val liftIsDownEnough = hardware.liftMotor.currentPosition < depo.preOutLiftPos
        when {
            gamepad1.right_bumper && liftIsDownEnough -> {
                if (driveReversed > 0) {
                    hardware.collector.power = forwardPower
                    hardware.collector2.power = -reversePower
                } else {
                    hardware.collector.power = -reversePower
                    hardware.collector2.power = forwardPower
                }
            }
            gamepad1.left_bumper && liftIsDownEnough -> {
                if (driveReversed > 0) {
                    hardware.collector.power = -reversePower
                    hardware.collector2.power = forwardPower
                } else {
                    hardware.collector.power = forwardPower
                    hardware.collector2.power = -reversePower
                }
            }
            gamepad1.b -> {
                hardware.collector.power = -reversePower
                hardware.collector2.power = -reversePower
            }
            speedMode && eject && liftIsDownEnough -> {
                hardware.collector.power = -reversePower
                hardware.collector2.power = -reversePower
            }
            else -> {
                hardware.collector.power = 0.0
                hardware.collector2.power = 0.0
            }
        }


        val blockInDropper = hardware.dropperColor.alpha() > hardware.colorThreshold

//        when {
//            gamepad1.x && !aDown -> {speedMode = !speedMode
//                                     aDown = true}
//            !gamepad1.x -> aDown = false
//        }

        if (blockInDropper) {
            if (blockIsNew)
                timeWhenBlockIn = System.currentTimeMillis()

            if (eject)
                timeSinceBlockIn = System.currentTimeMillis() - timeWhenBlockIn

            eject = timeSinceBlockIn < blockRejectCooldown

            blockIsNew = false
        } else {
            eject = false
            blockIsNew = true
            timeSinceBlockIn = 0L
        }



//        DEPOSITOR
        val goHome = gamepad2.right_stick_y > 0.3
        depo.moveWithJoystick(-gamepad2.left_stick_y.toDouble(), gamepad2.right_stick_x.toDouble(), goHome)

        when {
            gamepad2.a -> hardware.dropperServo.position = 1.0
            gamepad2.right_trigger != 0f -> hardware.dropperServo.position = DepositorLK.DropperPos.Open.posValue
            else -> hardware.dropperServo.position = DepositorLK.DropperPos.Closed.posValue
        }


//        Ducc
        val duccSide = when (AutoTeleopTransitionLK.alliance) {
            AutoTeleopTransitionLK.Alliance.Red -> -1.0
            AutoTeleopTransitionLK.Alliance.Blue -> 1.0
        }
        when {
            gamepad2.dpad_left -> {
                hardware.duccSpinner1.power = -duccSide
            }
            gamepad2.dpad_right -> {
                hardware.duccSpinner1.power = duccSide
            }
            else -> {
                hardware.duccSpinner1.power = 0.0
            }
        }

        console.display(1, "Drive Encoders: \n ${hardware.lFDrive.currentPosition} \n ${hardware.rFDrive.currentPosition} \n ${hardware.lBDrive.currentPosition} \n ${hardware.rBDrive.currentPosition}")
        console.display(2, "Front Range: ${hardware.frontDistance.getDistance(DistanceUnit.INCH)} \nBack Range: ${hardware.backDistance.getDistance(DistanceUnit.INCH)}")
        console.display(3, "X Motor currPos: ${hardware.horiMotor.currentPosition} \nY Motor currPos: ${hardware.liftMotor.currentPosition}")
        console.display(6, "Color Sensor: \n Alpha: ${hardware.dropperColor.alpha()}")
        console.display(7, "Current: ${hardware.collector.getCurrent(CurrentUnit.AMPS)}")
        console.display(8, "speedMode: $speedMode")
    }
}