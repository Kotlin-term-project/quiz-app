package com.example.termproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivityShowwronganswerBinding

class ShowWrongAnswerActivity: AppCompatActivity() {
    lateinit var binding: ActivityShowwronganswerBinding

    private var wrongList = arrayListOf<FileData>()
    private var currentWrongNum = 0

    override fun onCreate(savedStateInstance: Bundle?) {
        super.onCreate(savedStateInstance)
        binding = ActivityShowwronganswerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wrongList = intent.getParcelableArrayListExtra<FileData>("wrongList") ?: arrayListOf()

        loadWrongQuestion(currentWrongNum)

        // 이전 오답 문제로 돌아오는 버튼 구현
        binding.prevBtn.setOnClickListener {
            if (currentWrongNum > 0) {
                currentWrongNum--
                loadWrongQuestion(currentWrongNum)
            }
        }

        // 다음 오답 문제 넘어가는 버튼 구현
        binding.nextBtn.setOnClickListener {
            if (currentWrongNum < wrongList.size - 1) {
                currentWrongNum++
                loadWrongQuestion(currentWrongNum)
            }
        }

        // 나가기 버튼 구현
        binding.stopBtn.setOnClickListener {
            finish()
        }

        // AI 문제 생성 버튼 구현
        binding.AICreateBtn.setOnClickListener {
            val intent = Intent(this, AICreateActivity::class.java)
            startActivity(intent)
        }
    }

    fun loadWrongQuestion(num: Int) {

        val q = wrongList[num]

        val userAnswer = q.userAnswer ?: ""
        val correctAnswer = q.answer

        val checkBoxViews = listOf(
            binding.choice1 to q.choice1,
            binding.choice2 to q.choice2,
            binding.choice3 to q.choice3,
            binding.choiceAnswer to correctAnswer,
        )

        val choiceViews = listOf(
            binding.choice1Text to q.choice1,
            binding.choice2Text to q.choice2,
            binding.choice3Text to q.choice3,
            binding.answerText to q.answer,
        )

        binding.questionText.text = q.question
        binding.wrongAnswerCount.text = "${num + 1} / ${wrongList.size}"

        for (i in choiceViews.indices) {
            val (textView, text) = choiceViews[i]
            val (checkBox, _) = checkBoxViews[i]

            textView.text = text

            checkBox.setImageResource(
                if (text == userAnswer) R.drawable.take_test_checked
                else R.drawable.take_test_unchecked
            )
        }

        // 사용자 답은 빨간색, 원래 정답은 파란색으로 표시해서 한 눈에 보이게 해줌
        for((view, text) in choiceViews) {
            view.text = text

            when {
                userAnswer == text -> {
                    view.setBackgroundResource(R.drawable.make_test_answer_wrong_box)
                }
                correctAnswer == text -> {
                    view.setBackgroundResource(R.drawable.make_test_answer_right_box)
                }
                else -> {
                    view.setBackgroundResource(R.drawable.take_test_answer_box)
                }
            }
        }

        binding.prevBtn.visibility = if (num == 0) View.GONE else View.VISIBLE
        binding.nextBtn.visibility = if (num == wrongList.size - 1) View.GONE else View.VISIBLE
    }
}