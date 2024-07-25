package com.normanfr.lownotes

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notes -> {
                showNotes()
                true
            }
            R.id.action_create_note -> {
                openCreateNote()
                true
            }
            R.id.action_reload -> {
                reloadNotes()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showNotes() {
        val intent = Intent(this, NotesActivity::class.java)
        startActivity(intent)
    }

    private fun openCreateNote() {
        val intent = Intent(this, CreateNoteActivity::class.java)
        startActivity(intent)
    }

    private fun reloadNotes() {
        val intent = Intent(this, NotesActivity::class.java)
        startActivity(intent)
    }
}
