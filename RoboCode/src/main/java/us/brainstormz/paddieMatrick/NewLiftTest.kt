package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.*
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.pid.PID

@TeleOp
class NewLiftTest: OpMode() {

    lateinit var leftLift: DcMotorEx
    lateinit var rightLift: DcMotorEx
    lateinit var extraLift: DcMotorEx

    val liftPID = PID(kp= 0.003, ki= 0.0)
    var liftTarget = 0.0
    val liftSpeed = 1800.0

    override fun init() {
        leftLift = hardwareMap["leftLift"] as DcMotorEx
        rightLift = hardwareMap["rightLift"] as DcMotorEx
        extraLift = hardwareMap["cEncoder"] as DcMotorEx

        rightLift.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER

        leftLift.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        rightLift.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        extraLift.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        leftLift.direction = DcMotorSimple.Direction.FORWARD
        rightLift.direction = DcMotorSimple.Direction.REVERSE
        extraLift.direction = DcMotorSimple.Direction.FORWARD
    }

    var prevTime = System.currentTimeMillis()
    override fun loop() {
        /** TELE-OP PHASE */
        val dt = System.currentTimeMillis() - prevTime
        prevTime = System.currentTimeMillis()
        telemetry.addLine("dt: $dt")

        // Lift
        when {
            gamepad2.a -> {
                liftTarget += (-0.5 * liftSpeed / dt)
            }
        }

        liftTarget += (-gamepad2.left_stick_y.toDouble() * liftSpeed / dt)

        val liftPower = liftPID.calcPID(liftTarget, rightLift.currentPosition.toDouble())

        telemetry.addLine("lift position: ${rightLift.currentPosition}")
        telemetry.addLine("liftTarget: $liftTarget")
        telemetry.addLine("liftPower: $liftPower")
        telemetry.addLine("Lift Amps:\n    Left: ${leftLift.getCurrent(CurrentUnit.AMPS)}\n   Right: ${rightLift.getCurrent(CurrentUnit.AMPS)}")

        powerLift(liftPower)
    }

    fun powerLift(power: Double) {
        leftLift.power = power
        rightLift.power = power // Direction reversed in hardware map
        extraLift.power = power // Direction reversed in hardware map
    }
}