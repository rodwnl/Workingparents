package com.example.workingparents


import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.workingparents.Calendar.CalendarFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*


//MainActivity.kt
class MainActivity : AppCompatActivity() {

    private val fragmentManager = supportFragmentManager
    private var childCaringFragment: ChildCaringFragment? = null
    private var boardFragment: BoardFragment? = null
    private var alramFragment: NoticeFragment? = null
    private var mypageFragment: MypageFragment? = null
    private var calendarFragment: CalendarFragment?=null

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        var bnv_main = findViewById(R.id.bottom_menu) as BottomNavigationView
        bnv_main.itemIconTintList = null
        initBottomNavigation(bnv_main)

        //바텀바 아이템 꾹누르면 이름 떴던거 없앰
        val longClickListener = View.OnLongClickListener { true }
        findViewById<View>(R.id.first_tab).setOnLongClickListener(longClickListener)
        findViewById<View>(R.id.second_tab).setOnLongClickListener(longClickListener)
        findViewById<View>(R.id.third_tab).setOnLongClickListener(longClickListener)
        findViewById<View>(R.id.fourth_tab).setOnLongClickListener(longClickListener)
        findViewById<View>(R.id.fifth_tab).setOnLongClickListener(longClickListener)


        if(UserData.connectedCouple()) { // 부부이면
        } else{

            val bottomSheet = BottomSheetMain() {
            }
            bottomSheet.show((this@MainActivity).supportFragmentManager,bottomSheet.tag)
            //부부연결 해달라는 그림을 보여줌
        }

