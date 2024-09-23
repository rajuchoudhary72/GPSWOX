package com.shazcom.gps.app.ui.activities

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.repository.AuthRepository
import com.shazcom.gps.app.databinding.ActivityForgotPasswordBinding
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseActivity
import com.shazcom.gps.app.ui.viewmodal.AuthViewModel
import com.shazcom.gps.app.utils.hideKeyboard

import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class ForgotPassword : BaseActivity(), KodeinAware {

    private lateinit var binding: ActivityForgotPasswordBinding
    private var authViewModel: AuthViewModel? = null
    override val kodein by kodein()
    private val repository: AuthRepository by instance<AuthRepository>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding= ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolBar.setNavigationOnClickListener { finish() }

        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        authViewModel?.authRepository = repository

        binding. submitBtn.setOnClickListener {
            if (Patterns.EMAIL_ADDRESS.matcher(binding.email.text.toString()).matches()) {
                processRequest()
            } else {
                Toast.makeText(this, getString(R.string.error_valid_email), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processRequest() {
        binding.email.hideKeyboard()
        authViewModel?.passwordReminder(binding.email.text.toString())
            ?.observe(this, Observer { resources ->
                when (resources.status) {
                    Status.SUCCESS -> {
                        binding.progressBar.visibility = View.GONE
                        binding.submitBtn.visibility = View.VISIBLE
                        Toast.makeText(
                            this,
                            getString(R.string.pass_reset_instruction_message),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                    Status.LOADING -> {
                        binding. progressBar.visibility = View.VISIBLE
                        binding. submitBtn.visibility = View.INVISIBLE

                    }
                    Status.ERROR -> {
                        binding. progressBar.visibility = View.GONE
                        binding.  submitBtn.visibility = View.VISIBLE
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