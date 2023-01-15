package us.brainstormz.paddieMatrick

import com.qualcomm.robotcore.hardware.CRServo

class Collector() {
    private lateinit var servo: CRServo

    val heightInch = 4.0

    val funnelDown = 1.0
    val funnelUp = 0.2

    fun init(frontServo: CRServo) {
        this.servo = frontServo
    }

    enum class Powers(val power: Double) {
        Intaking(1.0),
        Scoring(-1.0)
    }

    fun manipulate(action: Powers) {
        servo.power = action.power
    }
}