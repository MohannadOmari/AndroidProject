package com.example.androidproject

import android.annotation.SuppressLint
import android.content.ContentValues
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    fun onClickAddName(view: View?) {
        val values = ContentValues()
        values.put(
            PetsProvider.NAME,
            (findViewById<View>(R.id.name) as EditText).text.toString()
        )
        values.put(
            PetsProvider.TYPE,
            (findViewById<View>(R.id.type) as EditText).text.toString()
        )
        values.put(
            PetsProvider.AGE,
            (findViewById<View>(R.id.age) as EditText).text.toString()
        )
        val uri = contentResolver.insert(
            PetsProvider.CONTENT_URI, values
        )
        Toast.makeText(baseContext, uri.toString(), Toast.LENGTH_LONG).show()
    }
    @SuppressLint("Range")
    fun onCLickRetrievePets(view: View?) {
        val URL = "content://com.example.androidproject.PetsProvider"
        val pets = Uri.parse(URL)
        var c = contentResolver.query(pets, null, null, null, null)
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    Toast.makeText(this, c.getString(c.getColumnIndex(PetsProvider._ID)) +
                    ", " + c.getString(c.getColumnIndex(PetsProvider.NAME)) + ", " +
                    c.getString(c.getColumnIndex(PetsProvider.TYPE)) + ", " +
                    c.getString(c.getColumnIndex(PetsProvider.AGE)),Toast.LENGTH_SHORT).show()
                } while (c.moveToNext())
            }
        }
    }
}