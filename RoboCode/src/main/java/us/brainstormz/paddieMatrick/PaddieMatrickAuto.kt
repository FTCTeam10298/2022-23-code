package us.brainstormz.paddieMatrick

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.openftc.easyopencv.OpenCvCamera
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.StackDetector
import us.brainstormz.localizer.StackDetectorVars
import us.brainstormz.localizer.polePosition.creamsicleGoalDetection.CreamsicleGoalDetector
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.motion.RRLocalizer
import us.brainstormz.openCvAbstraction.PipelineAbstraction
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import us.brainstormz.paddieMatrick.AutoTaskManager.AutoTask
import us.brainstormz.paddieMatrick.AutoTaskManager.ChassisTask
import us.brainstormz.paddieMatrick.AutoTaskManager.LiftTask
import us.brainstormz.paddieMatrick.AutoTaskManager.FourBarTask
import us.brainstormz.paddieMatrick.AutoTaskManager.OtherTask

enum class Alliance {
    Red,
    Blue
}

@Autonomous(name= "PaddieMatrick Auto", group= "!")
class PaddieMatrickAuto: OpMode() {
    private val hardware = PaddieMatrickHardware()

    private val console = TelemetryConsole(telemetry)
    private val wizard = TelemetryWizard(console, null)
    val dashboard = FtcDashboard.getInstance()
    val multipleTelemetry = telemetry//MultipleTelemetry(telemetry, dashboard.telemetry)

    var aprilTagGX = AprilTagEx()

    private lateinit var liftCam: OpenCvCamera
    private val junctionDetector = CreamsicleGoalDetector(console)
    private val junctionAimer = JunctionAimer(junctionDetector)

    private lateinit var backCam: OpenCvCamera
    private lateinit var stackAimer: StackAimer

    private lateinit var movement: MecanumMovement

    private val funnel = Funnel()
    private lateinit var collector: Collector
    private lateinit var fourBar: FourBar
    private lateinit var depositor: Depositor

    private var coneCollectionTime:Long? = null
    private var timeToFinishCollectingMilis = 0.15 * 1000

