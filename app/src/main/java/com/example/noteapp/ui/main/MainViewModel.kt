package com.example.noteapp.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.noteapp.R
import com.example.noteapp.ui.ObserverEvent

class MainViewModel : ViewModel() {
    val navigateToFragment: MutableLiveData<ObserverEvent<Int>> = MutableLiveData()

    private fun navigate(action: Int) {
        navigateToFragment.value = ObserverEvent(action)
    }

    fun toNote() {
        navigate(R.id.action_mainFragment_to_noteFragment)
    }

    fun toPassword() {
        navigate(R.id.action_noteFragment_to_passwordFragment)
    }

    fun toMainScreen() {
        navigate(R.id.action_passwordFragment_to_mainFragment)
    }
}