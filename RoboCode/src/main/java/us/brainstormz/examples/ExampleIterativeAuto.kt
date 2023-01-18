package us.brainstormz.examples

import us.brainstormz.IterativeOpMode
import us.brainstormz.hardwareClasses.HardwareClass

//@Autonomous
class ExampleIterativeAuto/** Change depending on robot */: IterativeOpMode() {
    override var hardware: HardwareClass = ExampleHardware()/** Change depending on robot */

    /** AUTONOMOUS TASKS */
    override var autoTasks: List<AutoTask> = listOf(
            AutoTask(subassemblyTasks = listOf(
                    MyTask(target = 1, requiredForCompletion = true)
            ))
    )

    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
    }

    /** Declare new task types for your subassemblies */
    class MyTask(val target: Int, override val requiredForCompletion: Boolean): SubassemblyTask(requiredForCompletion) {
        override val action: () -> Boolean = {
            val isDone = 10 > target
            isDone
        }
    }

}