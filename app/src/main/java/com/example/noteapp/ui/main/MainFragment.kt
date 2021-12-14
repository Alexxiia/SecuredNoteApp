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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.noteapp.MainActivity
import com.example.noteapp.R
import com.example.noteapp.databinding.MainFragmentBinding
import com.example.noteapp.ui.data.DataViewModel
import org.mindrot.jbcrypt.BCrypt

class MainFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val dataViewModel: DataViewModel by activityViewModels()
    private var _binding: MainFragmentBinding? = null

    val password: MutableLiveData<String> = MutableLiveData("")
    var hashPass: String = ""
    var _logIn: Boolean = true

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<MainFragmentBinding>(
            inflater, R.layout.main_fragment, container, false
        )
        dataViewModel.cleanAll()
        hashPass = dataViewModel.getPasswordData()!!
        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainFragment = this
        _binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            mainFragmentClass = mainFragment
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
        if(_logIn) {
            if(BCrypt.checkpw(password.value, hashPass)) {
                dataViewModel.getNotesData(password.value!!)
                hashPass = ""
                mainViewModel.toNoteFromPassword()
            } else {
                _logIn = false
                Toast.makeText(MainActivity.appCon, "False Password, waiting 5 seconds for the next try", Toast.LENGTH_LONG).show()
            }
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                _logIn = true
            }, 5000)
        }
    }
    fun goBack() {
        mainViewModel.goBackFromPassword()
    }
}