package com.cs.adamson.ricedx;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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

import java.util.ArrayList;

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
        tableLayout = (TableLayout)findViewById(R.id.tblLayout);
        tableLayoutPrediction = (TableLayout) findViewById(R.id.tblLayoutPrediction);

        myDb = new DBHelper(this);
        myDb.getReadableDatabase();

        btnViewGraph = (Button) findViewById(R.id.btnViewGraph);
        btnViewGraph.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent intent = new Intent(TableActivity.this, GraphActivity.class);
                startActivity(intent);
            }
        });

        btnClearPrediction = (Button)findViewById(R.id.btnClearPrediction);
        btnClearPrediction.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                AlertDialog.Builder builder = new AlertDialog().Builder(TableActivity.this);
                builder.setTitle("Prediction").setMessage("Are you sure you want to remove all the preicted values?");
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        myDb.deleteTable("predicted_data");
                        initializeTable();
                    }
                });
                builder.setCancelable(true);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        btnInputData = (Button)findViewById(R.id.btnInputData);
        btnInputData.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent intent = new Intent(TableActivity.this, InputDataActivity.class);
                startActivity(intent);
            }
        });
        initializeTable();
    }

    @Override
    protected void onResume(){
        super.onResume();
        initializeTable();
    }

    public void initializeTable(){
        TableRow.LayoutParams llp = new
                TableRow.LayoutParams(TableLayout.LayoutParams.FILL_PARENT,
                TableLayout.LayoutParams.FILL_PARENT);

        llp = new TableRow.LayoutParams();
        llp.setMargins(0,0,2,0);
        TableRow trHeader = new TableRow(this);
        TableRow trHeaderPrediction = new TableRow(this);
        trHeader.setBackgroundColor(Color.BLACK);
        trHeader.setPadding(0, 0, 0, 2);
        trHeaderPrediction.setBackgroundColor(Color.BLACK);
        trHeaderPrediction.setPadding(0, 0, 0, 2);
        String[] tableHeader = {"Date", "AT", "WS", "ST", "Cav"};

        tableLayoutPrediction.removeAllViews();
        tableLayout.removeAllViews();

        for(int j = 0; j < tableHeader.length; j++){
            LinearLayout cell = new LinearLayout(this);
            LinearLayout cellPrediction = new LinearLayout(this);
            cell.setBackgroundColor(Color.WHITE);
            cell.setLayoutParams(llp);
            cellPrediction.setBackgroundColor(Color.WHITE);
            cellPrediction.setLayoutParams(llp);

            TextView tv = new TextView(this);
            tv.setText(tableHeader[j]);
            tv.setPadding(0, 0, 4, 3);

            TextView tvPrediction = new TextView(this);
            tvPrediction.setText(tableHeader[j]);
            tvPrediction.setPadding(0,0,4,3);

            cell.addView(tv);
            trHeader.addView(cell);
            cellPrediction.addView(cellPrediction);
        }

        tableLayout.addView(trHeader);
        tableLayoutPrediction.addView(trHeaderPrediction);

        ArrayList<ArrayList<String>> harvestData = myDb.getAllHarvestData();
        for(int i = 0; i < harvestData.size();i++){
            TableRow tr = new TableRow(this);
            tr.setBackgroundColor(Color.BLACK);
            tr.setPadding(0,0,0,2);
            for(int j = 0; j < harvestData.get(i).size(); j++){
                LinearLayout cell = new LinearLayout(this);
                cell.setBackgroundColor(Color.WHITE);
                cell.setLayoutParams(llp);

                TextView tv = new TextView(this);
                tv.setText(harvestData.get(i).get(j));
                tv.setPadding(0, 0, 4, 3);

                cell.addView(tv);
                tr.addView(cell);
            }
            tableLayout.addView(tr);
        }

        ArrayList<ArrayList<String>> predictedData = myDb.getAllPredictedData();
        for (int i = 0; i < predictedData.size();i++){
            TableRow tr = new TableRow(this);
            tr.setBackgroundColor(Color.BLACK);
            tr.setPadding(0,0,0,2);

            for(int j = 0; j < predictedData.get(i).size(); j++){
                LinearLayout cell = new LinearLayout(this);
                cell.setBackgroundColor(Color.WHITE);
                cell.setLayoutParams(llp);

                TextView tv = new TextView(this);
                tv.setText(predictedData.get(i).get(j));
                tv.setPadding(0,0,4,3);

                cell.addView(tv);
                tr.addView(cell);
            }
            tableLayoutPrediction.addView(tr);
        }
    }
}