    /** Auto Tasks */
    private val midPointAccuracy = 2.5
    private var depositPosition = PositionAndRotation(x= -5.0, y= -57.5, r= 45.0)
    private val depositRoutine = listOf(
            /** Lineup */
            AutoTask(
                    ChassisTask(depositPosition, accuracyInches = 2.0, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Detection.counts, accuracyCounts = 700, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false)
            ),
            AutoTask(
                    ChassisTask(depositPosition, power= 0.0..0.2, rotationalPID = MecanumMovement.defaultRotationPID.copy(kp= 0.85) , accuracyInches = 0.4, accuracyDegrees = 2.0, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Detection.counts, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false),
                    nextTaskIteration = ::alignWithPole,
                    timeoutSeconds = 5.0
            ),
            /** Deposit */
            AutoTask(
                    ChassisTask(depositPosition, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, accuracyCounts = 500, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.DepositTarget.degrees, requiredForCompletion = false),
                    OtherTask(isDone= {
                        hardware.collector.power = 0.2
                        val isFourBarPastTarget = fourBar.current4BarDegrees() > Depositor.FourBarDegrees.DepositOk.degrees
                        isFourBarPastTarget
                    }, requiredForCompletion = true),
                    nextTaskIteration = ::stayLinedUpWithPole,
                    timeoutSeconds = 2.0
            ),
            AutoTask(
                    ChassisTask(depositPosition, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.DepositTarget.degrees, requiredForCompletion = false),
                    OtherTask(isDone= {
                        hardware.collector.power = -0.9
                        !depositor.isConeInCollector()
                    }, requiredForCompletion = true),
                    nextTaskIteration = ::stayLinedUpWithPole,
                    timeoutSeconds = 2.0
            ),
    )
    private val preloadDeposit = listOf(
            AutoTask(
                    ChassisTask(PositionAndRotation(x= 0.0, y= -49.0, r= 0.0), power= 0.0..0.65, accuracyInches = midPointAccuracy, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, accuracyCounts = 500, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.PreDeposit.degrees, requiredForCompletion = false)
            )) + depositRoutine + listOf(
            AutoTask(
                    ChassisTask(depositPosition, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = true),
                    OtherTask(isDone= {
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

    private val collectionY = -51.0
    private val cycleMidPoint = PositionAndRotation(x = 7.0, y = collectionY, r = 90.0)
    private val preCollectionPosition = PositionAndRotation(x = 20.0, y = collectionY, r = 90.0)

    private val collectionPosition = PositionAndRotation(x = 24.0, y = collectionY, r =  90.0)
    private var actualCollectionX = collectionPosition.x

    private var alignToStackStartTimeMilis: Long? = null
    private var prelinupCorrection:PositionAndRotation? = null

    private val cyclePreLinup = listOf(
            /** Prepare to collect */
            AutoTask(
                ChassisTask(cycleMidPoint, accuracyInches= midPointAccuracy, requiredForCompletion = true),
                LiftTask(Depositor.LiftCounts.StackPreCollection.counts, requiredForCompletion = false),
                FourBarTask(Depositor.FourBarDegrees.StackCollecting.degrees, requiredForCompletion = false),
                OtherTask(isDone= {
                    alignToStackStartTimeMilis = null
                    true
                }, requiredForCompletion = false)
            ),
            AutoTask(
                ChassisTask(cycleMidPoint, accuracyInches= midPointAccuracy, requiredForCompletion = false),
                LiftTask(Depositor.LiftCounts.StackPreCollection.counts, requiredForCompletion = false),
                FourBarTask(Depositor.FourBarDegrees.StackCollecting.degrees, requiredForCompletion = false),
                OtherTask(isDone = {
                    if (alignToStackStartTimeMilis == null) {
                        alignToStackStartTimeMilis = System.currentTimeMillis()
                    }

                    val timeSinceStart = System.currentTimeMillis() - alignToStackStartTimeMilis!!

                    timeSinceStart > 1000
                }, requiredForCompletion = true),
                nextTaskIteration= ::alignToStack,
    //                timeoutSeconds = 2.0
            ),
//            AutoTask(
//                ChassisTask(cycleMidPoint, accuracyInches= midPointAccuracy, requiredForCompletion = false),
//                LiftTask(Depositor.LiftCounts.StackPreCollection.counts, requiredForCompletion = false),
//                FourBarTask(Depositor.FourBarDegrees.StackCollecting.degrees, requiredForCompletion = false),
//                OtherTask(isDone = {
//                    telemetry.addLine("prelinupCorrection: $prelinupCorrection")
//                    false
//                }, requiredForCompletion = true),
//            ),
            AutoTask(
                    ChassisTask(preCollectionPosition, accuracyInches= 2.0, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.StackPreCollection.counts, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.StackCollecting.degrees, accuracyDegrees = 6.0, requiredForCompletion = true),
                    nextTaskIteration = ::withPrelinupCorrection
            ),
//            AutoTask(
//                    ChassisTask(preCollectionPosition, accuracyInches= 0.2, accuracyDegrees = 1.0, requiredForCompletion = true),
//                    LiftTask(Depositor.LiftCounts.StackPreCollection.counts, requiredForCompletion = false),
//                    FourBarTask(Depositor.FourBarDegrees.StackCollecting.degrees, accuracyDegrees = 6.0, requiredForCompletion = false),
//                    OtherTask(isDone= {
////                        hardware.funnelLifter.position = collector.funnelDown
//                        true
//                    }, requiredForCompletion = false),
//                    nextTaskIteration = ::withPrelinupCorrection
//            )
    )

    private val cycleCollectAndDepo = listOf(
            /** Collecting */
            AutoTask(
                    ChassisTask(collectionPosition, power = 0.0..0.2, accuracyInches= 0.2, accuracyDegrees = 1.0, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.StackPreCollection.counts, accuracyCounts = 100, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.StackCollecting.degrees, requiredForCompletion = false),
                    OtherTask(isDone= {
                        val message = "Collecting, position is ${movement.localizer.currentPositionAndRotation()}"
                        multipleTelemetry.addLine(message)
                        multipleTelemetry.update()

                        actualCollectionX = movement.localizer.currentPositionAndRotation().x

                        hardware.collector.power = 0.7
                        coneCollectionTime = null
                        true
                    }, requiredForCompletion = true),
                    nextTaskIteration = ::withPrelinupCorrection,
                    timeoutSeconds = 3.0
            ),
            AutoTask(
                    ChassisTask(collectionPosition, power = 0.0..0.2, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.Collection.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.StackCollecting.degrees, requiredForCompletion = false),
                    OtherTask(isDone= {
                        hardware.collector.power = 1.0
                        if (coneCollectionTime == null)
                            coneCollectionTime = System.currentTimeMillis()
                        val isConeCurrentlyInCollector = depositor.isConeInCollector()
                        val areWeDoneCollecting = System.currentTimeMillis() - coneCollectionTime!! >= timeToFinishCollectingMilis
                        isConeCurrentlyInCollector && areWeDoneCollecting
                    }, requiredForCompletion = true),
                    nextTaskIteration = ::withCollectionCorrections,
                    timeoutSeconds = 2.0
            ),
            AutoTask(
                    ChassisTask(collectionPosition, power = 0.0..0.2, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.MidJunction.counts, requiredForCompletion = true),
                    FourBarTask(Depositor.FourBarDegrees.StackCollecting.degrees, requiredForCompletion = false),
                    OtherTask(isDone= {
                        hardware.collector.power = 0.05
                        true
                    }, requiredForCompletion = false),
                    nextTaskIteration = ::withCollectionCorrections,
            ),
            /** Drive to pole */
            AutoTask(
                    ChassisTask(cycleMidPoint, power = 0.0..0.85, accuracyInches= midPointAccuracy, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.MidJunction.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false),
                    OtherTask(isDone= {
                        hardware.funnelLifter.position = collector.funnelUp
                        true
                    }, requiredForCompletion = false)
            )) + depositRoutine + listOf(
            /** Return to midpoint */
            AutoTask(
                    ChassisTask(cycleMidPoint, requiredForCompletion = false),
                    LiftTask(Depositor.LiftCounts.HighJunction.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false),
                    OtherTask(isDone= {
                        hardware.collector.power = 0.0
                        fourBar.current4BarDegrees() <= Depositor.FourBarDegrees.PreDeposit.degrees-5
                    }, requiredForCompletion = true)
            ),
            AutoTask(
                    ChassisTask(cycleMidPoint, accuracyInches= midPointAccuracy, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.StackPreCollection.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Collecting.degrees, requiredForCompletion = false)
            )
    )

    private fun alignWithPole(previousTask: AutoTask): AutoTask {
        val targetAngle = movement.localizer.currentPositionAndRotation().r - junctionAimer.getAngleFromPole()
        multipleTelemetry.addLine("angleFromPole: ${junctionAimer.getAngleFromPole()}")
        multipleTelemetry.addLine("targetAngle: $targetAngle")

        val newPosition = previousTask.chassisTask.targetPosition.copy(r=targetAngle)
        multipleTelemetry.addLine("position: $newPosition")

        depositPosition = newPosition
        return previousTask.copy(chassisTask = previousTask.chassisTask.copy(targetPosition= newPosition))
    }

    private fun stayLinedUpWithPole(previousTask: AutoTask): AutoTask {
        return previousTask.copy(chassisTask = previousTask.chassisTask.copy(targetPosition= depositPosition))
    }

    private fun alignToStack(previousTask: AutoTask): AutoTask {
        val inchesFromStack = 29 + movement.localizer.currentPositionAndRotation().x
        val stackInchesFromCentered = stackAimer.getStackInchesFromCenter(distanceFromStack= inchesFromStack)
        val stackInchesY = movement.localizer.currentPositionAndRotation().y - stackInchesFromCentered

        println("inchesFromStack: $inchesFromStack")
        telemetry.addLine("\ninchesFromStack: $inchesFromStack\n")
        telemetry.addLine("stackInchesFromCentered: $stackInchesFromCentered")
        telemetry.addLine("stackInchesY: $stackInchesY")

        val newPosition = previousTask.chassisTask.targetPosition.copy(y=stackInchesY)//, y= targetY)
        telemetry.addLine("position: $newPosition")

        prelinupCorrection = newPosition
        return previousTask
    }

    private fun withPrelinupCorrection(t:AutoTask):AutoTask {
        return (prelinupCorrection?.let {
//            changeTaskTargetPosition(it, previousTask)
            t.copy(
                    chassisTask = t.chassisTask.copy(
                            targetPosition = t.chassisTask.targetPosition.copy(y = it.y)
                    )
            )
        } ?: t)

    }

    fun withCollectionCorrections(t:AutoTask):AutoTask{
        val withPrelinupCorrection = withPrelinupCorrection(t)

        val withActualCollectionX = withPrelinupCorrection.copy(
                chassisTask = withPrelinupCorrection.chassisTask.copy(
                        withPrelinupCorrection.chassisTask.targetPosition.copy(x = actualCollectionX)
                )
        )
        return withActualCollectionX
    }

    private fun changeTaskTargetPosition(newTarget: PositionAndRotation, previousTask: AutoTask): AutoTask =
        previousTask.copy(chassisTask = previousTask.chassisTask.copy(targetPosition = newTarget))

    private fun adjustTargetToCorrectedCollectionRotation(previousTask: AutoTask, correctedCollectionAngle:Double): AutoTask {
        val previousTarget = previousTask.chassisTask.targetPosition
        val correctedPosition = previousTarget.copy(r= correctedCollectionAngle)

        return changeTaskTargetPosition(correctedPosition, previousTask)
    }

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
    private val park = listOf(
            AutoTask(
                    ChassisTask(ParkPositions.One.pos, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false),
                    OtherTask(isDone= compensateForAbruptEnd, requiredForCompletion = false),
                    startDeadlineSeconds = parkTimeout
            ),
            AutoTask(
                    ChassisTask(ParkPositions.One.pos, requiredForCompletion = true),
                    LiftTask(Depositor.LiftCounts.Bottom.counts, requiredForCompletion = false),
                    FourBarTask(Depositor.FourBarDegrees.Vertical.degrees, requiredForCompletion = false),
                    OtherTask(isDone= zeroLift, requiredForCompletion = true)))

    private fun generatePark(signalOrientation: SignalOrientation): List<AutoTask> {
        val parkPosition = when (signalOrientation) {
            SignalOrientation.One -> ParkPositions.One
            SignalOrientation.Two -> ParkPositions.Two
            SignalOrientation.Three -> ParkPositions.Three
        }

        return park.map { task ->
            task.copy(chassisTask= task.chassisTask.copy(targetPosition= parkPosition.pos))
        }
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
    private fun makePlanForAuto(signalOrientation:SignalOrientation, fieldSide:FieldSide, alliance: Alliance, numberOfCycles: Int):List<AutoTask> {
        val parkPath = generatePark(signalOrientation)

        val cycle = cyclePreLinup + cycleCollectAndDepo

        val cycles = when (numberOfCycles) {
            0 -> listOf()
            1 -> cycle
            2 -> cycle + cycle
            3 -> cycle + cycle + cycle
            else -> {cycle + cycle + cycle + cycle}
        }

        return flopped(preloadDeposit + cycles, fieldSide) + parkPath
    }

    private fun flopped(coreTasks: List<AutoTask>, side: FieldSide): List<AutoTask> {
        val swichedTasks = when (side) {
            FieldSide.Left -> flopToLeftSide(coreTasks)
            FieldSide.Right -> coreTasks
        }
        return swichedTasks
    }

    fun startAimerCameras(targetHue: StackDetector.TargetHue) {
        val stackDetectorVars = StackDetectorVars(targetHue, StackDetector.Mode.FRAME)
        val stackDetector = StackDetector(stackDetectorVars, telemetry)
        stackAimer = StackAimer(telemetry, stackDetector)

        val dualCamAbstraction = DualCamAbstraction(hardwareMap)
        val viewportContainerIds = dualCamAbstraction.setupViewport()

        val liftCamName = "liftCam"
        val liftPipeline = PipelineAbstraction()
        liftPipeline.userFun = junctionDetector::scoopFrame
        liftCam = dualCamAbstraction.startNewCamera(
                cameraName = liftCamName,
                cameraRotation = OpenCvCameraRotation.UPSIDE_DOWN,
                viewportContainerId = viewportContainerIds[0],
                pipeline = liftPipeline)

        val backCamName = "backCam"
        val backPipeline = PipelineAbstraction()
        backPipeline.userFun = stackDetector::processFrame
        backCam = dualCamAbstraction.startNewCamera(
                cameraName = backCamName,
                cameraRotation = OpenCvCameraRotation.UPSIDE_DOWN,
                viewportContainerId = viewportContainerIds[1],
                pipeline = backPipeline)
    }

    private var encoderLog:EncoderLog? = null

    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        aprilTagGX.initAprilTag(hardwareMap, telemetry, null)

        val localizer = RRLocalizer(hardware)
        movement = MecanumMovement(localizer, hardware, telemetry)

        funnel.init(hardware.lineSensor, hardware.funnelSensor, hardware.funnelLifter)
        collector = Collector()
        fourBar = FourBar(telemetry)
        fourBar.init(leftServo = hardware.left4Bar, rightServo = hardware.right4Bar, encoder = hardware.encoder4Bar)
        depositor = Depositor(collector = collector, fourBar = fourBar, hardware = hardware, funnel = funnel, telemetry = telemetry)

        wizard.newMenu("alliance", "What alliance are we on?", listOf("Red", "Blue"),"program", firstMenu = true)
        wizard.newMenu("program", "Which auto are we starting?", listOf("Cycle Auto" to "cycles", "Park Auto" to null))
        wizard.newMenu("cycles", "How many cycles are we doing?", listOf("1+0", "1+1", "1+2", "1+3"),"startPos")//listOf("1+4", "1+3", "1+2", "1+1", "1+0"),"startPos")
        wizard.newMenu("startPos", "Which side are we starting?", listOf("Right", "Left"))

//        encoderLog = EncoderLog(hardware)
        encoderLog?.start()
    }

    override fun init_loop() {
        val isWizardDone = wizard.summonWizard(gamepad1)

        if (isWizardDone)
            aprilTagGX.runAprilTag(telemetry)
    }

    private lateinit var autoTasks: List<AutoTask>
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
        aprilTagGX.camera?.closeCameraDeviceAsync{}

        val alliance = if (wizard.wasItemChosen("alliance", "Red")) {
            Alliance.Red
        } else {
            Alliance.Blue
        }

        val targetHue = when (alliance) {
            Alliance.Red -> StackDetector.TargetHue.RED
            Alliance.Blue -> StackDetector.TargetHue.BLUE
        }

        startAimerCameras(targetHue)

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
            else -> 0
        }
        autoTasks = makePlanForAuto(
            signalOrientation = aprilTagGXOutput,
            fieldSide = side,
            alliance= alliance,
            numberOfCycles = numberOfCycles)
    }

    private val autoTaskManager = AutoTaskManager()
    override fun loop() {
        /** AUTONOMOUS  PHASE */

        val botHeading: Double = hardware.imu.robotYawPitchRollAngles.getYaw(AngleUnit.RADIANS)
        multipleTelemetry.addLine("botHeading: $botHeading")

        autoTaskManager.loop(
                telemetry= multipleTelemetry,
                autoTasks= autoTasks,
                effectiveRuntimeSeconds = runtime,
                isChassisTaskCompleted = { chassisTask ->
                    movement.precisionInches = chassisTask.accuracyInches
                    movement.precisionDegrees = chassisTask.accuracyDegrees
                    movement.rotationPID = chassisTask.rotationalPID
                    movement.moveTowardTarget(chassisTask.targetPosition, chassisTask.power)
                },
                isLiftTaskCompleted = { liftTask ->
                    depositor.accuracy = liftTask.accuracyCounts
                    depositor.moveLift(liftTask.targetCounts)
                },
                isFourBarTaskCompleted = { fourBarTask ->
                    fourBar.accuracyDegrees = fourBarTask.accuracyDegrees
                    depositor.moveFourBar(fourBarTask.targetDegrees)
                },
                isOtherTaskCompleted = { otherTask ->
                    otherTask.isDone()
                }
        )

        telemetry.update()
    }

    override fun stop() {
        liftCam.closeCameraDeviceAsync { liftCam.closeCameraDevice() }
        backCam.closeCameraDeviceAsync { backCam.closeCameraDevice() }

        val motorsAndCRServos = hardware.hwMap.getAll(DcMotorSimple::class.java)
        motorsAndCRServos.forEach {
            it.power = 0.0
        }

        encoderLog?.stop()
    }

}
