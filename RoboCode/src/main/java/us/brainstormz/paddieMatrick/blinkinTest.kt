package us.brainstormz.paddieMatrick

import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.hardware.rev.RevBlinkinLedDriver.BlinkinPattern
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp

@TeleOp
class blinkinTest: LinearOpMode() {
    lateinit var blinkinLedDriver: RevBlinkinLedDriver

    override fun runOpMode() {
        blinkinLedDriver = hardwareMap.get("blinkin") as RevBlinkinLedDriver
        blinkinLedDriver.setPattern(BlinkinPattern.FIRE_LARGE)

        waitForStart()

        while(opModeIsActive()) {
            for (pattern in BlinkinPattern.values()) {
                telemetry.addLine("pattern: $pattern")
                telemetry.update()
                blinkinLedDriver.setPattern(pattern)
                while (!gamepad1.a) { }
            }
        }
    }
}
