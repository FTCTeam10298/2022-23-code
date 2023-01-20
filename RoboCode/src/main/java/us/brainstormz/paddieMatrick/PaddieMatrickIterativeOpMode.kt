package us.brainstormz.paddieMatrick

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.motion.RRLocalizer
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard

@Autonomous(name= "PaddieMatrick Auto", group= "!")
class PaddieMatrickIterativeOpMode: OpMode() {
    private val hardware = PaddieMatrickHardware()

    private val console = TelemetryConsole(telemetry)
    private val wizard = TelemetryWizard(console, null)
    var aprilTagGX = AprilTagEx()

    private lateinit var movement: MecanumMovement

    private lateinit var collector: Collector
    private lateinit var fourBar: FourBar
    private lateinit var depositor: Depositor

    /** Auto Tasks */
    private val depositPosition = PositionAndRotation(x= -5.2, y= -58.0, r= 45.0)
    private val depositPreload = listOf(
            AutoTask(
                    ChassisTask(PositionAndRotation(x= 0.0, y= -49.0, r= 0.0), power= 0.0..0.8, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.SafeDriving.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.PreDeposit.degrees, requiredForCompletion = false)
            ),
            AutoTask(
                    ChassisTask(depositPosition, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.PreDeposit.degrees, requiredForCompletion = false)
            ),
            AutoTask(
                    ChassisTask(depositPosition, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.PreDeposit.degrees, requiredForCompletion = false)
            ),
            AutoTask(
                    ChassisTask(depositPosition, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.Deposit.degrees, requiredForCompletion = true)
            ),
            AutoTask(
                    ChassisTask(depositPosition, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.Deposit.degrees, requiredForCompletion = true),
                    OtherTask(action = {
                        hardware.collector.power = -1.0

                        if (!depositor.isConeInCollector()) {
                            hardware.collector.power = 0.0
                            true
                        } else {
                            false
                        }
                    }, requiredForCompletion = true)
            ),
            AutoTask(
                    ChassisTask(depositPosition, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = true)
            ),
            AutoTask(
                    ChassisTask(PositionAndRotation(x= -4.0, y= -52.0, r= 45.0), requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false)
            )
    )

    private val cycleMidPoint = PositionAndRotation(x = 7.0, y = -52.0, r = 90.0)
    private val preCollectionPosition = PositionAndRotation(x = 19.0, y = -51.0, r = 90.0)
    private val collectionPosition = PositionAndRotation(x = 30.0, y = -51.0, r = 90.0)
    private val cycles = listOf(
            /** Prepare to collect */
            AutoTask(
                    ChassisTask(cycleMidPoint, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = true)
            ),
            AutoTask(
                    ChassisTask(preCollectionPosition, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false)
            ),
            /** Collecting */
            AutoTask(
                    ChassisTask(collectionPosition, power = 0.0..0.08, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.StackPreCollection.counts, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.Collecting.degrees, requiredForCompletion = true),
                    OtherTask(action= {
                        hardware.funnelLifter.position = collector.funnelDown

                        depositor.isConeInFunnel()
                    }, requiredForCompletion = true)
            ),
            AutoTask(
                    ChassisTask(collectionPosition, power = 0.0..0.0, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.Collection.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Collecting.degrees, requiredForCompletion = false),
                    OtherTask(action= {
                        hardware.collector.power = 1.0

                        depositor.isConeInCollector()
                    }, requiredForCompletion = true)
            ),
            AutoTask(
                    ChassisTask(collectionPosition, power = 0.0..0.0, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.StackPreCollection.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = true),
                    OtherTask(action= {
                        hardware.collector.power = 0.1
                        true
                    }, requiredForCompletion = false)
            ),
            AutoTask(
                    ChassisTask(collectionPosition, power = 0.0..0.0, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.MidJunction.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false),
                    OtherTask(action= {
                        hardware.funnelLifter.position = collector.funnelUp
                        hardware.collector.power = 0.0
                        true
                    }, requiredForCompletion = false)
            ),
            /** Drive to pole */
            AutoTask(
                    ChassisTask(cycleMidPoint, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.MidJunction.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false)
            ),
            AutoTask(
                    ChassisTask(depositPosition, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.PreDeposit.degrees, requiredForCompletion = true)
            ),
            /** Deposit */
            AutoTask(
                    ChassisTask(depositPosition, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Deposit.degrees, requiredForCompletion = true)
            ),
            AutoTask(
                    ChassisTask(depositPosition, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Deposit.degrees, requiredForCompletion = false),
                    OtherTask(action= {
                        hardware.collector.power = -1.0
                        !depositor.isConeInCollector()
                    }, requiredForCompletion = true)
            ),
            AutoTask(
                    ChassisTask(depositPosition, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = true),
                    OtherTask(action= {
                        hardware.collector.power = 0.0
                        true
                    }, requiredForCompletion = false)
            ),
            /** Return to midpoint */
            AutoTask(
                    ChassisTask(cycleMidPoint, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false)
            )
    )

