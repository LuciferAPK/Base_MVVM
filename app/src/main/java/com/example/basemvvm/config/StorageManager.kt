package com.example.basemvvm.config

import com.example.basemvvm.Logger
import com.example.basemvvm.data.response.RemoteConfigResponse

enum class StorageRegion(val value: String) {
    //Châu Mỹ
    US("US,CA,BR,MX,CO,AR,CL,PE,VE,BO,PY,UY,CR,EC,SV,GT,HN,NI,PA,PR,BS,BB,AG,GD,HT,JM,TT,DO,BZ,SR,GY,AI,BM,CW,FJ,FK,GF,GL,KN,LC,MF,MQ,MS,PM,SX,TC,VC,VG,VI,"),

    // Châu Âu. Châu Phi
    EU("AL,AD,AT,BY,BE,BA,BG,HR,CY,CZ,DE,DK,EE,FO,FI,FR,GI,GR,HU,IS,IE,IM,IT,RS,LV,LI,LT,LU,MK,MT,MD,MC,ME,NL,NO,PL,PT,RO,RU,SM,SK,SI,ES,SE,CH,UA,GB,VA,GE,AM,AZ,UZ,KZ,TJ,TM,ML,SO,NG,CI,YE,MR,BF,LY,SN,ZA,AO,BJ,BW,BI,CV,CF,TD,KM,CD,DJ,GQ,ER,SZ,GA,GM,GH,GN,GW,KE,LS,LR,MG,MW,MU,MZ,NA,NE,RW,ST,SC,SL,SD,TG,TZ,UG,ZM,ZW,SS,EH,RE,YT,SH,TF,AX,SJ,GG,JE,"),

    // Châu Á, Thái Bình Dương và Bắc Đại Tây Dương
    AS("AF,AM,AZ,BH,BD,BT,BN,KH,CX,CC,IO,GE,ID,IR,IQ,IL,JO,KZ,KW,KG,LA,LB,MO,MY,MV,MN,MM,NP,KP,OM,PS,PK,PH,QA,SA,SG,LK,SY,TJ,TH,TR,TM,AE,TW,JP,KR,HK,CN,VN,AU,NZ,AS,FJ,FM,KI,MH,NR,PW,PG,WS,SB,TO,TV,VU,NU,TK,NC,PF,WF,GS,CK,MP,GU,UM,NF,"),

}

object StorageManager {

    private var mStorageBaseUrl = ""

    private var mStorageFailed = ""

    fun getStorageBaseUrl() = mStorageBaseUrl

    fun getStorageFailedBaseUrl() = if (mStorageFailed.isEmpty()) "" else "$mStorageFailed/"

    fun setStorageBaseUrl(configResult: RemoteConfigResponse, country: String) {
        Logger.logAction("StorageBaseUrl: $mStorageBaseUrl")
        Logger.logAction("urlStorageFailed: $mStorageFailed")
    }

    private fun getBestStorage(country: String): String {
        return ""
    }
}