package co.bouk.proxynow;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SqliteHelper extends SQLiteOpenHelper {
    public static final String DATABASE = "database.db";

    public SqliteHelper(Context context) {
        super(context, DATABASE, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Setting.createTable(db);
        db.execSQL("INSERT INTO settings (regexp, task_name) VALUES (\"turn the lights on|turn on the lights\", \"Turn the lights on\");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