    enum class ParkPositions(val pos: PositionAndRotation) {
        One(PositionAndRotation(x = -22.0, y = -52.0, r = 0.0)),
        Two(PositionAndRotation(x = 0.0, y = -52.0, r = 0.0)),
        Three(PositionAndRotation(x = 22.0, y = -52.0, r = 0.0)),
    }
    private val compensateForAbruptEnd = {
        hardware.funnelLifter.position = collector.funnelUp
        hardware.collector.power = 0.0
        if (!fourBar.is4BarAtPosition(Depositor.FourBarDegrees.Vertical.degrees)) {
            depositor.powerLift(0.0)
        }
        true
    }
    private val parkTimeout = 27.5
    private val parkOne: List<AutoTask> = listOf(
            AutoTask(
                    ChassisTask(ParkPositions.One.pos, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false),
                    OtherTask(action= compensateForAbruptEnd, requiredForCompletion = false),
                    startDeadlineSeconds = parkTimeout
            ))
    private val parkTwo: List<AutoTask> = listOf(
            AutoTask(
                    ChassisTask(ParkPositions.Two.pos, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false),
                    OtherTask(action= compensateForAbruptEnd, requiredForCompletion = false),
                    startDeadlineSeconds = parkTimeout
            ))
    private val parkThree: List<AutoTask> = listOf(
            AutoTask(
                    ChassisTask(ParkPositions.Three.pos, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false),
                    OtherTask(action= compensateForAbruptEnd, requiredForCompletion = false),
                    startDeadlineSeconds = parkTimeout
            ))

    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        aprilTagGX.initAprilTag(hardwareMap, telemetry, null)

        val localizer = RRLocalizer(hardware)
        movement = MecanumMovement(localizer, hardware, telemetry)

        collector = Collector()
        fourBar = FourBar(telemetry)
        fourBar.init(leftServo = hardware.left4Bar, rightServo = hardware.right4Bar, encoder = hardware.encoder4Bar)
        depositor = Depositor(collector = collector, fourBar = fourBar, hardware = hardware, telemetry = telemetry)

