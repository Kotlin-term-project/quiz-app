package com.example.termproject

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.termproject.databinding.ItemFolderBinding

// recyclerView 활용 해서 문서를 나타냄
class FolderViewHolder(val binding: ItemFolderBinding) : RecyclerView.ViewHolder(binding.root)

class FolderAdapter(val folders: MutableList<String>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int = folders.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = FolderViewHolder(
        ItemFolderBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as FolderViewHolder).binding

        binding.folderName.text = folders[position]
    }
}