package com.putu.storyapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.putu.storyapp.data.model.StoryModel
import com.putu.storyapp.databinding.StoryListBinding

class StoryAdapter(private val storyList: ArrayList<StoryModel>): RecyclerView.Adapter<StoryAdapter.ViewHolder>() {

    private lateinit var onItemClickCallback: OnItemClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    inner class ViewHolder(var storyListBinding: StoryListBinding): RecyclerView.ViewHolder(storyListBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val storyListBinding = StoryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(storyListBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = storyList[position]

        Glide.with(holder.itemView.context)
            .load(story.photoUrl)
            .into(holder.storyListBinding.ivPhoto)

        holder.storyListBinding.tvName.text = story.name
        holder.storyListBinding.tvDate.text = story.createdAt.substringBefore("T")
        holder.storyListBinding.tvDescription.text = story.description

        holder.itemView.setOnClickListener {
            onItemClickCallback.onItemClicked(storyList[holder.adapterPosition])
        }
    }

    override fun getItemCount(): Int = storyList.size

    interface OnItemClickCallback {
        fun onItemClicked(data: StoryModel)
    }
}