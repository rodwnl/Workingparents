package com.example.workingparents

import kotlin.properties.Delegates

object UserData {

    var id: String = "NONE"
    lateinit var sex: String
    lateinit var name: String
    lateinit var token: String
    lateinit var village: String
    lateinit var pNumber: String

    var couplenum by Delegates.notNull<Int>()
    lateinit var spouseID: String
    lateinit var spouseName: String

    var childName: String = "NONE"

    fun setUserInfo(id: String, sex:String, name: String, pNumber: String, token: String, village: String){
        this.id=id
        this.sex=sex
        this.name=name
        this.pNumber=pNumber
        this.token=token
        this.village=village
    }

    fun setCoupleInfo(num:Int, id:String, spouseName: String){
        this.couplenum=num
        this.spouseID=id
        this.spouseName=spouseName
    }

    fun setChildInfo(name:String){
        this.childName=name
    }

    //부부연결된 사용자인지 아닌지 알려준다. 연결되어있으면 true
    fun connectedCouple():Boolean{
        return !(spouseID=="NONE" && couplenum==-1)
    }

    //User에게 아이 존재 유무를 알려준다. 연결되어있으면 true
    fun connectedChild():Boolean{
        return !(childName=="NONE")
    }


}
