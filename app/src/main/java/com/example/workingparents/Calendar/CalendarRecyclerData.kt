package com.example.workingparents.Calendar

import android.graphics.drawable.Drawable

//라이클러뷰를 위한 데이터 클래스파일
data class CalendarRecyclerData(
    var startTime: String,
    var endTime: String,
    var ccontent: String,
    var cdate: String,
    var viewType: Int
)