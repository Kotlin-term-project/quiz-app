package com.example.termproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivityAftertestBinding

class AfterTestActivity: AppCompatActivity() {
    lateinit var binding: ActivityAftertestBinding

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityAftertestBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}