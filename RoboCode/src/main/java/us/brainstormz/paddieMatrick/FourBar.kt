package us.brainstormz.paddieMatrick

import androidx.annotation.FloatRange
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.CRServo
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.localizer.PhoHardware
import us.brainstormz.pid.PID
import us.brainstormz.utils.MathHelps
import us.brainstormz.utils.MathHelps.smallestHeadingChange
import java.lang.Math.pow
import kotlin.math.abs
import kotlin.math.pow

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
        var degreesWhenVertical: Double = 277.0
    }

    var pid = PID(kp= 0.018, kd= 0.0005)
    var accuracyDegrees = 5.0
    var allowedZone = 50.0..300.0

    val mountHeightInch = 10.5
    val barLengthInch = 9.5

    private val servoToBarRatio = 1/4
    private val max4BarTravel = 250

    fun goToPosition(targetDegrees: Double): Boolean {
        val wrappedTarget = MathHelps.wrap360(targetDegrees)
        val wrappedError = (wrappedTarget - current4BarDegrees())//calculateBestHeadingPath(wrappedTarget, current4BarDegrees(), 120.0, 240.0)
        telemetry.addLine("\nwrappedTarget: $wrappedTarget\nwrappedError: $wrappedError\n")

        val power = pid.calcPID(wrappedError)

        setServoPower(power)

        return is4BarAtPosition(targetDegrees)
    }

    fun setServoPower(power: Double) {
//        telemetry.addLine("\nservo power: $power\n")
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

        return MathHelps.wrap360(degrees - degreesWhenVertical + 180)
        //360-277+180
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
        val degrees = encoder.voltage * 72
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


@TeleOp(name= "Four Bar Test And Tune", group="!")
class TuneFourBarPID(): OpMode() {

    val hardware = PaddieMatrickHardware()
    val fourBar = FourBar(telemetry)

    override fun init() {
        hardware.init(hardwareMap)
        fourBar.init(encoder = hardware.encoder4Bar, leftServo = hardware.left4Bar, rightServo = hardware.right4Bar)
    }

    var kp = 0.011
    var ki = 0.00000000//1// * 10.0.pow(-4.0)
    var kd = 0.0//02
    enum class Pid {
        KP,
        KI,
        KD
    }

    var target = 180.0
    override fun loop() {
        telemetry.addLine("pid values: $kp, $ki, $kd")

        fourBar.pid = PID(kp, ki, kd)

        when {
            gamepad1.x -> {
                target = 270.0
                telemetry.addLine("target: 270")
            }
            gamepad1.y -> {
                target = 90.0
                telemetry.addLine("target: 90")
            }
            gamepad1.a && !gamepad1.start -> {
                target = 180.0
                telemetry.addLine("target: 180.0")
            }
        }

        if (gamepad1.b) {
            fourBar.setServoPower(0.0)
            telemetry.addLine("power: 0.0")
        } else {
            fourBar.goToPosition(target)
        }

        var incrementAmount = 0.001
        var changingVariable = Pid.KP
        when {
                gamepad1.right_bumper -> {
                changingVariable = Pid.KP
            }
                gamepad1.left_bumper -> {
                changingVariable = Pid.KI
            }
                gamepad1.left_trigger > 0 -> {
                changingVariable = Pid.KD
            }
        }

        when {
            gamepad1.dpad_up -> {
                when (changingVariable) {
                    Pid.KP -> kp += incrementAmount
                    Pid.KI -> ki += incrementAmount
                    Pid.KD -> kd += incrementAmount
                }
            }
            gamepad1.dpad_down -> {
                when (changingVariable) {
                    Pid.KP -> kp -= incrementAmount
                    Pid.KI -> ki -= incrementAmount
                    Pid.KD -> kd -= incrementAmount
                }
            }
        }
    }

}