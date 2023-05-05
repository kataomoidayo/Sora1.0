package com.putu.storyapp.ui.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.putu.storyapp.R
import com.putu.storyapp.databinding.ActivityLoginBinding
import com.putu.storyapp.extra.Helper
import com.putu.storyapp.extra.UserPreferences
import com.putu.storyapp.viewmodel.LoginViewModel
import com.putu.storyapp.viewmodel.ViewModelFactory

class LoginActivity : AppCompatActivity() {

    private var _loginBind: ActivityLoginBinding? = null
    private val loginBind get() = _loginBind

    private lateinit var loginViewModel: LoginViewModel

    private val helper = Helper()

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _loginBind = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBind?.root)

        setUpView()
        setUpAction()
        setUpAnimation()
        setUpViewModel()

        loginViewModel.isLoading.observe(this) {
            loginBind?.let { it1 ->
                helper.isLoading(
                    it,
                    it1.loginProgressBar
                )}
        }

        onBackPressedDispatcher.addCallback(this@LoginActivity, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom)
            }
        })
    }

    @Suppress("DEPRECATION")
    private fun setUpView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setUpAction() {
        loginBind?.closeButton?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom)
        }

        loginBind?.loginButton?.setOnClickListener {
            val email = loginBind?.emailEditText?.text.toString()
            val password = loginBind?.passwordEditText?.text.toString()

            when {
                email.isEmpty() ->
                    loginBind?.emailEditText?.error = getString(R.string.error_email_empty)

                password.isEmpty() ->
                    loginBind?.passwordEditText?.error = getString(R.string.error_password_empty)

                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    loginBind?.emailEditText?.error = getString(R.string.error_invalid_email_format)

                password.length < 8 ->
                    loginBind?.passwordEditText?.error = getString(R.string.error_wrong_password_format)

                else ->
                    loginViewModel.login(email, password)
            }
        }
    }

    private fun setUpAnimation() {
        ObjectAnimator.ofFloat(loginBind?.logo, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(loginBind?.titleLogin, View.ALPHA, 1f).setDuration(500)
        val emailEditTextLayout = ObjectAnimator.ofFloat(loginBind?.emailEditTextLayout, View.ALPHA, 1f).setDuration(500)
        val passwordEditTextLayout = ObjectAnimator.ofFloat(loginBind?.passwordEditTextLayout, View.ALPHA, 1f).setDuration(500)
        val login = ObjectAnimator.ofFloat(loginBind?.loginButton, View.ALPHA, 1f).setDuration(500)

        AnimatorSet().apply {
            playSequentially(title, emailEditTextLayout, passwordEditTextLayout, login)
            startDelay = 500
        }.start()
    }

    private fun setUpViewModel() {
        loginViewModel = ViewModelProvider(this, ViewModelFactory(UserPreferences.getInstance(dataStore)))[LoginViewModel::class.java]

        loginViewModel.errorMessage.observe(this) {
            when (it) {
                "success" -> {
                    AlertDialog.Builder(this).apply {
                        setTitle(R.string.success_alert_title)
                        setMessage(R.string.login_alert_message)
                        setPositiveButton(R.string.continue_alert_button) { _, _ ->
                            val intent = Intent(context, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
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
                        .setTitle(R.string.login_failed_alert_title)
                        .setMessage(R.string.login_failed_alert_message)
                        .setPositiveButton(R.string.back_alert_button) {_, _ ->
                            alert.cancel()
                        }
                        .show()
                }
            }
        }
    }
}