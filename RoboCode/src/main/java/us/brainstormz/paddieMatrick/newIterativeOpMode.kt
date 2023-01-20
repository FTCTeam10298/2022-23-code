package us.brainstormz.paddieMatrick

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import us.brainstormz.IterativeOpMode
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.motion.RRLocalizer
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import us.brainstormz.IterativeOpMode.*
import us.brainstormz.localizer.PositionAndRotation

//@Autonomous(name= "new", group= "!")
class newIterativeOpMode: OpMode() {
    val iterativeOpMode = IterativeOpMode(this)

    val hardware = PaddieMatrickHardware()

    val console = TelemetryConsole(telemetry)
    val wizard = TelemetryWizard(console, null)

//    var aprilTagGX = AprilTagEx()

    lateinit var movement: MecanumMovement

    private lateinit var collector: Collector
    private lateinit var fourBar: FourBar
    private lateinit var depositor: Depositor

    /** AUTONOMOUS TASKS */

    /** Auto Tasks */
    private val depositPosition = PositionAndRotation(x= -5.8, y= -58.2, r= 45.0)
    private val depositPreload = listOf(
            AutoTask(listOf(
                    ChassisTask(PositionAndRotation(x = 0.0, y = -49.0, r = 0.0), movement= movement, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.MidJunction.counts, depositor= depositor, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.PreDeposit.degrees, depositor= depositor, requiredForCompletion = false
                    ))),
            AutoTask(listOf(
                    ChassisTask(depositPosition, movement= movement, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, depositor= depositor, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.PreDeposit.degrees, depositor= depositor, requiredForCompletion = false)
            )),
            AutoTask(listOf(
                    ChassisTask(depositPosition, movement= movement, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, depositor= depositor, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.PreDeposit.degrees, depositor= depositor, requiredForCompletion = false)
            )),
            AutoTask(listOf(
                    ChassisTask(depositPosition, movement= movement, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, depositor= depositor, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.Deposit.degrees, depositor= depositor, requiredForCompletion = true)
            )),
            AutoTask(listOf(
                    ChassisTask(depositPosition, movement= movement, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, depositor= depositor, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.Deposit.degrees, depositor= depositor, requiredForCompletion = true),
                    OtherTask(action = {
                        hardware.collector.power = -1.0

                        if (!depositor.isConeInCollector()) {
                            hardware.collector.power = 0.0
                            true
                        } else {
                            false
                        }
                    }, requiredForCompletion = true)
            )),
            AutoTask(listOf(
                    ChassisTask(depositPosition, movement= movement, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, depositor= depositor, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, depositor= depositor, requiredForCompletion = true)
            )),
            AutoTask(listOf(
                    ChassisTask(PositionAndRotation(x = -4.0, y = -52.0, r = 45.0), movement= movement, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, depositor= depositor, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, depositor= depositor, requiredForCompletion = false)
            ))
    )

    private val cycleMidPoint = PositionAndRotation(x = 0.0, y = -52.0, r = 90.0)
    private val preCollectionPosition = PositionAndRotation(x = 19.0, y = -51.0, r = 90.0)
    private val collectionPosition = PositionAndRotation(x = 30.0, y = -51.0, r = 90.0)
    private val cycles = listOf(
            /** Prepare to collect */
            AutoTask(listOf(
                    ChassisTask(PositionAndRotation(x = -4.0, y = -52.0, r = 90.0), movement= movement, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, depositor= depositor, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, depositor= depositor, requiredForCompletion = true)
            )),
            AutoTask(listOf(
                    ChassisTask(preCollectionPosition, movement= movement, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, depositor= depositor, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, depositor= depositor, requiredForCompletion = false)
            )),
            /** Collecting */
            AutoTask(listOf(
                    ChassisTask(collectionPosition, power = 0.0..0.05, movement= movement, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.StackPreCollection.counts, depositor= depositor, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.Collecting.degrees, depositor= depositor, requiredForCompletion = true),
                    OtherTask(action = {
                        hardware.funnelLifter.position = collector.funnelDown

                        depositor.isConeInFunnel()
                    }, requiredForCompletion = true)
            )),
            AutoTask(listOf(
                    ChassisTask(collectionPosition, power = 0.0..0.0, movement= movement, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.Collection.counts, depositor= depositor, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Collecting.degrees, depositor= depositor, requiredForCompletion = false),
                    OtherTask(action = {
                        hardware.collector.power = 1.0

                        depositor.isConeInCollector()
                    }, requiredForCompletion = true)
            )),
            AutoTask(listOf(
                    ChassisTask(collectionPosition, power = 0.0..0.0, movement= movement, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.StackPreCollection.counts, depositor= depositor, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, depositor= depositor, requiredForCompletion = true),
                    OtherTask(action = {
                        hardware.collector.power = 0.1
                        true
                    }, requiredForCompletion = false)
            )),
            AutoTask(listOf(
                    ChassisTask(collectionPosition, power = 0.0..0.0, movement= movement, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.MidJunction.counts, depositor= depositor, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, depositor= depositor, requiredForCompletion = false),
                    OtherTask(action = {
                        hardware.funnelLifter.position = collector.funnelUp
                        hardware.collector.power = 0.0
                        true
                    }, requiredForCompletion = false)
            )),
            /** Drive to pole */
            AutoTask(listOf(
                    ChassisTask(cycleMidPoint, movement= movement, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.MidJunction.counts, depositor= depositor, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, depositor= depositor, requiredForCompletion = false)
            )),
            AutoTask(listOf(
                    ChassisTask(depositPosition, movement= movement, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, depositor= depositor, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.PreDeposit.degrees, depositor= depositor, requiredForCompletion = true)
            )),
            /** Deposit */
            AutoTask(listOf(
                    ChassisTask(depositPosition, movement= movement, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, depositor= depositor, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Deposit.degrees, depositor= depositor, requiredForCompletion = true)
            )),
            AutoTask(listOf(
                    ChassisTask(depositPosition, movement= movement, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, depositor= depositor, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Deposit.degrees, depositor= depositor, requiredForCompletion = false),
                    OtherTask(action = {
                        hardware.collector.power = -1.0
                        !depositor.isConeInCollector()
                    }, requiredForCompletion = true)
            )),
            AutoTask(listOf(
                    ChassisTask(depositPosition, movement= movement, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, depositor= depositor, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, depositor= depositor, requiredForCompletion = true),
                    OtherTask(action = {
                        hardware.collector.power = 0.0
                        true
                    }, requiredForCompletion = false)
            )),
            /** Return to midpoint */
            AutoTask(listOf(
                    ChassisTask(cycleMidPoint, movement= movement, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, depositor= depositor, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, depositor= depositor, requiredForCompletion = false)
            ))
    )

