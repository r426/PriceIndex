package com.ryeslim.priceindex

import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.text.Html
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryeslim.priceindex.data.*
import com.ryeslim.priceindex.retrofit.QueryApi
import com.ryeslim.priceindex.retrofit.ServiceFactory
import kotlinx.coroutines.*
import java.text.NumberFormat
import java.text.ParseException

class MainViewModel : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var response: retrofit2.Response<Query>? = null

    lateinit var number: NumberFormat

    var lastQuery: Long = 0
    var currentQuery: Long = 0

    val rateFloat: FloatArray? = FloatArray(NUMBER_OF_CURRENCIES)

    val chartName = MutableLiveData<String>()
    val localTime = MutableLiveData<String>()
    val rateUsd = MutableLiveData<String>()
    val rateGbp = MutableLiveData<String>()
    val rateEur = MutableLiveData<String>()

    init {
        chartName.value = ""
        localTime.value = ""
        rateUsd.value = ""
        rateGbp.value = ""
        rateEur.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun launchDataLoad() {
        coroutineScope.launch {
            fetchData()
        }
    }

    private suspend fun fetchData() = withContext(Dispatchers.Default) {
        try {
            withContext(Dispatchers.IO) {
                response = ServiceFactory.createRetrofitService(
                    QueryApi::class.java,
                    "https://api.coindesk.com/v1/bpi/"
                )
                    .getQueryAsync().await()
            }
            if (response!!.body() != null) {
                setValues()
            } else {

            }
        } catch (e: Exception) {
            e.stackTrace
        }
    }

    private fun setValues() {
        val time: Time = response!!.body()!!.time
        val chrtName = response!!.body()!!.chartName
        val usd: Currency = response!!.body()!!.bpi.usd
        val gbp: Currency = response!!.body()!!.bpi.gbp
        val eur: Currency = response!!.body()!!.bpi.eur

        val timeString = time.updated

        // force postValue to notify Observers
        // postValue posts a task to a main thread to set the given values
        chartName.postValue(chrtName)
        localTime.postValue(localTime(timeString))
        rateUsd.postValue(rateString(usd))
        rateGbp.postValue(rateString(gbp))
        rateEur.postValue(rateString(eur))

        rateFloat?.set(0, toFloat(usd.rate))
        rateFloat?.set(1, toFloat(gbp.rate))
        rateFloat?.set(2, toFloat(eur.rate))
    }

    private fun localTime(updated: String): String {
        var localTime = ""
        val defaultTimeZone = TimeZone.getDefault()
        val strDefaultTimeZone = defaultTimeZone.getDisplayName(true, TimeZone.SHORT)
        val simpleDateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss z")
        simpleDateFormat.timeZone = TimeZone.getTimeZone(strDefaultTimeZone)

        try {
            localTime = simpleDateFormat.format(simpleDateFormat.parse(updated))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return localTime
    }

    private fun toFloat(rate: String): Float {
        val rateString = rate.replace(",".toRegex(), "")
        return java.lang.Float.parseFloat(rateString)
    }

    private fun rateString(currencyObject: Currency): String {
        var rateString = ""
        val currencySymbol = Html.fromHtml(currencyObject.symbol).toString()
        rateString = String.format("%s: %.4f", currencySymbol, currencyObject.rateFloat)
        return rateString
    }

    fun divide(value: Float, rate: Float?): String {
        val temp: Float = rate ?: 1.0F
        return number.format((value / temp).toDouble()).replace(",".toRegex(), "")
    }

    fun multiply(value: Float, rate: Float?): String {
        val temp: Float = rate ?: 1.0F
        return number.format((value * temp).toDouble()).replace(",".toRegex(), "")
    }
}