package us.brainstormz.paddieMatrick

import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.localizer.PositionAndRotation

class AutoTaskManager {

    private lateinit var taskListIterator: ListIterator<AutoTask>
    private fun getTask(previousTask: AutoTask?, autoTasks: List<AutoTask>, effectiveRuntimeSeconds: Double, telemetry: Telemetry): AutoTask {
        if (previousTask != null) {

            val timeSinceTaskStart = effectiveRuntimeSeconds - (previousTask.timeStartedSeconds
                    ?: (effectiveRuntimeSeconds + 1))
            val pastOrAtTaskTimeout: Boolean = if (previousTask.timeoutSeconds != null) previousTask.timeoutSeconds <= timeSinceTaskStart else false


            return if ((previousTask.isFinished() || pastOrAtTaskTimeout) && taskListIterator.hasNext()) {
                previousTask.taskStatus = TaskStatus.Completed
                taskListIterator.next()
            } else {
                previousTask.nextTaskIteration(previousTask)
            }

        } else {
            telemetry.addLine("Current task is null")
            telemetry.update()
            taskListIterator = autoTasks.listIterator()
            return taskListIterator.next()
        }
    }


    private var prevTime = System.currentTimeMillis()
    private var currentTask: AutoTask? = null
    fun loop(telemetry: Telemetry, autoTasks: List<AutoTask>, effectiveRuntimeSeconds: Double, isChassisTaskCompleted: (ChassisTask) -> Boolean, isLiftTaskCompleted: (LiftTask) -> Boolean, isFourBarTaskCompleted: (FourBarTask) -> Boolean, isOtherTaskCompleted: (OtherTask) -> Boolean) {
        /** AUTONOMOUS  PHASE */
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
            getTask(currentTask, autoTasks, effectiveRuntimeSeconds, telemetry)
        }
        telemetry.addLine("Current task is : $currentTask")
        telemetry.update()


        if (currentTask!!.taskStatus != TaskStatus.Running) {
            currentTask!!.timeStartedSeconds = effectiveRuntimeSeconds
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
            currentTask!!.taskStatus = TaskStatus.Completed
            currentTask!!.timeFinishedSeconds = effectiveRuntimeSeconds
        }
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
    }

    open class SubassemblyTask(open val requiredForCompletion: Boolean)
    data class ChassisTask(
            val targetPosition: PositionAndRotation,
            val power: ClosedRange<Double> = 0.0..1.0,
            val accuracyInches: Double = 0.5,
            val accuracyDegrees: Double = 5.0,
            override val requiredForCompletion: Boolean): SubassemblyTask(requiredForCompletion)
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