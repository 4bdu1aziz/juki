package com.example.juki

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

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
        bindSeekBar(
            view, R.id.seekRound, R.id.tvRoundLabel,
            format = { "Длительность раунда: $it сек" }
        )

        view.findViewById<Button>(R.id.btnSaveSettings).setOnClickListener {
            val settings = GameSettings(
                speed = view.findViewById<SeekBar>(R.id.seekSpeed).progress,
                maxRoaches = view.findViewById<SeekBar>(R.id.seekRoaches).progress,
                bonusIntervalSec = view.findViewById<SeekBar>(R.id.seekBonus).progress,
                roundDurationSec = view.findViewById<SeekBar>(R.id.seekRound).progress
            )
            Toast.makeText(
                requireContext(),
                "Сохранено: скорость ${settings.speed}, тараканов ${settings.maxRoaches}, " +
                        "бонус ${settings.bonusIntervalSec}с, раунд ${settings.roundDurationSec}с",
                Toast.LENGTH_LONG
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