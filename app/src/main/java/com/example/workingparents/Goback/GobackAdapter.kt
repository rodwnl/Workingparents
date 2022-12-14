package com.example.workingparents.Goback

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.workingparents.FCMRetrofitBuilder
import com.example.workingparents.R
import com.example.workingparents.RetrofitBuilder
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.goback_item.view.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class GobackAdapter(private val items: ArrayList<GobackData>) : RecyclerView.Adapter<GobackAdapter.ViewHolder>() {

    private var TAG="Goback"

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: GobackAdapter.ViewHolder, position: Int) {

        val item = items[position]
        val listener = View.OnClickListener { it ->
            //Toast.makeText(it.context, "Clicked -> ID : ${item.goback_childName}", Toast.LENGTH_SHORT).show()
        }
        holder.apply {
            bind(listener, item)
            itemView.tag = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.goback_item, parent, false)
        return GobackAdapter.ViewHolder(inflatedView)
    }

    // 각 항목에 필요한 기능을 구현
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v
        fun bind(listener: View.OnClickListener, item: GobackData) {

            var valiGo=false
            view.goback_childName.text = item.goback_childName

            //등원 버튼 클릭 시
            //알람보내기
            view.goback_btnGo.setOnClickListener {
                Toast.makeText(
                    it.context,
                   "등원알림을 보냈어요",
                    Toast.LENGTH_SHORT

                ).show()

                RetrofitBuilder.api.getTokenForGoback(item.goback_couplenum).enqueue(object :
                    Callback<List<String>> {
                    override fun onResponse(
                        call: Call<List<String>>,
                        response: Response<List<String>>
                    ) {
                        if (response.isSuccessful) {

                            var result: List<String>? = response.body()
                            if (result != null) {
                                Log.d(TAG, "onResponse 성공: getTokenForGoback")

                                for (token: String in result) {

                                        valiGo= true
                                        requestPushAlram(token, valiGo, item.goback_couplenum)
                                        Log.d(TAG, token + "에게 알람보냄 ")

                                }

                            } else {
                                Log.d(TAG, "결과가 없음")
                            }
                        } else {
                            Log.d(TAG, "동네등록안함")
                        }
                    }

                    override fun onFailure(call: Call<List<String>>, t: Throwable) {
                        Log.d(TAG, "실패3" + t.message)
                    }


                })
            }

            //하원 버튼 클릭 시
            //알람보내기
            view.goback_btnBack.setOnClickListener {
                Toast.makeText(
                    it.context,
                    "하원알림을 보냈어요",
                    Toast.LENGTH_SHORT

                ).show()

                RetrofitBuilder.api.getTokenForGoback(item.goback_couplenum).enqueue(object :
                    Callback<List<String>> {
                    override fun onResponse(
                        call: Call<List<String>>,
                        response: Response<List<String>>
                    ) {
                        if (response.isSuccessful) {

                            var result: List<String>? = response.body()
                            if (result != null) {
                                Log.d(TAG, "onResponse 성공: getTokenForGoback")

                                for (token: String in result) {

                                    valiGo= false
                                    requestPushAlram(token, valiGo, item.goback_couplenum)
                                    Log.d(TAG, token + "에게 알람보냄 ")

                                }

                            } else {
                                Log.d(TAG, "결과가 없음")
                            }
                        } else {
                            Log.d(TAG, "동네등록안함")
                        }
                    }

                    override fun onFailure(call: Call<List<String>>, t: Throwable) {
                        Log.d(TAG, "실패3" + t.message)
                    }


                })
            }

        }

        fun requestPushAlram(token: String, valiGo: Boolean, gobackCouplenum: Int) {

            var obj:JsonObject?= null


            if(valiGo==true) {
                obj = FCMRetrofitBuilder.takeJsonObject(
                    token, "등원 알람이 등록되었어요.",
                    "${view.goback_childName.text} 어린이 무사히 등원 완료!"

                )
            }else{
               obj = FCMRetrofitBuilder.takeJsonObject(
                    token, "하원 알람이 등록되었어요.",
                    "${view.goback_childName.text} 어린이 무사히 하원 완료!")

            }

            FCMRetrofitBuilder.api.pushAlram(obj.toString()).enqueue(object : Callback<ResponseBody> {

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "onResponse 성공:"+token)

                        if(valiGo==true) {
                            RetrofitBuilder.api.postAlarmGoback(
                                gobackCouplenum.toString(),
                                2,
                                "자녀가 방금 등원하였습니다."
                            ).enqueue(object : Callback<Int> {
                                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                                    if (response.isSuccessful) {
                                        var result: Int? = response.body()
                                        // 정상적으로 통신이 성공된 경우
                                        Log.d(TAG, "onResponse: 등원 알람 성공" + result?.toString())

                                    } else {
                                        // 통신이 실패한 경우(응답코드 3xx, 4xx 등)
                                        Log.d(TAG, "onResponse 등원 알람 실패")

                                    }
                                }

                                override fun onFailure(call: Call<Int>, t: Throwable) {
                                    Log.d(TAG, "onFailure 등원 알람 실패 : " + t.message.toString())
                                }

                            })
                        }else{

                            RetrofitBuilder.api.postAlarmGoback(
                                gobackCouplenum.toString(),
                                3,
                                "자녀가 방금 하원하였습니다."
                            ).enqueue(object : Callback<Int> {
                                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                                    if (response.isSuccessful) {
                                        var result: Int? = response.body()
                                        // 정상적으로 통신이 성공된 경우
                                        Log.d(TAG, "onResponse: 하원 알람 성공" + result?.toString())

                                    } else {
                                        // 통신이 실패한 경우(응답코드 3xx, 4xx 등)
                                        Log.d(TAG, "onResponse 하원 알람 실패")

                                    }
                                }

                                override fun onFailure(call: Call<Int>, t: Throwable) {
                                    Log.d(TAG, "onFailure 하원 알람 실패 : " + t.message.toString())
                                }

                            })


                        }







                    } else {
                        Log.d(TAG, "onResponse 실패: pushAlram");
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d(TAG, "onFailure 에러: " + t.message.toString());
                }
            })
        }
    }
}