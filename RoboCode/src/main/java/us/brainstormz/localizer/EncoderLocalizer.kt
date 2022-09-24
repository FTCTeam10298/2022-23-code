package us.brainstormz.localizer

import com.qualcomm.robotcore.hardware.*
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType
import org.firstinspires.ftc.robotcore.external.Func
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.hardwareClasses.EnhancedDCMotor
import us.brainstormz.hardwareClasses.MecOdometry
import us.brainstormz.hardwareClasses.MecanumHardware
import us.brainstormz.telemetryWizard.GlobalConsole
import us.brainstormz.telemetryWizard.TelemetryConsole
import kotlin.math.*

class EncoderLocalizer(private val hardware: MecanumHardware): Localizer {
    private val console = GlobalConsole.console
    var countsPerMotorRev = 28.0 // Rev HD Hex v2.1 Motor encoder
    var gearboxRatio = 19.2 // 40 for 40:1, 20 for 20:1
    var driveGearReduction = 1 / 1 // This is > 1.0 if geared for torque
    var wheelDiameterInches = 3.77953 // For figuring circumference
    var drivetrainError = 1.0 // Error determined from testing
    val countsPerInch = countsPerMotorRev * gearboxRatio * driveGearReduction / (wheelDiameterInches * PI) / drivetrainError
    val countsPerDegree: Double = countsPerInch * 0.268 * 2/3 // Found by testing
    var trackWidth = 15


    private var currentPos = PositionAndRotation()
    private var previousPos = PositionAndRotation()

//    used for the test below
//    data class powers(val lf: Int, val rf: Int, val lb: Int, val rb: Int)
//    var lF = 0
//    var rF = 0
//    var lB = 0
//    var rB = 0

    override fun currentPositionAndRotation(): PositionAndRotation = currentPos

    override fun recalculatePositionAndRotation() {

        val lF = hardware.lFDrive.currentPosition
        val rF = hardware.rFDrive.currentPosition
        val lB = hardware.lBDrive.currentPosition
        val rB = hardware.rBDrive.currentPosition


        val currentX = -(-lF + rF + lB - rB) / 4 / countsPerInch
        val currentY = (lF + rF + lB + rB) / 4 / countsPerInch
        val currentR = (-lF + rF - lB + rB) / 4 / countsPerDegree
        currentPos = PositionAndRotation(currentX, currentY, currentR)
    }

    override fun setPositionAndRotation(x: Double?, y: Double?, r: Double?) {
        currentPos.setCoordinate(x, y, r)
    }

    override fun startNewMovement() {
        setPositionAndRotation(0.0, 0.0, 0.0)
    }
}

//localization test
//fun main() {
//    val startLocation = PositionAndRotation()
//    val targetLocation = PositionAndRotation(10.0, 10.0, -90.0)
//    val locationDelta = targetLocation - startLocation
//
//    val hardware = PhoHardware()
//    val localizer = EncoderLocalizer(hardware, TelemetryConsole(hardware.telemetry))
//
//    localizer.recalculatePositionAndRotation()
//    val initPos = localizer.currentPositionAndRotation()
//    println("init Pos $initPos")
//
//    val y = (targetLocation.y * localizer.countsPerInch)
//    val x = (targetLocation.x * localizer.countsPerInch)
//    val r = (targetLocation.r * localizer.countsPerDegree)
//
//    val motorPowers = listOf((y + x - r), (y - x + r), (y - x - r), (y + x + r))
//
//    localizer.lF = motorPowers[0].toInt()
//    localizer.rF = motorPowers[1].toInt()
//    localizer.lB = motorPowers[2].toInt()
//    localizer.rB = motorPowers[3].toInt()
//
////    println("motor counts: ")
////    motorPowers.forEach { println(it) }
//
//    localizer.recalculatePositionAndRotation()
//    val rawEndLocation = localizer.currentPositionAndRotation()
//    val endLocation = PositionAndRotation(round(rawEndLocation.x), round(rawEndLocation.y), round(rawEndLocation.r))
//    println("\nnew Pos $endLocation")
//
////    if (endLocation == targetLocation) {
////        println("\n\npass")
////    } else {
////        throw Exception("EMOTIONAL DAMAGE!\nEnd Location Was: $endLocation \nShould have been: $targetLocation!")
////    }
//}

class PhoHardware(): MecOdometry {
    override val lOdom = EnhancedDCMotor(PhoMotor())
    override val rOdom = EnhancedDCMotor(PhoMotor())
    override val cOdom = EnhancedDCMotor(PhoMotor())

    override val lFDrive = PhoMotor()
    override val rFDrive = PhoMotor()
    override val lBDrive = PhoMotor()
    override val rBDrive = PhoMotor()
    override lateinit var hwMap: HardwareMap


    override fun init(ahwMap: HardwareMap) {
        TODO("Not yet implemented")
    }
    val telemetry = PhoTelemetry()

