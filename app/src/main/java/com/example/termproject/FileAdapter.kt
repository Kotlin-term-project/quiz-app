package com.example.termproject

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.termproject.databinding.ItemFileBinding

class FileAdapter(
    private val files: MutableList<FileData>,
    private val folderId: String,
    private  val onDelete: (questionId: String, position: Int) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    fun removeItem(position: Int) {
        files.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun shortenFileName(name: String, maxLen: Int = 12): String {
        return if (name.length > maxLen) {
            name.take(maxLen) + "..."
        } else {
            name
        }
    }

    inner class FileViewHolder(val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int = files.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]

        // 파일 이름 짧게 가져오기
        holder.binding.fileText.text = shortenFileName(file.question)

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

        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("삭제 확인")
                .setMessage("이 파일을 삭제하시겠습니까?")
                .setPositiveButton("삭제") { _, _ ->
                    onDelete(file.id, position)
                }
                .setNegativeButton("취소", null)
                .show()
            true
        }
    }
}