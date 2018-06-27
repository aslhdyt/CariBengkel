package id.assel.caribengkel.activity.main

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import id.assel.caribengkel.R
import id.assel.caribengkel.model.Order
import kotlinx.android.synthetic.main.item_order_history.view.*
import java.text.SimpleDateFormat
import java.util.*


internal class OrderHistoryAdapter(val itemList: List<Order>) : RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.tvId.text = item.uuid.takeLast(10)
        holder.tvWorkshopName.text = "Bengkel ${item.workshopId}"
        val endAt: Long? = item.endAt
        if (endAt != null) holder.tvEndAt.text = formatDate(endAt)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_order_history, parent, false))
    }


    override fun getItemCount(): Int {
        return itemList.size
    }
    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.tvId
        val tvWorkshopName: TextView = view.tvWorkshopName
        val tvEndAt: TextView = view.tvEndAt
    }

    private fun formatDate(milliseconds: Long) /* This is your topStory.getTime()*1000 */: String {
        val sdf = SimpleDateFormat("dd/MM/yyyy' 'HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliseconds
        val tz = TimeZone.getDefault()
        sdf.timeZone = tz
        return sdf.format(calendar.time)
    }
}
