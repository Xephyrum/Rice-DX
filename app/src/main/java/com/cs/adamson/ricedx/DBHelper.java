package com.cs.adamson.ricedx;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyHarvest.db";
    public static final String HARVEST_TABLE_NAME = "harvest_data";
    public static final String HARVEST_COLUMN_ID = "id";
    public static final String HARVEST_COLUMN_DATE = "date";
    public static final String HARVEST_COLUMN_AIRTEMP = "air_temp";
    public static final String HARVEST_COLUMN_SOILTEMP = "soil_temp";
    public static final String HARVEST_COLUMN_WINDSPEED = "wind_speed";
    public static final String HARVEST_COLUMN_CAVAN = "cavan";
    private HashMap hp;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE harvest_data " +
                        "(id integer primary key autoincrement, air_temp real, wind_speed real, soil_temp real, cavan real, date datetime);"
        );

        db.execSQL("CREATE TABLE predicted_data " +
                        "(id integer primary key autoincrement, air_temp real, wind_speed real, soil_temp real, cavan real, date datetime);"
        );

        db.execSQL("INSERT INTO harvest_data('date', 'air_temp', 'wind_speed', 'soil_temp', 'cavan') VALUES ('2014-09-01', 31, 2*1.60934, 35, 73);");
        db.execSQL("INSERT INTO harvest_data('date', 'air_temp', 'wind_speed', 'soil_temp', 'cavan') VALUES ('2015-06-01', 33, 3.2*1.60934, 37, 119);");
        db.execSQL("INSERT INTO harvest_data('date', 'air_temp', 'wind_speed', 'soil_temp', 'cavan') VALUES ('2014-03-01', 30, 3*1.60934, 34, 130);");
        db.execSQL("INSERT INTO harvest_data('date', 'air_temp', 'wind_speed', 'soil_temp', 'cavan') VALUES ('2014-08-01', 32, 2.5*1.60934, 36, 88);");
        db.execSQL("INSERT INTO harvest_data('date', 'air_temp', 'wind_speed', 'soil_temp', 'cavan') VALUES ('2016-04-01', 30, 3*1.60934, 34, 120);");
        db.execSQL("INSERT INTO predicted_data('date', 'air_temp', 'wind_speed', 'soil_temp', 'cavan') VALUES ('2016-09-01', 31, 3.2*1.60934, 35, 120);");
        db.execSQL("INSERT INTO predicted_data('date', 'air_temp', 'wind_speed', 'soil_temp', 'cavan') VALUES ('2016-01-01', 31, 3.2*1.60934, 35, 120);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS harvest_data");
        onCreate(db);
    }

    public ArrayList<ArrayList<String>> getChartData(int position) {
        ArrayList<ArrayList<String>> arrayList = new ArrayList<ArrayList<String>>();

        String[] fieldName = {"cavan", "wind_speed", "air_temp", "soil_temp"};
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT strftime('%m-%Y, date) AS month, " +
                fieldName[position] + ", date from harvest_data GROUP BY month ORDER BY date", null);

        res.moveToFirst();

        while (res.isAfterLast() == false) {
            ArrayList<String> tempArrayList = new ArrayList<String>();
            tempArrayList.add(res.getString(res.getColumnIndex("month")));
            tempArrayList.add(res.getString(res.getColumnIndex(fieldName[position])));
            arrayList.add(tempArrayList);
            res.moveToNext();
        }
        return arrayList;
    }

    public ArrayList<ArrayList<String>> getLineChartData() {
        ArrayList<ArrayList<String>> arrayList = new ArrayList<ArrayList<String>>();

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT strftime('%m-%Y', harvest_data.date AS month, harvest_data.date AS date, harvest_data.cavan AS harvestcavan, " +
                "predicted_data.cavan AS predictedcavan FROM harvest_data LEFT JOIN predicted_data ON strftime('%m-%Y', harvest_data.date) = strftime('%m-%Y', predicted_data.date) GROUP BY month)" +
                "UNION ALL" +
                "SELECT strftime('%m-%Y', harvest_data.date AS month, predicted_data.date AS date, harvest_data.cavan AS harvestcavan, " +
                "predicted_data.cavan AS predictedcavan FROM predicted_data LEFT JOIN harvest_data ON strftime('%m-%Y', harvest_data.date) = strftime('%m-%Y', predicted_data.date) GROUP BY month ORDER BY date";


        Cursor res = db.rawQuery(query, null);

        res.moveToFirst();

        while (res.isAfterLast() == false) {
            ArrayList<String> tempArrayList = new ArrayList<String>();
            tempArrayList.add(res.getString(res.getColumnIndex("month")));
            tempArrayList.add(res.getString(res.getColumnIndex("harvestcavan")));
            tempArrayList.add(res.getString(res.getColumnIndex("predictedcavan")));
            arrayList.add(tempArrayList);
            Log.v("line", tempArrayList.toString());
            res.moveToNext();
        }
        return arrayList;
    }

    public ArrayList<ArrayList<String>> getAllHarvestData() {
        ArrayList<ArrayList<String>> arrayList = new ArrayList<ArrayList<String>>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery("SELECT avg(wind_speed) AS wind_speed, avg(cavan) AS cavan, avg(air_temp) AS air_temp, avg(soil_temp) AS soil_temp, " +
                "strftime('%m-%Y', date) AS date, date AS date2 FROM harvest_data GROUP BY date ORDER BY date2", null);

        res.moveToFirst();

        DecimalFormat df = new DecimalFormat("0.00");
        df.setMaximumFractionDigits(2);

        while (res.isAfterLast() == false) {
            ArrayList<String> tempArrayList = new ArrayList<String>();
            tempArrayList.add(res.getString(res.getColumnIndex("date")));
            tempArrayList.add(res.getString(res.getColumnIndex(HARVEST_COLUMN_AIRTEMP)));
            tempArrayList.add(df.format(res.getColumnIndex(HARVEST_COLUMN_WINDSPEED)));
            tempArrayList.add(res.getString(res.getColumnIndex(HARVEST_COLUMN_SOILTEMP)));
            tempArrayList.add(res.getString(res.getColumnIndex(HARVEST_COLUMN_CAVAN)));
            arrayList.add(tempArrayList);
            Log.v("test", tempArrayList.toString());
            res.moveToNext();
        }
        return arrayList;
    }

    public double calculateMLR(double windSpeed, double soilTemp) {
        SQLiteDatabase db = this.getReadableDatabase();

        String meansQuery = "SELECT SUM(x1) AS sx1, SUM(x2) AS sx2, SUM(y) AS sy, COUNT(*) AS n " +
                "FROM (SELECT wind_speed AS x1, soil_temp AS x2, cavan AS y FROM harvest_data)";

        Cursor res = db.rawQuery(meansQuery, null);
        res.moveToFirst();
        double sx1 = Double.parseDouble(res.getString(res.getColumnIndex("sx1")));
        double sx2 = Double.parseDouble(res.getString(res.getColumnIndex("sx2")));
        double sy = Double.parseDouble(res.getString(res.getColumnIndex("sy")));
        int n = Integer.parseInt(res.getString(res.getColumnIndex("n")));
        double ybar = sy / n;
        double xbar1 = sx1 / n;
        double xbar2 = sx2 / n;
        String summationsQuery = "SELECT SUM(x1) AS sx1, SUM(x2) AS sx2, SUM(y) AS sy, " +
                "SUM((x1 - " + Double.toString(xbar1) + ") * (x1 - " + Double.toString(xbar1) + ")) AS sx11, " +
                "SUM((x2 - " + Double.toString(xbar2) + ") * (x2 - " + Double.toString(xbar2) + ")) AS sx22, " +
                "SUM((y - " + Double.toString(ybar) + ") * (y - " + Double.toString(ybar) + ")) AS syy, " +
                "SUM(x1 * x2) AS sx12, SUM(y*x1) AS syx1, SUM (y*x2) AS syx2, COUNT(*) AS n " +
                "FROM " +
                "(SELECT wind_speed AS x1, soil_temp AS x2, cavan AS y FROM harvest_data);";

        res = db.rawQuery(summationsQuery, null);
        res.moveToFirst();

        double sx11 = Double.parseDouble(res.getString(res.getColumnIndex("sx11")));
        double sx22 = Double.parseDouble(res.getString(res.getColumnIndex("sx22")));
        double syy = Double.parseDouble(res.getString(res.getColumnIndex("syy")));
        double sx12 = Double.parseDouble(res.getString(res.getColumnIndex("sx12")));
        double syx1 = Double.parseDouble(res.getString(res.getColumnIndex("syx1")));
        double syx2 = Double.parseDouble(res.getString(res.getColumnIndex("syx2")));

        syx1 = syx1 - sx1 * sy / n;
        syx2 = syx2 - sx2 * sy / n;
        sx12 = sx1 - sx1 * sx2 / n;

        double b1 = (sx22 * syx1 - sx12 * syx2) / (sx11 * sx22 - sx12 * sx12);
        double b2 = (sx11 * syx2 - sx12 * syx1) / (sx11 * sx22 - sx12 * sx12);

        double a = ybar - b1 * xbar1 - b2 * xbar2;

        return Math.ceil(a + b1 * windSpeed + b2 * soilTemp);
    }

    public boolean dateExists(String date) {

        SQLiteDatabase db = this.getWritableDatabase();
        String sqlQuery = "SELECT EXISTS(" +
                "SELECT 1 FROM harvest_data WHERE date = '" + date + "');";
        Cursor res = db.rawQuery(sqlQuery, null);
        res.moveToFirst();
        if (res.getInt(0) == 1)
            return true;
        else
            return false;
    }

    public void overwriteData(double windSpeed, double soilTemp, double cavans, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE harvest_data " +
                        "SET air_temp = " + Double.toString(soilTemp - 4) +
                        ", wind_speed = " + Double.toString(windSpeed) +
                        ", soil_temp = " + Double.toString(soilTemp) +
                        ", cavan = " + Double.toString(cavans) +
                        " WHERE date = '" + date + "';"
        );
    }

    public void insertData(double windSpeed, double soilTemp, double cavans, String date) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO harvest_data('date', 'air_temp', 'wind_speed', 'soil_temp', 'cavan') " +
                        "VALUES ('" +
                        date + "'," +
                        Double.toString(soilTemp - 4) + "," +
                        Double.toString(windSpeed) + "," +
                        Double.toString(soilTemp) + "," +
                        Double.toString(cavans) + ");"
        );
    }

    public void insertPrediction(double windSpeed, double soilTemp, double cavans) {

        SQLiteDatabase db = this.getWritableDatabase();
        String sqlQuery = "SELECT EXISTS(" +
                "SELECT 1 FROM predicted_data WHERE date = date('now'));";
        Cursor res = db.rawQuery(sqlQuery, null);
        res.moveToFirst();

        if (res.getInt(0) == 1) {
            db.execSQL("UPDATE harvest_data " +
                            "SET air_temp = " + Double.toString(soilTemp - 4) +
                            ", wind_speed = " + Double.toString(windSpeed) +
                            ", soil_temp = " + Double.toString(soilTemp) +
                            ", cavan = " + Double.toString(cavans) +
                            " WHERE date = date('now');"
            );
        } else {
            db.execSQL("INSERT INTO predicted_data('date', 'air_temp', 'wind_speed', 'soil_temp', 'cavan') " +
                    "VALUES (" +
                    "date('now')," +
                    Double.toString(soilTemp - 4) + "," +
                    Double.toString(windSpeed) + "," +
                    Double.toString(soilTemp) + "," +
                    Double.toString(cavans) + ");");
        }
    }

    public ArrayList<ArrayList<String>> getAllPredictedData() {
        ArrayList<ArrayList<String>> arrayList = new ArrayList<ArrayList<String>>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery("SELECT avg(wind_speed) AS wind_speed, avg(cavan) AS cavan, avg(air_temp) AS air_temp, avg(soil_temp) AS soil_temp, " +
                "strftime('%m-%Y', date) AS date, date AS date2 FROM predicted_data GROUP BY date ORDER BY date2", null);

        res.moveToFirst();

        DecimalFormat df = new DecimalFormat("0.00");
        df.setMaximumFractionDigits(2);

        while (res.isAfterLast() == false) {
            ArrayList<String> tempArrayList = new ArrayList<String>();
            tempArrayList.add(res.getString(res.getColumnIndex("date")));
            tempArrayList.add(res.getString(res.getColumnIndex(HARVEST_COLUMN_AIRTEMP)));
            tempArrayList.add(df.format(res.getColumnIndex(HARVEST_COLUMN_WINDSPEED)));
            tempArrayList.add(res.getString(res.getColumnIndex(HARVEST_COLUMN_SOILTEMP)));
            tempArrayList.add(res.getString(res.getColumnIndex(HARVEST_COLUMN_CAVAN)));
            arrayList.add(tempArrayList);
            //Log.v("test", tempArrayList.toString());
            res.moveToNext();
        }
        return arrayList;
    }

    public void deleteTable(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM " + tableName);
        db.execSQL("VACUUM");
    }
}
