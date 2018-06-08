package com.note4me.arduinopad;

import com.google.gson.Gson;

public class DeviceSerializer
{
	public static String serialize(SettingEntity settings)
	{
		Gson gson = new Gson();
		String s = gson.toJson(settings);
		return s;
	}

	public static SettingEntity deserialize(String s)
	{
		Gson gson = new Gson();		
		SettingEntity settings = gson.fromJson(s, SettingEntity.class);
		return settings;
	}
}
