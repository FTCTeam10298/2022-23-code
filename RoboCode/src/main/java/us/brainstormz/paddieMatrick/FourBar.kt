package us.brainstormz.paddieMatrick

import androidx.annotation.FloatRange
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.CRServo
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.localizer.PhoHardware
import us.brainstormz.pid.PID
import us.brainstormz.utils.MathHelps
import us.brainstormz.utils.MathHelps.smallestHeadingChange
import kotlin.math.abs

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
        var degreesWhenVertical: Double = -110.0
    }

    private val pid = PID(kp= 0.003)//, ki= 0.00000001)
    private val accuracyDegrees = 2.0


    val mountHeightInch = 10.5
    val barLengthInch = 9.5

    private val servoToBarRatio = 1/4
    private val max4BarTravel = 250

    fun goToPosition(targetDegrees: Double): Boolean {
        val wrappedTarget = MathHelps.wrap360(targetDegrees)
        val wrappedError = MathHelps.wrap360(wrappedTarget - current4BarDegrees())//calculateBestHeadingPath(wrappedTarget, current4BarDegrees(), 120.0, 240.0)
        telemetry.addLine("\nwrappedTarget: $wrappedTarget\nwrappedError: $wrappedError\n")

        val power = pid.calcPID(wrappedError)

        setServoPower(power)

        return is4BarAtPosition(targetDegrees)
    }

    fun setServoPower(power: Double) {
        telemetry.addLine("\nservo power: $power\n")
        leftServo.power = -power
        rightServo.power = -power
    }

    fun is4BarAtPosition(targetPos: Double): Boolean {
        val positionError = targetPos - current4BarDegrees()
        return positionError in -accuracyDegrees..accuracyDegrees
    }

    fun current4BarDegrees(): Double {
        val degrees = encoderDegrees()
//        telemetry.addLine("degrees: $degrees")
//        telemetry.addLine("degreesWhenVertical: $degreesWhenVertical")

        return MathHelps.wrap360(degrees + degreesWhenVertical + 180)
    }

    private fun calculateBestHeadingPath(currentHeading: Double, targetHeading: Double, deadzoneFront: Double, deadzoneBack: Double): Double {

        val absCurrentHeading = abs(currentHeading)
        val absHeadingToMouse = abs(targetHeading)


        // console.log("degreesToMouse: " + headingToMouse)
        // console.log("current angle : " + currentHeading)

        // # 1 & 2
        return when {
            MathHelps.getSign(currentHeading) != MathHelps.getSign(targetHeading) -> {
                if ((abs(currentHeading) + abs(targetHeading)) < (360 - absCurrentHeading - absHeadingToMouse)) {
                    // #1
                    // console.log("#1")

                    -MathHelps.getSign(currentHeading) * (absCurrentHeading + absHeadingToMouse)
                } else {
                    // #2
                    // console.log("#2")

                    MathHelps.getSign(currentHeading) * (360 - absCurrentHeading - absHeadingToMouse)
                }
            }
            absCurrentHeading != absHeadingToMouse -> {
                // console.log("#3")
                targetHeading - currentHeading
            }
            absCurrentHeading == absHeadingToMouse ->  0.0
            else -> 0.0
        }

    }

    fun encoderDegrees(): Double {
        //https://www.andymark.com/products/ma3-absolute-encoder-with-cable
        //0* = 0V
        //360* = 3.3V
//        telemetry.addLine("voltage: ${encoder.voltage}")
        val degrees = encoder.voltage * 109.091
//        telemetry.addLine("degrees: $degrees")
        return degrees
    }
}


@TeleOp(name= "Calibrate 4bar", group="!")
class CalibrateFourBar(): LinearOpMode() {

    val hardware = PaddieMatrickHardware()
    val fourBar = FourBar(telemetry)

    override fun runOpMode() {
        hardware.init(hardwareMap)
        fourBar.init(encoder=  hardware.encoder4Bar, leftServo = hardware.left4Bar, rightServo = hardware.right4Bar)
        telemetry.addLine("Rotate 4 bar so it is perfectly vertical.\nThen start the opomode to set the current 4bar position to the vertical position.")

        while (this.opModeInInit()) {

            telemetry.addLine("4 Bar position: ${fourBar.encoderDegrees()}")
            telemetry.update()
        }

        waitForStart()
        FourBar.degreesWhenVertical = fourBar.encoderDegrees()
    }

}