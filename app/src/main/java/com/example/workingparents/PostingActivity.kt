package com.example.workingparents

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workingparents.PostingActivity.Companion.post_ccnt
import kotlinx.android.synthetic.main.activity_posting.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.properties.Delegates

private val TAG="PostingActivity"
private var comments = ArrayList<Comment>()
private var ccomments = ArrayList<Ccomment>()
private val dataList = ArrayList<Dataitem>()
private var postingcno :Int = 0
private var pccnt:Int = 0
private var adapter: CommentAdapter? = null
private var post_id: String? =null



class PostingActivity : AppCompatActivity() {

    init {
        instance=this
    }

    companion object{
        lateinit var instance:PostingActivity
        lateinit var recyclerView: RecyclerView
        lateinit var handler: Handler
        lateinit var msg : Message
        var pno by Delegates.notNull<Int>()
        lateinit var post_ccnt : TextView
        lateinit var input : EditText
        lateinit var sendbtn : ImageButton
        lateinit var deletebtn : ImageButton
        var position by Delegates.notNull<Int>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posting)

        recyclerView= findViewById(R.id.comment_rv)
        post_ccnt=findViewById(R.id.ccnt)
        input=findViewById(R.id.input1)
        sendbtn=findViewById(R.id.sendBtn)
        deletebtn=findViewById(R.id.deleteBtn)


        pid.setText(intent.getStringExtra("rv_pid"))
        village.setText(intent.getStringExtra("rv_village"))
        goback.setText(intent.getStringExtra("rv_goback"))
        pcontent.setText(intent.getStringExtra("rv_pcontent"))
        hcnt.setText(intent.getStringExtra("rv_hcnt"))
        post_ccnt.setText(intent.getStringExtra("rv_ccnt"))
        pccnt = intent.getStringExtra("rv_ccnt")?.toInt()!!
        pno = intent.getStringExtra("rv_pno")?.toInt()!!
        post_id = intent.getStringExtra("rv_pid")
        position= intent.getStringExtra("rv_position")?.toInt()!!

        var post_time : String = intent.getStringExtra("rv_pdate")!!
        val stringBuilder = StringBuilder()

        stringBuilder.append(post_time.substring(5,7))
        stringBuilder.append("/")
        stringBuilder.append(post_time.substring(8,10))
        stringBuilder.append(" ")
        stringBuilder.append(post_time.substring(11,16))
        posttime.setText(stringBuilder.toString())


        Log.d(TAG, "??? ?????? ????????? ????????????"+(intent.getStringExtra("rv_pdate")))


        comments.clear()
        ccomments.clear()
        dataList.clear()

        comment_rv.layoutManager= LinearLayoutManager(this@PostingActivity, LinearLayoutManager.VERTICAL, false)
        handler=MainHandler()

        val checkCmentThread = CheckCmentThread(pno) //insert thread ????????????
        checkCmentThread.start()

        Log.d(TAG, pno.toString())
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //???????????? ????????? ?????????+????????? ?????????
        input.setOnClickListener {
            input.post(Runnable {
                input.setFocusableInTouchMode(true)
                input.requestFocus()
                imm.showSoftInput(input, 0)
            })

        }

        sendbtn.setOnClickListener {
            var comment = input.text.toString()

            if (pno != null) {
                //checkcomment?????? cno??????????????? ??????????????????

                Log.d(TAG, "333333333333333")
                Log.d(TAG,"checkThread ??????")
                val insertCmentThread = InsertCmentThread(pno,comment) //insert thread ????????????
                insertCmentThread.InsertCmentThread2(pno,comment)
                insertCmentThread.start()
                input1.setText(null)
                imm.hideSoftInputFromWindow(input1.windowToken,0)
            }

            Log.d(TAG, pno.toString())
            Log.d(TAG, postingcno.toString())
            Log.d(TAG, UserData.id.toString())
            Log.d(TAG, comment)

        }

