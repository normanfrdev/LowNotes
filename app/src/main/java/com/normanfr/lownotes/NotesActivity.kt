package com.normanfr.lownotes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotesActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        val listView: ListView = findViewById(R.id.listView)
        val notes = loadNotes()

        val adapter = NotesAdapter(this, notes) { title, timestamp ->
            showNoteDetails(title, timestamp)
        }

        listView.adapter = adapter
    }

    private fun loadNotes(): List<Pair<String, Long>> {
        val notes = mutableListOf<Pair<String, Long>>()
        val sharedPreferences = getSharedPreferences("notes", MODE_PRIVATE)
        val allEntries = sharedPreferences.all

        for ((key, value) in allEntries) {
            if (value is String && key.startsWith("note_")) {
                try {

                    val parts = key.removePrefix("note_").split("_", limit = 2)
                    if (parts.size == 2) {
                        val timestamp = parts[0].toLongOrNull()
                        val title = parts[1]
                        if (timestamp != null) {
                            notes.add(Pair(title, timestamp))
                        }
                    }
                } catch (e: Exception) {
                    Log.e("NotesActivity", "Error parsing note data", e)
                }
            }
        }


        notes.sortByDescending { it.second }

        return notes
    }

    private fun showNoteDetails(title: String, timestamp: Long) {
        val intent = Intent(this, EditNoteActivity::class.java).apply {
            putExtra("NOTE_TITLE", title)
            putExtra("NOTE_TIMESTAMP", timestamp)
        }
        startActivity(intent)
    }

    private fun parseDateToTimestamp(formattedDate: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return try {
            val date = dateFormat.parse(formattedDate)
            date?.time ?: -1
        } catch (e: Exception) {
            -1
        }
    }
}
