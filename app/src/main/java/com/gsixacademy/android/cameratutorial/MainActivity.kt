package com.gsixacademy.android.cameratutorial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_select_image.setOnClickListener {
            showPhotoDialog()
        }

    }


    private fun showPhotoDialog(){

        val builder = AlertDialog.Builder(this)

        builder.setTitle(R.string.access_to_camera)
        builder.setPositiveButton(R.string.take_photo) { a, b ->
            Toast.makeText(this,"take photo clicked", Toast.LENGTH_LONG).show()
        }
        builder.setNegativeButton(R.string.choose_from_gallery) { a, b ->
            Toast.makeText(this,"choose from gallery clicked", Toast.LENGTH_LONG).show()
        }
        builder.setNeutralButton(R.string.cancel){a,b ->
            Toast.makeText(this,"cancel clicked", Toast.LENGTH_LONG).show()
        }
        builder.show()

    }

}