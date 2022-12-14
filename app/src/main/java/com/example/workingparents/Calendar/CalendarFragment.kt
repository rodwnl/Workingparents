package com.example.workingparents.Calendar

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.LineBackgroundSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workingparents.*
import com.example.workingparents.R
import com.prolificinteractive.materialcalendarview.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class CalendarFragment : Fragment() {

    companion object{
        var hashMap= HashMap<CalendarDay,String>()
        var couplenum:Int =UserData.couplenum
        val sex:String = UserData.sex

        lateinit var recyclerCalendar: RecyclerView
        lateinit var calendar: MaterialCalendarView

    }
    private lateinit var mContext : Activity

    val CoupleColor = intArrayOf(
        Color.rgb(255, 168, 177),
        Color.rgb(158, 193, 255)
    )

    val FamaleColor = intArrayOf(
        Color.rgb(255, 168, 177)
    )

    val MaleColor = intArrayOf(
        Color.rgb(158, 193, 255)
    )

    val TAG: String = "CoupleCalendar"
    //메모리에 올라갔을 때
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            mContext = context
        }
    }


    //뷰가 생성되었을 때
    //프레그먼트와 레이아웃을 연결시켜주는 부분
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view=inflater.inflate(R.layout.activity_calendar, container, false)
        calendar = view.findViewById<MaterialCalendarView>(R.id.calendar)

        //캘린더 폰트변경 및 색깔변경
        calendar.setDateTextAppearance(R.style.CustomDateTextAppearance)
        calendar.setWeekDayTextAppearance(R.style.CustomWeekDayAppearance)
        calendar.setHeaderTextAppearance(R.style.CustomHeaderTextAppearance)


        var recyclerDay =view.findViewById<TextView>(R.id.recyclerDay)
        var button =view.findViewById<ImageButton>(R.id.button)

        recyclerCalendar =view.findViewById<RecyclerView>(R.id.recyclerCalendar)

        val sundayDecorator = SundayDecorator()
        val saturdayDecorator = SaturdayDecorator()
        val todayDecorator = TodayDecorator()

        calendar.addDecorators(saturdayDecorator,sundayDecorator, todayDecorator)
        //캘린더에 토요일, 일요일, 오늘을 각각 원하는 색깔로 칠해줌

        val list = ArrayList<CalendarRecyclerData>()
        //Get할 때 CalendarData형식으로 넣어주는 리스트

        var fragmentManager: FragmentManager?= null

        initCalendar()
        //처음에 캘린더에 들어갈 때 캘린더에 점을 찍어주는 역할


        calendar.setOnDateChangedListener(object : OnDateSelectedListener {
            //사용자가 날짜를 눌렀을 때

            override fun onDateSelected(
                widget: MaterialCalendarView,
                date: CalendarDay,
                selected: Boolean
            ) {

                val dayOfWeek: String? = doDayOfWeek(date)
                //사용자가 선택한 날짜의 요일을 담은 변수

                if(date.day <10){
                    recyclerDay.setText("0${date.day}.$dayOfWeek")
                    //date.day는 6.목 이런식으로 보여줘서 10일 이전과 이후를 if문으로 나눔
                }else{
                    recyclerDay.setText("${date.day}.$dayOfWeek")
                }

                var Calendardate:String
                //리사이클러뷰에서 내가 클릭한 날짜와 같은 날짜를 찾아오기 위한 변수

                Calendardate = "${date.year}-${date.month + 1}-${date.day}"
                //특이한 점이 month+1을 해줘야함 그냥 month는 해당 달보다 1씩 작음


                var adapter = CalendarAdapter(mContext, list)
                //adpater가 여러군데 쓰여서 매개변수로 넣어주기로 함

                clickGetRecycler(adapter, Calendardate,list)
                //클릭한 날짜의 일정을 리사이클러뷰로 보여줌

                button.setOnClickListener(object : View.OnClickListener {

                    override fun onClick(p0: View?) {
                        //일정추가 버튼을 클릭했을 때

                        var dialog = CustomDialog(mContext)
                        //다이얼로그 부르기
                        dialog.myDig(mContext, childFragmentManager, fragmentManager)
                        dialog.setOnClickedListener(object : CustomDialog.ButtonClickListener {
                            @RequiresApi(Build.VERSION_CODES.O)
                            override fun onClicked(
                                startTime: String,
                                endTime: String,
                                ccontent: String
                            ) {
                                //완료버튼 클릭 후 정보를 받아오는 리스너

                                var cdate:String?=null
                                //DB에 날짜를 저장하기 위한 변수

                                var ctime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                                cdate ="${Calendardate} ${ctime}"
                                //앞서 내가 클릭한 날짜의 리사이클러뷰를 가져오기 위해 Calendardate를 만들었다. 여기에 ctime을 추가해서 내가 DB에 넣어야할 날짜변수를 만듦

                                //리사이클러뷰 아이템 추가 시 갱신

                                if(UserData.sex=="F") {
                                    list.add(
                                        CalendarRecyclerData(
                                            startTime,
                                            endTime,
                                            ccontent,
                                            cdate,
                                            CalendarMode.female
                                        )
                                    )
                                }else{
                                    list.add(
                                        CalendarRecyclerData(
                                            startTime,
                                            endTime,
                                            ccontent,
                                            cdate,
                                            CalendarMode.male
                                        )
                                    )
                                }
                                adapter.notifyItemRangeChanged(list.size, 1)

                                postCalendar(startTime, cdate, endTime, ccontent, date,list)

                            }
                        })
                    }
                })
            }
        })


        return view
    }


    private fun initCalendar() {
//처음 캘린더를 시작할 때 점을 찍어주는 역할

    RetrofitBuilder.api.getCalendar(couplenum).enqueue(object : Callback<List<CalendarData>> {

        override fun onResponse(
            call: Call<List<CalendarData>>,
            response: Response<List<CalendarData>>
        ) {
            if (response.isSuccessful) {

                var result: List<CalendarData>? = response.body()
                // 정상적으로 통신이 성공된 경우
                if (result != null) {

                    for (i in result) {
                        //Get으로 가져온 result

                        var splitdate = i.cdate.split(" ")
                        //result의 날짜를 Calendar 형식이 되도록 문자열을 잘라줌

                        var CalendarFromDate: List<String> =
                            splitdate.get(0).split("-")
                        //자른 문자열을 다시 잘라줌

                        if (hashMap.containsKey(
                                CalendarDay.from(
                                    CalendarFromDate.get(0).toInt(),
                                    (CalendarFromDate.get(1).toInt() - 1),
                                    CalendarFromDate.get(2).toInt()
                                )
                            )
                        ) {
                            //기존에 이미 같은 키값(날짜)이 있다면
                            Log.d("HashMap", "여기들어가긴함?")
                            if (hashMap.get(
                                    CalendarDay.from(
                                        CalendarFromDate.get(0).toInt(),
                                        (CalendarFromDate.get(1).toInt() - 1),
                                        CalendarFromDate.get(2).toInt()
                                    )
                                ) == i.csex
                            ) {
                                //근데 같은 성별이라면
                                hashMap[CalendarDay.from(
                                    CalendarFromDate.get(0).toInt(),
                                    (CalendarFromDate.get(1).toInt() - 1),
                                    CalendarFromDate.get(2).toInt()
                                )] = i.csex
                                Log.d("HashMap", "여기들어가긴함?2")
                            } else {
                                //아니라면
                                hashMap[CalendarDay.from(
                                    CalendarFromDate.get(0).toInt(),
                                    (CalendarFromDate.get(1).toInt() - 1),
                                    CalendarFromDate.get(2).toInt()
                                )] = "C"
                                Log.d("HashMap", "여기들어가긴함?3")

                            }
                        } else {

                            hashMap[CalendarDay.from(
                                CalendarFromDate.get(0).toInt(),
                                (CalendarFromDate.get(1).toInt() - 1),
                                CalendarFromDate.get(2).toInt()
                            )] = i.csex
                            Log.d("HashMap", "여기들어가긴함?4    ")

                            Log.d(
                                "Calendar날짜",
                                "이게맞나" + CalendarFromDate.get(0)
                                    .toInt() + (CalendarFromDate.get(1)
                                    .toInt() - 1) + CalendarFromDate.get(2).toInt()
                            )
                                .toString()
                        }
                    }

                }

                for (map in hashMap) {
                    Log.d("HashMap", "key:" + map.key + "values:" + map.value)
                    if (map.value == "F") {
                        calendar.addDecorator(
                            EventDecorator2(
                                Collections.singleton(map.key),
                                mContext,
                                FamaleColor
                            )
                        )
                    } else if (map.value == "M") {
                        calendar.addDecorator(
                            EventDecorator2(
                                Collections.singleton(map.key),
                                mContext,
                                MaleColor
                            )
                        )

                    } else if (map.value == "C") {
                        calendar.addDecorator(
                            EventDecorator2(
                                Collections.singleton(map.key),
                                mContext,
                                CoupleColor
                            )
                        )
                    }
                }
            } else {
                // 통신이 실패한 경우(응답코드 3xx, 4xx 등)
                Log.d("Retrofit", "onResponse: 캘린더실패")
            }
        }

        override fun onFailure(
            call: Call<List<CalendarData>>,
            t: Throwable
        ) {
            Log.d("Retrofit", "onFailure 캘린더 실패 에러1: " + t.message.toString())

        }


    })

}

    private fun doDayOfWeek(date: CalendarDay): String? {
        val cal: Calendar = date.calendar
        var strWeek: String? = null
        val nWeek: Int = cal.get(Calendar.DAY_OF_WEEK)

        if (nWeek == 1) {
            strWeek = "일"
        } else if (nWeek == 2) {
            strWeek = "월"
        } else if (nWeek == 3) {
            strWeek = "화"
        } else if (nWeek == 4) {
            strWeek = "수"
        } else if (nWeek == 5) {
            strWeek = "목"
        } else if (nWeek == 6) {
            strWeek = "금"
        } else if (nWeek == 7) {
            strWeek = "토"
        }
        return strWeek
    }


    private fun clickGetRecycler(
        adapter: CalendarAdapter,
        Calendardate: String?,
        list: ArrayList<CalendarRecyclerData>
    ) {
        //get을 통해 클릭한 날짜의 일정을 가져와서 리사이클러뷰로 보여줌
        list.clear()
        //이전에 클릭한 일정을 다시 보지 않게 해줌
        RetrofitBuilder.api.getCalendar(UserData.couplenum).enqueue(object: Callback<List<CalendarData>> {

            override fun onResponse(call: Call<List<CalendarData>>, response: Response<List<CalendarData>>) {
                if(response.isSuccessful){

                    var result: List<CalendarData>? = response.body()
                    // 정상적으로 통신이 성공된 경우
                    if (result != null) {
                        for(i in result){

                            var startTime= i.startTime
                            var endTime = i.endTime
                            var content= i.ccontent
                            var cdate= i.cdate
                            var sex = i.csex
                            var splitdate = i.cdate.split(" ")
                            //현재 i.cdate는 2022-09-6 00:00:00 형식이여서 뒤에 00:00:00을 빼준다

                            if(splitdate.get(0).equals(Calendardate)) {


                                if(sex=="F") {
                                    list.add(
                                        CalendarRecyclerData(
                                            startTime,
                                            endTime,
                                            content,
                                            cdate,
                                            CalendarMode.female
                                        )
                                    )

                                }else{
                                    list.add(
                                        CalendarRecyclerData(
                                            startTime,
                                            endTime,
                                            content,
                                            cdate,
                                            CalendarMode.male
                                        )
                                    )
                                }
                            }

                        }

                        //리사이클러의 아이템 클릭 시 어댑터에 정의해둔 클릭리스너를 호출함
                        adapter.setOnItemClickListener(object : CalendarAdapter.OnItemClickListener {

                            override fun onItemClick(data: CalendarRecyclerData, pos: Int) {

                                var dialog = CustomDialog(mContext)
                                //아이템 클릭 시 다이얼로그 생성 및 생성삭제에 필요한 매개변수들을 전달해줌
                                dialog.adapterMyDig(data,data.startTime,data.ccontent,adapter,list,pos)
                            }

                            override fun onItemEditClick(data: CalendarRecyclerData, pos: Int) {



                            }

                            override fun onItemDeleteClick(data: CalendarRecyclerData, pos: Int) {


                            }
                        })
                        recyclerCalendar.adapter = adapter
                    }
                }else{
                    // 통신이 실패한 경우(응답코드 3xx, 4xx 등)
                    Log.d(TAG, "onResponse: 캘린더실패")
                }
            }

            override fun onFailure(call: Call<List<CalendarData>>, t: Throwable) {
                Log.d(TAG, "onFailure 캘린더 실패 에러2: " + t.message.toString())

            }

        })
    }

    private fun postCalendar(
        startTime: String,
        cdate: String,
        endTime: String,
        ccontent: String,
        date: CalendarDay,
        list: ArrayList<CalendarRecyclerData>
    ) {
        //cdate는 내가 DB에 넣을 날짜이고 date는 사용자가 누른 날짜임 즉 같은날짜지만 형식이 아예 다른 날짜로써 cdate자르기 싫어서 가져옴
        RetrofitBuilder.api.postCalender(
            UserData.couplenum, cdate, startTime, endTime, ccontent, UserData.sex
        ).enqueue(object : Callback<Int> { override fun onResponse(call: Call<Int>, response: Response<Int>) {

        if (response.isSuccessful) {

            var result: Int? = response.body()
            // 정상적으로 통신이 성공된 경우


            //month를 print하면 현재 달로부터 1씩 작게 나와서 DB에 값을 추가할땐 month+1을 해줬음
            //잘 모르지만 이제부터는 그냥 month를 쓰면 됨

            if (hashMap.containsKey(
                    CalendarDay.from(
                        date.year,
                        date.month,
                        date.day
                    )
                )
            ) {
                Log.d("점세개머선일이고", "기존에 같은날짜가 있다면1")
                //기존에 이미 같은 키 값(날짜)이 있다면
                Log.d("HashMap", "여기들어가긴함?5")
                if (hashMap.get(
                        CalendarDay.from(
                            date.year,
                            date.month,
                            date.day
                        )
                    ) == sex
                ) {
                    //근데 같은 성별이라면
                    Log.d("점세개머선일이고", "근데 같은성별이야2")
                    if (sex == "F") {

                        hashMap[CalendarDay.from(
                            date.year,
                            date.month,
                            date.day
                        )] = "F"

                        Log.d("점세개머선일이고", "근데 여자야3")
                        calendar!!.addDecorator(
                            EventDecorator2(
                                Collections.singleton(
                                    CalendarDay.from(
                                        date.year,
                                        date.month,
                                        date.day
                                    )
                                ),
                                mContext,
                                FamaleColor
                            )
                        )

                        Log.d("점세개머선일이고", "근데 여자야 점도 찍었어4")
                        //view.addSpan(CustmMultipleDotSpan(8.0f, FamaleColor))
                    } else if (sex == "M") {
                        hashMap[CalendarDay.from(
                            date.year,
                            date.month,
                            date.day
                        )] = "M"
                        Log.d("점세개머선일이고", "근데 남자야5")
                        calendar!!.addDecorator(
                            EventDecorator2(
                                Collections.singleton(
                                    CalendarDay.from(
                                        date.year,
                                        date.month,
                                        date.day
                                    )
                                ),
                                mContext,
                                MaleColor
                            )
                        )
                        Log.d("점세개머선일이고", "근데 남자야 점도찍었어6")
                        //  view.addSpan(CustmMultipleDotSpan(8.0f, MaleColor))
                    }
                    Log.d("HashMap", "여기들어가긴함?6")
                } else {
                    //아니라면
                    // view.addSpan(CustmMultipleDotSpan(8.0f, CoupleColor))

                    hashMap[CalendarDay.from(
                        date.year,
                        date.month,
                        date.day
                    )] = "C"
                    Log.d("점세개머선일이고", "근데 커플이야7")

                    calendar!!.addDecorator(
                        EventDecorator2(
                            Collections.singleton(
                                CalendarDay.from(
                                    date.year,
                                    date.month,
                                    date.day
                                )
                            ),
                            mContext,
                            CoupleColor
                        )
                    )

                    calendar!!.invalidateDecorators()

                    Log.d("점세개머선일이고", "근데 커플이야 점도 찍었어8")

                    // view.addSpan(CustmMultipleDotSpan(8.0f, CoupleColor))

                }
            } else {
                if (sex == "F") {
                    hashMap[CalendarDay.from(
                        date.year,
                        date.month,
                        date.day
                    )] = "F"

                    Log.d("점세개머선일이고", "아직 날짜가 없어 근데여자야")
                    calendar!!.addDecorator(
                        EventDecorator2(
                            Collections.singleton(
                                CalendarDay.from(
                                    date.year,
                                    date.month,
                                    date.day
                                )
                            ),
                            mContext,
                            FamaleColor
                        )
                    )

                    Log.d("점세개머선일이고", "아직 날짜가 없어 근데여자야 점도찍었어")
                    //  view.addSpan(CustmMultipleDotSpan(8.0f, FamaleColor))
                } else {
                    Log.d("점세개머선일이고", "아직 날짜가 없어 근데남자야")
                    hashMap[CalendarDay.from(
                        date.year,
                        date.month,
                        date.day
                    )] = "M"

                    list.add(
                        CalendarRecyclerData(
                            startTime,
                            endTime,
                            ccontent,
                            cdate,
                            CalendarMode.female
                        )
                    )


                    calendar!!.addDecorator(
                        EventDecorator2(
                            Collections.singleton(
                                CalendarDay.from(
                                    date.year,
                                    date.month,
                                    date.day
                                )
                            ),
                            mContext,
                            MaleColor
                        )
                    )


                    Log.d("점세개머선일이고", "아직 날짜가 없어 근데남자야 점도 찍었어")
                }
            }

        } else {
            // 통신이 실패한 경우(응답코드 3xx, 4xx 등)

            Log.d(
                "Calendar",
                "response 실패 에러"
            )
        }
        }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                Log.d(
                    "Calendar",
                    "onFailure  실패 에러: " + t.message.toString()
                )

            }

        })
    }


     class CustomDialog(val context: Context) {

        //다이얼로그를 만드는 클래스
        //일정입력, 수정, 삭제 모두 해당 클래스를 사용함

        private val dialog = Dialog(context)

        interface ButtonClickListener {
            //버튼클릭 인터페이스
            //myDig()함수에서 완료버튼을 눌렀을 때 제목과 내용을 전달함
            fun onClicked(
                startTime: String,
                endTime: String,
                ccontent: String
            )
        }
        private lateinit var onClickedListener: ButtonClickListener


        fun setOnClickedListener(listener: ButtonClickListener) {
            onClickedListener = listener
        }

        fun adapterMyDig(
            data: CalendarRecyclerData,
            title: String,
            content: String,
            adapter: CalendarAdapter,
            list: ArrayList<CalendarRecyclerData>,
            pos: Int
        ) {

            //일정 수정 및 삭제를 위한 다이얼로그를 생성하는 함수
            dialog.setContentView(com.example.workingparents.R.layout.calendar_dialog_update_delete)
            dialog.window!!.setLayout(
                950,
                WindowManager.LayoutParams.WRAP_CONTENT)
            dialog.window!!.setBackgroundDrawableResource(com.example.workingparents.R.drawable.orangeborder)
            dialog.setCanceledOnTouchOutside(true)
            dialog.setCancelable(true)

            val btnEditStartTime = dialog.findViewById<Button>(com.example.workingparents.R.id.btnEditStartTime)
            val text_waveEdit: TextView = dialog.findViewById(R.id.text_waveEdit)
            val btnDelete = dialog.findViewById<Button>(com.example.workingparents.R.id.btnDelete)
            val btnUpdate = dialog.findViewById<Button>(com.example.workingparents.R.id.btnUpdate)

            val btnEditEndTime: Button = dialog.findViewById(R.id.btnEditEndTime)

            val timepickerEdit: TimePicker = dialog.findViewById(R.id.timepickerEdit)
            val btn_EditdoneTimepicker: Button = dialog.findViewById(R.id.btn_EditdoneTimepicker)
            val btn_EditcancelTimepicker: Button = dialog.findViewById(R.id.btn_EditcancelTimepicker)
            val ccontentEdit: TextView = dialog.findViewById(R.id.ccontentEdit)

            btnEditStartTime.setText(data.startTime)
            btnEditEndTime.setText(data.endTime)
            ccontentEdit.setText(data.ccontent)

            dialog.show()

            var startTime:String?= null
            var endTime:String?= null


            btnEditStartTime.setOnClickListener() {
                //시작시간 버튼 눌렀을 때

                timepickerEdit.setVisibility(View.VISIBLE)
                btn_EditdoneTimepicker.setVisibility(View.VISIBLE)
                btn_EditcancelTimepicker.setVisibility(View.VISIBLE)

                btnUpdate.setVisibility(View.GONE)
                btnDelete.setVisibility(View.GONE)
                btnEditStartTime.setVisibility(View.GONE)
                btnEditEndTime.setVisibility(View.GONE)
                text_waveEdit.setVisibility(View.GONE)
                ccontentEdit.setVisibility(View.GONE)


                timepickerEdit.setOnTimeChangedListener {
                        timepicker, startHour, startMinutes ->
                    startTime= "${startHour}:${startMinutes}"
                    Log.d("TIme", "${startHour}시 ${startMinutes}분")
                }


                btn_EditcancelTimepicker.setOnClickListener() {
                    //취소버튼을 눌렀을 때
                    startTime= null
                    timepickerEdit.setVisibility(View.GONE)
                    btn_EditdoneTimepicker.setVisibility(View.GONE)
                    btn_EditcancelTimepicker.setVisibility(View.GONE)

                    btnUpdate.setVisibility(View.VISIBLE)
                    btnDelete.setVisibility(View.GONE)
                    btnEditStartTime.setVisibility(View.VISIBLE)
                    btnEditEndTime.setVisibility(View.VISIBLE)
                    text_waveEdit.setVisibility(View.VISIBLE)
                    ccontentEdit.setVisibility(View.VISIBLE)


                }

                btn_EditdoneTimepicker.setOnClickListener() {
                    //완료버튼 클릭 시
                    btnEditStartTime.text = startTime
                    timepickerEdit.setVisibility(View.GONE)
                    btn_EditdoneTimepicker.setVisibility(View.GONE)
                    btn_EditcancelTimepicker.setVisibility(View.GONE)


                    btnDelete.setVisibility(View.VISIBLE)
                    btnUpdate.setVisibility(View.VISIBLE)
                    btnEditStartTime.setVisibility(View.VISIBLE)
                    btnEditEndTime.setVisibility(View.VISIBLE)
                    text_waveEdit.setVisibility(View.VISIBLE)
                    ccontentEdit.setVisibility(View.VISIBLE)
                }


            }

            btnEditEndTime.setOnClickListener() {
                //종료시간 버튼 눌렀을 때

                timepickerEdit.setVisibility(View.VISIBLE)
                btn_EditdoneTimepicker.setVisibility(View.VISIBLE)
                btn_EditcancelTimepicker.setVisibility(View.VISIBLE)

                btnUpdate.setVisibility(View.GONE)
                btnDelete.setVisibility(View.GONE)
                btnEditStartTime.setVisibility(View.GONE)
                btnEditEndTime.setVisibility(View.GONE)
                text_waveEdit.setVisibility(View.GONE)
                ccontentEdit.setVisibility(View.GONE)



                timepickerEdit.setOnTimeChangedListener {
                        timepicker, endtHour, endMinutes ->
                    endTime= "${endtHour}:${endMinutes}"
                    Log.d("TIme", "${endtHour}시 ${endMinutes}분")
                }

                btn_EditcancelTimepicker.setOnClickListener() {
                    //취소버튼을 눌렀을 때
                    endTime= null
                    timepickerEdit.setVisibility(View.GONE)
                    btn_EditdoneTimepicker.setVisibility(View.GONE)
                    btn_EditcancelTimepicker.setVisibility(View.GONE)

                    btnUpdate.setVisibility(View.VISIBLE)
                    btnDelete.setVisibility(View.VISIBLE)
                    btnEditStartTime.setVisibility(View.VISIBLE)
                    btnEditEndTime.setVisibility(View.VISIBLE)
                    text_waveEdit.setVisibility(View.VISIBLE)
                    ccontentEdit.setVisibility(View.VISIBLE)


                }

                btn_EditdoneTimepicker.setOnClickListener() {
                    //완료버튼 클릭 시
                    btnEditEndTime.text = endTime
                    timepickerEdit.setVisibility(View.GONE)
                    btn_EditdoneTimepicker.setVisibility(View.GONE)
                    btn_EditcancelTimepicker.setVisibility(View.GONE)

                    btnUpdate.setVisibility(View.VISIBLE)
                    btnDelete.setVisibility(View.VISIBLE)
                    btnEditStartTime.setVisibility(View.VISIBLE)
                    btnEditEndTime.setVisibility(View.VISIBLE)
                    text_waveEdit.setVisibility(View.VISIBLE)
                    ccontentEdit.setVisibility(View.VISIBLE)
                }


            }


            btnUpdate.setOnClickListener() {
                //수정버튼을 눌렀을 때

                //리사이클러뷰 화면 수정
                data.startTime= btnEditStartTime.text.toString()
                data.endTime= btnEditEndTime.text.toString()
                data.ccontent = ccontentEdit.text.toString()

                adapter.updateItem(pos, data)
                adapter.notifyItemRangeChanged(pos, 1)

                //리사이클러뷰 DB수정

                println("커플넘:"+ couplenum +"데이트"+data.cdate+"타이틀"+data.startTime+"내용"+data.ccontent)

                RetrofitBuilder.api.putCalender(couplenum, data.cdate, data.startTime, data.endTime ,data.ccontent).enqueue(object :Callback<Int>{
                    override fun onResponse(call: Call<Int>, response: Response<Int>) {

                        if(response.isSuccessful){
                            val result: Int? = response.body()
                            if(result==1) {
                                Log.d("업데이트", "onResponse: putCalender 성공 ")
                            }
                        }else{
                            Log.d("업데이트", "onResponse: putCalender 실패 ")
                        }
                    }
                    override fun onFailure(call: Call<Int>, t: Throwable) {
                        Log.d("업데이트", "onFailure: putCalender 실패 ")
                    }
                })

                dialog.dismiss()

            }


            btnDelete.setOnClickListener() {
                //삭제버튼을 눌렀을 때

                //리사이클러뷰 화면 삭제
                adapter.deleteItem(pos)
                adapter.notifyItemRemoved(pos)
                adapter.notifyItemRangeChanged(pos, list.size)

                //리사이클러뷰 DB삭제

                RetrofitBuilder.api.deleteCalender(couplenum, data.cdate).enqueue(object :Callback<Int>{
                    override fun onResponse(call: Call<Int>, response: Response<Int>) {

                        if(response.isSuccessful){
                            val result: Int? = response.body()
                            if(result==1) {
                                Log.d("삭제", "onResponse: deleteCalender 성공 ")

                            }
                        }else{
                            Log.d("삭제", "onResponse: deleteCalender 실패 ")
                        }
                    }
                    override fun onFailure(call: Call<Int>, t: Throwable) {
                        Log.d("삭제", "onFailure: deleteCalender 실패 ")
                    }
                })

                dialog.dismiss()
            }

        }

         fun myDig(
            context: Context,
            childFragmentManager: FragmentManager,
            fragmentManager: FragmentManager?
        ) {
            //일정 생성을 위한 다이얼로그를 생성하는 함수

            dialog.setContentView(com.example.workingparents.R.layout.calendar_dialog_create)
            dialog.window!!.setLayout(
                950,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            dialog.window!!.setBackgroundDrawableResource(com.example.workingparents.R.drawable.orangeborder)
            dialog.setCanceledOnTouchOutside(true)
            dialog.setCancelable(true)

            dialog.show()

            val btnStartTime =
                dialog.findViewById<Button>(com.example.workingparents.R.id.btnStartTime)
            val text_wave: TextView = dialog.findViewById(R.id.text_wave)
            val btnEndTime: Button = dialog.findViewById(R.id.btnEndTime)

            val edit_ccontent =
                dialog.findViewById<EditText>(com.example.workingparents.R.id.ccontent)

            val btnDone = dialog.findViewById<Button>(com.example.workingparents.R.id.btnDone)

            var startTime:String?= null
            var endTime:String?= null


            btnStartTime.setOnClickListener() {
                //시작시간 버튼 눌렀을 때

                val timepicker: TimePicker = dialog.findViewById(R.id.timepicker)
                val btn_doneTimepicker: Button = dialog.findViewById(R.id.btn_doneTimepicker)
                val btn_cancelTimepicker: Button = dialog.findViewById(R.id.btn_cancelTimepicker)
                val edit_ccontent: TextView = dialog.findViewById(R.id.ccontent)

                timepicker.setVisibility(View.VISIBLE)
                btn_doneTimepicker.setVisibility(View.VISIBLE)
                btn_cancelTimepicker.setVisibility(View.VISIBLE)

                btnDone.setVisibility(View.GONE)
                btnStartTime.setVisibility(View.GONE)
                btnEndTime.setVisibility(View.GONE)
                text_wave.setVisibility(View.GONE)
                edit_ccontent.setVisibility(View.GONE)


                timepicker.setOnTimeChangedListener {
                        timepicker, startHour, startMinutes ->
                    startTime= "${startHour}:${startMinutes}"
                    Log.d("TIme", "${startHour}시 ${startMinutes}분")
                }


                btn_cancelTimepicker.setOnClickListener() {
                    //취소버튼을 눌렀을 때
                    startTime= null
                    timepicker.setVisibility(View.GONE)
                    btn_doneTimepicker.setVisibility(View.GONE)
                    btn_cancelTimepicker.setVisibility(View.GONE)

                    btnDone.setVisibility(View.VISIBLE)
                    btnStartTime.setVisibility(View.VISIBLE)
                    btnEndTime.setVisibility(View.VISIBLE)
                    text_wave.setVisibility(View.VISIBLE)
                    edit_ccontent.setVisibility(View.VISIBLE)


                }

                btn_doneTimepicker.setOnClickListener() {
                    //완료버튼 클릭 시
                    btnStartTime.text = startTime
                    timepicker.setVisibility(View.GONE)
                    btn_doneTimepicker.setVisibility(View.GONE)
                    btn_cancelTimepicker.setVisibility(View.GONE)

                    btnDone.setVisibility(View.VISIBLE)
                    btnStartTime.setVisibility(View.VISIBLE)
                    btnEndTime.setVisibility(View.VISIBLE)
                    text_wave.setVisibility(View.VISIBLE)
                    edit_ccontent.setVisibility(View.VISIBLE)
                }


            }

            btnEndTime.setOnClickListener() {
                //종료시간 버튼 눌렀을 때

                val timepicker: TimePicker = dialog.findViewById(R.id.timepicker)
                val btn_doneTimepicker: Button = dialog.findViewById(R.id.btn_doneTimepicker)
                val btn_cancelTimepicker: Button = dialog.findViewById(R.id.btn_cancelTimepicker)
                val edit_ccontent: TextView = dialog.findViewById(R.id.ccontent)

                timepicker.setVisibility(View.VISIBLE)
                btn_doneTimepicker.setVisibility(View.VISIBLE)
                btn_cancelTimepicker.setVisibility(View.VISIBLE)

                btnDone.setVisibility(View.GONE)
                btnStartTime.setVisibility(View.GONE)
                btnEndTime.setVisibility(View.GONE)
                text_wave.setVisibility(View.GONE)
                edit_ccontent.setVisibility(View.GONE)



                timepicker.setOnTimeChangedListener {
                        timepicker, endtHour, endMinutes ->
                    endTime= "${endtHour}:${endMinutes}"
                    Log.d("TIme", "${endtHour}시 ${endMinutes}분")
                }

                btn_cancelTimepicker.setOnClickListener() {
                    //취소버튼을 눌렀을 때
                    endTime= null
                    timepicker.setVisibility(View.GONE)
                    btn_doneTimepicker.setVisibility(View.GONE)
                    btn_cancelTimepicker.setVisibility(View.GONE)

                    btnDone.setVisibility(View.VISIBLE)
                    btnStartTime.setVisibility(View.VISIBLE)
                    btnEndTime.setVisibility(View.VISIBLE)
                    text_wave.setVisibility(View.VISIBLE)
                    edit_ccontent.setVisibility(View.VISIBLE)


                }

                btn_doneTimepicker.setOnClickListener() {
                    //완료버튼 클릭 시
                    btnEndTime.text = endTime
                    timepicker.setVisibility(View.GONE)
                    btn_doneTimepicker.setVisibility(View.GONE)
                    btn_cancelTimepicker.setVisibility(View.GONE)

                    btnDone.setVisibility(View.VISIBLE)
                    btnStartTime.setVisibility(View.VISIBLE)
                    btnEndTime.setVisibility(View.VISIBLE)
                    text_wave.setVisibility(View.VISIBLE)
                    edit_ccontent.setVisibility(View.VISIBLE)
                }




            }


            btnDone.setOnClickListener() {
                //완료버튼을 눌렀을 때
                //리스너를 이용해서 제목,내용,성별 전송

                if(startTime == null){
                    Toast.makeText(context, "시작시간을 설정해주세요.", Toast.LENGTH_SHORT).show()

                }else if(endTime == null){
                    Toast.makeText(context, "종료시간을 설정해주세요.", Toast.LENGTH_SHORT).show()

                }else {
                    onClickedListener.onClicked(
                        startTime!!,
                        endTime!!,
                        edit_ccontent.text.toString()
                    )
                    dialog.dismiss()
                }

        }

    }

}

    class EventDecorator2 (
        dates: Collection<CalendarDay>,
        val context: Context,
        val colors: IntArray
    ) : DayViewDecorator {


        var dates= dates

        override fun shouldDecorate(day: CalendarDay): Boolean {
            return dates.contains(day)
        }

        override fun decorate(view: DayViewFacade) {

            view.addSpan(CustmMultipleDotSpan(5.0f, colors))

        }

        class CustmMultipleDotSpan : LineBackgroundSpan {
            private val radius: Float
            private var color = IntArray(0)

            constructor(radius: Float, color: IntArray) {
                this.radius = radius
                this.color = color
            }

            override fun drawBackground(
                canvas: Canvas, paint: Paint,
                left: Int, right: Int, top: Int, baseline: Int, bottom: Int,
                charSequence: CharSequence,
                start: Int, end: Int, lineNum: Int
            ) {
                val total = if (color.size >3 ) 3 else color.size
                Log.d("Colors","color사이즈:"+color.size)
                var leftMost = (total - 1) * -10
                for (i in 0 until total) {
                    val oldColor = paint.color
                    if (color[i] != 0) {
                        paint.color = color[i]
                    }
                    canvas.drawCircle(
                        ((left + right) / 2 - leftMost).toFloat(),
                        bottom + radius,
                        radius,
                        paint
                    )
                    paint.color = oldColor
                    leftMost = leftMost + 20
                }
            }
        }


    }

    class TodayDecorator: DayViewDecorator {
        private var date = CalendarDay.today()

        override fun shouldDecorate(day: CalendarDay?): Boolean {
            return day?.equals(date)!!
        }

        override fun decorate(view: DayViewFacade?) {
            view?.addSpan(StyleSpan(Typeface.BOLD))
            view?.addSpan(RelativeSizeSpan(1.3f))
            view?.addSpan(ForegroundColorSpan(Color.parseColor("#FF9769")))
        }
    }

    class SaturdayDecorator: DayViewDecorator {
        private val calendar = Calendar.getInstance()
        override fun shouldDecorate(day: CalendarDay?): Boolean {
            day?.copyTo(calendar)
            val weekDay = calendar.get(Calendar.DAY_OF_WEEK)
            return weekDay == Calendar.SATURDAY
        }
        override fun decorate(view: DayViewFacade?) {
            view?.addSpan(object: ForegroundColorSpan(Color.rgb(2,82,205)){})
        }
    }

    class SundayDecorator: DayViewDecorator {
        private val calendar = Calendar.getInstance()
        override fun shouldDecorate(day: CalendarDay?): Boolean {
            day?.copyTo(calendar)
            val weekDay = calendar.get(Calendar.DAY_OF_WEEK)
            return weekDay == Calendar.SUNDAY
        }
        override fun decorate(view: DayViewFacade?) {
            view?.addSpan(object: ForegroundColorSpan(Color.rgb(221,46,95)){})
        }
    }
}

