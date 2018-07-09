package com.cs.adamson.ricedx;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.cs.adamson.ricedx.DBHelper;

public class TableActivity extends AppCompatActivity {

    private DBHelper myDb;
    private Button btnViewGraph;
    private Button btnInputData;
    private Button btnClearPrediction;
    private Button btnLegend;
    private TableLayout tableLayout;
    private TableLayout tableLayoutPrediction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_actvity);
    }


}
