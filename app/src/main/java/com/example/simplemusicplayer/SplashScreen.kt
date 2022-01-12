package com.example.simplemusicplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat

class SplashScreen : AppCompatActivity() {

    var permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.MODIFY_AUDIO_SETTINGS
    )

    val permissionCode = 10001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_splash_screen)

        if(checkPermission()){
            goHome()
        }
        else
        {
            ActivityCompat.requestPermissions(this@SplashScreen,permissions,permissionCode)
        }


    }
    private fun checkPermission():Boolean
    {
        for(perms :String in permissions) {
            var data : Int = application.checkCallingOrSelfPermission(perms)
            if(data != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }



    private fun goHome() {
        Handler().postDelayed({
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }, 3000)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode)
        {
            permissionCode ->
            {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                {
                    goHome()
                }
                else
                    Toast.makeText(applicationContext,"Please grant permission to access the files",Toast.LENGTH_SHORT).show()

            }
            else -> {
                Toast.makeText(applicationContext,"Error Occured",Toast.LENGTH_SHORT).show()

            }
        }
    }
}