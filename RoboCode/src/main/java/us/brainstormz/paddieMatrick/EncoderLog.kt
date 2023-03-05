package us.brainstormz.paddieMatrick

import au.com.bytecode.opencsv.CSVWriter
import com.qualcomm.robotcore.hardware.DcMotorEx
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import java.io.File
import java.io.FileWriter

class EncoderLog(
    private val encoders:List<DcMotorEx>) {

    constructor(hardware:PaddieMatrickHardware):this(
        encoders = listOf(
            hardware.leftOdomEncoder as DcMotorEx,
            hardware.centerOdomEncoder as DcMotorEx,
            hardware.rightOdomEncoder as DcMotorEx,
        ))

    private var path:File? = null

    init {
        try{
            path = File(System.getProperty("java.io.tmpdir"), "encoders.csv")?.absoluteFile
            path?.let{path ->
                log("Will write to ${path}")
                path?.delete()
                path?.parentFile?.mkdirs()
                path?.createNewFile()
                log("Initialized")
            }
        }catch(t:Throwable){
            log("Hey, there was a problem: ${t.message}")
            t.printStackTrace()
        }

        if(path==null){
            log("There was no path :(")
        }
    }
    private var keepRunning = false
    private val thread = object:Thread("encoder logger"){
        override fun run() {
            try{

                log("Starting")
                val out = CSVWriter(FileWriter(path))
                val header = arrayOf("time") + encoders.map{it.deviceName}.toTypedArray()
                log("Writing to $path with ${header.joinToString(",")}")
                out.writeNext(header)
                while(keepRunning){
                    out.writeNext(arrayOf(System.currentTimeMillis().toString()) + encoders.toTypedArray().map {
                        it.getVelocity(AngleUnit.DEGREES).toString()
                    })
                    out.flush()
                }

                out.close()
                log("[${this.javaClass.simpleName}] Finished writing to $path")
            }catch(t:Throwable){
                log("Hey, there was a problem: ${t.message}")
                t.printStackTrace()
            }
        }
    }

    fun log(m:String){
        println("[EncoderLog] $m")
    }

    fun start(){
        keepRunning = true
        thread.start()
    }

    fun stop(){
        keepRunning = false
    }
}