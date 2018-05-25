package id.assel.caribengkel.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import id.assel.caribengkel.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //todo check is logged in or  not
        if (true){
            val i = Intent(this, MapsActivity::class.java)
            startActivity(i)
        } else {
            val i = Intent(this, LoginActivity::class.java)
        }

    }
}