        deletebtn.setOnClickListener {
            if(UserData.id.equals(post_id))
            {
                Log.d(TAG,"?????? ?????? ??????")
                val builder = AlertDialog.Builder(this)
                    .setMessage("???????????? ?????????????????????????")
                    .setPositiveButton("??????",
                        DialogInterface.OnClickListener { dialog, which ->
                            val deletePostingThread = DeletePostingThread(pno) //insert thread ????????????
                            deletePostingThread.start()
                            finish()
                        })
                    .setNegativeButton("??????",
                        DialogInterface.OnClickListener { dialog, which ->
                        })
                builder.show()
            }
        }
    }

    //????????????????
    override fun onBackPressed() {
        Log.d(TAG,"?????? ??? ????????????"+ position.toString())
        positionAdapter(position)
        super.onBackPressed()
    }


    class CheckCmentThread(pno: Int?) : Thread() {
        private var pno = pno
        override fun run() {
            super.run()
            pno?.let {
                RetrofitBuilder.api.getComment(it).enqueue(object : Callback<List<Comment>>{
                    override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                        if (response.isSuccessful) {

                            Log.d(TAG, "1111111111111111")
                            var result: List<Comment>? = response.body()
                            comments= response.body() as ArrayList<Comment>
                            //????????? ????????????
                            Log.d(TAG, "222222222222")
                            msg= handler.obtainMessage(StateSet.BoardMsg.MSG_SUCCESS_GET_CMENTS)
                            handler.handleMessage(msg)

                        } else {
                            // ????????? ????????? ??????(???????????? 3xx, 4xx ???)
                        }
                    }

                    override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                        Log.d(TAG, "onFailure ?????? ?????? : " + t.message.toString())
                    }

                })
            }
        }

    }


    class InsertCmentThread(pno: Int?, comment: String) : Thread() {
        private var pno = 0
        private var cment: String? = null
        fun InsertCmentThread2(pno: Int, cment: String) {
            Log.d(TAG, "4444444444444")
            this.pno = pno
            this.cment = cment
            Log.d(TAG, comments.toTypedArray().toString() + "")
            if (pno != null) {
                if (comments.isEmpty()) {
                    Log.d(TAG, "empty??????")
                    postingcno = 1
                } else {
                    for (comment in comments) {         //????????? cno??????
                        if (comment.cno > postingcno) {
                            postingcno = comment.cno
                        }
                    }
                    postingcno++
                    Log.d(TAG, "cno ????????????"+postingcno)
                }
            }
        }
        override fun run() {
            super.run()
            RetrofitBuilder.api.postComment(pno, postingcno, UserData.id, cment)
                .enqueue(object : Callback<Comment> {
                    override fun onResponse(call: Call<Comment>, response: Response<Comment>) {
                        if (response.isSuccessful) {
                            Log.d(TAG, "55555555555555")

                            var result: Comment? = response.body()
                            // ??????????????? ????????? ????????? ??????
                            Log.d(TAG, "onResponse: ?????? ??????" + result?.toString())

                            comments.add(result!!)
                            var dataitem= Dataitem()
                            dataitem.Commentitem(result,1)
                            adapter?.cmentDataList?.add(dataitem)
                            adapter?.notifyDataSetChanged()
                            updateCmentCnt(pno,"plus") //?????? ??? ??????
                            Log.d(TAG,"?????? notify ??? result" + comments.toString())

                        } else {
                            // ????????? ????????? ??????(???????????? 3xx, 4xx ???)
                        }
                    }
                    override fun onFailure(call: Call<Comment>, t: Throwable) {
                        Log.d(TAG, "onFailure ?????? ?????? : " + t.message.toString())
                    }

                })
        }

    }

    class CheckCcmentThread(pno: Int?) : Thread() {
        private var pno = pno
        override fun run() {
            super.run()
            pno?.let {
                RetrofitBuilder.api.getCcomment(it).enqueue(object : Callback<List<Ccomment>> {
                    override fun onResponse(call: Call<List<Ccomment>>, response: Response<List<Ccomment>>) {
                        if (response.isSuccessful) {

                            var result: List<Ccomment>? = response.body()
                            ccomments= response.body() as ArrayList<Ccomment>
                            msg = handler.obtainMessage(StateSet.BoardMsg.MSG_SUCCESS_GET_CCMENTS)
                            handler.sendMessage(msg)

                            // ??????????????? ????????? ????????? ??????
                            Log.d(TAG, "onResponse: thread ????????? ???????????? ??????" + result?.toString())

                        } else {
                            // ????????? ????????? ??????(???????????? 3xx, 4xx ???)
                        }
                    }

                    override fun onFailure(call: Call<List<Ccomment>>, t: Throwable) {
                        Log.d(TAG, "onFailure ????????? ?????? : " + t.message.toString())
                    }

                })
            }
        }
    }

    class DeletePostingThread(pno: Int?) : Thread() {
        private var pno = pno
        override fun run() {
            super.run()
            pno?.let {
                RetrofitBuilder.api.deleteBoardPosting(pno!!).enqueue(object : Callback<Int> {
                    override fun onResponse(call: Call<Int>, response: Response<Int>) {
                        if (response.isSuccessful) {

                            var result: Int? = response.body()
                            if (result!!.toInt() == 1) {
                                msg = handler.obtainMessage(StateSet.BoardMsg.MSG_SUCCESS_DEL_POSTING)
                                handler.sendMessage(msg)
                            }

                        } else {
                            // ????????? ????????? ??????(???????????? 3xx, 4xx ???)
                        }
                    }

                    override fun onFailure(call: Call<Int>, t: Throwable) {
                        Log.d(TAG, "onFailure ????????? ?????? : " + t.message.toString())
                    }

                })
            }
        }
    }


    internal class MainHandler : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {

                StateSet.BoardMsg.MSG_SUCCESS_GET_CMENTS->{
                    for (cment in comments) {
                        var withcment = false
                        if (cment.cccnt > 0) {
                            Log.d(TAG, "1?????? ??????????????????"+dataList.toString())
                            val CheckCcmentThread = CheckCcmentThread(pno)
                            //insert thread ????????????
                            CheckCcmentThread.start()
                            withcment=true
                            break
                        }
                        if(withcment==false){
                            //????????? ????????? ?????? ?????? ?????? ?????????
                            var dataitem= Dataitem()
                            dataitem.Commentitem(cment,1)
                            dataList.add(dataitem)
                            Log.d(TAG, "????????? ?????? dataitem?????????" + dataitem)
                        }
                    }
                    var msg : Message = handler.obtainMessage(StateSet.BoardMsg.MSG_SUCCESS_GET_ALLCMENTS)
                    handler.sendMessage(msg)
                }

                StateSet.BoardMsg.MSG_SUCCESS_GET_CCMENTS->{
                    //????????? ???????????? dataList??? ?????????????????? ????????? ???????????? ?????? ???????????????.
                    //?????? ??? DB?????? ???????????? ????????? ?????? ??????????????? ?????? ??????????????? ????????? ?????? ??? ??????????????? ?????? ??? ????????? ???.  ---->????????? 12/01
                    for (cment in comments) {
                        var dataitem= Dataitem()
                        dataitem.Commentitem(cment,1)
                        dataList.add(dataitem)
                        Log.d(TAG, "????????? handler ??? ????????? ?????? ?????? ??????")
                        for (ccment in ccomments) {
                            if (cment.cno === ccment.cno) {
                                var dataitem= Dataitem()
                                dataitem.Ccommentitem(ccment,2)
                                dataList.add(dataitem)
                                Log.d(TAG, "????????? handler ??? ????????? ??????")

                            }
                        }
                    }
                    var msg : Message = handler.obtainMessage(StateSet.BoardMsg.MSG_SUCCESS_GET_ALLCMENTS)
                    handler.sendMessage(msg)

                }

                StateSet.BoardMsg.MSG_SUCCESS_GET_ALLCMENTS -> {
                    //?????????????????? ??????
                    Log.d(TAG, "3?????? ??????????????????"+dataList.toString())
                    recyclerView.visibility=View.VISIBLE
                    recyclerView.setHasFixedSize(true) //?????????????????? ?????? ???????
                    adapter= CommentAdapter(dataList)
                    recyclerView.adapter= adapter
                }

                StateSet.BoardMsg.MSG_SUCCESS_DEL_POSTING -> {
                    deleteAdapter(position)
                }

            }
        }
    }


}

