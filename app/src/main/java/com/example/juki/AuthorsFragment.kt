package com.example.juki

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment

class AuthorsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_authors, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val authors = listOf(
            Author("Мамарасулов Абдулазиз", R.drawable.author_1),
            Author("Авдеев Егор", R.drawable.author_2),
        )

        view.findViewById<ListView>(R.id.listAuthors).adapter =
            AuthorAdapter(requireContext(), authors)
    }
}