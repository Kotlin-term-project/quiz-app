package com.example.termproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivityAicreateBinding

class AICreateActivity: AppCompatActivity() {
    lateinit var binding: ActivityAicreateBinding

    override fun onCreate(savedStateInstance: Bundle?) {
        super.onCreate(savedStateInstance)
        binding = ActivityAicreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}