package com.example.termproject

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import com.example.termproject.databinding.ActivityStudytimerBinding


class StudyTimerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudytimerBinding
    private var totalCycles = 4
    private var studyMinutes = 1
    private var isRunning = false
    private var currentCycle = 1
    private var isBreak = false
    private var job: Job? = null
    private var remainingSeconds = 0
    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityStudytimerBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        updateCycleText()
        updateTimeText(studyMinutes * 60)

        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // 타이머 시간 설정 클릭
        binding.time.setOnClickListener {
            showTimeInputDialog()
        }

        binding.pause.setOnClickListener {
            if (isRunning) {
                // 일시정지
                job?.cancel()
                isRunning = false

                // 아이콘 변경
                binding.pause.setImageResource(R.drawable.timer_start)

                // 색상도 회색으로
                updateState()

                binding.progressBarCircle.progress = if (remainingSeconds == 0) 1 else remainingSeconds
            } else {
                // 재생 시작
                startPomodoro()
                updateState()

                // 아이콘 변경: ⏸로
                binding.pause.setImageResource(R.drawable.timer_pause)
            }
        }

        // 네비게이션 버튼들
        // MainActivity 가는 버튼 구현
        binding.mainBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // TestReadyActivity 가는 버튼 구현
        binding.takeTestBtn.setOnClickListener {
            val intent = Intent(this, TestReadyActivity::class.java)
            startActivity(intent)
        }

        // ShowRateActivity 가는 버튼 구현
        binding.rateBtn.setOnClickListener {
            val intent = Intent(this, ShowRateActivity::class.java)
            startActivity(intent)
        }

        // StudyTimerActivity 가는 버튼 구현
        binding.timerBtn.setOnClickListener {
            val intent = Intent(this, StudyTimerActivity::class.java)
            startActivity(intent)
        }

        // Drawer 열기
        binding.hamburgerButton.setOnClickListener {
            binding.drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
        }
    }

    private fun startPomodoro() {
        isRunning = true

        // 최초 시작이면 초기화
        if (currentCycle == 1 && remainingSeconds == 0) {
            isBreak = false
            startTimer(studyMinutes * 60)
        } else {
            // 이미 사이클 도는 중이면 재개
            startTimer(remainingSeconds)
        }
    }

    private fun startTimer(seconds: Int) {
        val total = if (isBreak) 5 * 60 else studyMinutes * 60
        remainingSeconds = seconds

        binding.progressBarCircle.max = total

        // 바 색상 설정
        val drawableRes = if (isBreak) R.drawable.drawable_circle_outer_grey else R.drawable.drawable_circle_outer
        binding.progressBarCircle.progressDrawable = resources.getDrawable(drawableRes, null)
        binding.progressBarCircle.progress = if (remainingSeconds == 0) 1 else remainingSeconds

        job = CoroutineScope(Dispatchers.Main).launch {
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
                updateTimeText(remainingSeconds)

                // 깜빡임 방지용 최소값 유지
                binding.progressBarCircle.progress = if (remainingSeconds == 0) 1 else remainingSeconds
            }
            onTimerComplete()
        }
    }


    private fun onTimerComplete() {
        if (!isRunning) return
        if (isBreak) {
            // 휴식 끝나고 다음 사이클로
            currentCycle++
            if (currentCycle > totalCycles) {
                Toast.makeText(this, "모든 사이클 완료!", Toast.LENGTH_SHORT).show()
                stopTimer()
                return
            }
            isBreak = false
            startTimer(studyMinutes * 60)
        } else {
            // 공부 끝 → 휴식 시작
            isBreak = true
            Toast.makeText(this, "휴식 시간입니다 (5분)", Toast.LENGTH_SHORT).show()
            startTimer(5 * 60)
        }

        updateState()
        updateCycleText()
    }

    private fun stopTimer() {
        job?.cancel()
        isRunning = false
        currentCycle = 0
        isBreak = false
        updateTimeText(studyMinutes * 60)
    }

    private fun updateTimeText(seconds: Int) {
        val min = seconds / 60
        val sec = seconds % 60
        binding.time.text = String.format("%02d  :  %02d", min, sec)
    }

    // 공부 <-> 휴식 상태 변경
    private fun updateState() {
        if (!isBreak) {
            binding.state.text = "공부"
            binding.timeMode.text = "${studyMinutes}분"

            if (isRunning) {
                binding.state.setTextColor(Color.parseColor("#4455C4"))
                binding.timeMode.setTextColor(Color.parseColor("#4455C4"))
                binding.progressBarCircle.progressDrawable =
                    resources.getDrawable(R.drawable.drawable_circle_outer, null)
            } else {
                binding.state.setTextColor(Color.parseColor("#828282"))
                binding.timeMode.setTextColor(Color.parseColor("#828282"))
                binding.progressBarCircle.progressDrawable =
                    resources.getDrawable(R.drawable.drawable_circle_outer_grey, null) // ✅ 회색으로!
            }

        } else {
            // 휴식은 원래 회색이므로 동일
            binding.state.text = "휴식"
            binding.timeMode.text = "5분"
            binding.state.setTextColor(Color.parseColor("#828282"))
            binding.timeMode.setTextColor(Color.parseColor("#828282"))
            binding.progressBarCircle.progressDrawable =
                resources.getDrawable(R.drawable.drawable_circle_outer_grey, null)
        }
    }



    private fun updateCycleText() {
        binding.cycleSetting.text = "$currentCycle Cycle"
    }

    private fun showTimeInputDialog() {
        val input = EditText(this)  // `this`는 Activity context
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = "분 단위 입력"

        AlertDialog.Builder(this)
            .setTitle("공부 시간 설정")
            .setView(input)
            .setPositiveButton("확인") { dialog: DialogInterface, which: Int ->
                val mins = input.text.toString().toIntOrNull()
                if (mins != null && mins > 0) {
                    studyMinutes = mins
                    updateTimeText(studyMinutes * 60)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }
}