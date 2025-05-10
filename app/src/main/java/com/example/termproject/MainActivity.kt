package com.example.termproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate((layoutInflater))
        setContentView(binding.root)

        binding.makeTestBtn.setOnClickListener {
            val intent = Intent(this, MakeTestActivity::class.java)
            startActivity(intent)
        }

        binding.takeTestBtn.setOnClickListener {
            val intent = Intent(this, TakeTestActivity::class.java)
            startActivity(intent)
        }

        binding.memorizeBtn.setOnClickListener {
            val intent = Intent(this, MemorizeActivity::class.java)
            startActivity(intent)
        }
    }
}