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

        hardware.servo1.position = when(gamepad1.a || gamepad2.a) {
            true -> 1.0
            false -> -1.0
        }

        hardware.servo2.position = when(gamepad1.b || gamepad2.b) {
            true -> 1.0
            false -> -1.0
        }

        hardware.servo3.position = when(gamepad1.x || gamepad2.x) {
            true -> 1.0
            false -> -1.0
        }
    }
}

class ClassHardware: HardwareClass {
    override lateinit var hwMap: HardwareMap

    lateinit var leftMotor: DcMotor
    lateinit var rightMotor: DcMotor

    lateinit var otherMotor: DcMotor

    lateinit var servo1: Servo
    lateinit var servo2: Servo
    lateinit var servo3: Servo

    override fun init(ahwMap: HardwareMap) {
        hwMap = ahwMap

        leftMotor = hwMap["leftMotor"] as DcMotor
        rightMotor = hwMap["rightMotor"] as DcMotor

        otherMotor = hwMap["otherMotor"] as DcMotor

        servo1 = hwMap["servo1"] as Servo
        servo2 = hwMap["servo2"] as Servo
        servo3 = hwMap["servo3"] as Servo
    }
}
