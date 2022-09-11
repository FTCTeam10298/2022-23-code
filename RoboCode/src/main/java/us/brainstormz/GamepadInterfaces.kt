package us.brainstormz

import com.qualcomm.robotcore.hardware.Gamepad

interface Listener {
    fun listen(value: Boolean)
}

class ButtonListener: Listener {
    private var prevValue = false
    private var onPressTime = 0L
    private val holdTime = 1_00

    var onPress = {}
    var onHold = {}
    var onRelease = {}

    override fun listen(value: Boolean) {
        if (!prevValue && value) {
            onPressTime = System.currentTimeMillis()
            onPress
        }

        if (prevValue && value && (onPressTime > holdTime)) {
            onHold
        }

        if (prevValue && !value) {
            onRelease
        }

        prevValue = value
    }
}


interface Button {
    val isDown: Boolean
}

class GamepadConstruct {
    enum class Dpad {
        Up, Down, Right, Left
    }
    enum class LetterButtons {
        A, B, X, Y
    }
    enum class StickButtons {
        LeftStick, RightStick
    }
    enum class Bumpers {
        LeftBumpers, RightBumpers
    }

    val allButtons = Dpad.values().map{it as Button} +
            LetterButtons.values().map{it as Button} +
            StickButtons.values().map{it as Button} +
            Bumpers.values().map{it as Button}
}

//class GamepadListener(private val gamepad: Gamepad): Listener {
//    val gamepadConstruct = GamepadConstruct()
//    var buttonValues = arrayOf<Button>()
//
//    override fun listen(value: Boolean) {
//        buttonValues = gamepadConstruct.allButtons.map {
//            data class Button(value: Boolean): Button
//            Button(true)
//        }
//    }
//
//
//}