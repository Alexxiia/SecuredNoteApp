package com.example.noteapp.ui.main

import android.os.Bundle
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
import com.example.noteapp.databinding.FingerprintFragmentBinding
import com.example.noteapp.ui.data.DataViewModel
import com.example.noteapp.ui.data.Fingerprint

class FingerprintFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private val dataViewModel: DataViewModel by activityViewModels()
    private var _binding: FingerprintFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FingerprintFragmentBinding>(
            inflater, R.layout.fingerprint_fragment, container, false
        )
        checkFiles()
        dataViewModel.cleanAll()

        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fingerprintFragment = this
        _binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            fingerprintFragmentClass = fingerprintFragment
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
        if(!dataViewModel.doNoteExist()) {
            mainViewModel.toNoteFromFingerprint()
        } else {
            val fingerprintExist: Boolean = Fingerprint.checkPhoneBiometricSetings()

            if(fingerprintExist) {
                Fingerprint.authenticationDialog()

                var available = true
                Fingerprint.ready.observe(this.viewLifecycleOwner, Observer {
                    if(available && it == true) {
                        available = false
                        logIn()
                    }
                })
            } else {
                Toast.makeText(MainActivity.appCon, "Add fingerprint on your device", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun logIn() {
        dataViewModel.getNoteByFingerprint()
        mainViewModel.toNoteFromFingerprint()
    }

}