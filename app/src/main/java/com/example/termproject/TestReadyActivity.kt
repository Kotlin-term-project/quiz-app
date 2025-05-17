package com.example.termproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivityTestreadyBinding


class TestReadyActivity : AppCompatActivity() {
    lateinit var binding: ActivityTestreadyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestreadyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TakeTestActivity 가는 버튼 구현
        binding.goTestBtn.setOnClickListener {
            val intent = Intent(this, TakeTestActivity::class.java)
            startActivity(intent)
        }

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
    }
}

