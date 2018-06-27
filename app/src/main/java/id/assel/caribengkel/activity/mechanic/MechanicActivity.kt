package id.assel.caribengkel.activity.mechanic

import android.Manifest
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import id.assel.caribengkel.R
import id.assel.caribengkel.activity.auth.SplashActivity
import id.assel.caribengkel.model.Order
import id.assel.caribengkel.model.OrderLiveData
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

        viewModel.workshops.observe(this, object : Observer<List<Workshop>> {
            override fun onChanged(workshops: List<Workshop>?) {
                if (workshops != null && workshops.isNotEmpty()) {
                    val sorted = workshops.sortedBy { it.id }

                    val adapter = ArrayAdapter(this@MechanicActivity, R.layout.spinner_item, sorted)
                    spinnerMechanic.adapter = adapter

                    spinnerMechanic.visibility = View.VISIBLE
                    switchJob.visibility = View.VISIBLE
                    textView2.visibility = View.VISIBLE
                    tvCoordinate.visibility = View.VISIBLE
                    textView3.visibility = View.VISIBLE

                    viewModel.workshops.removeObserver(this)
                } else {
                    spinnerMechanic.adapter = null
                }

            }

        })


        buttonSignOut.setOnClickListener { view ->
            LoginPref.clearAll(view.context)

            val intent = Intent(this@MechanicActivity, SplashActivity::class.java)
            startActivity(intent)
            finish()
        }

        switchJob.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setActiveWorkshop(isChecked, selectedWorkshop.value)
        }
    }

    val workshopObserver = Observer<Workshop> { workshop ->
        cvOrder.visibility = View.GONE
        switchJob.isEnabled = true
        if (workshop != null) {
            switchJob.isEnabled = true
            switchJob.isChecked = workshop.active
            tvCoordinate.text = "${workshop.latLng.latitude}\n${workshop.latLng.longitude}"

            val orderUUID = workshop.currentOrderUuid
            if (!orderUUID.isNullOrBlank() && orderUUID != null) {
                //delete last job

                val currentOrder = OrderLiveData(this@MechanicActivity, orderUUID)
                currentOrder.observe(this, Observer {order ->
                    if (order != null) {
                        val jobDialog = JobDialog.getInstance(this@MechanicActivity, order, object : JobDialog.JobResponse {
                            override fun onJobsAccepted(order: Order) {
                                if (ContextCompat.checkSelfPermission(this@MechanicActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    viewModel.acceptJob(order)
                                } else {
                                    viewModel.checkLocationPermission(this@MechanicActivity)
                                }
                            }

                            override fun onJobsRejected(order: Order) {
                                viewModel.rejectJob(order)
                            }

                        })
                        when (order.status) {
                            Order.ORDER_PENDING -> jobDialog.show()
                            Order.ORDER_ONGOING -> {
                                switchJob.isEnabled = false

                                jobDialog.dismiss()
                                JobDialog.destroyInstance()

                                println("TODO show current job")
                                cvOrder.visibility = View.VISIBLE
                                tvOrderId.text = "id: ${order.uuid.takeLast(10)}"
                                tvClientName.text = order.username
                                tvTargeLocation.text = "${order.location.latitude}\n${order.location.longitude}"
                                btnFinish.setOnClickListener { viewModel.finishOrder(order) }
                                btnMap.setOnClickListener {
                                    val intent = Intent(android.content.Intent.ACTION_VIEW,
                                            Uri.parse("http://maps.google.com/maps?" +
                                                    "saddr=${workshop.latLng.latitude},${workshop.latLng.longitude}&" +
                                                    "daddr=${order.location.latitude},${order.location.longitude}"))
                                    startActivity(intent)
                                }
//                                val locationUrl = "https://maps.googleapis.com/maps/api/staticmap?" +
//                                        "center=${order.location.latitude},${order.location.longitude}&"
//                                        "&zoom=5" +
//                                        "&size=200x200" +
//                                        "&maptype=roadmap" +
//                                        "&key=${R.string.google_maps_key}"
//                                //TODO set map preview
//                                Picasso.get().load(locationUrl).placeholder(android.R.drawable.ic_menu_mapmode).into(ivLocationMap)

                                viewModel.updatesMechanicPosition(this, orderUUID)
                            }
                            Order.ORDER_FINISH -> {
                                jobDialog.dismiss()
                                JobDialog.destroyInstance()

                                FirebaseFirestore.getInstance().document("workshop/${workshop.id}").update("currentOrderUuid", null)
                            }
                            Order.ORDER_USER_CANCEL, Order.ORDER_MECHANIC_CANCEL -> {
                                FirebaseFirestore.getInstance().document("workshop/${workshop.id}").update("currentOrderUuid", null)
                                jobDialog.dismiss()
                                JobDialog.destroyInstance()
                            }

                        }
                    } else {
                        println("order do not exist")
                    }
                })
            }
        } else {
            switchJob.isChecked = false
            switchJob.isEnabled = false
            tvCoordinate.text = "-"
        }
    }
}
