package us.brainstormz.paddieMatrick

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import org.openftc.easyopencv.OpenCvCamera
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.motion.RRLocalizer
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import java.lang.Thread.sleep

@Autonomous(name= "PaddieMatrick Auto", group= "!")
class PaddieMatrickAuto: OpMode() {
    private val hardware = PaddieMatrickHardware()

    private val console = TelemetryConsole(telemetry)
    private val wizard = TelemetryWizard(console, null)
//    val dashboard = FtcDashboard.getInstance()
    val multipleTelemetry = MultipleTelemetry(telemetry)//, dashboard.telemetry)


    var aprilTagGX = AprilTagEx()
    private val junctionAimer = JunctionAimer()

    private lateinit var movement: MecanumMovement

    private lateinit var collector: Collector
    private lateinit var fourBar: FourBar
    private lateinit var depositor: Depositor

    private var coneCollectionTime:Long? = null
    private var timeToFinishCollectingMilis = 0.15 * 1000

    /** Auto Tasks */
    private val midPointAccuracy = 2.5
    private val depositPosition = PositionAndRotation(x= -5.1, y= -58.5, r= 45.0)
    private val depositRoutine = listOf(
            /** Lineup */
            AutoTask(
                    ChassisTask(depositPosition, accuracyInches = 2.0, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, accuracyCounts = 700, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.PreDeposit.degrees, requiredForCompletion = false)
            ),
            AutoTask(
                    ChassisTask(depositPosition, accuracyInches = 0.2, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.PreDeposit.degrees, requiredForCompletion = false),
                    nextTaskIteration = ::lineUp
            ),
            /** Deposit */
            AutoTask(
                    ChassisTask(depositPosition, accuracyInches = 0.2, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.Deposit.degrees, requiredForCompletion = false),
                    OtherTask(action= {
                        val isFourBarPastTarget = fourBar.current4BarDegrees() > Depositor.FourBarDegrees.Deposit.degrees - 10
                        isFourBarPastTarget
                    }, requiredForCompletion = true),
                    nextTaskIteration = ::lineUp,
                    timeoutSeconds = 1.0
            ),
            AutoTask(
                    ChassisTask(depositPosition, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Deposit.degrees, requiredForCompletion = false),
                    OtherTask(action= {
                        hardware.collector.power = -0.9
                        !depositor.isConeInCollector()
                    }, requiredForCompletion = true)
            ),
            AutoTask(
                    ChassisTask(depositPosition, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.PreDeposit.degrees, requiredForCompletion = false),
//                    OtherTask(action= {
//                        hardware.collector.power = -0.9
//                        !depositor.isConeInCollector()
//                    }, requiredForCompletion = true)
            ),
    )
    private fun lineUp(previousTask: AutoTask): AutoTask {
        val targetAngle = movement.localizer.currentPositionAndRotation().r - junctionAimer.getAngleFromPole()
        multipleTelemetry.addLine("angleFromPole: ${junctionAimer.getAngleFromPole()}")
        multipleTelemetry.addLine("targetAngle: $targetAngle")

        val currentPosition = previousTask.chassisTask.targetPosition
        val newPosition = currentPosition + PositionAndRotation(r= targetAngle)
        multipleTelemetry.addLine("position: $newPosition")


        return previousTask.copy(chassisTask = previousTask.chassisTask.copy(targetPosition= newPosition))
    }
    private val depositPreload = listOf(
            AutoTask(
                    ChassisTask(PositionAndRotation(x= 0.0, y= -49.0, r= 0.0), power= 0.0..0.65, accuracyInches = midPointAccuracy, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, accuracyCounts = 500, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.PreDeposit.degrees, requiredForCompletion = false)
            )) + depositRoutine + listOf(
            AutoTask(
                    ChassisTask(depositPosition, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = true),
                    OtherTask(action= {
                        hardware.collector.power = 0.0
                        true
                    }, requiredForCompletion = false)
            ),
            AutoTask(
                    ChassisTask(PositionAndRotation(x= -4.0, y= -52.0, r= 45.0), requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.StackPreCollection.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false)
            )
    )
    private val collectionY = -51.5
    private val cycleMidPoint = PositionAndRotation(x = 7.0, y = collectionY, r = 90.0)
    private val preCollectionPosition = PositionAndRotation(x = 20.0, y = collectionY, r = 90.0)
    private val collectionPosition = PositionAndRotation(x = 30.5, y = collectionY, r = 80.0)
    private val cycle = listOf(
            /** Prepare to collect */
            AutoTask(
                    ChassisTask(cycleMidPoint, accuracyInches= midPointAccuracy, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.StackPreCollection.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Collecting.degrees, requiredForCompletion = false)
            ),
            AutoTask(
                    ChassisTask(preCollectionPosition, accuracyInches= 2.0, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.StackPreCollection.counts, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.Collecting.degrees, accuracyDegrees = 6.0, requiredForCompletion = true),
                    OtherTask(action= {
                        hardware.funnelLifter.position = collector.funnelDown
                        true
                    }, requiredForCompletion = false),
//                    timeoutSeconds = 5.0
            ),
            /** Collecting */
            AutoTask(
                    ChassisTask(collectionPosition, power = 0.0..0.2, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.StackPreCollection.counts, accuracyCounts = 100, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Collecting.degrees, requiredForCompletion = false),
                    OtherTask(action= {
                        hardware.collector.power = 0.7
                        coneCollectionTime = null
                        depositor.isConeInFunnel()
                    }, requiredForCompletion = true),
                    timeoutSeconds = 5.0
            ),
            AutoTask(
                    ChassisTask(collectionPosition, power = 0.0..0.0, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.Collection.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Collecting.degrees, requiredForCompletion = false),
                    OtherTask(action= {
                        hardware.collector.power = 1.0
                        if (coneCollectionTime == null)
                            coneCollectionTime = System.currentTimeMillis()
                        val isColeCurrentlyInCollector = depositor.isConeInCollector()
                        val areWeDoneCollecting = System.currentTimeMillis() - coneCollectionTime!! >= timeToFinishCollectingMilis
                        isColeCurrentlyInCollector && areWeDoneCollecting
                    }, requiredForCompletion = true),
                    timeoutSeconds = 2.0
            ),
            AutoTask(
                    ChassisTask(collectionPosition, power = 0.0..0.0, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.StackPreCollection.counts+300, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.Collecting.degrees, requiredForCompletion = false),
                    OtherTask(action= {
                        hardware.collector.power = 0.05
                        true
                    }, requiredForCompletion = false)
            ),
            /** Drive to pole */
            AutoTask(
                    ChassisTask(cycleMidPoint, power = 0.0..0.85, accuracyInches= midPointAccuracy, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.StackPreCollection.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false),
                    OtherTask(action= {
                        hardware.funnelLifter.position = collector.funnelUp
                        true
                    }, requiredForCompletion = false)
            )) + depositRoutine + listOf(
            /** Return to midpoint */
            AutoTask(
                    ChassisTask(cycleMidPoint, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false),
                    OtherTask(action= {
                        hardware.collector.power = 0.0
                        fourBar.current4BarDegrees() <= Depositor.FourBarDegrees.Vertical.degrees - 10
                    }, requiredForCompletion = false)
            ),
            AutoTask(
                    ChassisTask(cycleMidPoint, accuracyInches= midPointAccuracy, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.StackPreCollection.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Collecting.degrees, requiredForCompletion = false)
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
    private val zeroLift = {
        val isLimitPressed = !hardware.liftLimitSwitch.state
        if (!isLimitPressed)
            depositor.powerLift(-0.05)
        else
            depositor.powerLift(0.0)
        isLimitPressed
    }
    private val parkTimeout = 28.0
    private val parkOne: List<AutoTask> = listOf(
            AutoTask(
                    ChassisTask(ParkPositions.One.pos, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false),
                    OtherTask(action= compensateForAbruptEnd, requiredForCompletion = false),
                    startDeadlineSeconds = parkTimeout
            ),
            AutoTask(
                    ChassisTask(ParkPositions.One.pos, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false),
                    OtherTask(action= zeroLift, requiredForCompletion = true)))
    private val parkTwo: List<AutoTask> = listOf(
            AutoTask(
                    ChassisTask(ParkPositions.Two.pos, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false),
                    OtherTask(action= compensateForAbruptEnd, requiredForCompletion = false),
                    startDeadlineSeconds = parkTimeout
            ),
            AutoTask(
                    ChassisTask(ParkPositions.Two.pos, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false),
                    OtherTask(action= zeroLift, requiredForCompletion = true)))
    private val parkThree: List<AutoTask> = listOf(
            AutoTask(
                    ChassisTask(ParkPositions.Three.pos, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false),
                    OtherTask(action= compensateForAbruptEnd, requiredForCompletion = false),
                    startDeadlineSeconds = parkTimeout),
            AutoTask(
                    ChassisTask(ParkPositions.Three.pos, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false),
                    OtherTask(action= zeroLift, requiredForCompletion = true)))

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
        wizard.newMenu("program", "Which auto are we starting?", listOf("Cycle Auto" to "cycles", "Park Auto" to null))
        wizard.newMenu("cycles", "How many cycles are we doing?", listOf("1+4", "1+3", "1+2", "1+1", "1+0"),"startPos")
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
    private lateinit var currentTask: AutoTask
//    private lateinit var autoTaskIterator: ListIterator<AutoTask>
    override fun start() {
        this.resetRuntime()
        hardware.rightOdomEncoder.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        hardware.leftOdomEncoder.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        hardware.centerOdomEncoder.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        hardware.leftOdomEncoder.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        hardware.leftLift.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        val aprilTagGXOutput =  aprilTagGX.signalOrientation ?: SignalOrientation.Three
        aprilTagGX.camera?.closeCameraDevice()
        aprilTagGX.camera?.stopRecordingPipeline()
//        aprilTagGX.camera?.stopStreaming()
        aprilTagGX.camera?.closeCameraDeviceAsync{}

        val opencv = OpenCvAbstraction(this)
        junctionAimer.start(opencv, hardwareMap)

        val side = if (wizard.wasItemChosen("startPos", "Left"))
            FieldSide.Left
        else
            FieldSide.Right

//        autoTasks = depositPreload + cycles + cycles + parkTwo
        val numberOfCycles = when {
            wizard.wasItemChosen("cycles", "1+0") -> 0
            wizard.wasItemChosen("cycles", "1+1") -> 1
            wizard.wasItemChosen("cycles", "1+2") -> 2
            wizard.wasItemChosen("cycles", "1+3") -> 3
            else -> 4
        }

//        autoTasks = makePlanForAuto(
//            signalOrientation = aprilTagGXOutput,
//            fieldSide = side,
//            numberOfCycles = numberOfCycles)
        autoTasks = listOf(
                AutoTask(
                        ChassisTask(PositionAndRotation(x= 0.0, y= -49.0, r= 0.0), power= 0.0..0.65, accuracyInches = midPointAccuracy, requiredForCompletion = true),
                        LiftTask(Depositor.LiftCounts.Bottom.counts, accuracyCounts = 500, requiredForCompletion = false),
                        FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false)
                ),
                AutoTask(
                        ChassisTask(depositPosition, accuracyInches = 2.0, requiredForCompletion = true),
                        LiftTask(Depositor.LiftCounts.Detection.counts, accuracyCounts = 700, requiredForCompletion = true),
                        FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false)
                ),
                AutoTask(
                        ChassisTask(depositPosition, power= 0.0..0.2, accuracyInches = 0.2, requiredForCompletion = true),
                        LiftTask(Depositor.LiftCounts.Detection.counts, requiredForCompletion = true),
                        FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false),
                        nextTaskIteration = ::lineUp
                ))

