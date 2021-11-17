package com.example.noteapp.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.noteapp.MainActivity
import com.example.noteapp.R
import com.example.noteapp.databinding.PasswordFragmentBinding
import com.example.noteapp.ui.data.DataViewModel

class PasswordFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val dataViewModel: DataViewModel by activityViewModels()
    private var _binding: PasswordFragmentBinding? = null

    val pass1: MutableLiveData<String> = MutableLiveData("")
    val pass2: MutableLiveData<String> = MutableLiveData("")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<PasswordFragmentBinding>(
            inflater, R.layout.password_fragment, container, false
        )
        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val passwordFragment = this
        _binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            passwordFragmentClass = passwordFragment
        }
        mainViewModel.navigateToFragment.observe(this.viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(it)
            }
        })
    }

    override fun onDestroyView() {
        dataViewModel.cleanAll()
        super.onDestroyView()
        _binding = null
    }

    fun savePassword() {
        if(!pass1.value.equals(pass2.value)) {
            Toast.makeText(MainActivity.appCon, "The passwords are not equals", Toast.LENGTH_SHORT).show()
        } else if(!pass1.value!!.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"))) {
            if(pass1.value!!.length < 8) {
                Toast.makeText(MainActivity.appCon, "The passwords must have at least 8 symbols", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(MainActivity.appCon, "The passwords must have at least one small and one big letter, and a number", Toast.LENGTH_LONG).show()
            }
        } else {
            dataViewModel.save(pass1.value!!)
            cleanPassword()
            mainViewModel.toMainScreen()
        }
    }
    fun cleanPassword() {
        pass1.value = ""
        pass2.value = ""
    }
}