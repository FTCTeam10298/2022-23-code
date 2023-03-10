package us.brainstormz.paddieMatrick

import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.pid.PID

class AutoTaskManager {

    private lateinit var taskListIterator: ListIterator<AutoTask> // FIXME: Only initialized on line 28?  Potential use without init?
    fun getTask(previousTask: AutoTask?, autoTasks: List<AutoTask>, effectiveRuntimeSeconds: Double, telemetry: Telemetry): AutoTask {
        if (previousTask != null) {

            val timeSinceTaskStart = effectiveRuntimeSeconds - (previousTask.timeStartedSeconds
                    ?: (effectiveRuntimeSeconds + 1))
            val pastOrAtTaskTimeout: Boolean = if (previousTask.timeoutSeconds != null) previousTask.timeoutSeconds <= timeSinceTaskStart else false

            if (!taskListIterator.hasNext()) {telemetry.addLine("No remaining tasks!!!")}
            return if ((previousTask.isFinished() || pastOrAtTaskTimeout) && taskListIterator.hasNext()) {
                previousTask.taskStatus = TaskStatus.Completed // FIXME: Dupe of line 95?
                taskListIterator.next() // FIXME: THIS LIKELY ISN'T GETTING CALLED WHILE IN WEIRD STUCK STATE
            } else {
                previousTask.nextTaskIteration(previousTask)
            }

        } else {
            telemetry.addLine("Current task is null")
            taskListIterator = autoTasks.listIterator()
            return taskListIterator.next()
        }
    }


    private var prevTime = System.currentTimeMillis()
    private var currentTask: AutoTask? = null
    fun loop(
        telemetry: Telemetry,
        autoTasks: List<AutoTask>,
        getNextTask: (previousTask: AutoTask?, autoTasks: List<AutoTask>, effectiveRuntimeSeconds: Double, telemetry: Telemetry)->AutoTask = this::getTask,
        effectiveRuntimeSeconds: Double,
        isChassisTaskCompleted: (ChassisTask) -> Boolean,
        isLiftTaskCompleted: (LiftTask) -> Boolean,
        isFourBarTaskCompleted: (FourBarTask) -> Boolean,
        isOtherTaskCompleted: (OtherTask) -> Boolean
    ) {
        val dt = System.currentTimeMillis() - prevTime
        prevTime = System.currentTimeMillis()
        telemetry.addLine("dt: $dt")

        val nextDeadlinedTask = findNextDeadlinedTask(autoTasks)
        val nextDeadlineSeconds = nextDeadlinedTask?.startDeadlineSeconds
        val atOrPastDeadline = if (nextDeadlineSeconds != null) nextDeadlineSeconds <= effectiveRuntimeSeconds else false

        currentTask = if (atOrPastDeadline) {
//            multipleTelemetry.addLine("skipping ahead to deadline")
//            failSkippedTasks(nextDeadlinedTask!!, autoTasks)

            nextDeadlinedTask!!
        } else {
            getNextTask(currentTask, autoTasks, effectiveRuntimeSeconds, telemetry)
        }


        if (currentTask!!.taskStatus != TaskStatus.Running) {
            currentTask!!.timeStartedSeconds = effectiveRuntimeSeconds // Note: This value is being updated when stuck in the Completed task state
        }
        currentTask!!.taskStatus = TaskStatus.Running
        telemetry.addLine("timeStartedSeconds: ${currentTask!!.timeStartedSeconds}")

        val chassisTask = currentTask!!.chassisTask
        val liftTask = currentTask!!.liftTask
        val fourBarTask = currentTask!!.fourBarTask
        val subassemblyTask = currentTask!!.subassemblyTask

        val isSubassemblyTaskCompleted: (OtherTask?) -> Boolean = { nullableSubassemblyTask ->
            nullableSubassemblyTask?.let {subassemblyTask ->
                isOtherTaskCompleted(subassemblyTask)
            } ?: true
        }

        val completions = listOf(
                chassisTask to isChassisTaskCompleted(chassisTask),
                liftTask to isLiftTaskCompleted(liftTask),
                fourBarTask to isFourBarTaskCompleted(fourBarTask),
                subassemblyTask to isSubassemblyTaskCompleted(subassemblyTask))

        val isTaskCompleted: Boolean = completions.fold(true) { prevTasksCompleted, completion ->
            if (completion.first?.requiredForCompletion == true)
                prevTasksCompleted && completion.second!!
            else
                prevTasksCompleted
        }

        if (isTaskCompleted) {
            currentTask!!.taskStatus = TaskStatus.Completed // FIXME: Dupe of line 20?  Possible race condition?
            currentTask!!.timeFinishedSeconds = effectiveRuntimeSeconds // Note: This value is being updated when stuck in the Completed task state
        }

        telemetry.addLine("Current task is : $currentTask")
    }

