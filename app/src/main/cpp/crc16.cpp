//
// Created by hex on 2016/9/18.
//

#include "crc16.h"
//#include "common_log.h"

unsigned short getCRC16(unsigned char *puchMsg, int usDataLen) {
    unsigned char uchCRCHi = 0xFF;
    unsigned char uchCRCLo = 0xFF;
    unsigned short uIndex;
    while (usDataLen--) {
        uIndex = uchCRCLo ^ *puchMsg++;
        uchCRCLo = uchCRCHi ^ auchCRCHi[uIndex];
        uchCRCHi = auchCRCLo[uIndex];
    }
    return (((unsigned short) (uchCRCHi) << 8) | uchCRCLo);
}

unsigned short toCRC16(unsigned char *puchMsg) {
    unsigned char uchCRCHi = 0xFF;
    unsigned char uchCRCLo = 0xFF;
    int usDataLen = *(puchMsg + 3) + 3;
    unsigned short uIndex;
    puchMsg++;
    while (usDataLen--) {
        uIndex = uchCRCLo ^ *puchMsg++;
        uchCRCLo = uchCRCHi ^ auchCRCHi[uIndex];
        uchCRCHi = auchCRCLo[uIndex];
    }
    return (((unsigned short) (uchCRCHi) << 8) | uchCRCLo);
}

bool checkCRC16(unsigned char *puchMsg, int begin, int usDataLen) {
    unsigned char uchCRCHi = 0xFF;
    unsigned char uchCRCLo = 0xFF;
    unsigned short uIndex;
    //LOGI("----%d", usDataLen);
    begin++;
    while (usDataLen--) {
        uIndex = uchCRCLo ^ puchMsg[begin++];
        uchCRCLo = uchCRCHi ^ auchCRCHi[uIndex];
        uchCRCHi = auchCRCLo[uIndex];
    }
    //LOGI("----%x", uchCRCLo);
    //LOGI("----%x", uchCRCHi);
    //log_arr_Rdata(puchMsg, 11);
    return (uchCRCHi == puchMsg[begin + usDataLen + 1]) && (uchCRCLo == puchMsg[begin + usDataLen + 2]);
}