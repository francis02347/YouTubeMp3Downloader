package com.example.youtubemp3downloader

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar YoutubeDL
        try {
            YoutubeDL.getInstance().init(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val urlInput = findViewById<EditText>(R.id.urlInput)
        val downloadButton = findViewById<Button>(R.id.downloadButton)

        downloadButton.setOnClickListener {
            val url = urlInput.text.toString()
            if (url.isNotEmpty()) {
                downloadAudio(url)
            } else {
                Toast.makeText(this, "Por favor ingresa un link", Toast.LENGTH_SHORT).show()
            }
        }

        // Buscar actualizaciones automáticas al arrancar
        checkForUpdates()
    }

    private fun checkForUpdates() {
        Thread {
            try {
                val url = java.net.URL("https://raw.githubusercontent.com/francis02347/YouTubeMp3Downloader/main/update.json")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                
                if (conn.responseCode == 200) {
                    val stream = conn.inputStream
                    val reader = java.io.BufferedReader(java.io.InputStreamReader(stream))
                    val sb = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        sb.append(line)
                    }
                    reader.close()
                    
                    val json = org.json.JSONObject(sb.toString())
                    val latestVersionCode = json.getInt("versionCode")
                    val latestVersionName = json.getString("versionName")
                    val apkUrl = json.getString("apkUrl")
                    val releaseNotes = json.optString("releaseNotes", "")
                    
                    val currentVersionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        packageManager.getPackageInfo(packageName, 0).longVersionCode.toInt()
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getPackageInfo(packageName, 0).versionCode
                    }
                    
                    if (latestVersionCode > currentVersionCode) {
                        runOnUiThread {
                            showUpdateDialog(latestVersionName, apkUrl, releaseNotes)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun showUpdateDialog(versionName: String, apkUrl: String, notes: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Actualización Disponible (v$versionName)")
            .setMessage("Nueva versión disponible en GitHub.\n\nNotas:\n$notes\n\n¿Deseas descargarla ahora?")
            .setPositiveButton("Descargar") { _, _ ->
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(apkUrl))
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun downloadAudio(url: String) {
        val youtubeDLDir = File(externalCacheDir, "youtubedl")
        val request = YoutubeDLRequest(url)
        request.addOption("-x") // Extraer audio
        request.addOption("--audio-format", "mp3")
        request.addOption("--audio-quality", "0") // Mejor calidad

        Thread {
            try {
                YoutubeDL.getInstance().execute(request) { progress, etaInSeconds, line ->
                    runOnUiThread {
                        // Aquí podrías actualizar una barra de progreso
                    }
                }
                runOnUiThread { Toast.makeText(this, "Descarga completada", Toast.LENGTH_LONG).show() }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show() }
            }
        }.start()
    }
}
