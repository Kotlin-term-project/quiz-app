package com.example.termproject

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivityTimerBinding
import kotlinx.coroutines.*
import android.app.AlertDialog
import android.content.DialogInterface
import android.text.InputType
import android.widget.EditText


class StudyTimerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimerBinding
    private var totalCycles = 4
    private var studyMinutes = 25
    private var isRunning = false
    private var currentCycle = 1
    private var isBreak = false
    private var job: Job? = null
    private var remainingSeconds = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTimerBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        updateCycleText()
        updateTimeText(studyMinutes * 60)

        // 타이머 시간 설정 클릭
        binding.time.setOnClickListener {
            showTimeInputDialog()
        }

        binding.start.setOnClickListener {
            if (!isRunning) {
                startPomodoro()
            }
        }

        binding.pause.setOnClickListener {
            job?.cancel()
            isRunning = false
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
        remainingSeconds = seconds
        job = CoroutineScope(Dispatchers.Main).launch {
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
                updateTimeText(remainingSeconds)
            }
            onTimerComplete()
        }
    }

    private fun onTimerComplete() {
        if (isBreak) {
            // 휴식 끝이면 다음 사이클로
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
        binding.time.text = String.format("%02d:%02d", min, sec)
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