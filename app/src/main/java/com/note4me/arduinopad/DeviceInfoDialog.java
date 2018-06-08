package com.note4me.arduinopad;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

public class DeviceInfoDialog extends DialogFragment
{
	public static DeviceInfoDialog newInstance(ArrayList<InfoData> services)
	{
		DeviceInfoDialog frag = new DeviceInfoDialog();

		Bundle args = new Bundle();
		args.putSerializable("services", services);
		frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View textEntryView = factory.inflate(R.layout.device_info, null);

        final ListView infoView = (ListView)textEntryView.findViewById(R.id.devicesInfoView);
        
        ArrayList<InfoData> services = (ArrayList<InfoData>)getArguments().getSerializable("services");
        DeviceInfoRowAdapter adapter = new DeviceInfoRowAdapter(getActivity(), services);
        infoView.setAdapter(adapter);
        
        final Dialog dlg = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.device_info)
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
