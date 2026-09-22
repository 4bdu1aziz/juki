package com.example.juki

import java.util.Calendar

object Zodiac {

    data class Sign(val name: String, val drawableRes: Int)

    fun determineSign(calendar: Calendar): Sign {
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return when {
            (month == 3 && day >= 21) || (month == 4 && day <= 19) ->
                Sign("Овен", R.drawable.zodiac_aries)
            (month == 4 && day >= 20) || (month == 5 && day <= 20) ->
                Sign("Телец", R.drawable.zodiac_taurus)
            (month == 5 && day >= 21) || (month == 6 && day <= 20) ->
                Sign("Близнецы", R.drawable.zodiac_gemini)
            (month == 6 && day >= 21) || (month == 7 && day <= 22) ->
                Sign("Рак", R.drawable.zodiac_cancer)
            (month == 7 && day >= 23) || (month == 8 && day <= 22) ->
                Sign("Лев", R.drawable.zodiac_leo)
            (month == 8 && day >= 23) || (month == 9 && day <= 22) ->
                Sign("Дева", R.drawable.zodiac_virgo)
            (month == 9 && day >= 23) || (month == 10 && day <= 22) ->
                Sign("Весы", R.drawable.zodiac_libra)
            (month == 10 && day >= 23) || (month == 11 && day <= 21) ->
                Sign("Скорпион", R.drawable.zodiac_scorpio)
            (month == 11 && day >= 22) || (month == 12 && day <= 21) ->
                Sign("Стрелец", R.drawable.zodiac_sagittarius)
            (month == 12 && day >= 22) || (month == 1 && day <= 19) ->
                Sign("Козерог", R.drawable.zodiac_capricorn)
            (month == 1 && day >= 20) || (month == 2 && day <= 18) ->
                Sign("Водолей", R.drawable.zodiac_aquarius)
            else ->
                Sign("Рыбы", R.drawable.zodiac_pisces)
        }
    }
}