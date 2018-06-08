package com.note4me.arduinopad;

import android.util.Log;

public class IncomingMessageData
{
    private static final String TAG = "IncomingMessageData";
    private static final boolean D = true;

    public static final String START_SIGNATURE = "###";
    public static final String END_SIGNATURE = "***";
    public static final String SEPARATOR_SIGNATURE = "-";
    
    public static final int PARSE_ERROR_FLAG = 0;
    public static final int PARSE_OK_FLAG = 1;

    private String message = "";

	public void setMessage(String message)
	{
		this.message = message;
	}
	
	public String getMessage()
	{
		return message;
	}
	
	public static int[] getValues(String message)
	{
		int values[] = new int[7];
		values[6] = PARSE_ERROR_FLAG;

		if (message == null || message.isEmpty())
			return values;
		
		if (!message.startsWith(START_SIGNATURE) && !message.endsWith(END_SIGNATURE))
			return values;

		String temp = message.replace(START_SIGNATURE, "").replace(END_SIGNATURE, "");
		String parts[] = temp.split(SEPARATOR_SIGNATURE);
		if (parts.length == 6)
		{
			int cnt = 0;
			boolean hasErrors = false;
			for (String part : parts)
			{
				if (cnt == 6)
					break;

				try
				{
					int i = Integer.parseInt(part);
					values[cnt] = i;
				}
				catch (Exception ex)
				{
					if (D) Log.d(TAG, "Unable to parse " + part + " in " + message);
					hasErrors = true;
					break;
				}
				cnt++;
			}
			
			if (!hasErrors)
				values[6] = PARSE_OK_FLAG;
		}
		
		if (values[6] != PARSE_OK_FLAG)
		{
			if (D) Log.d(TAG, "Error while parsing " + message);
		}

		return values;
	}
}