    data class AutoTask(val chassisTask: ChassisTask,
                        val liftTask: LiftTask,
                        val fourBarTask: FourBarTask,
                        val subassemblyTask: OtherTask? = null,
                        val nextTaskIteration: (AutoTask) -> AutoTask = {it},
                        val startDeadlineSeconds: Double? = null /* time after start by which this is guaranteed to start */,
                        val timeoutSeconds: Double? = null /* time after which the task will be skipped if not already complete */,
                        var taskStatus: TaskStatus = TaskStatus.Todo,
                        var timeStartedSeconds: Double? = null,
                        var timeFinishedSeconds: Double? = null,
                        val extraState:Any? = null) {
        fun isFinished() = taskStatus == TaskStatus.Completed || taskStatus == TaskStatus.Failed
        override fun toString(): String =
                "Auto task: \n" +
                "   Chassis task: $chassisTask\n" +
                "   Lift task: $liftTask\n" +
                "   Four-bar task: $fourBarTask\n" +
                "   Other task: $subassemblyTask\n" +
//                "   Has next task iteration: \n" +
                "   Start deadline (seconds): $startDeadlineSeconds\n" +
                "   Timeout (seconds): $timeoutSeconds\n" +
                "   Time started (seconds): $timeStartedSeconds\n" +
                "   Time finished (seconds): $timeFinishedSeconds\n" +
                "   Extra state: $extraState\n" +
                "   Task status: $taskStatus\n"
    }

    open class SubassemblyTask(open val requiredForCompletion: Boolean)
    data class ChassisTask(
            val targetPosition: PositionAndRotation,
            val rotationalPID: PID = MecanumMovement.defaultRotationPID,
            val xTranslationPID: PID = MecanumMovement.defaultXTranslationPID,
            val power: ClosedRange<Double> = 0.0..1.0,
            val accuracyInches: Double = 0.5,
            val accuracyDegrees: Double = 1.0,
            override val requiredForCompletion: Boolean): SubassemblyTask(requiredForCompletion) {
//                fun copy(
//                        targetPosition: PositionAndRotation = this.targetPosition,
//                        power: ClosedRange<Double> = this.power,
//                        accuracyInches: Double = this.accuracyInches,
//                        accuracyDegrees: Double = this.accuracyDegrees,
//                        requiredForCompletion: Boolean = this.requiredForCompletion
//                        ): ChassisTask {
//                    return ChassisTask(
//                            targetPosition= targetPosition,
//                            power= power,
//                            accuracyInches= accuracyInches,
//                            accuracyDegrees= accuracyDegrees,
//                            requiredForCompletion= requiredForCompletion
//                    )
//                }
            }
    data class LiftTask(val targetCounts: Int,
                        val accuracyCounts: Int = 300,
                        override val requiredForCompletion: Boolean): SubassemblyTask(requiredForCompletion)
    data class FourBarTask(val targetDegrees: Double,
                           val accuracyDegrees: Double = 5.0,
                           override val requiredForCompletion: Boolean): SubassemblyTask(requiredForCompletion)
    data class OtherTask(val isDone: ()->Boolean,
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
    private fun failSkippedTasks(deadlinedTask: AutoTask, tasks: List<AutoTask>, effectiveRuntimeSeconds: Double) {
        tasks.forEach { task ->
            if (task != deadlinedTask && !task.isFinished()) {
                task.taskStatus = TaskStatus.Failed
                task.timeFinishedSeconds = effectiveRuntimeSeconds
            }
        }
    }
}