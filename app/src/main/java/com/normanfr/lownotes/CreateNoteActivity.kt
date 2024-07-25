package com.normanfr.lownotes

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CreateNoteActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        val etNote = findViewById<EditText>(R.id.et_note)
        val btnSave = findViewById<Button>(R.id.btn_save)

        btnSave.setOnClickListener {
            val noteText = etNote.text.toString()
            if (noteText.isNotEmpty()) {
                saveNoteToSharedPreferences(noteText)
                Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Please enter a note", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveNoteToSharedPreferences(note: String) {
        val sharedPreferences = getSharedPreferences("notes", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val noteId = System.currentTimeMillis() // UUID based on time
        editor.putString(noteId.toString(), note)
        editor.apply()
    }

}
