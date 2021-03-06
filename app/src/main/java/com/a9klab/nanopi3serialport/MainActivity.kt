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
import android.R.attr.data
import android.annotation.SuppressLint
import android.graphics.Color


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

    private var leftHandleType = 0
    private var rightHandleType = 0
    private var leftHandleTipType = 0
    private var rightHandleTipType = 0
    private var leftHandleTipFreq = 0
    private var rightHandleTipFreq = 0
    private var HandleSelect = 0

    private var power = 1
    private var pitch = 10
    private var fireMode = 0
    private var length = 25

    private var ready = 0
    private var errorCode = 0

    private var pos = 0
    private var isRun = 0;

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
        btn_h_conn.setOnClickListener {
            val data = byteArrayOf((0 + 5).toByte(), 0x01.toByte(), 0x04.toByte())
            SerialPort.Send(data)
        }
        btn_standby.setOnClickListener {
            val data = byteArrayOf((1 + 5).toByte(), 0x01.toByte(), 0x02.toByte(), 0x01.toByte())
            SerialPort.Send(data)
        }
        btn_ready.setOnClickListener {
            val data = byteArrayOf((1 + 5).toByte(), 0x01.toByte(), 0x02.toByte(), 0x02.toByte())
            SerialPort.Send(data)
        }
        btn_power_add.setOnClickListener {
            if (power < 15) {
                power++
            } else {
                power = 1
            }
            txt_power.text = String.format("%.01fJ", power.toFloat() / 10.0)
        }
        btn_power_dec.setOnClickListener {
            if (power > 1) {
                power--
            } else {
                power = 15
            }
            txt_power.text = String.format("%.01fJ", power.toFloat() / 10.0)
        }
        btn_pitch_add.setOnClickListener {
            if (pitch < 20) {
                pitch++
            } else {
                pitch = 10
            }
            txt_pitch.text = String.format("%.01fmm", pitch.toFloat() / 10.0)
        }
        btn_pitch_dec.setOnClickListener {
            if (pitch > 10) {
                pitch--
            } else {
                pitch = 20
            }
            //txt_pitch.text = (pitch.toFloat() / 10.0).toString() + "mm"
            txt_pitch.text = String.format("%.01fmm", pitch.toFloat() / 10.0)
        }
        btn_lenght_add.setOnClickListener {
            if (length < 25) {
                length += 5
            } else {
                length = 5
            }
            txt_lenght.text = String.format("%dmm", length)
        }
        btn_lenght_dec.setOnClickListener {
            if (length > 5) {
                length -= 5
            } else {
                length = 25
            }
            txt_lenght.text = String.format("%dmm", length)
        }
        btn_set.setOnClickListener {
            if (rbn_sigle.isChecked) {
                fireMode = 1
            } else if (rbn_con.isChecked) {
                fireMode = 2
            }
            val data = byteArrayOf((4 + 5).toByte(), 0x01.toByte(), 0x03.toByte(), power.toByte(), fireMode.toByte(), pitch.toByte(), length.toByte())
            SerialPort.Send(data)
        }
        txt_left_handle_type.text = leftHandleType.toString() + "+" + leftHandleTipType.toString()
        txt_right_handle_type.text = rightHandleType.toString() + "+" + rightHandleTipType.toString()

        txt_left_freq.text = String.format("%.01fMHz", leftHandleTipFreq.toDouble() / 1000000)
        txt_right_freq.text = String.format("%.01fMHz", rightHandleTipFreq.toDouble() / 1000000)

        txt_error_code.text = String.format("ErrorCode:%d", errorCode)

        txt_power.text = String.format("%.01fJ", power.toFloat() / 10.0)
        txt_pitch.text = String.format("%.01fmm", pitch.toFloat() / 10.0)
        txt_lenght.text = String.format("%dmm", length)
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            //更新UI
            when (msg.what) {
            //1 -> textView.text = "345"
                CmdMsg.H0104_HeartBeat.ordinal -> {

                }
                CmdMsg.H0102_SetStateInfo.ordinal -> {
                    txt_error_code.text = String.format("ErrorCode:%d", errorCode)
                }
                CmdMsg.H0103_SetWork.ordinal -> {
                    txt_error_code.text = String.format("ErrorCode:%02X", errorCode)
                }
                CmdMsg.H0301_H0302_ReflashHandleInfo.ordinal -> {
                    txt_left_handle_type.text = leftHandleType.toString() + "+" + leftHandleTipType.toString()
                    txt_right_handle_type.text = rightHandleType.toString() + "+" + rightHandleTipType.toString()
                    when (HandleSelect) {
                        0 -> {
                            txt_left_handle_type.setTextColor(Color.GRAY)
                            txt_right_handle_type.background = null
                            txt_right_handle_type.setTextColor(Color.GRAY)
                            txt_right_handle_type.background = null
                        }
                        1 -> {
                            txt_left_handle_type.setTextColor(Color.BLUE)
                            txt_left_handle_type.background = getDrawable(R.color.colorAccent)
                            txt_right_handle_type.setTextColor(Color.GRAY)
                            txt_right_handle_type.background = null
                        }
                        2 -> {
                            txt_left_handle_type.setTextColor(Color.GRAY)
                            txt_left_handle_type.background = null
                            txt_right_handle_type.setTextColor(Color.BLUE)
                            txt_right_handle_type.background = getDrawable(R.color.colorAccent)
                        }
                    }
                }
                CmdMsg.H0202_TipInfoReport.ordinal -> {
                    txt_left_handle_type.text = leftHandleType.toString() + "+" + leftHandleTipType.toString()
                    txt_right_handle_type.text = rightHandleType.toString() + "+" + rightHandleTipType.toString()
                    txt_left_freq.text = String.format("%.01fMHz", leftHandleTipFreq.toDouble() / 1000000)
                    txt_right_freq.text = String.format("%.01fMHz", rightHandleTipFreq.toDouble() / 1000000)

                }
                CmdMsg.H0206_WorkStateReport.ordinal -> {
                    if (isRun == 1)
                        progress.progress = pos
                    else
                        progress.progress = 0
                }
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
        val message = Message()
        val cmd = (data[2].toInt() shl 8) or data[3].toInt()
        when (cmd) {
        //TODO: 事件驱动,不能直接改写
            0x0101 -> {

                message.what = CmdMsg.H0101_GetStateInfo.ordinal
            }
            0x0102 -> {
                message.what = CmdMsg.H0102_SetStateInfo.ordinal
                when (data[4].toInt()) {
                    0 -> {
                        errorCode = 0
                        if (ready == 0) {
                            ready = 1
                        } else {
                            ready = 0
                        }
                    }
                    else -> {
                        errorCode = data[4].toInt() and 0xff
                    }
                }
            }
            0x0103 -> {
                message.what = CmdMsg.H0103_SetWork.ordinal
                errorCode = data[4].toInt() and 0xff
            }
            0x0104 -> {
                message.what = CmdMsg.H0104_HeartBeat.ordinal
                Log.i("_Recv_CMD", "H0104_HeartBeat")
            }
            0x0202 -> {
                message.what = CmdMsg.H0202_TipInfoReport.ordinal
                when (data[4].toInt()) {
                    1 -> {
                        leftHandleTipType = data[5].toInt()
                        leftHandleTipFreq = (data[19].toInt() and 0xff) or ((data[20].toInt() and 0xff) shl 8) or ((data[21].toInt() and 0xff) shl 16) or ((data[22].toInt() and 0xff) shl 24)
                    }
                    2 -> {
                        rightHandleTipType = data[5].toInt()
                        rightHandleTipFreq = (data[19].toInt() and 0xff) or ((data[20].toInt() and 0xff) shl 8) or ((data[21].toInt() and 0xff) shl 16) or ((data[22].toInt() and 0xff) shl 24)
                    }
                }
            }
            0x0206 -> {
                message.what = CmdMsg.H0206_WorkStateReport.ordinal
                isRun = data[4].toInt()
                pos = data[5].toInt() and 0xff
            }
            0x0301, 0x0302 -> {
                message.what = CmdMsg.H0301_H0302_ReflashHandleInfo.ordinal
                leftHandleType = data[4].toInt()
                rightHandleType = data[5].toInt()
                HandleSelect = data[6].toInt()
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
