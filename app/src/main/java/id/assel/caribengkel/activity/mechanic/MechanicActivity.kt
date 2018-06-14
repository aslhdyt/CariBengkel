package id.assel.caribengkel.activity.mechanic

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import id.assel.caribengkel.R
import id.assel.caribengkel.activity.auth.SplashActivity
import id.assel.caribengkel.model.Workshop
import id.assel.caribengkel.tools.LoginPref
import kotlinx.android.synthetic.main.activity_mechanic.*
import java.util.*

class MechanicActivity : AppCompatActivity() {
    lateinit var viewModel: MechanicViewModel
    lateinit var selectedWorkshop: LiveData<Workshop>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mechanic)

        viewModel = ViewModelProviders.of(this).get(MechanicViewModel::class.java)


        spinnerMechanic.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {

            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }

        viewModel.workshops.observe(this, Observer { workshops ->
            if (workshops != null) {
                val comparator = Comparator<Workshop> { left, right ->
                    left.getId() - right.getId() // use your logic
                }
                Collections.sort(workshops, comparator)
                val workshopName = ArrayList<String>()
                for (workshop in workshops) {
                    workshopName.add(workshop.name)
                }

                val adapter = ArrayAdapter(this@MechanicActivity, R.layout.support_simple_spinner_dropdown_item, workshopName)
                spinnerMechanic.adapter = adapter
            } else {
                spinnerMechanic.adapter = null
            }
        })




        findViewById<View>(R.id.buttonSignOut).setOnClickListener { view ->
            LoginPref.clearAll(view.context)

            val intent = Intent(this@MechanicActivity, SplashActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