//?????? ????????? update
fun updateCmentCnt(pno: Int, sign: String) {

    //?????? ??? ccnt??? ???????????? ????????? ?????? ?????????
    if(sign=="minus")
    {
        pccnt--
       post_ccnt.setText(pccnt.toString())
    }
    else if(sign=="plus")
    {
        pccnt++
        post_ccnt.setText(pccnt.toString())
    }


    RetrofitBuilder.api.putCommentCnt(pno, sign).enqueue(object : Callback<Int> {
        override fun onResponse(call: Call<Int>, response: Response<Int>) {
            if (response.isSuccessful) {
                var result: Int? = response.body()
                // ??????????????? ????????? ????????? ??????
                Log.d(TAG, "onResponse: ?????? ????????? ??????" + result?.toString())
            } else {
                // ????????? ????????? ??????(???????????? 3xx, 4xx ???)
            }
        }

        override fun onFailure(call: Call<Int>, t: Throwable) {
            Log.d(TAG, "onFailure ?????? ?????? : " + t.message.toString())
        }

    })
}

//?????????????????? update
fun updateCcmentCnt(pno: Int, cno:Int, sign: String) {

    RetrofitBuilder.api.putCcommentCnt(pno,cno,sign).enqueue(object : Callback<Int> {
        override fun onResponse(call: Call<Int>, response: Response<Int>) {
            if (response.isSuccessful) {
                var result: Int? = response.body()
                if (sign == "plus") {
                    Log.d("tag", "cccnt++")
                } else {
                    Log.d("tag", "cccnt--")
                }
            } else {
                // ????????? ????????? ??????(???????????? 3xx, 4xx ???)
            }
        }

        override fun onFailure(call: Call<Int>, t: Throwable) {
            Log.d(TAG, "onFailure ?????? ?????? : " + t.message.toString())
        }
    })

}
