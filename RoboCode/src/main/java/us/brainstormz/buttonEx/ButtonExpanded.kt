package us.brainstormz.buttonEx

import android.annotation.SuppressLint
import java.lang.Thread.sleep
import java.time.LocalDateTime

class ButtonExpanded {
    private var prevValue = false

    private var pressedMs = 0
    private var toggled = false

    /** Call on each loop */
    fun update(newValue: Boolean) {
        if (newValue && isChanged(newValue)) {
            pressedMs = currentTimeMs()
            toggled = !toggled
        }

        prevValue = newValue
    }

    fun isChanged(buttonValue: Boolean): Boolean = buttonValue != prevValue

    fun isToggled(): Boolean {
        return toggled
    }

    fun timeHeld() = currentTimeMs() - pressedMs

    /** Doesn't check whether it's currently pressed */
    fun heldFor(msHeld: Int): Boolean =
            timeHeld() >= msHeld

    @SuppressLint("NewApi")
    private fun currentTimeMs() = LocalDateTime.now().nano / 1000000
}

//fun main() {
//    val a1 = ButtonExpanded()
//
//    var a = false
//    while (true) {
//        a = !a
//        a1.update(a)
//
//        println("Toggled?: ${a1.isToggled()}")
//
//        println("Held?: ${b2.heldFor(500)}")
//
//        sleep(500)
//    }
//}