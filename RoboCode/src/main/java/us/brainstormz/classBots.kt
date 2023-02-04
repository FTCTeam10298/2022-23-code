package us.brainstormz

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.Servo
import us.brainstormz.hardwareClasses.HardwareClass

@TeleOp
class classBots: OpMode() {

    val hardware = ClassHardware()

    override fun init() {
        hardware.init(hardwareMap)
    }

    override fun loop() {
        hardware.rightMotor.power = gamepad1.right_stick_y.toDouble()
        hardware.leftMotor.power = gamepad1.left_stick_y.toDouble()

        hardware.otherMotor.power = gamepad2.left_stick_y.toDouble()

        if (gamepad1.a) {
            hardware.servo1.position = 1.0
        } else if (gamepad1.b){
            hardware.servo1.position = -1.0
        } else {
            hardware.servo1.position = 0.0
        }
    }
}

class ClassHardware: HardwareClass {
    override lateinit var hwMap: HardwareMap

    lateinit var leftMotor: DcMotor
    lateinit var rightMotor: DcMotor

    lateinit var otherMotor: DcMotor

    lateinit var servo1: Servo
    val servoPos1 = 1.0
    val servoPos2 = 0.0

    override fun init(ahwMap: HardwareMap) {
        hwMap = ahwMap

        leftMotor = hwMap["leftMotor"] as DcMotor
        rightMotor = hwMap["rightMotor"] as DcMotor

        otherMotor = hwMap["otherMotor"] as DcMotor

        servo1 = hwMap["servo1"] as Servo
        servo1.position = servoPos1
    }
}
