package us.brainstormz.paddieMatrick

import au.com.bytecode.opencsv.CSVWriter
import com.qualcomm.robotcore.hardware.DcMotorEx
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import java.io.File
import java.io.FileWriter

class EncoderLog(
        private val encoders:List<DcMotorEx>,
        private val path:File = File(System.getProperty("user.dir"), "encoders.csv")) {
    init {
        path.delete()
        path.parentFile.mkdirs()
    }
    private var keepRunning = false
    private val thread = object:Thread("encoder logger"){
        override fun run() {
            val out = CSVWriter(FileWriter(path))
            val header = arrayOf("time") + encoders.map{it.deviceName}.toTypedArray()
            println("[${this.javaClass.simpleName}] Writing to $path with ${header.joinToString(",")}")
            out.writeNext(header)
            while(keepRunning){
                out.writeNext(arrayOf(System.currentTimeMillis().toString()) + encoders.toTypedArray().map {
                    it.getVelocity(AngleUnit.DEGREES).toString()
                })
            }

            out.close()
            println("[${this.javaClass.simpleName}] Finished writing to $path")
        }
    }

    fun start(){
        keepRunning = true
        thread.start()
    }

    fun stop(){
        keepRunning = false
    }
}