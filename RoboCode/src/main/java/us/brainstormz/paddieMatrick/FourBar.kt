package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.CRServo
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.localizer.PhoHardware
import us.brainstormz.pid.PID
import us.brainstormz.utils.MathHelps

class FourBar(private val telemetry: Telemetry) {
    private lateinit var leftServo: CRServo
    private lateinit var rightServo: CRServo
    private lateinit var encoder: AnalogInput

    fun init(leftServo: CRServo, rightServo: CRServo, encoder: AnalogInput) {
        this.leftServo = leftServo
        this.rightServo = rightServo
        this.encoder = encoder
    }

    companion object {
        var degreesWhenVertical: Double = 180.0
    }

    private val pid = PID(kp= 0.02)
    private val accuracyDegrees = 1.0

    val barLengthInch = 9.5

    private val servoToBarRatio = 1/4
    private val max4BarTravel = 250

    fun goToPosition(targetDegrees: Double): Boolean {
        val wrappedTarget = MathHelps.wrap360(targetDegrees)
        val wrappedError = MathHelps.wrap360(wrappedTarget - 1.0)//current4BarDegrees())

        val power = pid.calcPID(wrappedError)

        setServoPower(power)

        return is4BarAtPosition(targetDegrees)
    }

    fun setServoPower(power: Double) {
        leftServo.power = power
        rightServo.power = power
    }

    fun is4BarAtPosition(targetPos: Double): Boolean {
        val positionError = targetPos - current4BarDegrees()
        return positionError in -accuracyDegrees..accuracyDegrees
    }

    fun current4BarDegrees(): Double {
        //https://www.andymark.com/products/ma3-absolute-encoder-with-cable
        //0* = 0V
        //360* = 3.3V
//        telemetry.addLine("voltage: ${encoder.voltage}")
        val degrees = encoder.voltage * 109.091
//        telemetry.addLine("degrees: $degrees")
        return degrees
    }
}

fun main() {
    val phoTelem = PhoHardware.PhoTelemetry()
    val fourBar = FourBar(phoTelem)

    fourBar.goToPosition(40.0)
}

@TeleOp
class CalibrateFourBar(): LinearOpMode() {

    val hardware = PaddieMatrickHardware()
    val fourBar = FourBar(telemetry)

    override fun runOpMode() {
        hardware.init(hardwareMap)
        fourBar.init(encoder=  hardware.encoder4Bar, leftServo = hardware.left4Bar, rightServo = hardware.right4Bar)
        telemetry.addLine("Rotate 4 bar so it is perfectly vertical.\nThen start the opomode to set the current 4bar position to the vertical position.")

        while (this.opModeInInit()) {

            telemetry.addLine("4 Bar position: ${fourBar.current4BarDegrees()}")
        }
        waitForStart()
        FourBar.degreesWhenVertical = fourBar.current4BarDegrees()
    }

}