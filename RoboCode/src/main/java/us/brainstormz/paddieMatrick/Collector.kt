package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.hardware.CRServo

class Collector(private val frontServo: CRServo, private val backServo: CRServo) {

    val heightInch = 4

    val servos = listOf(frontServo, backServo)

    enum class Powers(val power: Double) {
        Intaking(1.0),
        Scoring(-1.0)
    }

    fun manipulate(action: Powers) {
        servos.forEach{
            it.power = action.power
        }
    }
}