package com.example.juki

import android.os.Bundle	
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    private val roundTimes = listOf(30, 45, 60, 75)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindSeekBar(
            view, R.id.seekSpeed, R.id.tvSpeedLabel,
            format = { "Скорость игры: $it" }
        )
        bindSeekBar(
            view, R.id.seekRoaches, R.id.tvRoachesLabel,
            format = { "Максимум тараканов: $it" }
        )
        bindSeekBar(
            view, R.id.seekBonus, R.id.tvBonusLabel,
            format = { "Интервал бонусов: $it сек" }
        )

        // Настройка выпадающего списка для времени раунда
        val spinnerTime = view.findViewById<Spinner>(R.id.spinnerRoundTime)
        spinnerTime.adapter = ArrayAdapter(
            requireContext(), 
            android.R.layout.simple_spinner_dropdown_item, 
            roundTimes.map { "$it секунд" }
        )
        
        // Устанавливаем сохраненное значение при открытии
        val currentIndex = roundTimes.indexOf(GlobalSettings.roundDurationSec)
        if (currentIndex != -1) spinnerTime.setSelection(currentIndex)

        view.findViewById<Button>(R.id.btnSaveSettings).setOnClickListener {
            // Сохраняем все настройки в глобальный объект
            GlobalSettings.speed = view.findViewById<SeekBar>(R.id.seekSpeed).progress
            GlobalSettings.maxRoaches = view.findViewById<SeekBar>(R.id.seekRoaches).progress
            GlobalSettings.bonusIntervalSec = view.findViewById<SeekBar>(R.id.seekBonus).progress
            GlobalSettings.roundDurationSec = roundTimes[spinnerTime.selectedItemPosition]

            Toast.makeText(
                requireContext(),
                "Сохранено! Время раунда: ${GlobalSettings.roundDurationSec} сек.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun bindSeekBar(
        root: View,
        seekId: Int,
        labelId: Int,
        format: (Int) -> String
    ) {
        val seek = root.findViewById<SeekBar>(seekId)
        val label = root.findViewById<TextView>(labelId)
        label.text = format(seek.progress)
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                label.text = format(progress)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }
}