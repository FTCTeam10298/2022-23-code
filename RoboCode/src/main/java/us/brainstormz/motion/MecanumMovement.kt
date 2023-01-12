package us.brainstormz.motion

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.config.Config
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.util.Range
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.hardwareClasses.MecanumHardware
import us.brainstormz.localizer.Localizer
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.paddieMatrick.PaddieMatrickHardware
import us.brainstormz.pid.PID
import us.brainstormz.telemetryWizard.GlobalConsole
import kotlin.math.*

class MecanumMovement(override val localizer: Localizer, override val hardware: MecanumHardware, private val telemetry: Telemetry): Movement, MecanumDriveTrain(hardware) {

    var yTranslationPID = PID(0.0434, 0.0000004, 0.0)
    var xTranslationPID = PID(0.0988, 0.0000008, 0.0)
    var rotationPID = PID(0.8, 0.0, 0.0)
    override var precisionInches: Double = 0.4
    override var precisionDegrees: Double = 2.0

    /**
     * Blocking function
     */
    override fun goToPosition(target: PositionAndRotation, linearOpMode: LinearOpMode, powerRange: ClosedRange<Double>) {
        while (linearOpMode.opModeIsActive()) {
            val targetReached = moveTowardTarget(target, powerRange)

            if (targetReached)
                break
        }
    }

    override fun moveTowardTarget(target: PositionAndRotation, powerRange: ClosedRange<Double>): Boolean {
        localizer.recalculatePositionAndRotation()
        val currentPos = localizer.currentPositionAndRotation()
        telemetry.addLine("currentPos: $currentPos")


        // Find the error in distance for X
        val distanceErrorX = target.x - currentPos.x
        // Find there error in distance for Y
        val distanceErrorY = target.y - currentPos.y
        telemetry.addData("distanceErrorX: ", distanceErrorX)
        telemetry.addData("distanceErrorY: ", distanceErrorY)

        // Find the error in angle
        var tempAngleError = target.r - currentPos.r

        while (tempAngleError > Math.PI)
            tempAngleError -= Math.PI * 2

        while (tempAngleError < -Math.PI)
            tempAngleError += Math.PI * 2

        val angleError: Double = tempAngleError

        // Find the error in distance
        val distanceError = hypot(distanceErrorX, distanceErrorY)

        // Check to see if we've reached the desired position already
        if (abs(distanceError) <= precisionInches &&
                abs(angleError) <= Math.toRadians(precisionDegrees)) {
//            return true
        }

        // Calculate the error in x and y and use the PID to find the error in angle
        val speedX: Double = xTranslationPID.calcPID(sin(currentPos.r) * distanceErrorY + cos(currentPos.r) * -distanceErrorX)
        val speedY: Double = yTranslationPID.calcPID(cos(currentPos.r) * distanceErrorY + sin(currentPos.r) * distanceErrorX)
        val speedA: Double = rotationPID.calcPID(angleError)

        telemetry.addLine("\ndistance error: $distanceError, \nangle error degrees: ${Math.toDegrees(angleError)}\n")
        telemetry.addData("total distance error: ", distanceError)
        telemetry.addData("angle error degrees: ", Math.toDegrees(angleError))

        telemetry.addData("speedY: ", speedY)
        telemetry.addData("speedX: ", speedX)
        telemetry.addData("speedA: ", speedA)
        telemetry.addLine("speedX: $speedX, speedY: $speedY, speedA: $speedA")

        telemetry.update()

        setSpeedAll(vX= speedX, vY= speedY, vA= speedA, powerRange.start, powerRange.endInclusive)

        return false
    }

    /** works */
    fun setSpeedAll(vX: Double, vY: Double, vA: Double, minPower: Double, maxPower: Double) {

        // Calculate theoretical values for motor powers using transformation matrix
        var fl = vY + vX - vA
        var fr = vY - vX + vA
        var bl = vY - vX - vA
        var br = vY + vX + vA

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

        telemetry.addLine("Powers: $fl, $bl, $fr, $br" )
        println("Powers: $fl, $bl, $fr, $br")

        // Set powers
        driveSetPower(-fl, fr, -bl, br)
    }

}

@Config
object MovementTuning {
    @JvmField var ykp = 0.0
    @JvmField var yki = 0.0
    @JvmField var ykd = 0.0

    @JvmField var xkp = 0.0
    @JvmField var xki = 0.0
    @JvmField var xkd = 0.0

    @JvmField var rkp = 0.0
    @JvmField var rki = 0.0
    @JvmField var rkd = 0.0

    @JvmField var targetY = 0.0
    @JvmField var targetX = 0.0
    @JvmField var targetR = 0.0
}

@TeleOp(name= "Movement Tuning")
class OdomMoveTest: LinearOpMode() {
    val hardware = PaddieMatrickHardware()
    val console = GlobalConsole.newConsole(telemetry)

    var targetPos = PositionAndRotation(x= MovementTuning.targetX, y= MovementTuning.targetY, r= MovementTuning.targetR)

    override fun runOpMode() {
        hardware.init(hardwareMap)

        val dashboard = FtcDashboard.getInstance()
        val dashboardTelemetry = dashboard.telemetry
        val localizer = RRLocalizer(hardware)
        val movement = MecanumMovement(localizer= localizer, hardware= hardware, telemetry= dashboardTelemetry)

        movement.yTranslationPID = PID(MovementTuning.ykp, MovementTuning.yki, MovementTuning.ykd)
        movement.xTranslationPID = PID(MovementTuning.xkp, MovementTuning.xki, MovementTuning.xkd)
        movement.rotationPID = PID(MovementTuning.rkp, MovementTuning.rki, MovementTuning.rkd)

        dashboardTelemetry.addData("speedY: ", 0)
        dashboardTelemetry.addData("distanceErrorX: ", 0)
        dashboardTelemetry.addData("distanceErrorY: ", 0)
        dashboardTelemetry.addData("total distance error: ", 0)
        dashboardTelemetry.addData("angle error degrees: ", 0)
        dashboardTelemetry.addData("speedY: ", 0)
        dashboardTelemetry.addData("speedX: ", 0)
        dashboardTelemetry.addData("speedA: ", 0)
        dashboardTelemetry.update()
        waitForStart()

        while (opModeIsActive()) {
            val targetReached = movement.moveTowardTarget(targetPos, 0.0..1.0)

            if (targetReached) {
                dashboardTelemetry.addLine("target reached")
            }
        }
    }

}
//class PhoLocalizer(): Localizer {
//
//    var currentPositionAndRotation = PositionAndRotation()
//    override fun currentPositionAndRotation(): PositionAndRotation = currentPositionAndRotation
//
//    override fun recalculatePositionAndRotation() {
//        TODO("Not yet implemented")
//    }
//
//    override fun setPositionAndRotation(x: Double?, y: Double?, r: Double?) {
//        TODO("Not yet implemented")
//    }
//
//    override fun startNewMovement() {
//        TODO("Not yet implemented")
//    }
//
//}