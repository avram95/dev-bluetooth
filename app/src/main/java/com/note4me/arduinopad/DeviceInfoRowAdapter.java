package com.note4me.arduinopad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DeviceInfoRowAdapter extends ArrayAdapter<InfoData>
{
	private final Context context;
	private final ArrayList<InfoData> values;
	
	public DeviceInfoRowAdapter(Context context, ArrayList<InfoData> values)
	{
		super(context, R.layout.device_info_row, values);

		this.context = context;
	    this.values = values;

	    Collections.sort(this.values, new Comparator<InfoData>()
   		{
	        @Override
	        public int compare(InfoData o1, InfoData o2)
	        {
	            return o1.getUuid().compareTo(o2.getUuid());
	        }
	    });
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		InfoData info = values.get(position);	    

	    LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.device_info_row, parent, false);

	    TextView txtUuid = (TextView) rowView.findViewById(R.id.txtUuid);
	    TextView txtDesc = (TextView) rowView.findViewById(R.id.txtDesc);

	    txtUuid.setText("0x" + info.getUuid());
	    txtDesc.setText(info.getDesc());
	    
	    if (info.getIsKnown())
	    {
	    	txtUuid.setTextColor(Color.WHITE);
	    	txtDesc.setTextColor(Color.WHITE);
	    }
	    else
	    {
	    	txtUuid.setTextColor(Color.rgb(255, 209, 209));
	    	txtDesc.setTextColor(Color.rgb(255, 209, 209));
	    }
	    
	    return rowView;
	}
}
