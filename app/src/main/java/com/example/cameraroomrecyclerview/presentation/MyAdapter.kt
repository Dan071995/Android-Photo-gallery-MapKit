package com.example.cameraroomrecyclerview.presentation

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.cameraroomrecyclerview.databinding.MyListItemBinding
import com.example.cameraroomrecyclerview.entity.FileInfo


class MyAdapter(
    private val onClick: (Uri,Int) -> Unit
) : Adapter<MyViewHolder>() {

    private var fileInfoList = mutableListOf<FileInfo>()

    fun setData(fileInfoList: List<FileInfo>){
        this.fileInfoList = fileInfoList.toMutableList()
        notifyDataSetChanged() //Перересовываем RV
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = MyListItemBinding.inflate(LayoutInflater.from(parent.context))
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return fileInfoList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = fileInfoList[position]
        Log.d("123", fileInfoList.size.toString())
        //Устанавливаем картинку
        Glide.with(holder.itemView)
            .load(item.uri.toUri())
            .centerCrop()
            .into(holder.binding.imageView)
        //Устанавливаем дату и врмя снимка
        holder.binding.textViewDate.text = item.fileName

        //Устанавливаем слущатель клика на элемент
        holder.binding.root.setOnClickListener {
            onClick(item.uri.toUri(),position) //Возвращаем Uri при клике на элемент и его позицию в RV
        }

    }
}

class MyViewHolder(val binding: MyListItemBinding):ViewHolder(binding.root)