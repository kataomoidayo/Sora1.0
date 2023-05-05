package com.putu.storyapp.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.putu.storyapp.R
import com.putu.storyapp.databinding.ActivityPostStoryBinding
import com.putu.storyapp.extra.Helper
import com.putu.storyapp.extra.UserPreferences
import com.putu.storyapp.viewmodel.PostStoryViewModel
import com.putu.storyapp.viewmodel.ViewModelFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class PostStoryActivity : AppCompatActivity() {

    private var _postStoryBind: ActivityPostStoryBinding? = null
    private val postStoryBind get() = _postStoryBind

    private lateinit var postStoryViewModel: PostStoryViewModel

    private lateinit var token: String

    private val helper = Helper()

    private var getFile: File? = null

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _postStoryBind = ActivityPostStoryBinding.inflate(layoutInflater)
        setContentView(postStoryBind?.root)

        supportActionBar?.title = getString(R.string.new_post)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setViewModel()

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        postStoryBind?.btnCamera?.setOnClickListener {
            startCameraX()
        }

        postStoryBind?.btnGallery?.setOnClickListener {
            startGallery()
        }

        postStoryBind?.btnUpload?.setOnClickListener {
            uploadStory()
        }

        postStoryViewModel.isLoading.observe(this) {
            postStoryBind?.let { it1 ->
                helper.isLoading(
                    it,
                    it1.postStoryProgressBar
                )}
        }

        postStoryBind?.descriptionEditText?.setOnTouchListener { view, event ->
            view.parent.requestDisallowInterceptTouchEvent(true)
            if((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                view.parent.requestDisallowInterceptTouchEvent(false)
            }
            return@setOnTouchListener false
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE_PERMISSIONS) {
            if(!allPermissionsGranted()){
                AlertDialog.Builder(this).apply {
                    setTitle(R.string.permission_alert_title)
                    setMessage(R.string.permission_alert_message)
                    setPositiveButton(R.string.back_alert_button) { _, _ ->
                        finish()
                    }
                    create()
                    show()
                }
            }
        }
    }

    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    @Suppress("DEPRECATION")
    private val launcherIntentCameraX = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.data?.getSerializableExtra("picture", File::class.java)
            } else {
                it.data?.getSerializableExtra("picture")
            } as? File

            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean

            getFile = myFile

            myFile?.let { file ->
                helper.rotateFile(file, isBackCamera)
                postStoryBind?.imgPreview?.setImageBitmap(BitmapFactory.decodeFile(getFile?.path))
            }
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"

        val chooser = Intent.createChooser(intent, getString(R.string.choose_pic))
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = helper.uriToFile(selectedImg, this@PostStoryActivity)
            getFile = myFile

            postStoryBind?.imgPreview?.setImageURI(selectedImg)
        }
    }

    private fun uploadStory() {
        when {
            getFile == null -> {
                val builder = AlertDialog.Builder(this)
                val alert = builder.create()
                builder
                    .setTitle(R.string.empty_picture_alert_title)
                    .setMessage(R.string.empty_picture_alert_message)
                    .setPositiveButton(R.string.back_alert_button) {_, _ ->
                        alert.cancel()
                    }
                    .show()
            }

            postStoryBind?.descriptionEditText?.text?.isEmpty() == true -> {
                val builder = AlertDialog.Builder(this)
                val alert = builder.create()
                builder
                    .setTitle(R.string.empty_description_alert_title)
                    .setMessage(R.string.empty_description_alert_message)
                    .setPositiveButton(R.string.back_alert_button) {_, _ ->
                        alert.cancel()
                    }
                    .show()
            }

            else -> {
                val file = helper.compressFile(getFile as File)

                val description = postStoryBind?.descriptionEditText?.text.toString().toRequestBody("text/plain".toMediaType())

                val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())

                val imageMultipart : MultipartBody.Part = MultipartBody.Part.createFormData("photo", file.name, requestImageFile)

                postStoryViewModel.postStory(token, imageMultipart, description)
            }
        }
    }

    private fun setViewModel() {
        postStoryViewModel = ViewModelProvider(this, ViewModelFactory(UserPreferences.getInstance(dataStore)))[PostStoryViewModel::class.java]

        postStoryViewModel.getUser().observe(this) { user ->
            if (user.isLogin) {
                this.token = user.token
            } else {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }

        postStoryViewModel.errorMessage.observe(this) {
            when (it) {
                "Story created successfully" -> {
                    AlertDialog.Builder(this).apply {
                        setTitle(R.string.success_alert_title)
                        setMessage(R.string.post_story_success_alert_message)
                        setPositiveButton(R.string.continue_alert_button) {_, _ ->
                            onBackPressedDispatcher.onBackPressed()
                        }
                        create()
                        show()
                    }
                }

                "onFailure" -> {
                    val builder = AlertDialog.Builder(this)
                    val alert = builder.create()
                    builder
                        .setTitle(R.string.failed_response_alert_title)
                        .setMessage(R.string.failed_response_alert_message)
                        .setPositiveButton(R.string.back_alert_button) {_, _ ->
                            alert.cancel()
                        }
                        .show()
                }

                else -> {
                    val builder = AlertDialog.Builder(this)
                    val alert = builder.create()
                    builder
                        .setTitle(R.string.failed_response_alert_title)
                        .setMessage(R.string.post_story_failed_alert_message)
                        .setPositiveButton(R.string.back_alert_button) {_, _ ->
                            alert.cancel()
                        }
                        .show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}