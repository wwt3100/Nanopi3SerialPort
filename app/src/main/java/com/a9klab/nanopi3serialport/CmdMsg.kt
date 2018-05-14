package com.a9klab.nanopi3serialport

/**
 * Created by Administrator on 2018/5/9 0009.
 */
enum class CmdMsg {
    H0101_GetStateInfo,
    H0102_SetStateInfo,
    H0103_SetWork,
    H0104_HeartBeat,
    H0201_GetTipInfo,
    H0202_TipInfoReport,
    H0206_WorkStateReport,
    H0301_H0302_ReflashHandleInfo,
    H0402_GetFootSwitchState,
    H0403_FootSwitchStateReport,
    H0501_ErrorReport,
    //////////////////////////////
    //调试指令
    H0601_,
    H0602
}