    class PhoTelemetry(): Telemetry {
        override fun addData(caption: String?, format: String?, vararg args: Any?): Telemetry.Item {
            TODO("Not yet implemented")
        }

        override fun addData(caption: String?, value: Any?): Telemetry.Item {
            TODO("Not yet implemented")
        }

        override fun <T : Any?> addData(caption: String?, valueProducer: Func<T>?): Telemetry.Item {
            TODO("Not yet implemented")
        }

        override fun <T : Any?> addData(
            caption: String?,
            format: String?,
            valueProducer: Func<T>?
        ): Telemetry.Item {
            TODO("Not yet implemented")
        }

        override fun removeItem(item: Telemetry.Item?): Boolean {
            TODO("Not yet implemented")
        }

        override fun clear() {
            TODO("Not yet implemented")
        }

        override fun clearAll() {
            TODO("Not yet implemented")
        }

        override fun addAction(action: Runnable?): Any {
            TODO("Not yet implemented")
        }

        override fun removeAction(token: Any?): Boolean {
            TODO("Not yet implemented")
        }

        override fun speak(text: String?) {
            TODO("Not yet implemented")
        }

        override fun speak(text: String?, languageCode: String?, countryCode: String?) {
            TODO("Not yet implemented")
        }

        override fun update(): Boolean {
            TODO("Not yet implemented")
        }

        override fun addLine(): Telemetry.Line {
            TODO("Not yet implemented")
        }

        override fun addLine(lineCaption: String?): Telemetry.Line {
            TODO("Not yet implemented")
        }

        override fun removeLine(line: Telemetry.Line?): Boolean {
            TODO("Not yet implemented")
        }

        override fun isAutoClear(): Boolean {
            TODO("Not yet implemented")
        }

        override fun setAutoClear(autoClear: Boolean) {
            TODO("Not yet implemented")
        }

        override fun getMsTransmissionInterval(): Int {
            TODO("Not yet implemented")
        }

        override fun setMsTransmissionInterval(msTransmissionInterval: Int) {
            TODO("Not yet implemented")
        }

        override fun getItemSeparator(): String {
            TODO("Not yet implemented")
        }

        override fun setItemSeparator(itemSeparator: String?) {
            TODO("Not yet implemented")
        }

        override fun getCaptionValueSeparator(): String {
            TODO("Not yet implemented")
        }

        override fun setCaptionValueSeparator(captionValueSeparator: String?) {
            TODO("Not yet implemented")
        }

        override fun setDisplayFormat(displayFormat: Telemetry.DisplayFormat?) {
            TODO("Not yet implemented")
        }

        override fun log(): Telemetry.Log {
            TODO("Not yet implemented")
        }

    }

    class PhoMotor(): DcMotor {
        var powerFr = 0.0

        override fun getManufacturer(): HardwareDevice.Manufacturer {
            TODO("Not yet implemented")
        }

        override fun getDeviceName(): String {
            TODO("Not yet implemented")
        }

        override fun getConnectionInfo(): String {
            TODO("Not yet implemented")
        }

        override fun getVersion(): Int {
            TODO("Not yet implemented")
        }

        override fun resetDeviceConfigurationForOpMode() {
            TODO("Not yet implemented")
        }

        override fun close() {
            TODO("Not yet implemented")
        }

        override fun setDirection(direction: DcMotorSimple.Direction?) {
            TODO("Not yet implemented")
        }

        override fun getDirection(): DcMotorSimple.Direction {
            TODO("Not yet implemented")
        }

        override fun setPower(power: Double) {
            powerFr = power
        }

        override fun getPower(): Double = powerFr

        override fun getMotorType(): MotorConfigurationType {
            TODO("Not yet implemented")
        }

        override fun setMotorType(motorType: MotorConfigurationType?) {
            TODO("Not yet implemented")
        }

        override fun getController(): DcMotorController {
            TODO("Not yet implemented")
        }

        override fun getPortNumber(): Int {
            TODO("Not yet implemented")
        }

        override fun setZeroPowerBehavior(zeroPowerBehavior: DcMotor.ZeroPowerBehavior?) {
            TODO("Not yet implemented")
        }

        override fun getZeroPowerBehavior(): DcMotor.ZeroPowerBehavior {
            TODO("Not yet implemented")
        }

        override fun setPowerFloat() {
            TODO("Not yet implemented")
        }

        override fun getPowerFloat(): Boolean {
            TODO("Not yet implemented")
        }

        override fun setTargetPosition(position: Int) {
            TODO("Not yet implemented")
        }

        override fun getTargetPosition(): Int {
            TODO("Not yet implemented")
        }

        override fun isBusy(): Boolean {
            TODO("Not yet implemented")
        }

        var currentPos = 0
        override fun getCurrentPosition(): Int = currentPos

        override fun setMode(mode: DcMotor.RunMode?) {
            TODO("Not yet implemented")
        }

        override fun getMode(): DcMotor.RunMode {
            TODO("Not yet implemented")
        }

    }

}