package id.assel.caribengkel.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()

        //todo check is logged in or  not
        if (auth.currentUser != null){
            val i = Intent(this, MapsActivity::class.java)
            startActivity(i)
        } else {
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
        }

    }
}
