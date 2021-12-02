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
import com.example.noteapp.databinding.NoteFragmentBinding
import com.example.noteapp.ui.data.DataViewModel
import com.example.noteapp.ui.data.Fingerprint


class NoteFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private val dViewModel: DataViewModel by activityViewModels()
    private var _binding: NoteFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<NoteFragmentBinding>(
            inflater, R.layout.note_fragment, container, false
        )
        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val noteFragment = this
        _binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = mainViewModel
            dataViewModel = dViewModel
            noteFragmentClass = noteFragment
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

    fun save() {
        val fingerprintExist: Boolean = Fingerprint.checkPhoneBiometricSetings()

        if(fingerprintExist) {
            dViewModel.saveNoteByFingerprint()
            var available = true
            Fingerprint.ready.observe(this.viewLifecycleOwner, Observer {
                if(available && it == true) {
                    available = false
                    mainViewModel.toEndFromNote()
                }
            })
        } else {
            Toast.makeText(MainActivity.appCon, "There is a problem with fingerprint on your device", Toast.LENGTH_LONG).show()
        }
    }
}