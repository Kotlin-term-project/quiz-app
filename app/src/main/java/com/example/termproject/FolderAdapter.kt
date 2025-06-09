package com.example.termproject

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.termproject.databinding.ItemFolderBinding
import com.google.firebase.firestore.FirebaseFirestore


data class Folder(
    val id: String,
    val name: String,
    var isExpanded: Boolean = false,
    val files: MutableList<FileData> = mutableListOf()
)

// recyclerView 활용 해서 문서를 나타냄
class FolderViewHolder(val binding: ItemFolderBinding) : RecyclerView.ViewHolder(binding.root)

class FolderAdapter(
    val folders: MutableList<Folder>,
    val onDelete: (folderId: String, position: Int) -> Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    lateinit var fileAdapter: FileAdapter

    private fun shortenFolderName(name: String, maxLen: Int = 8): String {
        return if (name.length > maxLen) {
            name.take(maxLen) + "..."
        } else {
            name
        }
    }

    override fun getItemCount(): Int = folders.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = FolderViewHolder(
        ItemFolderBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as FolderViewHolder).binding
        val folder = folders[position]

        // 폴더 이름 짧게 가져오기
        binding.folderName.text = shortenFolderName(folder.name)

        // 폴더 안에 파일 리사이클러뷰 구현 (중첩 리사이클러뷰)
        fileAdapter = FileAdapter(folder.files, folder.id) { questionId, filePosition ->
            val db = FirebaseFirestore.getInstance()

            db.collection("folders")
                .document(folder.id)
                .collection("questions")
                .document(questionId)
                .delete()
                .addOnSuccessListener {
                    fileAdapter.removeItem(filePosition)
                }
                .addOnFailureListener {
                }
        }
        holder.binding.fileRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.binding.fileRecyclerView.adapter = fileAdapter
        binding.fileRecyclerView.setHasFixedSize(true)

        holder.binding.fileRecyclerView.visibility = if (folder.isExpanded) View.VISIBLE else View.GONE

        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("삭제 확인")
                .setMessage("이 폴더를 삭제하시겠습니까?")
                .setPositiveButton("삭제") { _, _ ->
                    onDelete(folder.id, position)
                }
                .setNegativeButton("취소", null)
                .show()
            true
        }

        // 토글 버튼 클릭 시 상태 변경 후 갱신
        binding.toggleBtn.setOnClickListener {
            folder.isExpanded = !folder.isExpanded
            notifyItemChanged(position)
        }

        // 시험 만들기 버튼 구현
        binding.makeTestBtn.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, MakeTestActivity::class.java)
            intent.putExtra("fId", folder.id)
            context.startActivity(intent)
        }
    }
}