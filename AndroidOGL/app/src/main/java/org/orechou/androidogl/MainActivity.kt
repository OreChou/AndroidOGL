package org.orechou.androidogl

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import org.orechou.androidogl.sticker.StickerActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var mBtnCamera: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mBtnCamera = findViewById(R.id.btn_camera)
        mBtnCamera!!.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_camera -> startActivity(Intent(MainActivity@this, StickerActivity::class.java))
        }
    }

}
