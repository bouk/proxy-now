package co.bouk.proxynow;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.List;
import java.util.ArrayList;

public class Setting {
    public static final String TABLE = "settings";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_REGEXP = "regexp";
    public static final String COLUMN_TASK_NAME = "task_name";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_REGEXP, COLUMN_TASK_NAME};

    private long id;
    private String regexp;
    private String taskName;
    private boolean isNew;

    private SQLiteDatabase db;

    public Setting(SQLiteDatabase db) {
        this.db = db;
        this.id = -1;
        isNew = true;
    }


    public long getId() {
        return id;
    }

    public void setId(long i) {
        id = i;
    }

    public String getRegexp() {
        return regexp;
    }

    public void setRegexp(String r) {
        regexp = r;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String t) {
        taskName = t;
    }

    private ContentValues values() {
        ContentValues cv = new ContentValues();
        if(id != -1)
            cv.put(COLUMN_ID, id);
        cv.put(COLUMN_REGEXP, regexp);
        cv.put(COLUMN_TASK_NAME, taskName);
        return cv;
    }

    public void save() {
        if(isNew) {
            id = db.insert(TABLE, null, values());
            isNew = false;
        } else {
            db.update(TABLE, values(), COLUMN_ID + " = ?", new String[]{Long.toString(id)});
        }
    }

    public void delete() {
        db.delete(TABLE, COLUMN_ID + " = ?", new String[]{Long.toString(id)});
        id = -1;
        isNew = true;
    }

    @Override
    public String toString() {
        return regexp + taskName;
    }

    public static Setting fromCursor(SQLiteDatabase db, Cursor c) {
        Setting setting = new Setting(db);
        setting.setId(c.getLong(0));
        setting.setRegexp(c.getString(1));
        setting.setTaskName(c.getString(2));
        setting.isNew = false;

        return setting;
    }

    public static Setting find(SQLiteDatabase db, long id) {
        Cursor c = db.query(TABLE, ALL_COLUMNS, COLUMN_ID + " = ?", new String[]{Long.toString(id)}, null, null, "1");
        if(c.getCount() == 0) {
            c.close();
            return null;
        }
        c.moveToFirst();
        if(c.isAfterLast()) {
            c.close();
            return null;
        }
        Setting s = fromCursor(db, c);
        c.close();
        return s;
    }

    public static Cursor allCursor(SQLiteDatabase db) {
        return db.query(TABLE, ALL_COLUMNS, null, null, null, null, null);
    }

    public static List<Setting> all(SQLiteDatabase db) {
        List<Setting> settings = new ArrayList<Setting>();
        Cursor c = allCursor(db);
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            Setting s = fromCursor(db, c);
            settings.add(s);
        }
        c.close();
        return settings;
    }

    public static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +
                TABLE + "(" +
                COLUMN_ID        + " integer primary key autoincrement," +
                COLUMN_REGEXP    + " text not null," +
                COLUMN_TASK_NAME + " text not null" +
                ");");
    }
}
