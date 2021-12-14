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
import com.example.noteapp.MainActivity
import com.example.noteapp.R
import com.example.noteapp.databinding.EndFragmentBinding
import com.example.noteapp.ui.data.DataViewModel

class EndFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private val dataViewModel: DataViewModel by activityViewModels()
    private var _binding: EndFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<EndFragmentBinding>(
            inflater, R.layout.end_fragment, container, false
        )

        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val endFragment = this
        _binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            endFragmentClass = endFragment
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

    fun continueApp() {
        dataViewModel.cleanAll()
        mainViewModel.toMainScreen()
    }

    fun closeApp() {
        dataViewModel.cleanAll()
        MainActivity.instance.finish()
    }
}