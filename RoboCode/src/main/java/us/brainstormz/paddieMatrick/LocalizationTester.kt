package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.localizer.OdometryFacts
import us.brainstormz.telemetryWizard.TelemetryConsole
import java.math.BigDecimal

@Autonomous(name= "LocalizationTester", group= "!")
class LocalizationTester: OpMode() {
    val hardware = PaddieMatrickHardware()/** Change Depending on robot */
    val movement = EncoderDriveMovement(hardware, TelemetryConsole(telemetry))

    val console = TelemetryConsole(telemetry)
    var rOdom = 0
    var lOdom = 0
    var cOdom = 0


     override fun init() {
        hardware.init(hardwareMap)
     }
//
     override fun loop() {
        rOdom = hardware.rightOdomEncoder.currentPosition
        lOdom = hardware.leftOdomEncoder.currentPosition
        cOdom = hardware.centerOdomEncoder.currentPosition
        val delta = Math.abs(rOdom) - Math.abs(lOdom)
        telemetry.addData("lr-delta", delta)
        telemetry.addData("lr-delta rotations",
            BigDecimal(delta).setScale(5) / BigDecimal(OdometryFacts.ticksPerRotation).setScale(5))
        telemetry.addData("rOdom", rOdom)
        telemetry.addData("lOdom", lOdom)
        telemetry.addData("cOdom", cOdom)
        telemetry.update()
        println("rOdom: $rOdom, lOdom: $lOdom, cOdom: $cOdom")
    }



}