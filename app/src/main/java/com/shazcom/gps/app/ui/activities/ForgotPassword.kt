package com.shazcom.gps.app.ui.activities

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.repository.AuthRepository
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseActivity
import com.shazcom.gps.app.ui.viewmodal.AuthViewModel
import com.shazcom.gps.app.utils.hideKeyboard
import kotlinx.android.synthetic.main.activity_forgot_password.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class ForgotPassword : BaseActivity(), KodeinAware {

    private var authViewModel: AuthViewModel? = null
    override val kodein by kodein()
    private val repository: AuthRepository by instance<AuthRepository>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        toolBar.setNavigationOnClickListener { finish() }

        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        authViewModel?.authRepository = repository

        submitBtn.setOnClickListener {
            if (Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
                processRequest()
            } else {
                Toast.makeText(this, getString(R.string.error_valid_email), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processRequest() {
        email.hideKeyboard()
        authViewModel?.passwordReminder(email.text.toString())
            ?.observe(this, Observer { resources ->
                when (resources.status) {
                    Status.SUCCESS -> {
                        progressBar.visibility = View.GONE
                        submitBtn.visibility = View.VISIBLE
                        Toast.makeText(
                            this,
                            getString(R.string.pass_reset_instruction_message),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                    Status.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                        submitBtn.visibility = View.INVISIBLE

                    }
                    Status.ERROR -> {
                        progressBar.visibility = View.GONE
                        submitBtn.visibility = View.VISIBLE
                        Toast.makeText(
                            this,
                            getString(R.string.pass_reset_instruction_message),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            })
    }
}