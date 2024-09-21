package com.shazcom.gps.app.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns.EMAIL_ADDRESS
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.AuthRepository
import com.shazcom.gps.app.data.response.AuthResponse
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.network.request.UserAuth
import com.shazcom.gps.app.ui.BaseActivity
import com.shazcom.gps.app.ui.viewmodal.AuthViewModel
import com.shazcom.gps.app.utils.LocaleHelper
import com.shazcom.gps.app.utils.hideKeyboard
import kotlinx.android.synthetic.main.activity_login.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class LoginActivity : BaseActivity(), KodeinAware {

    private var authViewModel: AuthViewModel? = null
    private var logHidePass = false

    override val kodein by kodein()
    private val localDB: LocalDB by instance<LocalDB>()
    private val repository: AuthRepository by instance<AuthRepository>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        localDB.getToken()?.let {
            Intent(this@LoginActivity, Dashboard::class.java).apply {
                startActivity(this)
                finish()
            }
        }

        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        authViewModel?.authRepository = repository

        forgotPassword.setOnClickListener {
            Intent(this@LoginActivity, ForgotPassword::class.java).apply {
                startActivity(this)
            }
        }

        loginBtn.setOnClickListener {
            loginBtn.hideKeyboard()

            if (isValidData()) {
                authViewModel?.authUser(
                    UserAuth(
                        userName.text.toString().trim(),
                        password.text.toString().trim()
                    )
                )
                    ?.observe(this, Observer { resources ->
                        when (resources.status) {
                            Status.SUCCESS -> {
                                progressBar.visibility = View.INVISIBLE
                                loginBtn.isEnabled = true
                                processData(resources.data!!)
                            }
                            Status.LOADING -> {
                                progressBar.visibility = View.VISIBLE
                                loginBtn.isEnabled = false
                            }
                            Status.ERROR -> {
                                showSnackbar(resources.message.toString())
                                progressBar.visibility = View.INVISIBLE
                                loginBtn.isEnabled = true
                            }
                        }
                    })
            } else {
                showSnackbar(getString(R.string.enter_valid_cred))
            }
        }

        languageSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (parent?.selectedItem.toString()) {
                    "English" -> {
                        LocaleHelper.setLocale(this@LoginActivity, "en");
                        LocaleHelper.getLanguage(this@LoginActivity)
                            ?.let { Log.e("desiredLocale", it) }
                        refreshLang()

                    }
                    "Chinese" -> {
                        LocaleHelper.setLocale(this@LoginActivity, "cn");
                        LocaleHelper.getLanguage(this@LoginActivity)
                            ?.let { Log.e("desiredLocale", it) }
                        refreshLang()
                    }
                }
            }
        }
    }

    private fun refreshLang() {
        val intent = intent
        finish()
        startActivity(intent)
    }

    private fun processData(data: AuthResponse) {
        if (data.status == 1) {
            localDB.saveToken(data.user_api_hash)
            Intent(this@LoginActivity, Dashboard::class.java).apply {
                startActivity(this)
                finish()
            }
        } else {
            showSnackbar(getString(R.string.invalid_email_password))
        }
    }

    private fun isValidData(): Boolean {
        if (userName.text.isNullOrEmpty() || password.text.isNullOrEmpty()) {
            return false
        } else if (!EMAIL_ADDRESS.matcher(userName.text.toString().trim()).matches()) {
            return false
        }

        return true
    }


    fun logPasswordTransform(view: View) {

        if (!logHidePass) {
            logHidePass = true
            rightIcon.setImageResource(R.drawable.eye)
            password.transformationMethod = HideReturnsTransformationMethod.getInstance()
        } else {
            logHidePass = false
            rightIcon.setImageResource(R.drawable.eye_cross)
            password.transformationMethod = PasswordTransformationMethod.getInstance()
        }

        password.post {
            password.setSelection(password.length())
        }
    }
}