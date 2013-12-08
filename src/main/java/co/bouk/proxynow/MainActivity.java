package co.bouk.proxynow;


import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemLongClickListener;

public class MainActivity extends ListActivity implements AdapterView.OnItemClickListener, EditDialog.SettingSavedListener {
    SqliteHelper dbHelper;
    SQLiteDatabase db;
    SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        dbHelper = new SqliteHelper(this);
        db = dbHelper.getWritableDatabase();
        adapter = new SimpleCursorAdapter(
            this,
            R.layout.settings_item,
            Setting.allCursor(db),
            new String[]{
                    Setting.COLUMN_REGEXP,
                    Setting.COLUMN_TASK_NAME
            },
            new int[]{
                    R.id.text_regexp,
                    R.id.text_task_name
            },
            0
        );
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
        registerForContextMenu(getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.setting_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete:
                Setting.find(db, info.id).delete();
                refreshList();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.action_settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        case R.id.action_new:
            editDialog(new Setting(db));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.changeCursor(Setting.allCursor(db));
            }
        });
    }

    private void editDialog(Setting setting) {
        (new EditDialog(setting, this)).show(getFragmentManager(), "EditNotice");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        editDialog(Setting.find(db, id));
    }

    @Override
    public void onSettingSaved() {
        refreshList();
    }
}
