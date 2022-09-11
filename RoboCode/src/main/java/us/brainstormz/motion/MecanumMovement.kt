package us.brainstormz.motion

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.util.Range
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.hardwareClasses.MecanumHardware
import us.brainstormz.localizer.Localizer
import us.brainstormz.localizer.OdometryLocalizer
import us.brainstormz.localizer.PhoHardware
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.pid.PID
import us.brainstormz.rataTony.RataTonyHardware
import us.brainstormz.telemetryWizard.GlobalConsole
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min

class MecanumMovement(override val localizer: Localizer, override val hardware: MecanumHardware): Movement, MecanumDriveTrain(hardware) {

    private val console = GlobalConsole.console

    override var movementPID = PID(0.17, 0.000002, 0.00)
    override var precisionInches: Double = 0.5
    override var precisionDegrees: Double = 1.0


    /**
     * Only required when using goToPosition
     */
//    override lateinit var linearOpMode: LinearOpMode

    /**
     * Blocking function
     */
    override fun goToPosition(target: PositionAndRotation, linearOpMode: LinearOpMode, powerRange: ClosedRange<Double>) {
//        localizer.startNewMovement()
//        console.display(1, "Target position: $target")

        while (linearOpMode.opModeIsActive()) {
//            console.display(2, "Current position: ${localizer.currentPositionAndRotation()}")

            localizer.recalculatePositionAndRotation()
            val targetReached = moveTowardTarget(target, powerRange)

            if (targetReached)
                break
        }
    }

    override fun moveTowardTarget(target: PositionAndRotation, powerRange: ClosedRange<Double>): Boolean {

        val posError = target - localizer.currentPositionAndRotation()

        console.display(2, "error $posError")

        // Check to see if we've reached the target
        val distanceError = hypot(posError.x, posError.y)
        if (distanceError in -precisionInches..precisionInches && posError.r in -precisionDegrees..precisionDegrees) {
            console.display(3, "Reached Target")
            setSpeedAll(0.0, 0.0, 0.0, 0.0, 0.0)
            return true
        }


        val speed = movementPID.calcPID((posError.x + posError.y + posError.r) / 3)
        println("speed $speed")
        setSpeedAll(-posError.x, posError.y, -posError.r, powerRange.start, min(speed, powerRange.endInclusive))

        return false
    }

    /** works */
    fun setSpeedAll(vX: Double, vY: Double, vA: Double, minPower: Double, maxPower: Double) {

        // Calculate theoretical values for motor powers using transformation matrix
        var fl = vY - vX + vA
        var fr = vY + vX - vA
        var bl = vY + vX + vA
        var br = vY - vX - vA

        // Find the largest magnitude of power and the average magnitude of power to scale down to
        // maxPower and up to minPower
        var max = abs(fl)
        max = max.coerceAtLeast(abs(fr))
        max = max.coerceAtLeast(abs(bl))
        max = max.coerceAtLeast(abs(br))
        val ave = (abs(fl) + abs(fr) + abs(bl) + abs(br)) / 4
        if (max > maxPower) {
            fl *= maxPower / max
            bl *= maxPower / max
            br *= maxPower / max
            fr *= maxPower / max
        } else if (ave < minPower) {
            fl *= minPower / ave
            bl *= minPower / ave
            br *= minPower / ave
            fr *= minPower / ave
        }

        // Range clip just to be safe
        fl = Range.clip(fl, -1.0, 1.0)
        fr = Range.clip(fr, -1.0, 1.0)
        bl = Range.clip(bl, -1.0, 1.0)
        br = Range.clip(br, -1.0, 1.0)

        console.display(6,"Powers: $fl, $bl, $fr, $br" )
        println("Powers: $fl, $bl, $fr, $br")

        // Set powers
        driveSetPower(fl, fr, bl, br)
    }

}


@TeleOp(name= "Odom Movement Test")
class OdomMoveTest: LinearOpMode() {
//    192.168.1.128
//    192.168.43.45
//    192.168.43.1
    val hardware = RataTonyHardware()
    val console = GlobalConsole.newConsole(telemetry)
    val localizer = OdometryLocalizer(hardware)
    val movement = MecanumMovement(localizer, hardware)
//    val robot = MecanumDriveTrain(hardware)

    var targetPos = PositionAndRotation(y= 10.0, x= 10.0, r= 90.0)

    override fun runOpMode() {
        hardware.init(hardwareMap)
        localizer.setPositionAndRotation(0.0, 0.0, 0.0)
//        distance between l & r odom wheels: 7 8/16 = 7.5
        localizer.trackwidth = 7.5
//        center of chassis: 10 6/16 = 10.375 / 2 = 5.1875
//        center odom: 5 2/16 = 5.125
//        forward offset: 5.1875 - 5.125 = 0.0625
        localizer.forwardOffset = 0.0625
        waitForStart()

//        movement.setSpeedAll(0.0, 1.0, 0.0, 0.0, 0.5)
//        sleep(500)

        movement.goToPosition(targetPos, this, 0.0..0.5)

//        movement.moveTowardTarget(targetPos, 0.0..0.2)
//        sleep(1000)

    }

}

fun main() {
    val targetLocation = PositionAndRotation(0.0, 10.0, 0.0)

    val hardware = PhoHardware()
    val localizer = PhoLocalizer()
    val movement = MecanumMovement(localizer, hardware)

//    movement.setSpeedAll(1.0, 1.0, 1.0, 0.0, 1.0)
    localizer.currentPositionAndRotation = PositionAndRotation()

    val status1 = movement.moveTowardTarget(targetLocation)
    println("status $status1")

    localizer.currentPositionAndRotation = PositionAndRotation(y = 10.0)
    println("current pos: ${localizer.currentPositionAndRotation}")

    val status2 = movement.moveTowardTarget(targetLocation)
    println("status $status2")
}



class PhoLocalizer(): Localizer {

    var currentPositionAndRotation = PositionAndRotation()
    override fun currentPositionAndRotation(): PositionAndRotation = currentPositionAndRotation

    override fun recalculatePositionAndRotation() {
        TODO("Not yet implemented")
    }

    override fun setPositionAndRotation(x: Double?, y: Double?, r: Double?) {
        TODO("Not yet implemented")
    }

    override fun startNewMovement() {
        TODO("Not yet implemented")
    }

}