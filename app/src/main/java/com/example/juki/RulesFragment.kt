package com.example.juki

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class RulesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_rules, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val html = resources.openRawResource(R.raw.rules)
            .bufferedReader().use { it.readText() }

        val tvRules = view.findViewById<TextView>(R.id.tvRules)
        tvRules.text = Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
    }
}