package id.assel.caribengkel.activity.mechanic

import android.arch.lifecycle.*
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

class MechanicActivity : AppCompatActivity() {
    lateinit var viewModel: MechanicViewModel
    lateinit var selectedWorkshop: LiveData<Workshop>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mechanic)

        viewModel = ViewModelProviders.of(this).get(MechanicViewModel::class.java)
        selectedWorkshop = Transformations.switchMap(viewModel.workshops) { null }

        spinnerMechanic.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                val workshop = spinnerMechanic.adapter.getItem(i) as Workshop?
                selectedWorkshop.removeObservers(this@MechanicActivity)
                selectedWorkshop = if (workshop != null)
                    Transformations.switchMap(viewModel.workshops) { viewModel.getWorkshopRepository(workshop) }
                else
                    Transformations.switchMap(viewModel.workshops) { null }
                selectedWorkshop.observe(this@MechanicActivity, workshopObserver)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }

        viewModel.workshops.observe(this, Observer { workshops ->
            if (workshops != null) {
                val sorted = workshops.sortedBy { it.id }

                val adapter = ArrayAdapter(this@MechanicActivity, android.R.layout.simple_spinner_dropdown_item, sorted)
                spinnerMechanic.adapter = adapter

                spinnerMechanic.visibility = View.VISIBLE
                switchJob.visibility = View.VISIBLE
                textView2.visibility = View.VISIBLE
                tvCoordinate.visibility = View.VISIBLE
                textView3.visibility = View.VISIBLE

                viewModel.workshops.removeObservers(this)
            } else {
                spinnerMechanic.adapter = null
            }
        })


        buttonSignOut.setOnClickListener { view ->
            LoginPref.clearAll(view.context)

            val intent = Intent(this@MechanicActivity, SplashActivity::class.java)
            startActivity(intent)
            finish()
        }

        switchJob.setOnCheckedChangeListener { compoundButton, isChecked ->
            viewModel.setActiveWorkshop(isChecked, selectedWorkshop.value)
        }
    }

    val workshopObserver = Observer<Workshop> {
        if (it != null) {
            switchJob.isEnabled = true
            switchJob.isChecked = it.active ?: false
            tvCoordinate.text = "${it.latLng.latitude}\n${it.latLng.longitude}"
        } else {
            switchJob.isChecked = false
            switchJob.isEnabled = false
            tvCoordinate.text = "-"
        }
    }


}
