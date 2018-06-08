package com.note4me.arduinopad;

public class LogData
{
	private String datetime = "";
	private String data = "";
	
	public LogData(String datetime, String data)
	{
		this.data = data;
		this.datetime = datetime;
	}
	
	public String getDateTime()
	{
		return datetime;
	}

	public String getData()
	{
		return data;
	}
}
