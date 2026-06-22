package com.example.calendar

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class NepaliDate(
    val year: Int,
    val month: Int, // 1 to 12
    val day: Int,   // 1 to 32
    val dayOfWeek: Int // 1 (Sunday) to 7 (Saturday)
) {
    val monthNameEn: String get() = NepaliCalendarHelper.MONTHS_EN[month - 1]
    val monthNameNp: String get() = NepaliCalendarHelper.MONTHS_NP[month - 1]
    val dayOfWeekNameNp: String get() = NepaliCalendarHelper.DAYS_NP[dayOfWeek - 1]
    val dayOfWeekNameEn: String get() = NepaliCalendarHelper.DAYS_EN[dayOfWeek - 1]
    val devanagariDay: String get() = NepaliCalendarHelper.toDevanagari(day)
    val devanagariYear: String get() = NepaliCalendarHelper.toDevanagari(year)
}

object NepaliCalendarHelper {
    const val START_YEAR_BS = 2070
    const val END_YEAR_BS = 2095
    private const val START_AD_DATE_STR = "2013-04-13" // 1st Baishakh 2070 BS

    val MONTHS_EN = listOf(
        "Baishakh", "Jestha", "Ashadh", "Shrawan", "Bhadra", "Ashwin",
        "Kartik", "Mangsir", "Poush", "Magh", "Falgun", "Chaitra"
    )

    val MONTHS_NP = listOf(
        "बैशाख", "जेठ", "असार", "साउन", "भदौ", "असोज",
        "कात्तिक", "मंसिर", "पुस", "माघ", "फागुन", "चैत"
    )

    val DAYS_EN = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val DAYS_NP = listOf("आइतबार", "सोमबार", "मङ्गलबार", "बुधबार", "बिहीबार", "शुक्रबार", "शनिबार")

