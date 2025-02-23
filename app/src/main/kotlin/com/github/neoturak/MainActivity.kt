package com.github.neoturak

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.github.neoturak.viewer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding by lazy { _binding!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //
        binding.attrList.setOnClickListener {
            val i = Intent(this, ViewEffectActivity::class.java)
            startActivity(i)
        }

        val testImageUrl =
            "https://img0.baidu.com/it/u=238910512,2075984702&fm=253&fmt=auto&app=120&f=JPEG?w=889&h=500"

        /*     CoroutineScope(Dispatchers.IO).launch {
                 delay(3000)
                 var bitmap: Bitmap?
                 runCatching {
                     val url = URL(testImageUrl)
                     val connection = url.openConnection() as HttpURLConnection
                     connection.doInput = true
                     connection.connect()
                     bitmap = Bitmap.createScaledBitmap(
                         BitmapFactory.decodeStream(connection.inputStream),
                         binding.ivImage.width, binding.ivImage.height, false
                     )
                     withContext(Dispatchers.Main) {
                         binding.ivImage.setImageBitmap(bitmap)
                     }
                 }
             }*/

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}