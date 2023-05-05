package com.putu.storyapp.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.putu.storyapp.data.model.StoryModel
import com.putu.storyapp.databinding.ActivityDetailStoryBinding

@Suppress("DEPRECATION")
class DetailStoryActivity : AppCompatActivity() {

    private var _detailBind : ActivityDetailStoryBinding? = null
    private val detailBind get() = _detailBind


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _detailBind = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(detailBind?.root)

        val detailStory = intent.getParcelableExtra<StoryModel>(EXTRA_DATA) as StoryModel
        detailBind?.apply {
            Glide.with(this@DetailStoryActivity)
                .load(detailStory.photoUrl)
                .into(ivPhoto)

            tvName.text = detailStory.name
            tvDescription.text = detailStory.description
            tvDate.text = detailStory.createdAt.substringBefore("T")
        }

        supportActionBar?.title = detailStory.name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
       const val EXTRA_DATA = "Extra_Data"
    }
}