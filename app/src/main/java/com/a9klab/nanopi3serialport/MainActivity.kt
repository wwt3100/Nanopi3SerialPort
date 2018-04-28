package com.a9klab.nanopi3serialport

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

import com.friendlyarm.AndroidSDK.HardwareControler
import com.a9klab.nanopi3serialport.SerialPort
import android.widget.EditText
import android.widget.TextView
import java.util.*
import android.view.View.FOCUS_DOWN
import android.widget.Button
import android.widget.ScrollView
import java.nio.file.Files.delete


class MainActivity : AppCompatActivity(), DataListener {
    private val TAG = "SerialPort"
    private var fromTextView: TextView? = null
    private var toEditor: EditText? = null
    private val MAXLINES = 200
    private val remoteData = StringBuilder(256 * MAXLINES)

    private val devName = "/dev/ttySAC4"
    private var speed: Long = 115200
    private var dataBits = 8
    private var stopBits = 1
    private var devfd = -1

    private val gSerialPort = SerialPort(this)
    var dataProcess: DataProcess? = null
    //private var  textView :TextView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example of a call to a native method
        sample_text.text = stringFromJNI()

        button2.setOnClickListener {
            textView.text = "123"
            devfd = gSerialPort.InitComm()
            if (devfd >= 0) {
                //Log.d(TAG, "Open Success")
                SerialPort.Start()
            } else {
                devfd = -1
                Log.d(TAG, "Fail to open " + devName + "!")
            }
        }
        btn_send.setOnClickListener {
            val b = byteArrayOf(0xAA.toByte(), 0xBB.toByte(), 0x0d.toByte(), 0x0a.toByte())
            txv_send.text = SerialPort.SendEx(b).toString()
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            //更新UI
            when (msg.what) {
                1 -> textView.text = "345"
            }
        }
    }

    override fun getData(data: ByteArray?) {
        if (data == null) {
            return
        }
        val length = data.size
        if (length < 1) {
            return
        }
        var s = "Serial get"
        for (i in 0 until length) {
            s = s + "-" + StringUtils.print(data[i])
        }
        Log.i("MainActivity", "$s, length= $length")
//        var cmd=data[1].toInt()
//        cmd=cmd.shl(8)
//        cmd=cmd or data[2].toInt()
        val message = Message()
       when ((data[1] .toInt() shl 8) or data[2].toInt()) {
       //when (cmd) {
            0x0101 -> {
                //TODO: 事件驱动,不能直接改写
                message.what = 1
            }
            else -> {
            }
        }
        mHandler.sendMessage(message)
    }

    interface DataProcess {
        fun notice(msg: Int, arg1: Int)
    }

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }


}
