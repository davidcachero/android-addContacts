package com.example.agendacontactosdavid

import java.io.ByteArrayOutputStream
import android.app.Activity
import android.Manifest
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class MainActivity : AppCompatActivity() {
    val REQUEST_IMAGE_CAPTURE = 100
    private val PERMISSION_REQUEST_CODE: Int = 101
    lateinit var saveButton: Button
    lateinit var imageView: ImageView
    lateinit var captureButton: ImageButton
    lateinit var bitmap: Bitmap
    lateinit var nombre: EditText
    lateinit var apellidos: EditText
    lateinit var telefono: EditText
    lateinit var correo: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        saveButton = findViewById(R.id.btnGuardar)
        imageView = findViewById(R.id.imageView)
        captureButton = findViewById(R.id.imageButton)
        nombre = findViewById(R.id.nombre)
        apellidos = findViewById(R.id.apellidos)
        telefono = findViewById(R.id.telefono)
        correo = findViewById(R.id.correo)
        bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        captureButton.setOnClickListener(View.OnClickListener {
            if (checkPersmission()) {
                takePicture()
            } else {
                requestPermission()
            }
        })
        saveButton.setOnClickListener {
            val miIntent = Intent(Intent.ACTION_INSERT)
            nombre = findViewById(R.id.nombre)
            apellidos = findViewById(R.id.apellidos)
            miIntent.type = ContactsContract.Contacts.CONTENT_TYPE
            miIntent.putExtra(
                ContactsContract.Intents.Insert.NAME, "${nombre.text} ${apellidos.text}"
            )
            miIntent.putExtra(ContactsContract.Intents.Insert.EMAIL, correo.text)
            miIntent.putExtra(ContactsContract.Intents.Insert.PHONE, telefono.text)
            val elements = ContentValues().apply {
                put(ContactsContract.CommonDataKinds.Photo.PHOTO, takeBitmap(bitmap))
                put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                )
            }
            val list = arrayListOf(elements)
            miIntent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, list)
            if (miIntent.resolveActivity(packageManager) != null) {
                startActivity(miIntent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        save()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    takePicture()
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
            else -> {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val ref = getPreferences(MODE_PRIVATE)
        val cod = ref.edit()
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                bitmap = data!!.extras!!.get("data") as Bitmap
                imageView.setImageBitmap(bitmap)
                cod.putString("img", codifier(bitmap))
                cod.commit()
            }
        }
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Salir")
        builder.setMessage("¿Quieres salir de la aplicacion?")
        builder.setPositiveButton("SI") { dialog, which ->
            dialog.dismiss()
            finish()
        }
        builder.setNegativeButton("NO") { dialog, which -> dialog.dismiss() }
        val alert = builder.create()
        alert.show()
    }

    private fun checkPersmission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CODE
        )
    }

    private fun takePicture() {
        val intent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    private fun takeBitmap(bitmap: Bitmap): ByteArray {
        val bit = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, bit)
        return bit.toByteArray()
    }

    private fun codifier(image: Bitmap): String? {
        val str = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, str)
        val byte = str.toByteArray()
        val codeImg: String = Base64.encodeToString(byte, Base64.DEFAULT)
        Log.d("Picture:", codeImg)
        return codeImg
    }

    private fun decoder(input: String?): Bitmap {
        val bytecode = Base64.decode(input, 0)
        return BitmapFactory.decodeByteArray(bytecode, 0, bytecode.size)
    }

    private fun save() {
        val ref = getPreferences(MODE_PRIVATE)
        val log = ref.getString("img", "")
        if (log != "") bitmap = decoder(log)
        imageView.setImageBitmap(bitmap)
    }
}