package com.example.termproject

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.termproject.databinding.ItemFileBinding

data class FileData(
    val question: String,
    val choice1: String,
    val choice2: String,
    val choice3: String,
    val answer: String
)

class FileAdapter(private val files: List<FileData>) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    inner class FileViewHolder(val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int = files.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.binding.fileText.text = file.question

        holder.binding.fileDataBtn.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, FileDataActivity::class.java)
            intent.putExtra("question", file.question)
            intent.putExtra("choice1", file.choice1)
            intent.putExtra("choice2", file.choice2)
            intent.putExtra("choice3", file.choice3)
            intent.putExtra("answer", file.answer)
            context.startActivity(intent)
        }
    }
}
