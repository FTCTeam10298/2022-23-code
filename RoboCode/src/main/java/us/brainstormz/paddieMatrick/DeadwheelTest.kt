package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.motion.RRLocalizer

@TeleOp
class DeadwheelTest: OpMode() {
    private val hardware = PaddieMatrickHardware()

    private lateinit var movement: MecanumMovement

    override fun init() {
        hardware.init(hardwareMap)
        val localizer = RRLocalizer(hardware)
        movement = MecanumMovement(localizer, hardware, telemetry)

    }

    var targetPosition = PositionAndRotation(y= 60.0)
    override fun loop() {
        val isChassisTaskCompleted = movement.moveTowardTarget(targetPosition)
        if (isChassisTaskCompleted) {
//            targetPosition = PositionAndRotation(y= 0.0)
        }
    }
}