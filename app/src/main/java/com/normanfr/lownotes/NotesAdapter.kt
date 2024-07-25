
package com.normanfr.lownotes

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Locale

class NotesAdapter(
    private val context: Context,
    private var notes: List<Pair<String, Long>>,
    private val onNoteClick: (String, Long) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = notes.size

    override fun getItem(position: Int): Any = notes[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_note, parent, false)
        val noteTitle = view.findViewById<TextView>(R.id.note_title)
        val noteContent = view.findViewById<TextView>(R.id.note_date)

        val note = notes[position]
        noteTitle.text = note.first
        noteContent.text = formatTimestamp(note.second)

        view.setOnClickListener {
            onNoteClick(note.first, note.second)
        }

        return view
    }

    private fun loadNoteContent(title: String, timestamp: Long): String {
        val sharedPreferences = context.getSharedPreferences("notes", Context.MODE_PRIVATE)
        return sharedPreferences.getString("note_${timestamp}_${title}", "") ?: ""
    }

    private fun formatTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(timestamp)
    }


    fun updateNotes(newNotes: List<Pair<String, Long>>) {
        notes = newNotes
        notifyDataSetChanged()
    }
}
