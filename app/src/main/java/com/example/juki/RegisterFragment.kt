package com.example.juki

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.juki.db.AppDatabase
import com.example.juki.db.PlayerEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterFragment : Fragment() {

    private var selectedDate: Calendar = Calendar.getInstance()
    private lateinit var db: AppDatabase
    private lateinit var tvCurrentPlayer: TextView
    private lateinit var listPlayers: ListView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_register, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getInstance(requireContext())

        tvCurrentPlayer = view.findViewById(R.id.tvCurrentPlayer)
        listPlayers = view.findViewById(R.id.listPlayers)

        refreshCurrentPlayerLabel()

        lifecycleScope.launch {
            db.playerDao().getAll().collect { players ->
                val names = players.map { it.fullName }
                listPlayers.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, names)
                listPlayers.setOnItemClickListener { _, _, position, _ ->
                    CurrentPlayer.player = players[position]
                    refreshCurrentPlayerLabel()
                }
            }
        }

        // Spinner
        val spinner = view.findViewById<Spinner>(R.id.spinnerCourse)
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, (1..6).map { "$it курс" })

        // Календарь
        view.findViewById<CalendarView>(R.id.calendarView).setOnDateChangeListener { _, y, m, d ->
            selectedDate.set(y, m, d)
        }

        // Кнопка регистрации
        view.findViewById<Button>(R.id.btnRegister).setOnClickListener {
            registerNewPlayer(view)
        }
    }

    private fun refreshCurrentPlayerLabel() {
        tvCurrentPlayer.text = CurrentPlayer.player?.fullName ?: "Не выбран"
    }

    private fun registerNewPlayer(root: View) {
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
        val date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(selectedDate.time)
        val zodiac = Zodiac.determineSign(selectedDate)

        root.findViewById<ImageView>(R.id.ivZodiac).setImageResource(zodiac.drawableRes)

        lifecycleScope.launch {
            val id = db.playerDao().insert(
                PlayerEntity(
                    fullName = fullName,
                    gender = gender,
                    course = course,
                    birthDate = date,
                    zodiacSign = zodiac.name
                )
            )
            CurrentPlayer.player = db.playerDao().getById(id)
            refreshCurrentPlayerLabel()
            Toast.makeText(requireContext(), "Игрок $fullName зарегистрирован", Toast.LENGTH_SHORT).show()
        }
    }
}