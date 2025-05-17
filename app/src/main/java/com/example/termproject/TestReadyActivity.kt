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

        // 뒤로 가기 버튼 구현
        binding.backBtn.setOnClickListener {
            finish()
        }
    }
}

