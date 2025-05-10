package com.example.termproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivityTaketestBinding


class TakeTestActivity : AppCompatActivity() {
    lateinit var binding: ActivityTaketestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaketestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            finish()
        }
    }
}

