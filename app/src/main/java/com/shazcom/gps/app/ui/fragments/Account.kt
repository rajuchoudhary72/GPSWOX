package com.shazcom.gps.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.AuthRepository
import com.shazcom.gps.app.data.response.UserDataResponse
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseFragment
import com.shazcom.gps.app.ui.viewmodal.AuthViewModel
import kotlinx.android.synthetic.main.fragment_account.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.support.closestKodein
import org.kodein.di.generic.instance

class Account : BaseFragment(), KodeinAware {

    private var authViewModel: AuthViewModel? = null

    override val kodein by closestKodein()
    private val localDB: LocalDB by instance<LocalDB>()
    private val repository: AuthRepository by instance<AuthRepository>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        authViewModel?.authRepository = repository
        checkAccountDetail()

    }

    private fun checkAccountDetail() {
        authViewModel?.getUserData(localDB.getToken()!!)
            ?.observe(requireActivity(), Observer { resources ->

                if (!isVisible) return@Observer

                when (resources.status) {
                    Status.SUCCESS -> {
                        progressBar.visibility = View.INVISIBLE
                        showData(resources?.data)
                    }
                    Status.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                    }
                    Status.ERROR -> {
                        progressBar.visibility = View.INVISIBLE
                    }
                }
            })
    }

    private fun showData(data: UserDataResponse?) {
        accountEmail.text = String.format(getString(R.string.email_n_a), "${data?.email}")
        plan.text = String.format(getString(R.string.plan_n_a), "${data?.plan}")

        if (data?.devices_limit != null) {
            if (data?.devices_limit == 0) {
                device.text = String.format(getString(R.string.devices_limit_n_a), "Unlimited")
            } else {
                device.text =
                    String.format(getString(R.string.devices_limit_n_a), "${data?.devices_limit}")
            }
        }

        if (data?.expiration_date != null) {
            expDate.text =
                String.format(getString(R.string.exp_date_n_a), data?.expiration_date)
            expDate.visibility = View.VISIBLE
        }

        if(data?.days_left != null && data?.expiration_date != null) {
            expDate.append(" (${data?.days_left} days left)")
        }


    }
}