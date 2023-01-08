package us.brainstormz.motion

//import us.brainstormz.rataTony.RataTonyHardware
import com.acmerobotics.dashboard.FtcDashboard
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

    val yTranslationPID = PID(0.04, 0.0, 0.0)
    val xTranslationPID = PID(0.06, 0.0, 0.0)
    val rotationPID = PID(0.3, 0.0, 0.0)
    override var precisionInches: Double = 0.2
    override var precisionDegrees: Double = 1.0

    private val dashboard = FtcDashboard.getInstance()
    private val dashboardTelemetry = dashboard.telemetry

    /**
     * Only required when using goToPosition
     */
//    override lateinit var linearOpMode: LinearOpMode

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
        dashboardTelemetry.addData("distanceErrorX: ", distanceErrorX)
        dashboardTelemetry.addData("distanceErrorY: ", distanceErrorY)

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
            return true
        }

        // Calculate the error in x and y and use the PID to find the error in angle
//        val translationSpeed = 1.0 //translationPID.calcPID(distanceError)
//        val speedX: Double = 0.0 //translationSpeed * (sin(currentPos.r) * distanceErrorY + cos(currentPos.r) * -distanceErrorX)
//        val speedY: Double = translationSpeed * (cos(currentPos.r) * distanceErrorY + sin(currentPos.r) * distanceErrorX)
        val speedX: Double = xTranslationPID.calcPID(sin(currentPos.r) * distanceErrorY + cos(currentPos.r) * -distanceErrorX)
        val speedY: Double = yTranslationPID.calcPID(cos(currentPos.r) * distanceErrorY + sin(currentPos.r) * distanceErrorX)
        val speedA: Double = rotationPID.calcPID(angleError)

        telemetry.addLine("distance error: $distanceError, angle error degrees: ${Math.toDegrees(angleError)}")
        dashboardTelemetry.addData("speedY: ",speedY)
        telemetry.addLine("speedX: $speedX, speedY: $speedY, speedA: $speedA")

        dashboardTelemetry.update()
        telemetry.update()
//        driveSetPower((speedY + speedX - speedA),
//                      (speedY - speedX + speedA),
//                      (speedY - speedX - speedA),
//                      (speedY + speedX + speedA))
        setSpeedAll(vX= speedX, vY= speedY, vA= speedA, powerRange.start, powerRange.endInclusive)

        return false
    }

    /** works */
    fun setSpeedAll(vX: Double, vY: Double, vA: Double, minPower: Double, maxPower: Double) {

        // Calculate theoretical values for motor powers using transformation matrix
//        movement.driveSetPower((y + x - r),
//                               (y - x + r),
//                               (y - x - r),
//                               (y + x + r))
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


@TeleOp(name= "Odom Movement Test")
class OdomMoveTest: LinearOpMode() {
    val hardware = PaddieMatrickHardware()
    val console = GlobalConsole.newConsole(telemetry)

    var targetPos = PositionAndRotation(y= 10.0, x= 0.0, r= 0.0)

    override fun runOpMode() {
        hardware.init(hardwareMap)
        val localizer = RRLocalizer(hardware)
        val movement = MecanumMovement(localizer= localizer, hardware= hardware, telemetry= telemetry)
//        val movement = OdometryDriveMovement(console,  hardware, this)

        waitForStart()

//        movement.fineTunedGoToPos(targetPos)
        movement.goToPosition(targetPos, this, 0.0..1.0)

    }

}
//
//fun main() {
//    val targetLocation = PositionAndRotation(0.0, 10.0, 0.0)
//
//    val hardware = PhoHardware()
//    val localizer = PhoLocalizer()
//    val movement = MecanumMovement(localizer, hardware)
//
////    movement.setSpeedAll(1.0, 1.0, 1.0, 0.0, 1.0)
//    localizer.currentPositionAndRotation = PositionAndRotation()
//
//    val status1 = movement.moveTowardTarget(targetLocation)
//    println("status $status1")
//
//    localizer.currentPositionAndRotation = PositionAndRotation(y = 10.0)
//    println("current pos: ${localizer.currentPositionAndRotation}")
//
//    val status2 = movement.moveTowardTarget(targetLocation)
//    println("status $status2")
//}



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