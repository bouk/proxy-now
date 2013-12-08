package co.bouk.proxynow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.dinglisch.android.tasker.TaskerIntent;

public class QueryReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Log.i("ProxyNow", "Received broadcast");

        String action = intent.getAction();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(action.equals(ProxyNow.INTENT_QUERY)) {
            String query = intent.getStringExtra(ProxyNow.DATA_QUERY);
            boolean isVoiceSearch = intent.getBooleanExtra(ProxyNow.DATA_IS_VOICE_SEARCH, true);
            TaskerIntent.Status status = TaskerIntent.testStatus(context);
            if(!isVoiceSearch && preferences.getBoolean("only_on_voice", false)) {
                return;
            }
            switch (status) {
                case OK:
                    SqliteHelper helper = new SqliteHelper(context);
                    SQLiteDatabase db = helper.getReadableDatabase();
                    for(Setting setting : Setting.all(db)) {
                        Pattern pattern;
                        try {
                            pattern = Pattern.compile("(?i)\\A" + setting.getRegexp() + "\\z");
                        } catch (PatternSyntaxException e) {
                            continue;
                        }
                        Matcher matcher = pattern.matcher(query);
                        if(matcher.matches()) {
                            Log.d("QueryReceiver", "Found matching setting " + setting.getId());
                            TaskerIntent taskerIntent = new TaskerIntent(setting.getTaskName());
                            taskerIntent.addLocalVariable("%query", query);
                            for(int i = 1; i <= matcher.groupCount(); i++) {
                                taskerIntent.addLocalVariable("%match" + i, matcher.group(i));
                            }
                            context.sendBroadcast(taskerIntent);
                        }
                    }
                    break;
                case NotInstalled:
                    Toast.makeText(context, "ProxyNow requires Tasker to be installed\nPlease install Tasker", Toast.LENGTH_LONG).show();
                    break;
                case NotEnabled:
                    Toast.makeText(context, "ProxyNow requires Tasker to be enabled", Toast.LENGTH_LONG).show();
                    break;
                case AccessBlocked:
                    Toast.makeText(context, "ProxyNow requires Tasker to have third-party access enabled\nPlease enable third-party access", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(context, "ProxyNow failed to connect to Tasker\n" + status.toString(), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
}
