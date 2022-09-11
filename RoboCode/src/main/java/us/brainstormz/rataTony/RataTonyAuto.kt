//package us.brainstormz.rataTony
//
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
//import org.openftc.easyopencv.OpenCvCameraRotation
//import us.brainstormz.hardwareClasses.EncoderDriveMovement
//import us.brainstormz.openCvAbstraction.OpenCvAbstraction
//import us.brainstormz.rataTony.Depositor.LiftPos
//import us.brainstormz.rataTony.TeamScoringElementDetector.TSEPosition
//import us.brainstormz.telemetryWizard.TelemetryConsole
//import us.brainstormz.telemetryWizard.TelemetryWizard
//
//@Autonomous(name="RataTony Auto", group="B")
//class RataTonyAuto: LinearOpMode() {
//
//    val console = TelemetryConsole(telemetry)
//    val wizard = TelemetryWizard(console, this)
//
//    val hardware = RataTonyHardware()
//    val movement = EncoderDriveMovement(hardware, console)
//    val depositor = Depositor(hardware,console)
//
//
//    val opencv = OpenCvAbstraction(this)
//    val tseDetector = TeamScoringElementDetector(console)
//
//    override fun runOpMode() {
//        /** INIT PHASE */
//        hardware.init(hardwareMap)
//
//        opencv.init(hardwareMap)
//        opencv.cameraName = hardware.cameraName
//        opencv.cameraOrientation = OpenCvCameraRotation.SIDEWAYS_RIGHT
//        opencv.start()
//        opencv.onNewFrame(tseDetector::processFrame)
//
//        wizard.newMenu("Alliance", "Which alliance are we on?", listOf("Blue", "Red"), "StartPos", firstMenu = true)
//        wizard.newMenu("StartPos", "Which are we closer to?", listOf("Warehouse" to null, "Ducc" to "ParkLocation"))
//        wizard.newMenu("ParkLocation", "Where to park?", listOf("Warehouse", "Storage Unit"))
//        wizard.summonWizard(gamepad1)
//
//        console.display(1, "Initialization Complete")
//        waitForStart()
//        /** AUTONOMOUS  PHASE */
//
//
//        depositor.runInLinearOpmode(this)
//
//        val tsePosition = tseDetector.position
//        opencv.stop()
//
//        val level: LiftPos = when (tsePosition) {
//            TSEPosition.One -> LiftPos.LowGoal
//            TSEPosition.Two -> LiftPos.MidGoal
//            TSEPosition.Three -> LiftPos.HighGoal
//        }
//
//        when {
//            wizard.wasItemChosen("Alliance", "Blue") -> {
//                AutoTeleopTransition.alliance = AutoTeleopTransition.Alliance.Blue
//                when {
//                    wizard.wasItemChosen("StartPos", "Ducc") -> {
////                        Deposit
//                        movement.driveRobotStrafe(1.0, 10.0, true)
//                        movement.driveRobotTurn(1.0, -35.0, true)
//                        movement.driveRobotStrafe(1.0, 18.0, true)
//                        movement.driveRobotPosition(1.0,5.0, true)
//                        deposit(1500, level)
////                        Ducc
//                        movement.driveRobotTurn(1.0, 35.0, true)
//                        movement.driveRobotPosition(1.0, 38.0, true)
//                        movement.driveRobotStrafe(0.5, -20.0,true)
//                        movement.driveRobotStrafe(0.5, -1.0,true)
//                        hardware.carouselSpinner.power = 1.0
//                        movement.driveRobotStrafe(0.1, -1.0, true)
//                        sleep(4000)
//                        hardware.carouselSpinner.power = 0.0
////                        park
//                        when {
//                            wizard.wasItemChosen("ParkLocation", "Storage Unit") -> {
//                                movement.driveRobotStrafe(1.0, 25.0, true)
//                                movement.driveRobotPosition(1.0, 10.0, true)
//                            }
//                            wizard.wasItemChosen("ParkLocation", "Warehouse") -> {
//                                hardware.collector2.power = 1.0
//                                movement.driveRobotStrafe(1.0, 2.0, true)
//                                movement.driveRobotPosition(1.0, -15.0, true)
//                                movement.driveRobotTurn(1.0, 13.0, true)
//                                movement.driveRobotStrafe(1.0, -10.0, false)
//                                movement.driveRobotPosition(1.0, -50.0, true)
//                                hardware.collector2.power = 0.0
//                                movement.driveRobotStrafe(1.0, -5.0, false)
//                                movement.driveRobotPosition(1.0, -60.0, true)
//                            }
//                        }
//
//                    }
//                    wizard.wasItemChosen("StartPos", "Warehouse") -> {
////                        Deposit
//                        movement.driveRobotStrafe(1.0, 10.0, true)
//                        movement.driveRobotTurn(1.0, 38.0, true)
//                        movement.driveRobotStrafe(1.0, 22.0, true)
//                        movement.driveRobotPosition(1.0, 2.0, true)
//                        deposit(1700, level)
////                        Warehouse park
//                        movement.driveRobotStrafe(1.0, -18.0, true)
//                        movement.driveRobotTurn(1.0, -38.0, true)
//                        movement.driveRobotStrafe(1.0, -15.0, false)
//                        movement.driveRobotPosition(1.0,-38.0,true)
//                    }
//                }
//            }
//            wizard.wasItemChosen("Alliance", "Red") -> {
//                AutoTeleopTransition.alliance = AutoTeleopTransition.Alliance.Red
//                when {
//                    wizard.wasItemChosen("StartPos", "Ducc") -> {
////                        Deposit
//                        movement.driveRobotStrafe(1.0, 10.0, true)
//                        movement.driveRobotTurn(1.0, 35.0, true)
//                        movement.driveRobotStrafe(1.0, 22.0, true)
//                        movement.driveRobotPosition(1.0, -1.5, true)
//                        if (level == Depositor.LiftPos.MidGoal)
//                            deposit(1500, level)
//                        else
//                            deposit(1600, level)
//
////                        Ducc
//                        movement.driveRobotTurn(1.0, -34.0, true)
//                        movement.driveRobotStrafe(1.0,-12.0,true)
//                        movement.driveRobotPosition(1.0, -38.0, true)
//                        movement.driveRobotStrafe(1.0,12.0,true)
//                        movement.driveRobotTurn(1.0,90.0,true)
//                        movement.driveRobotStrafe(1.0,-7.0,false)
//                        movement.driveRobotPosition(1.0, 12.0,true)
//                        movement.driveRobotStrafe(0.5,-2.0,false)
//                        movement.driveRobotPosition(0.4, 5.0,false)
//                        hardware.carouselSpinner.power = -1.0
//                        movement.driveRobotPosition(0.1, 2.0, true)
//                        sleep(4000)
//                        hardware.carouselSpinner.power = 0.0
////                        park
//                        when {
//                            wizard.wasItemChosen("ParkLocation", "Storage Unit") -> {
//                                movement.driveRobotPosition(1.0, -22.0, true)
//                                movement.driveRobotStrafe(1.0, -10.0, true)
//                            }
//                            wizard.wasItemChosen("ParkLocation", "Warehouse") -> {
//                                hardware.collector.power = 1.0
//                                movement.driveRobotPosition(1.0,-12.0,true)
//                                movement.driveRobotStrafe(1.0,-6.0,true)
//                                movement.driveRobotStrafe(1.0,12.0,true)
//                                movement.driveRobotTurn(1.0,-90.0,true)
//                                movement.driveRobotPosition(1.0,10.0,true)
//                                movement.driveRobotStrafe(1.0,-30.0,true)
//                                sleep(2000)
//                                movement.driveRobotPosition(1.0,45.0,true)
//                                hardware.collector.power = 0.0
//                                movement.driveRobotStrafe(1.0,-10.0,false)
//                                movement.driveRobotPosition(1.0,37.0,true)
//                            }
//                        }
//                    }
//                    wizard.wasItemChosen("StartPos", "Warehouse") -> {
////                        Deposit
//                        movement.driveRobotStrafe(1.0, 10.0, true)
//                        movement.driveRobotTurn(1.0, -35.0, true)
//                        movement.driveRobotStrafe(1.0, 20.0, true)
//                        deposit(1700, level)
////                        Warehouse park
//                        movement.driveRobotStrafe(1.0,-18.0,true)
//                        movement.driveRobotTurn(1.0, 35.0, true)
//                        movement.driveRobotStrafe(1.0, -11.0, true)
//                        movement.driveRobotPosition(1.0,30.0,true)
//                    }
//                }
//            }
//        }
//
//    }
//
////    out, drop, in
//    fun deposit(xCounts: Int, yLevel: LiftPos) {
//        if (yLevel == LiftPos.HighGoal)
//            depositor.yToPosition(yLevel.counts + 10)
//        else
//            depositor.yToPosition(yLevel.counts)
//            sleep(100)
//        depositor.xToPosition(xCounts)
//        depositor.drop()
//        sleep(500)
//        depositor.close()
//        depositor.xToPosition(0)
//        depositor.xTowardPosition(depositor.innerLimit)
//        depositor.yToPosition(depositor.lowerLimit)
//        depositor.xAtPower(0.0)
//    }
//}
