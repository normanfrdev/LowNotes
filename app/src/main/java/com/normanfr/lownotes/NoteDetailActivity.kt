package com.normanfr.lownotes

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class NoteDetailActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        val noteContent = intent.getStringExtra("NOTE_CONTENT")
        val textView: TextView = findViewById(R.id.textViewNoteContent)
        textView.text = noteContent
    }
}
