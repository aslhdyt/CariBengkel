package id.assel.caribengkel.activity.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import id.assel.caribengkel.R;

public class OrderDialog extends Dialog implements View.OnClickListener {

    private DialogListener listener;
    private TextView progressText;

    public OrderDialog(Activity activity, DialogListener a) {
        super(activity);
        listener = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Mohon tunggu...");
        setContentView(R.layout.dialog_order);
        setCanceledOnTouchOutside(false);
        findViewById(R.id.btnCancel).setOnClickListener(this);
        progressText = findViewById(R.id.tvProgress);




        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        Double width = metrics.widthPixels*.9;
        Window win = getWindow();
        win.setLayout(width.intValue(), -2);



    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCancel:
                listener.onCancel();
                dismiss();
                break;
            default:
                break;
        }
    }

    public void progressMessage(String progressMessage) {
        progressText.setText(progressMessage);
    }

    @Override
    public void dismiss() {
        listener = null;
        super.dismiss();
    }

    interface DialogListener {
        void onCancel();
    }
}
