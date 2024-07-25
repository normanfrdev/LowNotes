package com.normanfr.lownotes

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.IOException

class EditNoteActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etNote: EditText
    private lateinit var btnSave: Button
    private lateinit var btnExportTxt: Button
    private lateinit var btnExportPdf: Button
    private lateinit var sharedPreferences: SharedPreferences
    private var noteTimestamp: Long = -1
    private var noteTitle: String = ""

    private val filePickerLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri: Uri? ->
            uri?.let {
                saveNoteToUri(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        etTitle = findViewById(R.id.et_title)
        etNote = findViewById(R.id.et_note)
        btnSave = findViewById(R.id.btn_save)
        btnExportTxt = findViewById(R.id.btn_export_txt)
        btnExportPdf = findViewById(R.id.btn_export_pdf)

        sharedPreferences = getSharedPreferences("notes", MODE_PRIVATE)

        noteTitle = intent.getStringExtra("NOTE_TITLE") ?: ""
        noteTimestamp = intent.getLongExtra("NOTE_TIMESTAMP", System.currentTimeMillis())

        etTitle.setText(noteTitle)
        etNote.setText(loadNoteContent(noteTitle, noteTimestamp))

        btnSave.setOnClickListener { saveNote() }
        btnExportTxt.setOnClickListener { openFilePicker("note.txt") }
        btnExportPdf.setOnClickListener { openFilePicker("note.pdf") }

        checkPermissions()
    }

    private fun loadNoteContent(title: String, timestamp: Long): String {
        return sharedPreferences.getString("note_${timestamp}_$title", "") ?: ""
    }

    private fun saveNote() {
        val title = etTitle.text.toString()
        val content = etNote.text.toString()

        val editor = sharedPreferences.edit()
        editor.putString("note_${noteTimestamp}_$title", content)
        editor.apply()

        Toast.makeText(this, "Note saved successfully", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun openFilePicker(fileName: String) {
        val fileNameWithExtension = if (fileName.contains('.')) fileName else "$fileName.txt"
        filePickerLauncher.launch(fileNameWithExtension)
    }

    private fun saveNoteToUri(uri: Uri) {
        val noteContent = etNote.text.toString()
        when {
            uri.toString().endsWith(".pdf") -> savePdfToUri(uri, noteContent)
            else -> saveFileToUri(uri, noteContent)
        }
    }

    private fun saveFileToUri(uri: Uri, content: String) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
                Toast.makeText(this, "File saved successfully", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePdfToUri(uri: Uri, content: String) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                val document = Document()
                PdfWriter.getInstance(document, outputStream)
                document.open()
                document.add(Paragraph(content))
                document.close()
                Toast.makeText(this, "PDF saved successfully", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyStyle(style: Int) {
        val start = etNote.selectionStart
        val end = etNote.selectionEnd
        val spannable = SpannableString(etNote.text)
        spannable.setSpan(StyleSpan(style), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        etNote.setText(spannable)
        etNote.setSelection(start, end)
    }

    private fun applyUnderline() {
        val start = etNote.selectionStart
        val end = etNote.selectionEnd
        val spannable = SpannableString(etNote.text)
        spannable.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        etNote.setText(spannable)
        etNote.setSelection(start, end)
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (!hasPermissions(permissions)) {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissionsGranted = permissions.values.all { it }
        if (!allPermissionsGranted) {
            //Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
        }
    }
}
