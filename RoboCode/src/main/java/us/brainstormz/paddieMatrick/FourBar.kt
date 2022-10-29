package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.CRServo
import org.firstinspires.ftc.robotcore.external.Telemetry
import java.lang.Thread.sleep

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

    val barLengthInch = 10.0

//    private val maxServoTravel = 300
    private val centerPosition = 180
    private val servoToBarRatio = 48/48
    private val max4BarTravel = 250//maxServoTravel * servoToBarRatio //250 degrees

    private val barRangeDegrees = -(max4BarTravel / 2)..(max4BarTravel / 2) //puts 0 as vertical

    fun goToPositionBlocking(targetDegrees: Double) {
        goToPosition(targetDegrees)
        while (!is4BarAtPosition(targetDegrees)) {
            sleep(200)
        }
    }

    fun goToPosition(targetDegrees: Double): Boolean {
        val degreesFromZero = targetDegrees + max4BarTravel / 2
        val servoPosition = degreesToServoPosition(degreesFromZero)

        setServoPosition(servoPosition)

        return is4BarAtPosition(targetDegrees)
    }

    fun setServoPosition(position: Double) {
//        leftServo.position = position
//        rightServo.position = position
    }

    fun is4BarAtPosition(targetPos: Double): Boolean {
        val accuracyDegrees = 1.0
        return targetPos in -accuracyDegrees..accuracyDegrees
    }

    private fun degreesToServoPosition(degrees: Double): Double {
        return degrees / 70
    }

//    private val encoderToServoRatio = 30/40
//    fun current4BarDegrees(): Double = convertEncoderToDegrees() * encoderToServoRatio * servoToBarRatio //doesn't account for wrap

    fun current4BarDegrees(): Double {
        //https://www.andymark.com/products/ma3-absolute-encoder-with-cable
        //0* = 0V
        //360* = 3.3V
//        telemetry.addLine("voltage: ${encoder.voltage}")
        val degrees = encoder.voltage * 109.090909090909091
//        telemetry.addLine("degrees: $degrees")
        return degrees
    }
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