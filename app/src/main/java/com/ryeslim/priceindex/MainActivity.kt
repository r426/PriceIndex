package com.ryeslim.priceindex

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ryeslim.priceindex.databinding.ActivityMainBinding
import java.text.NumberFormat

const val MAX_FRACTION_DIGITS = 4
const val ONE_MINUTE = (60 * 1000).toLong()
const val NUMBER_OF_CURRENCIES = 3

class MainActivity : AppCompatActivity() {

    companion object {
        var instance: MainActivity? = null
            private set
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding

    private var calculate = arrayOfNulls<Button>(NUMBER_OF_CURRENCIES)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        // Setting up LiveData observation relationship
        viewModel.rateUsd.observe(this, Observer { newRateUsd ->
            binding.currency1.text = newRateUsd
        })
        viewModel.rateGbp.observe(this, Observer { newRateGbp ->
            binding.currency2.text = newRateGbp
        })
        viewModel.rateEur.observe(this, Observer { newRateEur ->
            binding.currency3.text = newRateEur
        })
        viewModel.chartName.observe(this, Observer { newChartName ->
            binding.chartName.text = newChartName
        })
        viewModel.localTime.observe(this, Observer { newUpdatedAt ->
            binding.updatedAt.text = newUpdatedAt
        })

        instance = this

        val currencyField = arrayOf(
            arrayOf(binding.amountOfCurrency1, binding.amountOfBitcoins1),
            arrayOf(binding.amountOfCurrency2, binding.amountOfBitcoins2),
            arrayOf(binding.amountOfCurrency3, binding.amountOfBitcoins3)
        )

        calculate[0] = binding.calculate1
        calculate[1] = binding.calculate2
        calculate[2] = binding.calculate3

        val clear = arrayOf(binding.clear1, binding.clear2, binding.clear3)

        CalculatorWatcher(currencyField[0])
        CalculatorWatcher(currencyField[1])
        CalculatorWatcher(currencyField[2])

        viewModel.number = NumberFormat.getInstance(this.resources.configuration.locale)
        viewModel.number.maximumFractionDigits = MAX_FRACTION_DIGITS


        if (savedInstanceState == null) {
            viewModel.lastQuery = System.currentTimeMillis()
            viewModel.launchDataLoad()
        }

        // Set a click listener for the refresh image
        binding.refresh.setOnClickListener {
            viewModel.currentQuery = System.currentTimeMillis()
            if (viewModel.currentQuery - viewModel.lastQuery > ONE_MINUTE) {
                viewModel.lastQuery = viewModel.currentQuery
                viewModel.launchDataLoad()
                for (i in 0 until NUMBER_OF_CURRENCIES) {
                    calculate[i]?.callOnClick()
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    getText(R.string.toast_message),
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }

        // Set a click listener for all three calculate buttons
        for (ii in 0..2) {
            calculate[ii]?.setOnClickListener {
                val rate = viewModel.rateFloat?.get(ii)
                when {
                    currencyField[ii][0].text.toString().isNotEmpty() -> {
                        val value = java.lang.Float.valueOf(currencyField[ii][0].text.toString())
                        currencyField[ii][1].setText(viewModel.divide(value, rate))
                    }
                    currencyField[ii][1].text.toString().isNotEmpty() -> {
                        val value = java.lang.Float.valueOf(currencyField[ii][1].text.toString())
                        currencyField[ii][0].setText(viewModel.multiply(value, rate))
                    }
                    else -> { /* both fields are empty */
                    }
                }
            }

            // Set a click listener for all three clear buttons
            clear[ii].setOnClickListener {
                for (j in 0..1) {
                    currencyField[ii][j].setText("")
                }
            }
        }
    }
}

