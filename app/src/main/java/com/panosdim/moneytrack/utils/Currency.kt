package com.panosdim.moneytrack.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

const val currencyRegex = "([1-9][0-9]*(\\.[0-9]{0,2})?|0(\\.[0-9]{0,2})?|(\\.[0-9]{1,2})?)"

fun moneyFormat(obj: Any): String {
    val symbols = DecimalFormatSymbols()
    symbols.groupingSeparator = '.'
    symbols.decimalSeparator = ','
    val moneyFormat = DecimalFormat("#,##0.00 €", symbols)
    return moneyFormat.format(obj)
}

