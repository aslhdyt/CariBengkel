package id.assel.caribengkel.activity.mechanic

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import id.assel.caribengkel.R
import id.assel.caribengkel.activity.auth.SplashActivity
import id.assel.caribengkel.model.Workshop
import id.assel.caribengkel.tools.LoginPref
import kotlinx.android.synthetic.main.activity_mechanic.*

class MechanicActivity : AppCompatActivity() {
    lateinit var viewModel: MechanicViewModel
    var selectedWorkshop: MutableLiveData<Workshop> = MutableLiveData<Workshop>();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mechanic)

        viewModel = ViewModelProviders.of(this).get(MechanicViewModel::class.java)


        spinnerMechanic.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                val workshop = viewModel.workshops.value?.get(i)
                if (workshop != null) {
                    selectedWorkshop.postValue(workshop)
                } else
                    selectedWorkshop.postValue(null)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }

        viewModel.workshops.observe(this, Observer { workshops ->
            if (workshops != null) {
                workshops.sortBy { it.id }

                val adapter = ArrayAdapter(this@MechanicActivity, android.R.layout.simple_spinner_dropdown_item, workshops.map { it.name })
                spinnerMechanic.adapter = adapter
            } else {
                spinnerMechanic.adapter = null
            }
        })
        selectedWorkshop.observe(this, Observer {
            Toast.makeText(this@MechanicActivity, it?.name, Toast.LENGTH_SHORT).show()
        })




        findViewById<View>(R.id.buttonSignOut).setOnClickListener { view ->
            LoginPref.clearAll(view.context)

            val intent = Intent(this@MechanicActivity, SplashActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


}
