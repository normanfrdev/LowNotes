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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.apache.poi.xwpf.usermodel.XWPFDocument
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var etNote: EditText
    private lateinit var btnSave: Button
    private lateinit var btnExportTxt: Button
    private lateinit var btnExportPdf: Button
    private lateinit var btnExportDoc: Button
    private lateinit var btnBold: Button
    private lateinit var btnItalic: Button
    private lateinit var btnUnderline: Button
    private lateinit var sharedPreferences: SharedPreferences

    private val filePickerLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri: Uri? ->
            uri?.let {
                saveNoteToUri(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        etNote = findViewById(R.id.et_note)
        btnSave = findViewById(R.id.btn_save)
        btnExportTxt = findViewById(R.id.btn_export_txt)
        btnExportPdf = findViewById(R.id.btn_export_pdf)
        btnExportDoc = findViewById(R.id.btn_export_doc)
        btnBold = findViewById(R.id.btn_bold)
        btnItalic = findViewById(R.id.btn_italic)
        btnUnderline = findViewById(R.id.btn_underline)

        sharedPreferences = getSharedPreferences("notes", MODE_PRIVATE)

        btnSave.setOnClickListener { showTitleInputDialog() }
        btnExportTxt.setOnClickListener { openFilePicker("note.txt") }
        btnExportPdf.setOnClickListener { openFilePicker("note.pdf") }
        btnExportDoc.setOnClickListener { openFilePicker("note.docx") }

        btnBold.setOnClickListener { applyStyle(Typeface.BOLD) }
        btnItalic.setOnClickListener { applyStyle(Typeface.ITALIC) }
        btnUnderline.setOnClickListener { applyUnderline() }

        checkPermissions()
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

    private fun showTitleInputDialog() {
        val titleEditText = EditText(this)
        titleEditText.hint = "Enter title"

        AlertDialog.Builder(this)
            .setTitle("Save Note")
            .setMessage("Enter a title for your note:")
            .setView(titleEditText)
            .setPositiveButton("Save") { _, _ ->
                val title = titleEditText.text.toString().trim()
                if (title.isNotEmpty()) {
                    saveNote(title)
                } else {
                    Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveNote(title: String) {
        val content = etNote.text.toString()
        val timestamp = System.currentTimeMillis()

        val editor = sharedPreferences.edit()
        editor.putString("note_${timestamp}_$title", content)
        editor.putLong("timestamp_$title", timestamp)
        editor.putString("title_${timestamp}", title)
        editor.apply()

        Toast.makeText(this, "Note saved successfully", Toast.LENGTH_SHORT).show()
    }

    private fun openFilePicker(fileName: String) {
        val fileNameWithExtension = if (fileName.contains('.')) fileName else "$fileName.txt"
        val mimeType = when {
            fileNameWithExtension.endsWith(".pdf") -> "application/pdf"
            fileNameWithExtension.endsWith(".docx") -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            else -> "text/plain"
        }
        filePickerLauncher.launch(fileNameWithExtension)
    }

    private fun saveNoteToUri(uri: Uri) {
        val noteContent = etNote.text.toString()
        when {
            uri.toString().endsWith(".pdf") -> savePdfToUri(uri, noteContent)
            uri.toString().endsWith(".docx") -> saveDocToUri(uri, noteContent)
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

    private fun saveDocToUri(uri: Uri, content: String) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                val document = XWPFDocument()
                val paragraph = document.createParagraph()
                val run = paragraph.createRun()
                run.setText(content)
                document.write(outputStream)
                Toast.makeText(this, "DOC saved successfully", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving DOC", Toast.LENGTH_SHORT).show()
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

    private fun showNotes() {
        val intent = Intent(this, NotesActivity::class.java)
        startActivity(intent)
    }

    private fun openCreateNote() {
        val intent = Intent(this, CreateNoteActivity::class.java)
        startActivity(intent)
    }

    private fun reloadNotes() {
        Toast.makeText(this, "Notes reloaded", Toast.LENGTH_SHORT).show()
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val permissionsNotGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNotGranted.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsNotGranted.toTypedArray())
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissionsGranted = permissions.values.all { it }
        if (!allPermissionsGranted) {
            //Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
            //REmoved cuz app shows this message even though its granted.
        }
    }
}
