//package us.brainstormz.lankyKong
//
//import com.qualcomm.hardware.lynx.LynxModule
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
//import com.qualcomm.robotcore.hardware.DcMotor
//import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
//import org.openftc.easyopencv.OpenCvCameraRotation
//import us.brainstormz.hardwareClasses.EncoderDriveMovement
//import us.brainstormz.openCvAbstraction.OpenCvAbstraction
//import us.brainstormz.telemetryWizard.TelemetryWizard
//import us.brainstormz.lankyKong.DepositorLK.DropperPos
//import us.brainstormz.lankyKong.DepositorLK.LiftPos
//import us.brainstormz.telemetryWizard.GlobalConsole
//
//@Autonomous(name= "Lanky Kong Auto", group= "A")
//class LankyKongAuto: LinearOpMode() {
//
//    val console = GlobalConsole.newConsole(telemetry)
//    val wizard = TelemetryWizard(console, this)
//
//    val hardware = LankyKongHardware()
//    val movement = EncoderDriveMovement(hardware, console)
//    lateinit var depo: DepositorLK
//
//    val opencv = OpenCvAbstraction(this)
//    val tseDetector = TseDetectorLK(console)
//
//    var distanceInWarehouse = 0.0
//
//    override fun runOpMode() {
//        hardware.cachingMode = LynxModule.BulkCachingMode.OFF
//        hardware.init(hardwareMap)
//
//        hardwareMap.allDeviceMappings.forEach { m ->
//            println("HW: ${m.deviceTypeClass} ${m.entrySet().map{it.key}.joinToString(",")}")
//        }
//
//        depo = DepositorLK(hardware)
//        depo.runInLinearOpmode(this)
//
//        opencv.init(hardwareMap)
//        opencv.internalCamera = false
//        opencv.cameraName = hardware.cameraName
//        opencv.cameraOrientation = OpenCvCameraRotation.SIDEWAYS_LEFT
//
//        wizard.newMenu("Alliance", "Which alliance are we on?", listOf("Blue", "Red"), "StartPos", firstMenu = true)
//        wizard.newMenu("StartPos", "Which are we closer to?", listOf("Warehouse", "Ducc"))
//        wizard.summonWizard(gamepad1)
//
//        tseDetector.alliance = if (wizard.wasItemChosen("Alliance", "Blue"))
//            AutoTeleopTransitionLK.Alliance.Blue
//        else
//            AutoTeleopTransitionLK.Alliance.Red
//
//        opencv.onNewFrame(tseDetector::processFrame)
//
//        var initBackDistance = 0.0
//        var initFrontDistance = 0.0
//        while(!opModeIsActive()) {
//            initBackDistance = hardware.backDistance.getDistance(DistanceUnit.INCH)
//            initFrontDistance = hardware.frontDistance.getDistance(DistanceUnit.INCH)
//            console.display(2,
//                            "Front Range: $initFrontDistance \nBack Range: $initBackDistance")
//        }
//
//        console.display(1, "Initialization Complete")
//        waitForStart()
//        val autoStartTime = System.currentTimeMillis()
//
//        val tsePosition = tseDetector.position
//        opencv.stop()
//
//        val level: LiftPos = when (tsePosition) {
//            TseDetectorLK.TSEPosition.One -> LiftPos.LowGoal
//            TseDetectorLK.TSEPosition.Two -> LiftPos.MidGoal
//            TseDetectorLK.TSEPosition.Three -> LiftPos.HighGoal
//        }
//
//        /**
//         * Auto Sequence
//         * */
//
//        when {
//            wizard.wasItemChosen("Alliance", "Red") -> {
//                val alliance = AutoTeleopTransitionLK.Alliance.Red
//                AutoTeleopTransitionLK.alliance = alliance
//                when {
//                    wizard.wasItemChosen("StartPos", "Warehouse") -> {
//
////                        val initDistance = 38
////                        movement.driveRobotPosition(1.0, (initFrontDistance - initDistance), false)
//                        scorePreload(alliance, StartPos.Warehouse, level)
//                        preCycle(alliance)
//                        cycle(alliance, autoStartTime)
//                    }
//                    wizard.wasItemChosen("StartPos", "Ducc") -> {
////                        deliver preload
//                        val preloadTurn = 42.0
//                        synchronousDeposit(level.counts, 4500, syncAction = {
//                            movement.driveRobotStrafe(0.8, 25.0, true)
//                            movement.driveRobotTurn(1.0, preloadTurn, true)
//                        })
//                        synchronousRetract(initLiftPos = level.counts){}
////                        Spin ducc
//                        movement.driveRobotTurn(1.0, -preloadTurn, true)
//                        movement.driveRobotPosition(1.0, -25.0, true)
//                        movement.driveRobotTurn(1.0, 90.0, true)
//                        movement.driveRobotStrafe(1.0, -13.0, false)
//                        sleep(700)
//                        val frontDistance = hardware.frontDistance.getDistance(DistanceUnit.INCH)
//                        val targetDistance = 9
//                        movement.driveRobotPosition(1.0, frontDistance-targetDistance, true)
//                        hardware.duccSpinner1.power = 1.0
//                        sleep(3000)
//                        hardware.duccSpinner1.power = 0.0
////                        Park
//                        val target = -25.0
//                        val disantsFormWall: Double = hardware.frontDistance.getDistance(DistanceUnit.INCH)
//                        movement.driveRobotPosition(1.0, target + disantsFormWall, true)
////                        movement.driveRobotPosition(1.0, -15.0, true)
//                    }
//                }
//            }
//            wizard.wasItemChosen("Alliance", "Blue") -> {
//                val alliance = AutoTeleopTransitionLK.Alliance.Blue
//                AutoTeleopTransitionLK.alliance = alliance
//                when {
//                    wizard.wasItemChosen("StartPos", "Warehouse") -> {
//                        scorePreload(alliance, StartPos.Warehouse, level)
//                        preCycle(alliance)
////                        cycle(alliance, autoStartTime)
//                    }
//                    wizard.wasItemChosen("StartPos", "Ducc") -> {
////                        deliver preload
//                        val preloadTurn = -42.0
//                        synchronousDeposit(level.counts, 4500, syncAction = {
//                            movement.driveRobotStrafe(0.8, 25.0, true)
//                            movement.driveRobotTurn(1.0, preloadTurn, true)
//                        })
//                        synchronousRetract(initLiftPos = level.counts){}
////                        Spin ducc
//                        movement.driveRobotTurn(1.0, -preloadTurn, true)
//                        movement.driveRobotPosition(1.0, 25.0, true)
//                        movement.driveRobotPosition(1.0, 13.0, false)
//                        sleep(700)
//                        movement.driveRobotStrafe(1.0, -15.0, true)
//                        hardware.duccSpinner1.power = -1.0
//                        sleep(3000)
//                        hardware.duccSpinner1.power = 0.0
////                        Park
//                        movement.driveRobotStrafe(1.0,-3.0,true)
//                        movement.driveRobotPosition(1.0,5.0,true)
//                        movement.driveRobotTurn(1.0,-90.0,true)
//                        movement.driveRobotStrafe(1.0,-6.0,true)
//                        val target = 20.0
//                        val disantsFormWall: Double = hardware.backDistance.getDistance(DistanceUnit.INCH)
//                        movement.driveRobotPosition(1.0, target + disantsFormWall, true)
////                        movement.driveRobotPosition(1.0, -15.0, true)
//                    }
//                }
//            }
//        }
//    }
//
//
//    /**
//     * Functions
//     * */
//    fun scorePreload(alliance: AutoTeleopTransitionLK.Alliance, startPos: StartPos, level: LiftPos) {
//        val allianceMultiplier = when(alliance) {
//            AutoTeleopTransitionLK.Alliance.Red -> 1
//            AutoTeleopTransitionLK.Alliance.Blue -> -1
//        }
//        val extensionLength = when(level) {
//            LiftPos.LowGoal -> 3800
//            LiftPos.MidGoal -> 3900
//            LiftPos.HighGoal -> 4600
//        }
//        val startPosMultiplier = when(startPos) {
//            StartPos.Warehouse -> -1
//            StartPos.Ducc -> 1
//        }
//
//        val preloadStrafe = 25.0
//        val preloadTurn = startPosMultiplier * if (alliance == AutoTeleopTransitionLK.Alliance.Blue && startPos == StartPos.Warehouse)
//            34.0
//        else
//            42.5
//
//        if (level != LiftPos.HighGoal)
//            movement.driveRobotStrafe(0.8, preloadStrafe, true)
//
//        synchronousDeposit(
//            liftHeight = level.counts,
//            extensionLength = extensionLength,
//            syncAction = {
//                if (level == LiftPos.HighGoal)
//                    movement.driveRobotStrafe(0.8, preloadStrafe, true)
//                movement.driveRobotTurn(1.0, preloadTurn * allianceMultiplier, true) })
//
//        if (startPos != StartPos.Ducc) {
//            synchronousRetract(initLiftPos = level.counts) {
//                movement.driveRobotTurn(1.0, -preloadTurn * allianceMultiplier, true)
//                movement.driveRobotStrafe(1.0, -(preloadStrafe + 2), true) }
//        }
//    }
//
//    fun preCycle(alliance: AutoTeleopTransitionLK.Alliance) {
//        val allianceMultiplier = when(alliance) {
//            AutoTeleopTransitionLK.Alliance.Red -> 1
//            AutoTeleopTransitionLK.Alliance.Blue -> -1
//        }
//
////        go into warehouse
//        val preCollectMovement = 27.5
//        movement.driveRobotPosition(1.0, preCollectMovement * allianceMultiplier, true)
//
////        collect a block
//        collect(alliance)
//    }
//
//    fun cycle(alliance: AutoTeleopTransitionLK.Alliance, startTime: Long) {
//        var allianceMultiplier = 1
//        var mainCollector = hardware.collector
//        var secondaryCollector = hardware.collector2
//        val cycleDistanceBlueOffset = 3.0
//        when (alliance) {
//            AutoTeleopTransitionLK.Alliance.Red -> {
//                allianceMultiplier = 1
//                mainCollector = hardware.collector
//                secondaryCollector = hardware.collector2
//            }
//            AutoTeleopTransitionLK.Alliance.Blue ->{
//                allianceMultiplier = -1
//                mainCollector = hardware.collector2
//                secondaryCollector = hardware.collector
//            }
//        }
//
//        /**
//         * Cycles
//         * */
//        val autoTime = 30_000
//        val cycleTime = 10_500
//        fun remainingTime() = Math.max(0,  autoTime - (System.currentTimeMillis() - startTime))
//        while ((remainingTime() > cycleTime) && opModeIsActive()) {
//
////            drive to hub while extending
//            synchronousDeposit(
//                liftHeight = LiftPos.HighGoal.counts,
//                extensionLength = 6500,
//                syncAction = {
//                    alignToWall()
//                    val targetDistance = 67 - cycleDistanceBlueOffset
//                    mainCollector.power = -1.0
//                    secondaryCollector.power = 0.0
//                    movement.driveRobotPosition(1.0, (distanceInWarehouse - targetDistance) * allianceMultiplier, true)
//                    mainCollector.power = 0.0
//                })
//
////            drive to warehouse while retracting
//            synchronousRetract(initLiftPos = LiftPos.HighGoal.counts) {
//                alignToWall(5.0)
//                val distanceToWarehouse = 53.0 - cycleDistanceBlueOffset
//                movement.driveRobotPosition(1.0, distanceToWarehouse * allianceMultiplier, true) }
//
////            collect
//            collect(alliance)
//
//            console.display(4, "Remaining Time: ${remainingTime()}")
//        }
//    }
//
//    fun alignToWall(inches: Double = 3.0) {
//        movement.driveRobotStrafe(1.0, -inches, false)
//    }
//
//    fun synchronousDeposit(liftHeight: Int, extensionLength: Int, syncAction: ()->Unit) {
//        val syncThread = Thread {
//            console.display(11, "Thread running.")
//            syncAction()
//        }
//
////        do an action while it's going up
//        syncThread.start()
//
////        start raising depo
//        depo.moveToPosition(depo.preOutLiftPos.coerceAtMost(liftHeight), hardware.horiMotor.currentPosition + 20)
//        depo.moveToPosition(depo.preOutLiftPos.coerceAtMost(liftHeight), 600)
//        depo.moveToPosition(liftHeight, depo.outWhileMovingPos.coerceAtMost(extensionLength))
//
//        syncThread.join()
//
////        move fully out
//        depo.moveToPosition(liftHeight, extensionLength)
//
////        drop
//        hardware.dropperServo.position = DropperPos.Open.posValue
//        sleep(250)
//        hardware.dropperServo.position = DropperPos.Closed.posValue
//    }
//
//    fun synchronousRetract(initLiftPos: Int, syncAction: ()->Unit) {
//
//        depo.moveToPosition(
//            yPosition = initLiftPos,
//            xPosition = depo.outWhileMovingPos)
//
//        val syncThread = Thread {
//            if(depo.currentXIn > depo.outWhileMovingPos){
//                println("\n\nWARNING!!!!\nI expected > ${depo.outWhileMovingPos} but it was ${depo.currentXIn}\n\n")
//            }
//
//            console.display(11, "Thread running.")
//            syncAction()
//        }
//        syncThread.start()
//
//        depo.moveToPosition(
//            yPosition = 350,
//            xPosition = depo.safeXPosition)
//
//        depo.moveToPosition(
//            yPosition = 350,
//            xPosition = depo.xFullyRetracted)
//
////        make sure it's down
//        depo.moveToPosition(
//            yPosition = depo.fullyDown,
//            xPosition = depo.xFullyRetracted)
//        syncThread.join()
//    }
//
//    fun collect(alliance: AutoTeleopTransitionLK.Alliance, stopEjectingAtEnd: Boolean = false) {
//        var allianceMultiplier = 1
//        var collectorMotor = hardware.collector
//        var forwardDistanceSensor = hardware.frontDistance
//
//        if (AutoTeleopTransitionLK.alliance == AutoTeleopTransitionLK.Alliance.Blue) {
//            allianceMultiplier = -1
//            collectorMotor = hardware.collector2
//            forwardDistanceSensor = hardware.backDistance
//        }
//
//        val creepPower = 0.05 * allianceMultiplier
//        val strafePower = 0.05
//        val startCollectTime = System.currentTimeMillis()
//
//        collectorMotor.power = 1.0
//        movement.driveSetMode(DcMotor.RunMode.RUN_USING_ENCODER)
//        while (hardware.dropperColor.alpha() < hardware.colorThreshold && opModeIsActive()) {
//            val timeCollecting = System.currentTimeMillis() - startCollectTime
//            if (timeCollecting < 5_000)
//                movement.driveSetPower(creepPower + strafePower, creepPower, creepPower, creepPower + strafePower)
//            else {
//                movement.drivePowerAll(0.0)
//                collectorMotor.power = -1.0
//                sleep(500)
//                collectorMotor.power = 1.0
//            }
//
//            if (collectorMotor.isOverCurrent) {
//                collectorMotor.power = -1.0
//                sleep(700)
//                collectorMotor.power = 1.0
//                movement.driveSetPower(-creepPower, -(creepPower + strafePower), -(creepPower + strafePower), -creepPower)
//            }
//        }
//        movement.drivePowerAll(0.0)
//
//        hardware.collector.power = -1.0
//        hardware.collector2.power = -1.0
//        sleep(200)
//        distanceInWarehouse = forwardDistanceSensor.getDistance(DistanceUnit.INCH)
//        if (stopEjectingAtEnd) {
//            hardware.collector.power = 0.0
//            hardware.collector2.power = 0.0
//        }
//    }
//
//    fun isDepoHome(): Boolean {
//        return depo.moveTowardPosition(
//            yPosition = depo.fullyDown,
//            xPosition = depo.xFullyRetracted)
//    }
//
//
//    enum class StartPos {
//        Ducc,
//        Warehouse
//    }
//}
//
