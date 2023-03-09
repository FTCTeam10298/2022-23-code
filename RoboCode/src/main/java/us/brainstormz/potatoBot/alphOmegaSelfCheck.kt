package us.brainstormz.potatoBot

import android.graphics.Camera
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotorEx
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import org.opencv.core.Mat
import org.openftc.easyopencv.OpenCvCamera
import org.openftc.easyopencv.OpenCvCameraFactory
import org.openftc.easyopencv.OpenCvPipeline
import us.brainstormz.motion.aprilTag.AprilTagDetectionPipeline
import java.util.logging.Logger.global


//2 divisions of testing: fine tests (individual component) and gross tests
// Simplest test: check power pulled


@Autonomous(name="potatoBotSelfCheck", group = "!")
class paddieMatrickSelfCheck/** Change Depending on robot */: LinearOpMode() {
    enum class Status {
        OK,
        FAIL
    }
    class MotorAndStatus(val motor: DcMotorEx, val powerStatus: Status, val encoderStatus: Status)


    val hardware = AlphOmegaHardware()
    val motorTime: Long = 125 //in ms
    var ret: Boolean? = null

    lateinit var motors: List<DcMotorEx>

    /** Change Depending on robot */

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
        motors = hardware.hwMap.getAll(DcMotorEx::class.java)


        waitForStart()
        telemetry.addLine("TESTING..........")
        /** AUTONOMOUS  PHASE */

        fun camPowerTest(): Boolean? {
            var aprilTagDetectionPipeline: AprilTagDetectionPipeline? = null

            val newCamera: OpenCvCamera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName::class.java, "Webcam 1"))

            newCamera.showFpsMeterOnViewport(false)

            newCamera.openCameraDeviceAsync(object : OpenCvCamera.AsyncCameraOpenListener {
            inner class leadPipe: OpenCvPipeline() {
               override fun processFrame(i: Mat): Mat{
                   sleep(10)
                   return(Mat())
               }

            }

                override fun onOpened() {
                    println("[camera] opened ")
                    newCamera.setPipeline(leadPipe())
                    newCamera.startStreaming(320, 240)
                    var ret = true
                }

                override fun onError(errorCode: Int) {
                    println("[camera] not opened - error code $errorCode")
                    var ret = false
                }
            })
            return ret
        }

        //Note to Future Engineers: ALL MOTORS MUST BE DcMotorEx TO BE SELF-TESTED  - @JAMES, THIS MEANS YOU
        fun motorPowerTest (motor: DcMotorEx): Status {
            motor.power = 0.01
            sleep(motorTime.toLong())
            var consumedPower = motor.getCurrent(CurrentUnit.MILLIAMPS)
            motor.power = 0.0
            //this is the  power draw without a motor, ambient voltage definition
            if (consumedPower > 30) {
                return Status.OK//("M-PWR-OK")
            }  else {
                return Status.FAIL//("M-PWR-FAIL")
            }
        }
        fun motorEncoderUpdateTest (motor: DcMotorEx): Status {
            var initialPos: Int = motor.currentPosition
            motor.power = 0.1
            sleep(motorTime.toLong() * 2)
            motor.power = 0.0
            var finalPos = motor.currentPosition
            //this is the  power draw without a motor, ambient voltage definition
            if (finalPos - initialPos > 30) {
                return Status.OK//("M-PWR-OK")
            }  else {
                return Status.FAIL//("M-PWR-FAIL")
            }
        }

        val listOfStatus = motors.map { motor ->
            val powerStatus = motorPowerTest(motor)
            val encoderStatus = motorEncoderUpdateTest(motor)
            MotorAndStatus(motor, powerStatus, encoderStatus)
        }
        listOfStatus.forEach{motorAndStatus ->
            val index = listOfStatus.indexOf(motorAndStatus)

            val motorName = hardwareMap.getNamesOf(motorAndStatus.motor)

            telemetry.addLine("${motorName}: PWR ${motorAndStatus.powerStatus} TX ${motorAndStatus.encoderStatus}")

        }
        camPowerTest()
        while(ret == null){
            sleep(100)
        }
        telemetry.addLine("ONE Camera Status: $ret")
        telemetry.update()
        sleep(5000)
    }


}
