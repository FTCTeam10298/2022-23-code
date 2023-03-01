package us.brainstormz.paddieMatrick

import com.qualcomm.hardware.rev.RevColorSensorV3
import com.qualcomm.robotcore.hardware.ColorSensor
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit

class Funnel {
    private lateinit var lineSensor: ColorSensor
    private lateinit var funnelSensor: RevColorSensorV3
    fun init(lineSensor: ColorSensor, funnelSensor: RevColorSensorV3) {
        this.lineSensor = lineSensor
        this.funnelSensor = funnelSensor
    }

    enum class Color {
        Blue,
        Red,
        Neither
    }

    data class ColorData(val colorLowThreshold: Int, val colorHighThreshold: Int) {
        fun thisIsTheColor(lowColor: Int, highColor: Int): Boolean =
                (lowColor <= colorLowThreshold) && (highColor >= colorHighThreshold)

        override fun toString(): String = "low threshold: $colorLowThreshold, high threshold: $colorHighThreshold"
    }

    val dataWhenRed = ColorData(colorHighThreshold= 200, colorLowThreshold= 100)
    val dataWhenBlue = ColorData(colorHighThreshold= 130, colorLowThreshold= 70)

    fun getColor(): Color = when {
            dataWhenBlue.thisIsTheColor(lowColor = lineSensor.red(), highColor = lineSensor.blue()) -> Color.Blue
            dataWhenRed.thisIsTheColor(lowColor = lineSensor.blue(), highColor = lineSensor.red()) -> Color.Red
            else -> Color.Neither
        }

    fun coneIsInFrontOfFunnel(): Boolean {
        val threshold = 280 * 100000
        return lineSensor.argb() >= threshold
    }

    fun isConeInFunnel(collectableDistance: Double = 30.0): Boolean {
//        val collectableDistance = 30
        val opticalThreshold = 2046
        val funnelBlueThreshold = 60
        val funnelRedThreshold = 60

        val red = funnelSensor.red()
        val blue = funnelSensor.blue()
        val optical = funnelSensor.rawOptical()
        val distance = funnelSensor.getDistance(DistanceUnit.MM)
//        telemetry.addLine("red: $red")
//        telemetry.addLine("blue: $blue")
//        telemetry.addLine("funnel distance: $distance")
        return distance < collectableDistance || optical >= opticalThreshold // && (blue > funnelBlueThreshold || red > funnelRedThreshold)
    }
}