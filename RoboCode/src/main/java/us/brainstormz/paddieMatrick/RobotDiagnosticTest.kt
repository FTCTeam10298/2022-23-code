package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.OpMode

class RobotDiagnosticTest: OpMode() {
    val hardware = PaddieMatrickHardware()
    override fun init() {
        hardware.init(hardwareMap)
        //print inital values
    }

    override fun loop() {
        //move stuff around and make sure the readings match the expectation
    }
}