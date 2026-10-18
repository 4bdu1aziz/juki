package com.example.juki

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.juki.db.AppDatabase
import kotlinx.coroutines.launch

class RecordsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_records, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getInstance(requireContext())
        val list = view.findViewById<ListView>(R.id.listScores)

        lifecycleScope.launch {
            db.scoreDao().getTopScores().collect { scores ->
                val adapter = ScoreAdapter(requireContext(), scores)
                list.adapter = adapter
            }
        }
    }
}

class ScoreAdapter(context: android.content.Context, private val items: List<com.example.juki.db.ScoreWithName>) :
    ArrayAdapter<com.example.juki.db.ScoreWithName>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_record, parent, false)
        val item = items[position]

        view.findViewById<android.widget.TextView>(R.id.tvRecordPlayer).text = item.playerName
        view.findViewById<android.widget.TextView>(R.id.tvRecordScore).text =
            "Очки: ${item.score} | Сложность: ${item.difficulty} | Точность: ${"%.1f".format(item.accuracy)}%"

        return view
    }
}