//package us.brainstormz.lankyKong
//
//import com.qualcomm.robotcore.eventloop.opmode.OpMode
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp
//import us.brainstormz.hardwareClasses.MecanumDriveTrain
//import us.brainstormz.telemetryWizard.TelemetryConsole
//
//@TeleOp(name= "Lanky Kong Debug Test ", group= "B")
//class LankyKongDebugTest: OpMode() {
//
//    val console = TelemetryConsole(telemetry)
//
//    val hardware = LankyKongHardware() /** Change Depending on robot */
//    val movement = MecanumDriveTrain(hardware)
//
//    var isStickButtonDown = false
//    var driveReversed = if (AutoTeleopTransitionLK.alliance == AutoTeleopTransitionLK.Alliance.Red) 1 else -1
//
//    var aDown = false
//    var speedMode = false
//    var blockRejectCooldown = 1000
//    var timeWhenBlockIn = 0L
//    var blockIn = true
//
//    lateinit var depo: DepositorLK
//
//    override fun init() {
//        /** INIT PHASE */
//        hardware.init(hardwareMap)
//        depo = DepositorLK(hardware)
//    }
//
//    override fun loop() {
//        /** TELE-OP PHASE */
////        hardware.clearHubCache()
//
//////        DRONE DRIVE
////        val adjustPower = 0.2
////        val yAdjust = when {
////            gamepad2.right_bumper -> adjustPower
////            gamepad2.left_bumper -> -adjustPower
////            else -> 0.0
////        }
////
////        fun isPressed(v:Float):Boolean = v > 0
////        val wallRiding = if (isPressed(gamepad1.right_trigger)) 0.5 else 0.0
////
////        if (gamepad1.left_stick_button || gamepad1.right_stick_button && !isStickButtonDown) {
////            isStickButtonDown = true
////            driveReversed = -driveReversed
////        } else if (!gamepad1.left_stick_button && !gamepad1.right_stick_button) {
////            isStickButtonDown = false
////        }
////
////        val yInput = -gamepad1.left_stick_y.toDouble()
////        val xInput = gamepad1.left_stick_x.toDouble()
////        val rInput = -gamepad1.right_stick_x.toDouble()
////
////        val y = (yInput + yAdjust) * driveReversed
////        val x = (xInput * driveReversed) + wallRiding
////        val r = rInput * .9
////
////        movement.driveSetPower((y + x - r),
////                               (y - x + r),
////                               (y - x - r),
////                               (y + x + r))
////
////        console.display(1, "Drive Encoders: \n ${hardware.lFDrive.currentPosition} \n ${hardware.rFDrive.currentPosition} \n ${hardware.lBDrive.currentPosition} \n ${hardware.rBDrive.currentPosition}")
////        console.display(2, "Front Range: ${hardware.frontDistance.getDistance(DistanceUnit.INCH)} \nBack Range: ${hardware.backDistance.getDistance(DistanceUnit.INCH)}")
////        console.display(3, "X Motor currPos: ${hardware.horiMotor.currentPosition}")
////        console.display(4, "Y Motor currPos: ${hardware.liftMotor.currentPosition}")
////        console.display(5, "Color Sensor: \n Alpha: ${hardware.dropperColor.alpha()} \n Red: ${hardware.dropperColor.red()}")
////
////
//////        COLLECTOR
////        val forwardPower = 1.0
////        val reversePower = 0.9
////        val colorThreshold = 250
////
////        when {
////            gamepad1.right_bumper -> {
////                if (driveReversed > 0) {
////                    hardware.collector.power = forwardPower
////                    hardware.collector2.power = -reversePower
////                } else {
////                    hardware.collector.power = -reversePower
////                    hardware.collector2.power = forwardPower
////                }
////            }
////            gamepad1.left_bumper -> {
////                if (driveReversed < 0) {
////                    hardware.collector.power = forwardPower
////                    hardware.collector2.power = -reversePower
////                } else {
////                    hardware.collector.power = -reversePower
////                    hardware.collector2.power = forwardPower
////                }
////            }
////            else -> {
////                hardware.collector.power = 0.0
////                hardware.collector2.power = 0.0
////            }
////        }
////
////        when {
////            gamepad1.a && !aDown -> {speedMode = !speedMode
////                                     aDown = true}
////            !gamepad1.a -> aDown = false
////            speedMode -> {
////                if (hardware.dropperColor.alpha() > colorThreshold) {
////                    val timeSinceBlockIn = System.currentTimeMillis() - timeWhenBlockIn
////                    console.display(20, "timeSinceBlockIn: $timeSinceBlockIn")
////                    if (blockIn && timeSinceBlockIn > blockRejectCooldown) {
////                        hardware.collector.power = -reversePower
////                        hardware.collector2.power = -reversePower
////                    }
////                } else
////                    timeWhenBlockIn = System.currentTimeMillis()
////            }
////        }
////        console.display(21, "speedMode: $speedMode")
//
//
//
////        DEPOSITOR
//        depo.moveWithJoystick(-gamepad2.left_stick_y.toDouble(), gamepad2.right_stick_x.toDouble(), false)
//
//        if (gamepad2.right_trigger != 0f)
//            hardware.dropperServo.position = DepositorLK.DropperPos.Open.posValue
//        else
//            hardware.dropperServo.position = DepositorLK.DropperPos.Closed.posValue
//
////        hardware.duccSpinner1.power = 0.0
////
//////        Ducc
////        val duccSide = when (AutoTeleopTransition.alliance) {
////            AutoTeleopTransition.Alliance.Red -> -1.0
////            AutoTeleopTransition.Alliance.Blue -> 1.0
////        }
////        when {
////            gamepad2.dpad_left -> {
////                hardware.duccSpinner1.power = -duccSide
////            }
////            gamepad2.dpad_right -> {
////                hardware.duccSpinner1.power = duccSide
////            }
////            else -> {
////                hardware.duccSpinner1.power = 0.0
////            }
////        }
//
//    }
//}