    enum class ParkPositions(val pos: PositionAndRotation) {
        One(PositionAndRotation(x = -20.0, y = -52.0, r = 0.0)),
        Two(PositionAndRotation(x = 0.0, y = -52.0, r = 0.0)),
        Three(PositionAndRotation(x = 20.0, y = -52.0, r = 0.0)),
    }
    private val parkOne: List<AutoTask> = listOf(
            AutoTask(listOf(
                    ChassisTask(ParkPositions.One.pos, movement= movement, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, depositor= depositor, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, depositor= depositor, requiredForCompletion = false)
            )))
    private val parkTwo: List<AutoTask> = listOf(
            AutoTask(listOf(
                    ChassisTask(ParkPositions.Two.pos, movement= movement, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, depositor= depositor, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, depositor= depositor, requiredForCompletion = false)),
                    startDeadlineSeconds = 10.0
            ))
    private val parkThree: List<AutoTask> = listOf(
            AutoTask(listOf(
                    ChassisTask(ParkPositions.Three.pos, movement= movement, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, depositor= depositor, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, depositor= depositor, requiredForCompletion = false)),
                    startDeadlineSeconds = 10.0
            ))

    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        val localizer = RRLocalizer(hardware)
        movement = MecanumMovement(localizer, hardware, telemetry)

        collector = Collector()
        fourBar = FourBar(telemetry)
        fourBar.init(leftServo = hardware.left4Bar, rightServo = hardware.right4Bar, encoder = hardware.encoder4Bar)
        depositor = Depositor(collector = collector, fourBar = fourBar, hardware = hardware, telemetry = telemetry)

//        wizard.newMenu("alliance", "What alliance are we on?", listOf("Red", "Blue"),"program", firstMenu = true)
        wizard.newMenu("program", "Which auto are we starting?", listOf("Park Auto" to null, "New Cycle Auto" to "startPos"), firstMenu = true)
//        wizard.newMenu("alliance", "Which alliance are we on?", listOf("Blue", "Red"), nextMenu = "startPos")
        wizard.newMenu("startPos", "Which side are we starting?", listOf("Right", "Left"))
    }

    override fun init_loop() {
//        wizard.summonWizard(gamepad1)
//
//        aprilTagGX.initAprilTag(hardwareMap, telemetry, null)
//        aprilTagGX.runAprilTag(telemetry)

//        val fourBarTarget = fourBar.current4BarDegrees() - 70
//        val fourBarPower = fourBarPID.calcPID(fourBarTarget, fourBar.current4BarDegrees())
//        hardware.left4Bar.power = fourBarPower
//        hardware.right4Bar.power = fourBarPower
    }

    override fun start() {
        val aprilTagGXOutput = SignalOrientation.Two// aprilTagGX.signalOrientation ?: SignalOrientation.Three
        val parkPath = when (aprilTagGXOutput) {
            SignalOrientation.One -> parkOne
            SignalOrientation.Two -> parkTwo
            SignalOrientation.Three -> parkThree
        }

        /** Auto assembly */
        iterativeOpMode.init(tasksTodo = depositPreload + cycles + cycles + parkPath)
    }

    override fun loop() {
        val dashboard = FtcDashboard.getInstance()
        val multipleTelemetry = MultipleTelemetry(telemetry, dashboard.telemetry)
        iterativeOpMode.loop()
    }

    /** Declare new task types for your subassemblies */
    class ChassisTask(targetPosition: PositionAndRotation, power: ClosedRange<Double> = 0.0..1.0, movement: MecanumMovement, requiredForCompletion: Boolean): SubassemblyTask(requiredForCompletion) {
        override val action: () -> Boolean = { movement.moveTowardTarget(targetPosition, power) }
    }
    class LiftTask(targetCounts: Int, depositor: Depositor, requiredForCompletion: Boolean): SubassemblyTask(requiredForCompletion) {
        override val action: () -> Boolean = {depositor.moveLift(targetCounts)}
    }
    class FourBarTask(targetDegrees: Double, requiredForCompletion: Boolean, depositor: Depositor): SubassemblyTask(requiredForCompletion) {
        override val action: () -> Boolean = {depositor.moveFourBar(targetDegrees)}
    }
    class OtherTask(override val action: ()->Boolean, requiredForCompletion: Boolean): SubassemblyTask(requiredForCompletion)

}