        // OnNavigationItemSelectedListener를 통해 탭 아이템 선택 시 이벤트를 처리
        // navi_menu.xml에서 설정했던 각 아이템들의 id를 통해 알맞은 프래그먼트로 변경하게 한다.
      /*
        bnv_main.run {

            setOnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.first_tab -> {
                        //다른 프래그먼트 화면으로 이동하는 기능
                        val MainFragment = ChildCaringFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, MainFragment).commit()
                        //아이콘 변경
                        it.setIcon(R.drawable.bottom_main2)
                        menu.findItem(R.id.third_tab).setIcon(R.drawable.bottom_information)
                        menu.findItem(R.id.second_tab).setIcon(R.drawable.bottom_board)
                        menu.findItem(R.id.fourth_tab).setIcon(R.drawable.bottom_mypage)
                        menu.findItem(R.id.fifth_tab).setIcon(R.drawable.bottom_calendar)

                    }
                    R.id.second_tab -> {
                        val boardFragment = BoardFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, boardFragment).commit()

                        it.setIcon(R.drawable.bottom_board2)
                        menu.findItem(R.id.first_tab).setIcon(R.drawable.bottom_main)
                        menu.findItem(R.id.third_tab).setIcon(R.drawable.bottom_information)
                        menu.findItem(R.id.fourth_tab).setIcon(R.drawable.bottom_mypage)
                        menu.findItem(R.id.fifth_tab).setIcon(R.drawable.bottom_calendar)

                    }
                    R.id.third_tab -> {
                        val InfoFragment = InfoFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, InfoFragment).commit()

                        it.setIcon(R.drawable.bottom_information2)
                        menu.findItem(R.id.first_tab).setIcon(R.drawable.bottom_main)
                        menu.findItem(R.id.second_tab).setIcon(R.drawable.bottom_board)
                        menu.findItem(R.id.fourth_tab).setIcon(R.drawable.bottom_mypage)
                        menu.findItem(R.id.fifth_tab).setIcon(R.drawable.bottom_calendar)
                    }

                    R.id.fourth_tab -> {
                        val MypageFragment = MypageFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, MypageFragment).commit()

                        it.setIcon(R.drawable.bottom_mypage2)
                        menu.findItem(R.id.first_tab).setIcon(R.drawable.bottom_main)
                        menu.findItem(R.id.second_tab).setIcon(R.drawable.bottom_board)
                        menu.findItem(R.id.third_tab).setIcon(R.drawable.bottom_information)
                        menu.findItem(R.id.fifth_tab).setIcon(R.drawable.bottom_calendar)
                    }

                    R.id.fifth_tab -> {

                        it.setIcon(R.drawable.bottom_calendar2)
                        menu.findItem(R.id.first_tab).setIcon(R.drawable.bottom_main)
                        menu.findItem(R.id.second_tab).setIcon(R.drawable.bottom_board)
                        menu.findItem(R.id.third_tab).setIcon(R.drawable.bottom_information)
                        menu.findItem(R.id.fourth_tab).setIcon(R.drawable.bottom_mypage)
                    }

                }
                true
            }
            selectedItemId = R.id.first_tab

        }
      */
    }


    private fun initBottomNavigation(bottomNavi: BottomNavigationView) {

        var menu = bottomNavi.menu

        // 최초로 보이는 프래그먼트
        childCaringFragment = ChildCaringFragment()
        fragmentManager.beginTransaction().replace(R.id.container, childCaringFragment!!).commit()

        bottomNavi.run {

            setOnNavigationItemSelectedListener {
                /*
                  최초 선택 시 fragment add, 선택된 프래그먼트 show, 나머지 프래그먼트 hide

                  다른 프래그먼트로 화면이 전환되었을 때 이전에 만든 프래그먼트가 유지될수있도록 하기위함이다.
                  예를들어 사용자가 육아분담프래그먼트에서 저번달을 선택해서 분담목록을 보다가 문득 캘린더에서 저번달 스케쥴이 보고싶어짐. ->캘린더프래그먼트로 이동,확인함
                  -> 다시 육아분담프래그먼트로 돌아왔음 -> 기존: 프래그먼트 재생성되어 이번달정보가 보이는게 아니라 오늘정보가 보임
                                                     개선: 재생성되지않아 저번달이 그대로 보임
                 */
                when (it.itemId) {

                    R.id.first_tab -> {
                        if (childCaringFragment == null) {
                            childCaringFragment = ChildCaringFragment()
                            fragmentManager.beginTransaction().add(R.id.container, childCaringFragment!!).commit()
                        }
                        if (childCaringFragment != null) fragmentManager.beginTransaction().show(childCaringFragment!!).commit()
                        if (calendarFragment != null) fragmentManager.beginTransaction().hide(calendarFragment!!).commit()
                        if (boardFragment != null) fragmentManager.beginTransaction().hide(boardFragment!!).commit()
                        if (alramFragment != null) fragmentManager.beginTransaction().hide(alramFragment!!).commit()
                        if (mypageFragment != null) fragmentManager.beginTransaction().hide(mypageFragment!!).commit()

                        //아이콘 변경
                        it.setIcon(R.drawable.bottom_main2)
                        menu.findItem(R.id.third_tab).setIcon(R.drawable.bottom_information)
                        menu.findItem(R.id.second_tab).setIcon(R.drawable.bottom_board)
                        menu.findItem(R.id.fourth_tab).setIcon(R.drawable.bottom_mypage)
                        menu.findItem(R.id.fifth_tab).setIcon(R.drawable.bottom_calendar)

                    }
                    R.id.second_tab -> {
                        if (boardFragment == null) {
                            boardFragment = BoardFragment()
                            fragmentManager.beginTransaction()
                                .add(R.id.container, boardFragment!!).commit()
                        }
                        if (childCaringFragment != null) fragmentManager.beginTransaction().hide(childCaringFragment!!).commit()
                        if(calendarFragment != null) fragmentManager.beginTransaction().hide(calendarFragment!!).commit()
                        if (boardFragment != null) fragmentManager.beginTransaction().show(boardFragment!!).commit()
                        if (alramFragment != null) fragmentManager.beginTransaction().hide(alramFragment!!).commit()
                        if (mypageFragment != null) fragmentManager.beginTransaction().hide(mypageFragment!!).commit()

                        it.setIcon(R.drawable.bottom_board2)
                        menu.findItem(R.id.first_tab).setIcon(R.drawable.bottom_main)
                        menu.findItem(R.id.third_tab).setIcon(R.drawable.bottom_information)
                        menu.findItem(R.id.fourth_tab).setIcon(R.drawable.bottom_mypage)
                        menu.findItem(R.id.fifth_tab).setIcon(R.drawable.bottom_calendar)
                    }
                    R.id.third_tab -> {
                        if (alramFragment == null) {
                            alramFragment = NoticeFragment()
                            fragmentManager.beginTransaction()
                                .add(R.id.container, alramFragment!!).commit()
                        }
                        if (childCaringFragment != null) fragmentManager.beginTransaction().hide(childCaringFragment!!).commit()
                        if (calendarFragment != null) fragmentManager.beginTransaction().hide(calendarFragment!!).commit()
                        if (boardFragment != null) fragmentManager.beginTransaction().hide(boardFragment!!).commit()
                        if (alramFragment != null) fragmentManager.beginTransaction().show(alramFragment!!).commit()
                        if (mypageFragment != null) fragmentManager.beginTransaction().hide(mypageFragment!!).commit()


                        it.setIcon(R.drawable.bottom_information2)
                        menu.findItem(R.id.first_tab).setIcon(R.drawable.bottom_main)
                        menu.findItem(R.id.second_tab).setIcon(R.drawable.bottom_board)
                        menu.findItem(R.id.fourth_tab).setIcon(R.drawable.bottom_mypage)
                        menu.findItem(R.id.fifth_tab).setIcon(R.drawable.bottom_calendar)
                    }
                    R.id.fourth_tab -> {

                        if(mypageFragment == null) {
                            mypageFragment = MypageFragment()
                            fragmentManager.beginTransaction()
                                .add(R.id.container, mypageFragment!!).commit()
                        }

                        if (childCaringFragment != null) fragmentManager.beginTransaction().hide(childCaringFragment!!).commit()
                        if (calendarFragment != null) fragmentManager.beginTransaction().hide(calendarFragment!!).commit()
                        if (boardFragment != null) fragmentManager.beginTransaction().hide(boardFragment!!).commit()
                        if (alramFragment != null) fragmentManager.beginTransaction().hide(alramFragment!!).commit()
                        if (mypageFragment != null) fragmentManager.beginTransaction().show(mypageFragment!!).commit()



                        it.setIcon(R.drawable.bottom_mypage2)
                        menu.findItem(R.id.first_tab).setIcon(R.drawable.bottom_main)
                        menu.findItem(R.id.second_tab).setIcon(R.drawable.bottom_board)
                        menu.findItem(R.id.third_tab).setIcon(R.drawable.bottom_information)
                        menu.findItem(R.id.fifth_tab).setIcon(R.drawable.bottom_calendar)
                    }


                    R.id.fifth_tab->{

                        if (calendarFragment == null) {
                            calendarFragment = CalendarFragment()
                            fragmentManager.beginTransaction()
                                .add(R.id.container, calendarFragment!!).commit()
                        }
                        if (childCaringFragment != null) fragmentManager.beginTransaction().hide(childCaringFragment!!).commit()
                        if (calendarFragment != null) fragmentManager.beginTransaction().show(calendarFragment!!).commit()
                        if (boardFragment != null) fragmentManager.beginTransaction().hide(boardFragment!!).commit()
                        if (alramFragment != null) fragmentManager.beginTransaction().hide(alramFragment!!).commit()
                        if (mypageFragment != null) fragmentManager.beginTransaction().hide(mypageFragment!!).commit()


                        it.setIcon(R.drawable.bottom_calendar2)
                        menu.findItem(R.id.first_tab).setIcon(R.drawable.bottom_main)
                        menu.findItem(R.id.second_tab).setIcon(R.drawable.bottom_board)
                        menu.findItem(R.id.third_tab).setIcon(R.drawable.bottom_information)
                        menu.findItem(R.id.fourth_tab).setIcon(R.drawable.bottom_mypage)
                    }

                }
                true

            }
            selectedItemId = R.id.first_tab
        }
    }
}