        wizard.newMenu("alliance", "What alliance are we on?", listOf("Red", "Blue"),"program", firstMenu = true)
        wizard.newMenu("program", "Which auto are we starting?", listOf("Park Auto" to null, "New Cycle Auto" to "startPos"))
        wizard.newMenu("startPos", "Which side are we starting?", listOf("Right", "Left"))
    }

    override fun init_loop() {
        val isWizardDone = wizard.summonWizard(gamepad1)

        if (isWizardDone)
            aprilTagGX.runAprilTag(telemetry)
//        else
//            telemetry.addLine("Running The Wiz")

//        val fourBarTarget = fourBar.current4BarDegrees() - 70
//        val fourBarPower = fourBarPID.calcPID(fourBarTarget, fourBar.current4BarDegrees())
//        hardware.left4Bar.power = fourBarPower
//        hardware.right4Bar.power = fourBarPower
    }

    private lateinit var autoTasks: List<AutoTask>
    private lateinit var autoTaskIterator: ListIterator<AutoTask>
    private lateinit var currentTask: AutoTask
    override fun start() {
        this.resetRuntime()
        hardware.rightOdomEncoder.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        hardware.leftOdomEncoder.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        hardware.centerOdomEncoder.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        hardware.leftOdomEncoder.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        hardware.leftLift.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        val aprilTagGXOutput =  aprilTagGX.signalOrientation ?: SignalOrientation.Three

        val side = if (wizard.wasItemChosen("startPos", "Left"))
            FieldSide.Left
        else
            FieldSide.Right

//        autoTasks = depositPreload + cycles + cycles + parkTwo
        autoTasks = makePlanForAuto(
            signalOrientation = aprilTagGXOutput,
            fieldSide = side)


        autoTaskIterator = autoTasks.listIterator()
        currentTask = autoTaskIterator.next()
    }

    private fun flopToLeftSide(task:ChassisTask):ChassisTask {
        val pos = task.targetPosition
        return task.copy(
                targetPosition = PositionAndRotation(
                        x = flopLateralPosToLeftSide(pos.x),
                        y = pos.y,
                        r = flopRotationToLeftSide(pos.r)
                )
        )
    }

    private fun flopRotationToLeftSide(r: Double) = r * -1
    private fun flopLateralPosToLeftSide(x: Double) = x * -1

    private fun flopToLeftSide(tasks:List<AutoTask>):List<AutoTask> {
        return tasks.map{task->
            task.copy(
                chassisTask = flopToLeftSide(task.chassisTask)
            )
        }
    }
    enum class FieldSide {
        Left, Right
    }
    private fun makePlanForAuto(signalOrientation:SignalOrientation, fieldSide:FieldSide):List<AutoTask> {
        val parkPath = when (signalOrientation) {
            SignalOrientation.One -> parkOne
            SignalOrientation.Two -> parkTwo
            SignalOrientation.Three -> parkThree
        }
        return flopped(depositPreload + cycles + cycles, fieldSide) + parkPath
    }

    private fun PaddieMatrickIterativeOpMode.flopped(coreTasks: List<AutoTask>, side: FieldSide): List<AutoTask> {
        val swichedTasks = when (side) {
            FieldSide.Left -> flopToLeftSide(coreTasks)
            FieldSide.Right -> coreTasks
        }
        return swichedTasks
    }

    private var prevTime = System.currentTimeMillis()
    override fun loop() {
        val dashboard = FtcDashboard.getInstance()
        val multipleTelemetry = MultipleTelemetry(telemetry, dashboard.telemetry)

        /** AUTONOMOUS  PHASE */
        val dt = System.currentTimeMillis() - prevTime
        prevTime = System.currentTimeMillis()
        multipleTelemetry.addLine("dt: $dt")
        multipleTelemetry.addLine("runtime: ${getEffectiveRuntime()}")

        val nextDeadlinedTask = findNextDeadlinedTask(autoTasks)
        val nextDeadlineSeconds = nextDeadlinedTask?.startDeadlineSeconds
        val atOrPastDeadline = if (nextDeadlineSeconds != null) nextDeadlineSeconds <= getEffectiveRuntime() else false
//        multipleTelemetry.addLine("getEffectiveRuntime(): ${getEffectiveRuntime()}")
//        multipleTelemetry.addLine("nextDeadlineSeconds: $nextDeadlineSeconds")
//        multipleTelemetry.addLine("isDeadlineUponUs: $atOrPastDeadline")

        if (atOrPastDeadline) {
            multipleTelemetry.addLine("skipping ahead to deadline")
            failSkippedTasks(nextDeadlinedTask!!, autoTasks)
            currentTask = nextDeadlinedTask
            multipleTelemetry.addLine("deadlined Task: $nextDeadlinedTask")
        } else {
            if (currentTask.isFinished()) {
                if (autoTaskIterator.hasNext()) {
                    multipleTelemetry.addLine("current task finished: $currentTask")
                    currentTask = autoTaskIterator.next()
                    multipleTelemetry.addLine("next task is: $currentTask")
                }
//                else {
//                    requestOpModeStop()
//                }
            }
        }

        currentTask.taskStatus = TaskStatus.Running
        if (currentTask.timeStartedSeconds != null) {
            currentTask.timeStartedSeconds = getEffectiveRuntime()
        }

        multipleTelemetry.addLine("\n\ncurrent task: $currentTask")

        val chassisTask = currentTask.chassisTask
        val isChassisTaskCompleted = movement.moveTowardTarget(chassisTask.targetPosition, chassisTask.power)

        val liftTask = currentTask.liftTask
        val isLiftTaskCompleted = depositor.moveLift(liftTask.targetCounts)

        val fourBarTask = currentTask.fourBarTask
        val isFourBarTaskCompleted = depositor.moveFourBar(fourBarTask.targetDegrees)

        val subassemblyTask = currentTask.subassemblyTask
        val isSubassemblyTaskCompleted = subassemblyTask?.action?.invoke()


        val completions = listOf(chassisTask to isChassisTaskCompleted, liftTask to isLiftTaskCompleted, fourBarTask to isFourBarTaskCompleted, subassemblyTask to isSubassemblyTaskCompleted)
        val isTaskCompleted: Boolean = completions.fold(true) { prevTasksCompleted, completion ->
            if (completion.first?.requiredForCompletion == true)
                prevTasksCompleted && completion.second!!
            else
                prevTasksCompleted
        }

        if (isTaskCompleted) {
            currentTask.taskStatus = TaskStatus.Completed
            currentTask.timeFinishedSeconds = getEffectiveRuntime()
        }
    }

    fun getEffectiveRuntime() = this.runtime

    data class AutoTask(val chassisTask: ChassisTask,
                        val liftTask: LiftTask,
                        val fourBarTask: FourBarTask,
                        val subassemblyTask: OtherTask? = null,
                        val startDeadlineSeconds: Double? = null /* time after start by which this is guaranteed to start */,
                        val timeoutSeconds: Double? = null /* time after which the task will be skipped if not already complete */,
                        var taskStatus: TaskStatus = TaskStatus.Todo,
                        var timeStartedSeconds: Double? = null,
                        var timeFinishedSeconds: Double? = null) {
        fun isFinished() = taskStatus == TaskStatus.Completed || taskStatus == TaskStatus.Failed
//        override fun toString(): String = ""
    }

    open class SubassemblyTask(open val requiredForCompletion: Boolean)
    data class ChassisTask(
            val targetPosition: PositionAndRotation,
            val power: ClosedRange<Double> = 0.0..1.0,
            override val requiredForCompletion: Boolean): SubassemblyTask(requiredForCompletion)
    data class LiftTask(val targetCounts: Int, override val requiredForCompletion: Boolean): SubassemblyTask(requiredForCompletion)
    data class FourBarTask(val targetDegrees: Double, override val requiredForCompletion: Boolean): SubassemblyTask(requiredForCompletion)
    data class OtherTask(val action: ()->Boolean, override val requiredForCompletion: Boolean): SubassemblyTask(requiredForCompletion)

    enum class TaskStatus {
        Todo,
        Running,
        Failed,
        Completed
    }

    private var tasksWithDeadlines: List<AutoTask> = emptyList()
    private fun findNextDeadlinedTask(tasks: List<AutoTask>): AutoTask? {
        if (tasksWithDeadlines.isEmpty()) {
            tasksWithDeadlines = tasks.filter{ it.startDeadlineSeconds != null && (it.taskStatus != TaskStatus.Completed || it.taskStatus != TaskStatus.Failed)}
            tasksWithDeadlines = tasksWithDeadlines.sortedBy { task ->
                task.startDeadlineSeconds
            }
        }
        tasksWithDeadlines.filter { task -> !task.isFinished() }

        return tasksWithDeadlines.firstOrNull()
    }
    private fun failSkippedTasks(deadlinedTask: AutoTask, tasks: List<AutoTask>) {
        tasks.forEach { task ->
            if (task != deadlinedTask && !task.isFinished()) {
                task.taskStatus = TaskStatus.Failed
                task.timeFinishedSeconds = getEffectiveRuntime()
            }
        }
    }
}