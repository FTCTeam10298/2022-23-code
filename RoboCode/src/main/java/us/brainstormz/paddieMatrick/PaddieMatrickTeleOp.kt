package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import kotlin.math.abs

@TeleOp
class PaddieMatrickTeleOp: OpMode() {

    val hardware = PaddieMatrickHardware()
    val movement = MecanumDriveTrain(hardware)

    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
    }

    override fun loop() {
        /** TELE-OP PHASE */

//        // DRONE DRIVE
//        val yInput = gamepad1.left_stick_y.toDouble()
//        val xInput = gamepad1.left_stick_x.toDouble()
//        val rInput = gamepad1.right_stick_x.toDouble()
//
//        val y = -yInput
//        val x = xInput
//        val r = -rInput * abs(rInput)
//        movement.driveSetPower((y + x - r),
//                               (y - x + r),
//                               (y - x - r),
//                               (y + x + r))

        val fourBarPower = gamepad1.right_stick_y.toDouble()
        hardware.left4Bar.power = fourBarPower
        hardware.right4Bar.power = fourBarPower

        val liftPower = gamepad1.left_stick_y.toDouble()
        hardware.leftLift.power = liftPower
        hardware.rightLift.power = liftPower // Direction reversed in hardware map
    }
}