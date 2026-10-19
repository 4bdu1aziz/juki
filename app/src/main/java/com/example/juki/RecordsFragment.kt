package com.example.juki

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.juki.db.AppDatabase
import com.example.juki.db.ScoreWithName
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RecordsFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var listScores: ListView
    private var collectJob: Job? = null // Для отмены предыдущего запроса при переключении

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_records, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        db = AppDatabase.getInstance(requireContext())
        listScores = view.findViewById(R.id.listScores)
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayoutTime)

        // Добавляем вкладки для времени
        tabLayout.addTab(tabLayout.newTab().setText("30 сек").setTag(30))
        tabLayout.addTab(tabLayout.newTab().setText("45 сек").setTag(45))
        tabLayout.addTab(tabLayout.newTab().setText("60 сек").setTag(60))
        tabLayout.addTab(tabLayout.newTab().setText("75 сек").setTag(75))

        // По умолчанию выбираем 60 секунд (индекс 2)
        tabLayout.getTabAt(2)?.select()
        loadScoresForDuration(60)

        // Слушатель переключения вкладок
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val duration = tab?.tag as Int
                loadScoresForDuration(duration)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadScoresForDuration(duration: Int) {
        // Отменяем предыдущий запрос, если он еще выполняется
        collectJob?.cancel()
        
        collectJob = lifecycleScope.launch {
            db.scoreDao().getTopScoresByDuration(duration).collect { scores ->
                listScores.adapter = ScoreAdapter(requireContext(), scores)
            }
        }
    }
}

// Кастомный адаптер для вывода рекордов
class ScoreAdapter(
    context: Context, 
    private val items: List<ScoreWithName>
) : ArrayAdapter<ScoreWithName>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_record, parent, false)
        val item = items[position]

        view.findViewById<TextView>(R.id.tvRecordPlayer).text = 
            "${position + 1}. ${item.playerName}"

        view.findViewById<TextView>(R.id.tvRecordScore).text =
            "Рейтинг: ${item.score} (Сложность: ${item.difficulty}) | Точность: ${"%.1f".format(item.accuracy)}%"

        return view
    }
}