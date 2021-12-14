package com.example.noteapp.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
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

    fun logIn() {
        dataViewModel.getNoteByFingerprint()

        var available = true
        Fingerprint.ready.observe(this.viewLifecycleOwner, Observer {
            if(available && it == true) {
                available = false
                mainViewModel.toNoteFromFingerprint()
            }
        })
    }
    fun goBack() {
        mainViewModel.goBackFromFingerprint()
    }
}