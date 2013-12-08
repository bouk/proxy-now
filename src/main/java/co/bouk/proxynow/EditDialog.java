package co.bouk.proxynow;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import net.dinglisch.android.tasker.TaskerIntent;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class EditDialog extends DialogFragment implements View.OnClickListener, DialogInterface.OnShowListener {
    private static final int TASK_SELECT = 1;
    Setting setting;
    EditText regexpEdit;
    EditText taskNameEdit;
    ImageButton taskSelectButton;
    AlertDialog dialog;
    SettingSavedListener listener;

    @Override
    public void onClick(View v) {
        Intent intent = TaskerIntent.getTaskSelectIntent();
        startActivityForResult(intent, TASK_SELECT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == TASK_SELECT && (data != null)) {
            taskNameEdit.setText(data.getDataString());
        }
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid = true;
                String regexp = regexpEdit.getText().toString();
                if (regexp.length() == 0) {
                    Toast.makeText(getActivity(), R.string.fill_in_regexp, Toast.LENGTH_SHORT).show();
                    valid = false;
                }
                try {
                    Pattern.compile(regexp);
                } catch (PatternSyntaxException e) {
                    Toast.makeText(getActivity(), R.string.invalid_regexp, Toast.LENGTH_SHORT).show();
                    valid = false;
                }
                if (taskNameEdit.length() == 0) {
                    Toast.makeText(getActivity(), R.string.fill_in_task_name, Toast.LENGTH_SHORT).show();
                    valid = false;
                }
                if (!valid) {
                    return;
                }
                setting.setRegexp(regexp);
                setting.setTaskName(taskNameEdit.getText().toString());
                setting.save();
                listener.onSettingSaved();
                dialog.dismiss();
            }
        });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
    }

    public interface SettingSavedListener {
        public void onSettingSaved();
    }


    public EditDialog(Setting s, SettingSavedListener l) {
        setting = s;
        listener = l;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstance)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_setting_dialog, null);
        builder.setView(view)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null);
        regexpEdit = (EditText)view.findViewById(R.id.regexp);
        regexpEdit.setText(setting.getRegexp());
        taskNameEdit = (EditText)view.findViewById(R.id.task_name);
        taskNameEdit.setText(setting.getTaskName());
        taskSelectButton = (ImageButton)view.findViewById(R.id.select_task_button);
        taskSelectButton.setOnClickListener(this);

        dialog = builder.create();
        dialog.setOnShowListener(this);
        return dialog;
    }
}
