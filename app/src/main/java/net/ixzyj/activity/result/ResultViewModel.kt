package net.ixzyj.activity.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ResultViewModel : ViewModel() {

    public var errorList = MutableLiveData<List<String>>()

    fun getErrorList(): LiveData<List<String>> {
        return errorList
    }

}