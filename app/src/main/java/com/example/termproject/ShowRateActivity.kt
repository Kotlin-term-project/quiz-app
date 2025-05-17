package com.example.termproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivityShowrateBinding


class ShowRateActivity : AppCompatActivity() {
    lateinit var binding: ActivityShowrateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowrateBinding.inflate((layoutInflater))
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            finish()
        }
    }
}