        currentTask = getTask(null)
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
    private fun makePlanForAuto(signalOrientation:SignalOrientation, fieldSide:FieldSide, numberOfCycles: Int):List<AutoTask> {
        val parkPath = when (signalOrientation) {
            SignalOrientation.One -> parkOne
            SignalOrientation.Two -> parkTwo
            SignalOrientation.Three -> parkThree
        }
        val cycles = when (numberOfCycles) {
            0 -> listOf()
            1 -> cycle
            2 -> cycle + cycle
            3 -> cycle + cycle + cycle
            else -> {cycle + cycle + cycle + cycle}
        }

        return flopped(depositPreload + cycles, fieldSide) + parkPath
    }

    private fun PaddieMatrickAuto.flopped(coreTasks: List<AutoTask>, side: FieldSide): List<AutoTask> {
        val swichedTasks = when (side) {
            FieldSide.Left -> flopToLeftSide(coreTasks)
            FieldSide.Right -> coreTasks
        }
        return swichedTasks
    }

    private lateinit var taskListIterator: ListIterator<AutoTask>
    private fun getTask(previousTask: AutoTask?): AutoTask {
        if (previousTask != null) {

            val timeSinceTaskStart = getEffectiveRuntime() - (previousTask.timeStartedSeconds
                    ?: (getEffectiveRuntime() + 1))
            val pastOrAtTaskTimeout: Boolean = if (previousTask.timeoutSeconds != null) previousTask.timeoutSeconds <= timeSinceTaskStart else false


            return if ((previousTask.isFinished() || pastOrAtTaskTimeout) && taskListIterator.hasNext()) {
                previousTask.taskStatus = TaskStatus.Completed
                taskListIterator.next()
            } else {
                previousTask.nextTaskIteration(previousTask)
            }

        } else {
            taskListIterator = autoTasks.listIterator()
            return taskListIterator.next()
        }
    }

