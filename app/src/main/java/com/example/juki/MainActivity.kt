package com.example.juki

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Spinner — курс
        val spinnerCourse = findViewById<Spinner>(R.id.spinnerCourse)
        val courses = (1..6).map { "$it курс" }
        spinnerCourse.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            courses
        )

        // SeekBar — уровень сложности
        val seekBar = findViewById<SeekBar>(R.id.seekBarDifficulty)
        val tvDifficulty = findViewById<TextView>(R.id.tvDifficultyLabel)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                tvDifficulty.text = "Уровень сложности: $progress"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // CalendarView — дата рождения
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate.set(year, month, dayOfMonth)
        }

        // Кнопка
        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            registerPlayer()
        }
    }

    private fun registerPlayer() {
        val fullName = findViewById<EditText>(R.id.etFullName).text.toString().trim()
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Введите ФИО", Toast.LENGTH_SHORT).show()
            return
        }

        val gender = when (findViewById<RadioGroup>(R.id.rgGender).checkedRadioButtonId) {
            R.id.rbMale -> "Мужской"
            R.id.rbFemale -> "Женский"
            else -> "Не указан"
        }

        val course = findViewById<Spinner>(R.id.spinnerCourse).selectedItem.toString()
        val difficulty = findViewById<SeekBar>(R.id.seekBarDifficulty).progress

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val birthDate = dateFormat.format(selectedDate.time)

        val zodiac = Zodiac.determineSign(selectedDate)

        val player = Player(
            fullName = fullName,
            gender = gender,
            course = course,
            difficulty = difficulty,
            birthDate = birthDate,
            zodiacSign = zodiac.name
        )

        findViewById<ImageView>(R.id.ivZodiac).setImageResource(zodiac.drawableRes)

        findViewById<TextView>(R.id.tvResult).text = """
            ФИО: ${player.fullName}
            Пол: ${player.gender}
            Курс: ${player.course}
            Уровень сложности: ${player.difficulty}
            Дата рождения: ${player.birthDate}
            Знак зодиака: ${player.zodiacSign}
        """.trimIndent()
    }
}