package com.example.noteapp.ui.main

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.noteapp.MainActivity
import com.example.noteapp.R
import com.example.noteapp.databinding.ComboFragmentBinding
import com.example.noteapp.ui.data.DataViewModel
import com.example.noteapp.ui.data.Fingerprint

class ComboFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val dViewModel: DataViewModel by activityViewModels()
    private var _binding: ComboFragmentBinding? = null
    var hashPass: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<ComboFragmentBinding>(
            inflater, R.layout.combo_fragment, container, false
        )
        dViewModel.cleanAll()
        hashPass = dViewModel.getPasswordData()!!
        checkFiles()
        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val comboFragment = this
        _binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = mainViewModel
            dataViewModel = dViewModel
            comboFragmentClass = comboFragment
        }
        mainViewModel.navigateToFragment.observe(this.viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(it)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun checkFiles() {
        if(!dViewModel.doNoteExist()) {
            mainViewModel.toNoteFromCombo()
        }
    }

    fun logInByFingerprint() {
        val fingerprintExist: Boolean = Fingerprint.checkPhoneBiometricSetings()

        if(fingerprintExist) {
            Handler(Looper.getMainLooper()).postDelayed({
                mainViewModel.toLogInFingerprint()
            }, 2000)
        } else {
            Toast.makeText(MainActivity.appCon, "Add fingerprint on your device", Toast.LENGTH_LONG).show()
        }
    }

    fun logInByPassword() {
        if(hashPass.isNullOrEmpty()) {
            hashPass = ""
            Toast.makeText(MainActivity.appCon, "You don't have a password", Toast.LENGTH_LONG).show()
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                mainViewModel.toLogInPassword()
            }, 2000)
        }
    }
}