package com.note4me.arduinopad;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

public class ButtonSetupDialog extends DialogFragment
{
	public static ButtonSetupDialog newInstance(int btnId, String text)
	{
		ButtonSetupDialog frag = new ButtonSetupDialog();

		Bundle args = new Bundle();
        args.putInt("id", btnId);
        args.putString("text", text);

        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View textEntryView = factory.inflate(R.layout.button_setup_dialog, null);

        final EditText editor = (EditText)textEntryView.findViewById(R.id.button_message_edit);
        
        final int id = getArguments().getInt("id");
        final String text = getArguments().getString("text");
        
        if (text.trim().toLowerCase().equals(DeviceControlActivity.NOT_SET_TEXT.toLowerCase()))
        	editor.setText("");
        else
        	editor.setText(text.trim());
        
        final Dialog dlg = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.button_message)
            .setView(textEntryView)
            .setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int whichButton)
                {
                	DeviceControlActivity activity = (DeviceControlActivity)getActivity();
                	String textToSet = editor.getText().toString().trim();

                	if (textToSet.equals(""))
                		textToSet = DeviceControlActivity.NOT_SET_TEXT;

                	activity.updateButtonText(id, textToSet);

                	dialog.dismiss();
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int whichButton)
                {
                	dialog.dismiss();
                }
            }).create();
        
        editor.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                	dlg.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        
        return dlg;
    }
}
