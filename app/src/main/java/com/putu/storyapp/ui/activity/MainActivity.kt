package com.putu.storyapp.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.putu.storyapp.R
import com.putu.storyapp.adapter.StoryAdapter
import com.putu.storyapp.data.model.StoryModel
import com.putu.storyapp.databinding.ActivityMainBinding
import com.putu.storyapp.extra.Helper
import com.putu.storyapp.extra.UserPreferences
import com.putu.storyapp.viewmodel.MainViewModel
import com.putu.storyapp.viewmodel.ViewModelFactory

class MainActivity : AppCompatActivity() {

    private var _mainBind: ActivityMainBinding? = null
    private val mainBind get() = _mainBind

    private lateinit var mainViewModel: MainViewModel

    private val helper = Helper()

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _mainBind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBind?.root)

        setUpViewModel()

        supportActionBar?.title = getString(R.string.sora)

        mainViewModel.storyList.observe(this) { storyList ->
            if (storyList != null) {
                setUpRecyclerData(storyList)
            }
        }

        mainBind?.addStory?.setOnClickListener {
            startActivity(Intent(this, PostStoryActivity::class.java))
        }

        mainViewModel.isLoading.observe(this) {
            mainBind?.let { it1 ->
                helper.isLoading(
                    it,
                    it1.mainProgressBar
                )}
        }
    }

    private fun setUpRecyclerData(stories: List<StoryModel>) {
        val storyList = ArrayList<StoryModel>()
        for (story in stories) {
            storyList.clear()
            storyList.addAll(stories)
        }

        mainBind?.rvStoryList?.layoutManager = LinearLayoutManager(this@MainActivity)

        val storyAdapter = StoryAdapter(storyList)
        mainBind?.rvStoryList?.adapter = storyAdapter

        storyAdapter.setOnItemClickCallback(object : StoryAdapter.OnItemClickCallback {
            override fun onItemClicked(data: StoryModel) {
                val intent = Intent(this@MainActivity, DetailStoryActivity::class.java)
                intent.putExtra(DetailStoryActivity.EXTRA_DATA, data)
                startActivity(intent)
            }
        })
    }

    private fun setUpViewModel() {
        mainViewModel = ViewModelProvider(this, ViewModelFactory(UserPreferences.getInstance(dataStore)))[MainViewModel::class.java]

        mainViewModel.errorMessage.observe(this) {
            when (it) {
                "Stories fetched successfully" -> {
                    mainBind?.root?.let { it1 ->
                        Snackbar.make(it1, R.string.story_loaded, Snackbar.LENGTH_SHORT).show()
                    }
                }

                "onFailure" -> {
                    mainBind?.root?.let { it1 ->
                        Snackbar.make(it1, R.string.failed_response_alert_message, Snackbar.LENGTH_SHORT).show()
                    }
                }

                else -> {
                    mainBind?.root?.let { it1 ->
                        Snackbar.make(it1, R.string.error_story_not_loaded, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }

        mainViewModel.getUser().observe(this) { user ->
            if (user.isLogin) {
                mainViewModel.getAllStories(user.token)
            } else {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        setUpMenu(item.itemId)
        return super.onOptionsItemSelected(item)
    }

    private fun setUpMenu(itemId: Int) {
        when(itemId) {
            R.id.settingsBtn -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
    }
}