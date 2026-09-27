package com.example.juki

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterFragment : Fragment() {

    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_register, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinnerCourse = view.findViewById<Spinner>(R.id.spinnerCourse)
        val courses = (1..6).map { "$it курс" }
        spinnerCourse.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            courses
        )

        val seekBar = view.findViewById<SeekBar>(R.id.seekBarDifficulty)
        val tvDifficulty = view.findViewById<TextView>(R.id.tvDifficultyLabel)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                tvDifficulty.text = "Уровень сложности: $progress"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate.set(year, month, dayOfMonth)
        }

        view.findViewById<Button>(R.id.btnRegister).setOnClickListener {
            registerPlayer(view)
        }
    }

    private fun registerPlayer(root: View) {
        val fullName = root.findViewById<EditText>(R.id.etFullName).text.toString().trim()
        if (fullName.isEmpty()) {
            Toast.makeText(requireContext(), "Введите ФИО", Toast.LENGTH_SHORT).show()
            return
        }

        val gender = when (root.findViewById<RadioGroup>(R.id.rgGender).checkedRadioButtonId) {
            R.id.rbMale -> "Мужской"
            R.id.rbFemale -> "Женский"
            else -> "Не указан"
        }

        val course = root.findViewById<Spinner>(R.id.spinnerCourse).selectedItem.toString()
        val difficulty = root.findViewById<SeekBar>(R.id.seekBarDifficulty).progress

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

        root.findViewById<ImageView>(R.id.ivZodiac).setImageResource(zodiac.drawableRes)
        root.findViewById<TextView>(R.id.tvResult).text = """
            ФИО: ${player.fullName}
            Пол: ${player.gender}
            Курс: ${player.course}
            Уровень сложности: ${player.difficulty}
            Дата рождения: ${player.birthDate}
            Знак зодиака: ${player.zodiacSign}
        """.trimIndent()
    }
}