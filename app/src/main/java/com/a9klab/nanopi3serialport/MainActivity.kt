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
import java.util.*
import android.view.View.FOCUS_DOWN
import java.nio.file.Files.delete
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import android.app.Activity
import android.opengl.Visibility
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.*
import android.text.Editable
import android.R.id.edit
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.view.MotionEvent


class MainActivity : AppCompatActivity(), DataListener, View.OnClickListener {
    private val TAG = "SerialPort"
    private var fromTextView: TextView? = null
    private var toEditor: EditText? = null
    private val MAXLINES = 200
    private val remoteData = StringBuilder(256 * MAXLINES)
    private var mInputMethodManager: InputMethodManager? = null
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
        mInputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        devfd = gSerialPort.InitComm()
        if (devfd >= 0) {
            //Log.d(TAG, "Open Success")
            SerialPort.Start()
        } else {
            devfd = -1
            Toast.makeText(this, "串口未打开", Toast.LENGTH_LONG).show()
            btn_conn.visibility = View.VISIBLE
            btn_fire.isEnabled = false
            Log.d(TAG, "Fail to open " + devName + "!")
        }
        btn_conn.setOnClickListener {
            devfd = gSerialPort.InitComm()
            if (devfd >= 0) {
                //Log.d(TAG, "Open Success")
                SerialPort.Start()
                btn_fire.isEnabled = true
                btn_conn.visibility = View.GONE
            } else {
                devfd = -1
                Toast.makeText(this, "串口未打开", Toast.LENGTH_LONG).show()
                Log.d(TAG, "Fail to open " + devName + "!")
            }
        }
        t_left_freq.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                t_left_freq.setText("")
            } else {
                if (mInputMethodManager!!.isActive) {
                    mInputMethodManager!!.hideSoftInputFromWindow(t_right_freq.windowToken, 0)// 隐藏输入法
                }
            }
        }
        t_right_freq.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                t_right_freq.setText("")
            } else {
                if (mInputMethodManager!!.isActive) {
                    mInputMethodManager!!.hideSoftInputFromWindow(t_right_freq.windowToken, 0)// 隐藏输入法
                }
            }
        }
        ll.setOnTouchListener { view, motionEvent ->
            ll.setFocusable(true)
            ll.setFocusableInTouchMode(true)
            ll.requestFocus()
            false
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
            //1 -> textView.text = "345"
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
        when ((data[1].toInt() shl 8) or data[2].toInt()) {
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

    override fun onClick(v: View) {
        when (v.id) {
            R.id.t_left_freq//输入框
            -> {
                t_left_freq.clearFocus()
                ll.requestFocus()
                if (mInputMethodManager!!.isActive) {
                    mInputMethodManager!!.hideSoftInputFromWindow(t_left_freq.windowToken, 0)// 隐藏输入法
                }
            }
            R.id.t_right_freq//确定按钮
            -> {
                t_right_freq.clearFocus()
                ll.requestFocus()
                if (mInputMethodManager!!.isActive) {
                    mInputMethodManager!!.hideSoftInputFromWindow(t_right_freq.windowToken, 0)// 隐藏输入法
                }
            }
        }
    }

}
