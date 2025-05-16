package com.example.termproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivityMemorizeBinding


class ShowRateActivity : AppCompatActivity() {
    lateinit var binding: ActivityMemorizeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemorizeBinding.inflate((layoutInflater))
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            finish()
        }
    }
}