    private var prevTime = System.currentTimeMillis()
    override fun loop() {

        /** AUTONOMOUS  PHASE */
        val dt = System.currentTimeMillis() - prevTime
        prevTime = System.currentTimeMillis()
        multipleTelemetry.addLine("dt: $dt")
        multipleTelemetry.addLine("runtime: ${getEffectiveRuntime()}")

        val nextDeadlinedTask = findNextDeadlinedTask(autoTasks)
        val nextDeadlineSeconds = nextDeadlinedTask?.startDeadlineSeconds
        val atOrPastDeadline = if (nextDeadlineSeconds != null) nextDeadlineSeconds <= getEffectiveRuntime() else false

        currentTask = if (atOrPastDeadline) {
//            multipleTelemetry.addLine("skipping ahead to deadline")
//            failSkippedTasks(nextDeadlinedTask!!, autoTasks)

            nextDeadlinedTask!!
        } else {
            getTask(currentTask)
        }


        if (currentTask.taskStatus != TaskStatus.Running) {
            currentTask.timeStartedSeconds = getEffectiveRuntime()
        }
        currentTask.taskStatus = TaskStatus.Running
        multipleTelemetry.addLine("timeStartedSeconds: ${currentTask.timeStartedSeconds}")

//        multipleTelemetry.addLine("\n\ncurrent task: $currentTask")

        val chassisTask = currentTask.chassisTask
        movement.precisionInches = chassisTask.accuracyInches
        val isChassisTaskCompleted = movement.moveTowardTarget(chassisTask.targetPosition, chassisTask.power)

        val liftTask = currentTask.liftTask
        depositor.accuracy = liftTask.accuracyCounts
        val isLiftTaskCompleted = depositor.moveLift(liftTask.targetCounts)

        val fourBarTask = currentTask.fourBarTask
        fourBar.accuracyDegrees = fourBarTask.accuracyDegrees
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
            if (chassisTask.targetPosition == depositPosition) {
                val message = "Robot has reached target: (x= ${depositPosition.x}, y= ${depositPosition.y} r= ${depositPosition.r}). current position is: (x= ${movement.localizer.currentPositionAndRotation().x}, y= ${movement.localizer.currentPositionAndRotation().y} r= ${movement.localizer.currentPositionAndRotation().r})"
                telemetry.addLine(message)
//                dashboard.telemetry.addData("Robot has reached target: ", depositPosition)
//                dashboard.telemetry.addData("current position is: ", movement.localizer.currentPositionAndRotation())
                print(message)
            }
            currentTask.taskStatus = TaskStatus.Completed
            currentTask.timeFinishedSeconds = getEffectiveRuntime()
        }
    }


    fun getEffectiveRuntime() = this.runtime

    data class AutoTask(val chassisTask: ChassisTask,
                        val liftTask: LiftTask,
                        val fourBarTask: FourBarTask,
                        val subassemblyTask: OtherTask? = null,
                        val nextTaskIteration: (AutoTask) -> AutoTask = {it},
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
            val accuracyInches: Double = 0.5,
            override val requiredForCompletion: Boolean): SubassemblyTask(requiredForCompletion)
    data class LiftTask(val targetCounts: Int,
                        val accuracyCounts: Int = 300,
                        override val requiredForCompletion: Boolean): SubassemblyTask(requiredForCompletion)
    data class FourBarTask(val targetDegrees: Double,
                           val accuracyDegrees: Double = 5.0,
                           override val requiredForCompletion: Boolean): SubassemblyTask(requiredForCompletion)
    data class OtherTask(val action: ()->Boolean,
                         override val requiredForCompletion: Boolean): SubassemblyTask(requiredForCompletion)

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