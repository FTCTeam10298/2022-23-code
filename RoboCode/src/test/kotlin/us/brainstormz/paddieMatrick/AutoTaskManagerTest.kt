package us.brainstormz.paddieMatrick

import org.firstinspires.ftc.robotcore.external.Telemetry
import org.junit.Assert
import org.junit.Test
import us.brainstormz.localizer.PhoHardware
import us.brainstormz.localizer.PositionAndRotation

class AutoTaskManagerTest {
    @Test
    fun `completes deadline tasks`(){
        // given
        var otherTaskWasDelivered = false
        var numTimesOtherTaskRun = 0
        val otherTask = AutoTaskManager.AutoTask(
            chassisTask =  AutoTaskManager.ChassisTask(targetPosition = PositionAndRotation(), requiredForCompletion = false),
            liftTask = AutoTaskManager.LiftTask(targetCounts = 0, requiredForCompletion = false),
            fourBarTask = AutoTaskManager.FourBarTask(targetDegrees = 0.0, requiredForCompletion = false),
            nextTaskIteration = {
                println("I ran too")
                numTimesOtherTaskRun += 1
                it
            }
        )

        val tasks = listOf(
            AutoTaskManager.AutoTask(
                chassisTask =  AutoTaskManager.ChassisTask(targetPosition = PositionAndRotation(), requiredForCompletion = false),
                liftTask = AutoTaskManager.LiftTask(targetCounts = 0, requiredForCompletion = false),
                fourBarTask = AutoTaskManager.FourBarTask(targetDegrees = 0.0, requiredForCompletion = false),
                nextTaskIteration = {
                    println("first ran")
                    it
                }
            ),
            AutoTaskManager.AutoTask(
                chassisTask =  AutoTaskManager.ChassisTask(targetPosition = PositionAndRotation(), requiredForCompletion = false),
                liftTask = AutoTaskManager.LiftTask(targetCounts = 0, requiredForCompletion = false),
                fourBarTask = AutoTaskManager.FourBarTask(targetDegrees = 0.0, requiredForCompletion = false),
                startDeadlineSeconds = 2.0,
                nextTaskIteration = {
                    println("I ran")
                    otherTaskWasDelivered = true
                    otherTask
                }
            )
        )
        val testSubject = AutoTaskManager()
        val telemetry = object:PhoHardware.PhoTelemetry(){
            override fun addLine(lineCaption: String?): Telemetry.Line{
                println("[telemetry] $lineCaption")
                return super.addLine(lineCaption)
            }
        }

        data class CompletionState (
            val isChassisTaskCompleted:Boolean = false,
            val isLiftTaskCompleted:Boolean = false,
            val isFourBarTaskCompleted:Boolean = false
        )

        val foo = listOf(
            1.0 to CompletionState(),
            2.0 to CompletionState(),
            3.0 to CompletionState(),
            4.0 to CompletionState(),
            5.0 to CompletionState(),
            6.0 to CompletionState()
        )

        // when
        foo.forEach{(time, completionstate) ->
            println("Executing item @$time")
            testSubject.loop(
                telemetry = telemetry,
                autoTasks = tasks,
                effectiveRuntimeSeconds = time,
                isLiftTaskCompleted = {completionstate.isLiftTaskCompleted},
                isChassisTaskCompleted = {completionstate.isChassisTaskCompleted},
                isFourBarTaskCompleted = {completionstate.isFourBarTaskCompleted},
                isOtherTaskCompleted = { otherTask -> otherTask.isDone()}
            )
        }

        // then
        Assert.assertTrue(otherTaskWasDelivered)
        Assert.assertEquals(3, numTimesOtherTaskRun)
    }
}