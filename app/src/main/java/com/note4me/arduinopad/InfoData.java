package com.note4me.arduinopad;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class InfoData implements Serializable
{
	private static final long serialVersionUID = 0L;
	
	private String uuid = "";
	private String desc = "";
	private boolean isKnown = false;
	
	public InfoData(String uuid, String desc, boolean isKnown)
	{
		this.uuid = uuid;
		this.desc = desc;
		this.isKnown = isKnown;
	}
	
	public String getUuid()
	{
		return uuid;
	}

	public boolean getIsKnown()
	{
		return isKnown;
	}
	
	public String getDesc()
	{
		return desc;
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException 
	{
		uuid = (String)in.readObject();
		desc = (String)in.readObject();
		isKnown = in.readBoolean();
	}

	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeObject(uuid);
		out.writeObject(desc);
		out.writeBoolean(isKnown);
	}
}
