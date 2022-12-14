package com.example.workingparents
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workingparents.NoticeFragment.Companion.noticeAdapter
import kotlinx.android.synthetic.main.fragment_teacher_notice.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private lateinit var mContext: Activity
private var TAG="Notice"
lateinit var notices : ArrayList<Notice>

class NoticeFragment : Fragment() {

    companion object{
        lateinit var noticeAdapter: NoticeAdapter
        lateinit var noticerecyclerView: RecyclerView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is TeacherMainActivity){
            //writenotice_btn.visibility=View.VISIBLE
            mContext=context
        }

        if(context is MainActivity){
            //writenotice_btn.visibility=View.GONE
            mContext=context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        var view =inflater.inflate(R.layout.fragment_teacher_notice, container, false)
        val writenotice_btn = view.findViewById<ImageButton>(R.id.writenotice_btn)
        noticerecyclerView = view.findViewById<RecyclerView>(R.id.rv_notice)

        if(mContext is MainActivity){
            writenotice_btn.visibility=View.GONE
        }else{
            writenotice_btn.visibility=View.VISIBLE
        }

        writenotice_btn.setOnClickListener{
            Log.d(TAG,"클릭됨")
            val intent = Intent(mContext, WriteNoticeActivity::class.java)
            mContext.startActivity(intent)
        }

        RetrofitBuilder.api.getNotice().enqueue(object : Callback<List<Notice>> {
            override fun onResponse(call: Call<List<Notice>>, response: Response<List<Notice>>) {
                if (response.isSuccessful) {
                    notices = response.body() as ArrayList<Notice>
                    //맨 최근 것 부터 담김
                    Log.d(TAG, notices.toString())
                    noticerecyclerView.addItemDecoration(MyDecoration(2, Color.parseColor("#f2f2f2")))
                    noticerecyclerView.layoutManager= LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
                    //리사이클러뷰 선언
                    noticerecyclerView.visibility=View.VISIBLE
                    noticerecyclerView.setHasFixedSize(true) //리사이클러뷰 성능 개선?
                    noticeAdapter = NoticeAdapter(notices,mContext)
                    noticerecyclerView.adapter= noticeAdapter//adapter 선언
                } else {
                    Log.d(TAG, "onResponse 후 Notice 실패 에러: ")
                    // 통신이 실패한 경우(응답코드 3xx, 4xx 등)
                }
            }
            override fun onFailure(call: Call<List<Notice>>, t: Throwable) {
                Log.d(TAG, "onFailure 연결 실패 에러 테스트: " + t.message.toString())
            }
        })

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


}

fun refreshNotice(result : Notice){
    notices.add(0,result)
    if(notices.size==1)
    {
        NoticeFragment.noticerecyclerView.visibility=View.VISIBLE
        NoticeFragment.noticerecyclerView.setHasFixedSize(true) //리사이클러뷰 성능 개선?
        noticeAdapter = NoticeAdapter(notices,mContext)
        NoticeFragment.noticerecyclerView.adapter= noticeAdapter//adapter 선언
        noticeAdapter.notifyDataSetChanged()

    }else{
        NoticeAdapter(notices, mContext)
        noticeAdapter.notifyItemInserted(0)
        NoticeFragment.noticerecyclerView.smoothScrollToPosition(0)
    }
}

//삭제하고 refresh
fun deleteNoticeAdapter(position : Int){
    noticeAdapter.notifyItemRemoved(position)
    notices.removeAt(position)
    noticeAdapter.notifyItemChanged(position, notices.size)
}


