package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.Servo
import java.lang.Thread.sleep

class FourBar(private val leftServo: Servo, private val rightServo: Servo, private val encoder: AnalogInput) {

    val barLengthInch = 6.0

    private val maxServoTravel = 300
    private val servoToBarRatio = 40/48
    private val max4BarTravel = maxServoTravel * servoToBarRatio //250 degrees

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
        leftServo.position = position
        rightServo.position = position
    }

    fun is4BarAtPosition(targetPos: Double): Boolean {
        val accuracyDegrees = 1.0
        return targetPos in -accuracyDegrees..accuracyDegrees
    }

    private fun degreesToServoPosition(degrees: Double): Double {
        return degrees / 70
    }

    private val encoderToServoRatio = 30/40
    fun current4BarDegrees(): Double = convertEncoderToDegrees() * encoderToServoRatio * servoToBarRatio //doesn't account for wrap

    fun convertEncoderToDegrees(): Double {
        //https://www.andymark.com/products/ma3-absolute-encoder-with-cable
        //0* = 0V
        //180* = 2.5V
        //360* = 5V
        val degrees = encoder.voltage / 72
        return degrees
    }
}