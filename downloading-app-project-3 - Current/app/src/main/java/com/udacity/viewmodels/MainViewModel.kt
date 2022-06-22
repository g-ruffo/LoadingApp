package com.udacity.viewmodels

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.*

class MainViewModel() : ViewModel() {

    private val _selectedDownloadRadioButton = MutableLiveData<String>()
    val selectedDownloadRadioButton: LiveData<String>
    get() = _selectedDownloadRadioButton

    private val _selectedTitleRadioButton = MutableLiveData<String>()
    val selectedTitleRadioButton: LiveData<String>
        get() = _selectedTitleRadioButton

    val customUrlString = MutableLiveData<String>()

    var customUrlRadioButton = ObservableBoolean()

    private val customUrlObserver = Observer<String> { url ->
        _selectedDownloadRadioButton.value = url
    }

    init {
        customUrlRadioButton.set(false)
        customUrlString.value = ""
        customUrlString.observeForever(customUrlObserver)
    }


    fun setDownloadUrl(url: String, title: String, customUrl: Boolean) {
        if (customUrl) {
            _selectedDownloadRadioButton.value = customUrlString.value
            _selectedTitleRadioButton.value = customUrlString.value

        } else {
            _selectedDownloadRadioButton.value = url
            _selectedTitleRadioButton.value = title

        }
            customUrlRadioButton.set(customUrl)
    }

    override fun onCleared() {
        customUrlString.removeObserver(customUrlObserver)
        super.onCleared()
    }


}

