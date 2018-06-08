package com.note4me.arduinopad;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;

public class LogViewDialog extends DialogFragment
{
    public static final int MESSAGE_LOG_ADDED = 1;	
	public static final String MESSAGE_TEXT = "txt";
	
	private LedView ledView0;
	private LedView ledView1;
	private LedView ledView2;
	private LedView ledView3;
	private LedView ledView4;
	private LedView ledView5;

	public static LogViewDialog newInstance()
	{
		LogViewDialog frag = new LogViewDialog();

		Bundle args = new Bundle();
		frag.setArguments(args);
        return frag;
    }
	
    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
            	case MESSAGE_LOG_ADDED:
            		String txt = msg.getData().getString(MESSAGE_TEXT);
           			updateLedView(txt);
            		break;
            }
        }
    };
    
    private void updateLedView(String txt)
    {
    	if (txt == null || txt.isEmpty() || getActivity() == null)
    	{
    		return;
    	}
    	
    	int values[] = IncomingMessageData.getValues(txt);

    	if (values[6] != IncomingMessageData.PARSE_OK_FLAG)
    		return;

    	ledView0.setBrightness(values[0]);
    	ledView1.setBrightness(values[1]);
    	ledView2.setBrightness(values[2]);
    	ledView3.setBrightness(values[3]);
    	ledView4.setBrightness(values[4]);
    	ledView5.setBrightness(values[5]);
    }
    
    public Handler getHandler()
    {
    	return mHandler;
    }
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View textEntryView = factory.inflate(R.layout.log_dialog, null);
        
        ledView0 = (LedView)textEntryView.findViewById(R.id.ledView0);
        ledView1 = (LedView)textEntryView.findViewById(R.id.ledView1);
        ledView2 = (LedView)textEntryView.findViewById(R.id.ledView2);
        ledView3 = (LedView)textEntryView.findViewById(R.id.ledView3);
        ledView4 = (LedView)textEntryView.findViewById(R.id.ledView4);
        ledView5 = (LedView)textEntryView.findViewById(R.id.ledView5);

        final Dialog dlg = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.log_dlg_title)
            .setView(textEntryView)
            .setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int whichButton)
                {
                	dialog.dismiss();
                }
            }).create();
        
        return dlg;
    }
}
