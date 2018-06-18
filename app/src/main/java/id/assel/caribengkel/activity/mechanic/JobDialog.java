package id.assel.caribengkel.activity.mechanic;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import id.assel.caribengkel.R;
import id.assel.caribengkel.model.Order;

public class JobDialog extends Dialog implements View.OnClickListener {
    private JobResponse listener;
    private Order order;

    public JobDialog(@NonNull Context context, Order order, JobResponse listener) {
        super(context);
        this.listener = listener;
        this.order = order;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Pesanan");
        setContentView(R.layout.dialog_job);
        setCanceledOnTouchOutside(false);
        findViewById(R.id.btnAccept).setOnClickListener(this);
        findViewById(R.id.btnReject).setOnClickListener(this);
        ((TextView)findViewById(R.id.tvCustomer)).setText(order.getUsername());
        ((TextView)findViewById(R.id.tvLocation)).setText(order.getLocation().getLatitude() +" : "+order.getLocation().getLatitude());

    }

    @Override
    public void dismiss() {
        listener = null;
        super.dismiss();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAccept:
                listener.onJobsAccepted(order);
                break;
            case R.id.btnReject:
                listener.onJobsRejected(order);
                break;
        }
        dismiss();
    }

    interface JobResponse {
        void onJobsAccepted(Order order);
        void onJobsRejected(Order order);
    }
}
