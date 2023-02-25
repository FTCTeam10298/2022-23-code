package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.hardware.ColorSensor

class Funnel {
    private lateinit var lineSensor: ColorSensor
    fun init(lineSensor: ColorSensor) {
        this.lineSensor = lineSensor
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

}