    val MONTH_DAYS_BS = mapOf(
        2070 to listOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
        2071 to listOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30),
        2072 to listOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30),
        2073 to listOf(31, 32, 32, 31, 32, 30, 29, 30, 29, 30, 29, 30),
        2074 to listOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2075 to listOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2076 to listOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30),
        2077 to listOf(31, 32, 32, 31, 32, 30, 29, 30, 29, 30, 29, 30),
        2078 to listOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2079 to listOf(31, 31, 32, 32, 31, 31, 29, 30, 29, 30, 29, 30),
        2080 to listOf(31, 32, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
        2081 to listOf(31, 32, 32, 31, 32, 30, 30, 29, 30, 29, 30, 30),
        2082 to listOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2083 to listOf(31, 31, 32, 32, 31, 31, 29, 30, 29, 30, 29, 30),
        2084 to listOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30),
        2085 to listOf(31, 32, 32, 31, 32, 30, 29, 30, 29, 30, 29, 30),
        2086 to listOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2087 to listOf(31, 31, 32, 32, 31, 31, 29, 30, 29, 30, 29, 30),
        2088 to listOf(31, 32, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30),
        2089 to listOf(31, 32, 32, 31, 32, 30, 30, 29, 30, 29, 30, 30),
        2090 to listOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2091 to listOf(31, 31, 32, 32, 31, 31, 29, 30, 29, 30, 29, 30),
        2092 to listOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30),
        2093 to listOf(31, 32, 32, 31, 32, 30, 29, 30, 29, 30, 29, 30),
        2094 to listOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30),
        2095 to listOf(31, 31, 32, 32, 31, 31, 29, 30, 29, 30, 29, 30)
    )

    val TITHIS = listOf(
        "Pratipada" to "शुकल प्रतिपदा",
        "Dwitiya" to "द्वितीया",
        "Tritiya" to "तृतीया",
        "Chaturthi" to "चतुर्थी",
        "Panchami" to "पञ्चमी",
        "Shasthi" to "षष्ठी",
        "Saptami" to "सप्तमी",
        "Ashtami" to "अष्टमी",
        "Navami" to "नवमी",
        "Dashami" to "दशमी",
        "Ekadashi" to "एकादशी",
        "Dwadashi" to "द्वादशी",
        "Trayodashi" to "त्रयोदशी",
        "Chaturdashi" to "चतुर्दशी",
        "Purnima" to "पूर्णिमा (Purnima)",
        "Pratipada" to "कृष्ण प्रतिपदा",
        "Dwitiya" to "द्वितीया",
        "Tritiya" to "तृतीया",
        "Chaturthi" to "चतुर्थी",
        "Panchami" to "पञ्चमी",
        "Shasthi" to "षष्ठी",
        "Saptami" to "सप्तमी",
        "Ashtami" to "अष्टमी",
        "Navami" to "नवमी",
        "Dashami" to "दशमी",
        "Ekadashi" to "एकादशी",
        "Dwadashi" to "द्वादशी",
        "Trayodashi" to "त्रयोदशी",
        "Chaturdashi" to "चतुर्दशी",
        "Aunshi" to "औंसी (Aunshi)"
    )

    fun toDevanagari(number: Int): String {
        val numStr = number.toString()
        val devMap = mapOf(
            '0' to '०', '1' to '१', '2' to '२', '3' to '३', '4' to '४',
            '5' to '५', '6' to '६', '7' to '७', '8' to '८', '9' to '९'
        )
        return numStr.map { devMap[it] ?: it }.joinToString("")
    }

    private fun getStartADDate(): Date {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.parse(START_AD_DATE_STR)!!
    }

    fun adToBs(adDate: Date): NepaliDate? {
        val startAD = getStartADDate()
        val diffMs = adDate.time - startAD.time
        if (diffMs < 0) return null // Out of bounds

        val diffDays = (diffMs / (24 * 60 * 60 * 1000)).toInt()

        var remainingDays = diffDays
        var currentYear = START_YEAR_BS

        while (currentYear <= END_YEAR_BS) {
            val monthDays = MONTH_DAYS_BS[currentYear] ?: break
            val totalYearDays = monthDays.sum()

            if (remainingDays < totalYearDays) {
                // Inside this year
                var currentMonthIndex = 0
                while (currentMonthIndex < 12) {
                    val daysInMonth = monthDays[currentMonthIndex]
                    if (remainingDays < daysInMonth) {
                        val cal = Calendar.getInstance()
                        cal.time = adDate
                        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 to 7

                        return NepaliDate(
                            year = currentYear,
                            month = currentMonthIndex + 1,
                            day = remainingDays + 1,
                            dayOfWeek = dayOfWeek
                        )
                    }
                    remainingDays -= daysInMonth
                    currentMonthIndex++
                }
            }
            remainingDays -= totalYearDays
            currentYear++
        }
        return null // Beyond supported index
    }

    fun bsToAd(bsYear: Int, bsMonth: Int, bsDay: Int): Date? {
        if (bsYear < START_YEAR_BS || bsYear > END_YEAR_BS || bsMonth < 1 || bsMonth > 12) return null
        val monthDaysForYear = MONTH_DAYS_BS[bsYear] ?: return null
        if (bsDay < 1 || bsDay > monthDaysForYear[bsMonth - 1]) return null

        var totalDays = 0

        // Days from start year up to bsYear
        for (yr in START_YEAR_BS until bsYear) {
            totalDays += MONTH_DAYS_BS[yr]?.sum() ?: 0
        }

        // Days in current year up to bsMonth
        for (mth in 0 until (bsMonth - 1)) {
            totalDays += monthDaysForYear[mth]
        }

        totalDays += (bsDay - 1)

        val cal = Calendar.getInstance()
        cal.time = getStartADDate()
        cal.add(Calendar.DAY_OF_YEAR, totalDays)
        return cal.time
    }

    // Dynamic lunar Phase-based tithi approximation
    fun getTithiForDate(adDate: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val baseNewMoon = sdf.parse("2023-12-11")!! // Known Aunshi
        val diffMs = adDate.time - baseNewMoon.time
        val diffDays = diffMs.toDouble() / (24.0 * 60.0 * 60.0 * 1000.0)

        // Scientific Lunar cycle length
        val synodicMonth = 29.530588853
        var lunarAge = diffDays % synodicMonth
        if (lunarAge < 0) lunarAge += synodicMonth

        val tithiIndex = (((lunarAge / synodicMonth) * 30.0).toInt()) % 30
        return TITHIS[tithiIndex].second
    }

    // Get specific holiday, if any, for a given BS date
    fun getFestivalAndHoliday(bsMonth: Int, bsDay: Int, tithi: String): Pair<String?, Boolean> {
        // Fixed holidays
        val fixedHoliday = when (bsMonth) {
            1 -> when (bsDay) {
                1 -> "नयाँ वर्ष (Nepali New Year)" to true
                11 -> "लोकतन्त्र दिवस (Loktantra Diwas)" to false
                else -> null
            }
            2 -> when (bsDay) {
                15 -> "गणतन्त्र दिवस (Ganatantra Diwas)" to true
                else -> null
            }
            3 -> when (bsDay) {
                15 -> "धान दिवस / दही चिउरा खाने दिन (Rice Paddy Day)" to false
                else -> null
            }
            4 -> when (bsDay) {
                15 -> "खीर खाने दिन (Kheer Khane Din)" to false
                else -> null
            }
            5 -> when (bsDay) {
                22 -> "Civil Service Day" to false
                else -> null
            }
            9 -> when (bsDay) {
                27 -> "पृथ्वी जयन्ती (Prithvi Jayanti)" to true
                else -> null
            }
            10 -> when (bsDay) {
                1 -> "माघे संक्रान्ति (Maghe Sankranti)" to true
                else -> null
            }
            11 -> when (bsDay) {
                7 -> "राष्ट्रिय प्रजातन्त्र दिवस (Democracy Day)" to true
                else -> null
            }
            else -> null
        }

        if (fixedHoliday != null) return fixedHoliday

        // Traditional festivals derived from Tithi Name + BS Month
        return when {
            bsMonth == 1 && tithi.contains("पूर्णिमा") -> "बुद्ध जयन्ती (Buddha Jayanti)" to true
            bsMonth == 4 && tithi.contains("पूर्णिमा") -> "जनै पूर्णिमा / रक्षाबन्धन (Janai Purnima)" to true
            bsMonth == 5 && tithi.contains("अष्टमी") && tithi.contains("कृष्ण") -> "श्रीकृष्ण जन्माष्टमी (Janmashtami)" to true
            bsMonth == 6 && tithi.contains("तृतीया") && tithi.contains("शुकल") -> "हरितालिका तीज (Teej Festival)" to true
            bsMonth == 6 && tithi.contains("प्रतिपदा") && tithi.contains("कृष्ण") -> "गाईजात्रा (Gai Jatra)" to false
            bsMonth == 6 && tithi.contains("तृतीया") && tithi.contains("कृष्ण") -> "ऋषि पञ्चमी (Rishi Panchami)" to false
            bsMonth == 7 && tithi.contains("प्रतिपदा") && tithi.contains("शुकल") -> "बडा दशैँ घटस्थापना (Ghatasthapana)" to true
            bsMonth == 7 && tithi.contains("सप्तमी") && tithi.contains("शुकल") -> "महा फूलपाती (Dashain Fulpati)" to true
            bsMonth == 7 && tithi.contains("अष्टमी") && tithi.contains("शुकल") -> "महा अष्टमी (Maha Ashtami)" to true
            bsMonth == 7 && tithi.contains("नवमी") && tithi.contains("शुकल") -> "महा नवमी (Maha Navami)" to true
            bsMonth == 7 && tithi.contains("दशमी") && tithi.contains("शुकल") -> "विजया दशमी (Bijaya Dashami)" to true
            bsMonth == 7 && tithi.contains("पूर्णिमा") -> "कोजाग्रत पूर्णिमा (Kojagrat Purnima)" to true
            bsMonth == 8 && tithi.contains("औंसी") -> "लक्ष्मी पूजा (Laxmi Puja)" to true
            bsMonth == 8 && tithi.contains("प्रतिपदा") && tithi.contains("शुकल") -> "म्हः पूजा / गोवर्धन पूजा (Mha Puja)" to true
            bsMonth == 8 && tithi.contains("द्वितीया") && tithi.contains("शुकल") -> "भाइटीका / तिहार (Bhai Tika)" to true
            bsMonth == 8 && tithi.contains("षष्ठी") && tithi.contains("शुकल") -> "छठ पर्व (Chhath Parva)" to true
            bsMonth == 9 && tithi.contains("पूर्णिमा") -> "योमरी पुन्ही / उधौली (Yomari Punhi)" to false
            bsMonth == 11 && tithi.contains("चतुर्दशी") && tithi.contains("कृष्ण") -> "महा शिवरात्रि (Maha Shivaratri)" to true
            bsMonth == 11 && tithi.contains("पूर्णिमा") -> "फागु पूर्णिमा / होली (Holi Festival)" to true
            bsMonth == 12 && tithi.contains("अष्टमी") && tithi.contains("शुकल") -> "चैते दशैँ (Chaite Dashain)" to false
            bsMonth == 12 && tithi.contains("नवमी") && tithi.contains("शुकल") -> "राम नवमी (Ram Navami)" to true
            else -> null to false
        }
    }
}
