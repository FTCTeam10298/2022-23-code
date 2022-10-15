//package us.brainstormz.lankyKong
//
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
//import com.qualcomm.robotcore.hardware.DcMotor
//import com.qualcomm.robotcore.hardware.DcMotorEx
//import us.brainstormz.pid.PID
//import us.brainstormz.telemetryWizard.GlobalConsole
//import us.brainstormz.telemetryWizard.TelemetryConsole
//import us.brainstormz.utils.MathHelps
//
//class DepositorLK(private val hardware: LankyKongHardware) {
//    private val console = GlobalConsole.console
//
//    enum class Axis { X, Y }
//    data class SlideConversions(private val countsPerMotorRev: Double = 28.0,
//                                private val gearReduction: Int = 1 / 1,
//                                private val spoolCircumferenceMM: Double = 112.0) {
//        val countsPerInch: Double = 1.0 /*countsPerMotorRev * gearReduction / (spoolCircumferenceMM / 25.4)*/
//        fun countsToIn(counts: Int): Double = counts.toDouble() /** countsPerInch*/
//        fun inToCounts(In: Double) = In /*/ countsPerInch*/
//    }
//
//    data class Constraint(val constraint: (target: Double)->Boolean, val name: String)
//    data class MovementConstraints(val limits: ClosedRange<Double>, val conditions: List<Constraint>) {
//        var problem: Constraint? = null
//        private val completeConditions = conditions + Constraint({ target-> target in limits }, "limits")
//        fun withinConstraints(target: Double): Boolean {
//            return completeConditions.fold(true) { acc, it ->
//                val condition = it.constraint(target)
//                if (!condition) {
//                    problem = it
//                    false
//                } else
//                    acc
//            }
//        }
//
//        fun validOrFail(target:Double):Double{
//            return if(withinConstraints(target)) target else throw Exception("Out of constraints for ${problem?.name}: $target")
//        }
//    }
////    General
//    private val inRobot = 3.0
//
////    Dropper Variables
//    enum class DropperPos(val posValue: Double) {
//        Open(0.7),
//        Closed(0.0)
//    }
//    private val dropperConstraints = MovementConstraints(DropperPos.Open.posValue..DropperPos.Closed.posValue,
//                                                               listOf(/*Constraint({target-> target > inRobot}, ""),
//                                                                      Constraint({currentXIn > inRobot}, "")*/))
//    enum class LiftPos(val counts: Int) {
//        LowGoal(350),
//        MidGoal(1100),
//        HighGoal(2500)
//    }
//
//    val preOutLiftPos = 200
//    val outWhileMovingPos = 3000
//
////    X Variables
//    val safeXPosition = 400
//    private val xMotor = hardware.horiMotor
//    val currentXIn get() = xMotor.currentPosition
//    private val xPrecision = 100
//    private val xPIDPosition = PID(kp= 0.00095455, ki= 0.0000000015)
//    private val xPIDJoystick = PID(kp= 0.002)
//    private var xPID = xPIDPosition
//    val xFullyRetracted = 20
//    val xConstraints = MovementConstraints(5.0..6500.0, listOf(Constraint({target-> !(target < inRobot && currentXIn > inRobot)}, ""),
//                                                                            /*Constraint({}, "")*/))
//
////    Y Variables
//    private val yMotor = hardware.liftMotor
//    val currentYIn get() = yMotor.currentPosition
//    private val yPrecision = 15
////    private val yConversion = SlideConversions(countsPerMotorRev = 28.0)
//    val yConstraints = MovementConstraints(0.0..3700.0, listOf())
//    val fullyDown = 10
//
//    fun moveToPosition(yPosition: Int, xPosition: Int) {
////        throw Exception("tune the pid")
////        xPID = xPIDPosition
//        var atPosition = false
//
//        while (!atPosition && opmode.opModeIsActive()) {
//            atPosition = moveTowardPosition(yPosition, xPosition)
//        }
//    }
//
//
//    fun moveTowardPosition(yPosition: Int, xPosition: Int): Boolean {
//        return moveTowardPosition(yPosition =yPosition.toDouble(), xPosition = xPosition.toDouble())
//    }
//    fun moveTowardPosition(yPosition: Double, xPosition: Double): Boolean {
//        val yAtTarget = positionAndHold(
//            inches = yConstraints.validOrFail(yPosition),
//            motor = hardware.liftMotor,
//            tolerance = yPrecision,
//            name = "lift",
//            console = console)
//
//        val xAtTarget = if (hardware.horiMotor.isOverCurrent) {
//            println("\n\nI'm stuck because I'm over current!!!!\n")
//            true
//        }else {
//            positionAndHold(
//                inches = xConstraints.validOrFail(xPosition),
//                motor = hardware.horiMotor,
//                //            pid = xPID,
//                tolerance = xPrecision,
//                name = "extension",
//                console = console
//            )
//        }
//
//
////        console.display(8, "X at target: $xAtTarget \nY at target: $yAtTarget")
////        console.display(9, "X current: $currentXIn \nY current: $currentYIn")
////
////        console.display(15, "yIn: $yPosition, xIn $xPosition")
//
//        return yAtTarget && xAtTarget
//    }
//
//    fun moveTowardPositionJoystick(yPosition: Double, xPosition: Double): Boolean {
//        val yAtTarget = positionAndHold(
//            inches = yConstraints.validOrFail(yPosition),
//            motor = hardware.liftMotor,
//            tolerance = yPrecision,
//            name = "lift",
//            console = console)
//
//        val xAtTarget = moveToPositionWithPID(
//            targetCounts  = xConstraints.validOrFail(xPosition),
//            motor = hardware.horiMotor,
//            pid = xPID,
//            tolerance = xPrecision,
//            console = console)
////
////        console.display(8, "X at target: $xAtTarget \nY at target: $yAtTarget")
////        console.display(9, "X current: $currentXIn \nY current: $currentYIn")
////
////        console.display(15, "yIn: $yPosition, xIn $xPosition")
//
//        return yAtTarget && xAtTarget
//    }
//
//    private var lastYPos = 0
//    private var xMoving = 0.0
//    private var lastXPos = 0.0
//    fun moveWithJoystick(yStick: Double, xStick: Double, homeCondition: Boolean) {
//        xPID = xPIDJoystick
//        val isLiftHighEnough = hardware.liftMotor.currentPosition > preOutLiftPos
//
//        val yIn = when {
//            yStick > 0 -> MathHelps.scaleBetween(yStick, 0.0..1.0, currentYIn.toDouble().coerceIn(yConstraints.limits)..yConstraints.limits.endInclusive)
//            (xStick != 0.0) && !isLiftHighEnough -> preOutLiftPos.toDouble() + 30
//            homeCondition -> {
//                if (hardware.horiMotor.currentPosition <= xFullyRetracted + 200)
//                    fullyDown.toDouble()
//                else
//                    preOutLiftPos.toDouble()
//            }
//            else -> MathHelps.scaleBetween(yStick, -1.0..0.0, yConstraints.limits.start..currentYIn.toDouble().coerceIn(yConstraints.limits))
//        }
//
//        console.display(10, "isLiftHighEnough?: $isLiftHighEnough")
//        val xIn =
//            when {
//                xStick > 0 && isLiftHighEnough-> {
//                    xMoving = 0.0
//                    MathHelps.scaleBetween(xStick, 0.0..1.0, currentXIn.toDouble().coerceIn(xConstraints.limits)..xConstraints.limits.endInclusive)
//                }
//                xStick < 0 && isLiftHighEnough -> {
//                    xMoving = 0.0
//                    MathHelps.scaleBetween(xStick, -1.0..0.0, xConstraints.limits.start..currentXIn.toDouble().coerceIn(xConstraints.limits))
//                }
//                homeCondition -> {
//                    xMoving = 0.0
//                    xFullyRetracted.toDouble()
//                }
//                else -> {
//                    if (xMoving < 5) {
//                        lastXPos = currentXIn.toDouble().coerceIn(xConstraints.limits)
//                        xMoving += 1
//                    }
//                    lastXPos
//            }
//        }
//
//
////        console.display(11, "X moving: $xMoving")
//        moveTowardPositionJoystick(yIn, xIn)
//    }
//
//
//    companion object {
//        private fun positionAndHold(
//            inches: Double,
//            motor: DcMotorEx,
//            tolerance:Int,
//            name:String,
//            console:TelemetryConsole): Boolean {
//
//            val targetCounts = inches.toInt()
//
//            motor.power = 1.0
//            motor.targetPosition = targetCounts
//            motor.mode = DcMotor.RunMode.RUN_TO_POSITION
//
//            val error = motor.currentPosition - motor.targetPosition
//            console.display(12, "$name error: $error")
//            return error in (-tolerance..tolerance)
//        }
//
//        private fun moveToPositionWithPID(targetCounts: Double,
//                                          motor:DcMotorEx,
//                                          pid:PID,
//                                          tolerance:Int,
//                                          console: TelemetryConsole): Boolean {
//
//            val error = targetCounts.toInt() - motor.currentPosition
//            console.display(10, "X error: $error")
//
//            return if (error in (-tolerance..tolerance)) {
//                motor.power = 0.0
//                true
//            } else {
//                motor.power = pid.calcPID(error.toDouble())
//                false
//            }
//        }
//    }
//
//
//    fun moveToPositionRelative(yIn: Int = 0, xIn: Int = 0) {
//        moveToPosition(currentYIn + yIn,
//            currentXIn + xIn)
//    }
//
////    fun moveTowardPositionRelative(yIn: Double, xIn: Double): Boolean =
////        moveTowardPosition(currentYIn + yIn,
////            currentXIn + xIn)
//
//    fun dropper(position: DropperPos, autoResolve: Boolean = false): Boolean =
//        when {
//            dropperConstraints.withinConstraints(position.posValue) -> {
//                hardware.dropperServo.position = position.posValue
//                true
//            }
////            autoResolve -> {
////                when (dropperConstraints.problem) {
////                    Axis.Y -> {
////                        moveTowardPosition(yIn = dropperConstraints.allowedYHeights!!.minOf { it.start })
////                        if (dropperConstraints.withinConstraints()) {
////                            hardware.dropperServo.position = position.posValue
////                            true
////                        } else
////                            false
////                    }
////                    Axis.X -> {
////                        moveTowardPosition(xIn = dropperConstraints.allowedXLengths!!.minOf { it.start })
////                        if (dropperConstraints.withinConstraints()) {
////                            hardware.dropperServo.position = position.posValue
////                            true
////                        } else
////                            false
////                    }
////                    else -> false
////                }
////            }
//            else -> false
//        }
//
//    fun dropperBlocking(position: DropperPos, autoResolve: Boolean) {
//        while (opmode.opModeIsActive()) {
//            val dropped = dropper(position, autoResolve)
//            if (dropped)
//                break
//        }
//    }
//
//    /**
//     * run at the beginning of the program
//     * */
//    private lateinit var opmode: LinearOpMode
//    fun runInLinearOpmode(opmode: LinearOpMode) {
//        this.opmode = opmode
//    }
//}
