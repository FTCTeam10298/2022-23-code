package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx

@TeleOp
class MotorTest: OpMode() {
    lateinit var leftLift: DcMotor
    override fun init() {

        leftLift = hardwareMap["leftLift"] as DcMotor
    }

    override fun start() {
        leftLift.power = 1.0
    }

    override fun loop() {
    }

    override fun stop() {
        leftLift.power = 0.0
    }
}