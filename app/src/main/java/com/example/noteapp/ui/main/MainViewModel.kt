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

    fun toMainScreen() {
        navigate(R.id.action_endFragment_to_comboFragment)
    }

    fun toLogInPassword() {
        navigate(R.id.action_comboFragment_to_mainFragment)
    }

    fun toLogInFingerprint() {
        navigate(R.id.action_comboFragment_to_fingerprintFragment)
    }

    fun toNoteFromCombo() {
        navigate(R.id.action_comboFragment_to_noteFragment)
    }

    fun toNoteFromFingerprint() {
        navigate(R.id.action_fingerprintFragment_to_noteFragment)
    }

    fun toNoteFromPassword() {
        navigate(R.id.action_mainFragment_to_noteFragment)
    }

    fun toEndFromNote() {
        navigate(R.id.action_noteFragment_to_endFragment)
    }

    fun toPassword() {
        navigate(R.id.action_noteFragment_to_passwordFragment)
    }

    fun toEndFromPassword() {
        navigate(R.id.action_passwordFragment_to_endFragment)
    }

    fun goBackFromPassword() {
        navigate(R.id.action_mainFragment_to_comboFragment)
    }

    fun goBackFromFingerprint() {
        navigate(R.id.action_fingerprintFragment_to_comboFragment)
    }
}