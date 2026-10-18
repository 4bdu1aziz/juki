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

class LoginFragment : Fragment() {

    private lateinit var db: AppDatabase
    private var selectedDate: Calendar = Calendar.getInstance()
    private lateinit var tvBirthDate: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getInstance(requireContext())

        val layoutLogin = view.findViewById<LinearLayout>(R.id.layoutLogin)
        val layoutRegister = view.findViewById<LinearLayout>(R.id.layoutRegister)
        val listPlayers = view.findViewById<ListView>(R.id.listPlayers)

        view.findViewById<Button>(R.id.btnGoToRegister).setOnClickListener {
            layoutLogin.visibility = View.GONE
            layoutRegister.visibility = View.VISIBLE
        }
        view.findViewById<Button>(R.id.btnBackToLogin).setOnClickListener {
            layoutLogin.visibility = View.VISIBLE
            layoutRegister.visibility = View.GONE
        }

        lifecycleScope.launch {
            db.playerDao().getAll().collect { players ->
                val names = players.map { it.fullName }
                listPlayers.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, names)

                listPlayers.setOnItemClickListener { _, _, position, _ ->
                    val selectedPlayer = players[position]
                    loginPlayer(selectedPlayer)
                }
            }
        }

        val spinner = view.findViewById<Spinner>(R.id.spinnerCourse)
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, (1..6).map { "$it курс" })

        tvBirthDate = view.findViewById(R.id.tvBirthDate)

        selectedDate.set(2000, Calendar.JANUARY, 1)
        updateDateLabel()

        view.findViewById<Button>(R.id.btnPickDate).setOnClickListener {
            val dialog = android.app.DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    selectedDate.set(year, month, dayOfMonth)
                    updateDateLabel()
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            )
            dialog.datePicker.maxDate = System.currentTimeMillis()
            dialog.show()
        }
        view.findViewById<Button>(R.id.btnRegister).setOnClickListener {
            registerNewPlayer(view)
        }
    }

    private fun loginPlayer(player: PlayerEntity) {
        CurrentPlayer.player = player
        Toast.makeText(requireContext(), "С возвращением, ${player.fullName}!", Toast.LENGTH_SHORT).show()

        android.util.Log.d("LOGIN_DEBUG", "Игрок выбран, переходим в меню. ID игрока: ${player.id}")

        try {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MenuFragment())
                .commit()
        } catch (e: Exception) {
            android.util.Log.e("LOGIN_DEBUG", "Ошибка при переходе: ${e.message}")
            Toast.makeText(requireContext(), "Ошибка перехода: ${e.message}", Toast.LENGTH_LONG).show()
        }
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
            val newPlayer = db.playerDao().getById(id)
            if (newPlayer != null) {
                loginPlayer(newPlayer)
            }
        }
    }
    private fun updateDateLabel() {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        tvBirthDate.text = "Выбрано: ${format.format(selectedDate.time)}"
    }
}