package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.Servo
import java.lang.Thread.sleep

class FourBar(private val servo1: Servo, private val servo2: Servo, private val encoder: AnalogInput) {

    private val positionAccuracy = 2.0

    fun goToPosition(targetDegrees: Double) {
        
    }

    fun goToPositionBlocking(targetDegrees: Double) {
        goToPosition(targetDegrees)
        while (!is4BarAtPosition(targetDegrees)) {
            sleep(200)
        }
    }

    fun is4BarAtPosition(targetPos: Double): Boolean {
        return targetPos in -positionAccuracy..positionAccuracy
    }

    fun currentEncoderDegrees(): Double {
        //https://www.andymark.com/products/ma3-absolute-encoder-with-cable
        //0* = 0V
        //180* = 2.5V
        //360* = 5V
        val degrees = encoder.voltage / 72
        return degrees
    }

}