package com.note4me.arduinopad;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LogRowAdapter extends ArrayAdapter<LogData>
{
	private final Context context;
	private final ArrayList<LogData> values;
	
	public LogRowAdapter(Context context, ArrayList<LogData> values)
	{
		super(context, R.layout.log_row, values);

		this.context = context;
	    this.values = values;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LogData info = values.get(position);	    

	    LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.log_row, parent, false);

	    TextView txtDateTime = (TextView) rowView.findViewById(R.id.txtDateTime);
	    TextView txtData = (TextView) rowView.findViewById(R.id.txtData);

	    txtDateTime.setText(info.getDateTime());
	    txtData.setText(info.getData());
	    
	    return rowView;
	}
}
