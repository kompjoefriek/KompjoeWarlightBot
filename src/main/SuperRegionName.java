package main;

import java.util.HashMap;

public class SuperRegionName
{
	private static final HashMap<Integer, String> m_names;
	static
	{
		m_names = new HashMap<Integer, String>();
		m_names.put(1, "North America");
		m_names.put(2, "South America");
		m_names.put(3, "Europe");
		m_names.put(4, "Africa");
		m_names.put(5, "Asia");
		m_names.put(6, "Australia");
	}

	// Private constructor
	private SuperRegionName()
	{
	}


	public static String getName(int id)
	{
		if (m_names.containsKey(id))
		{
			return m_names.get(id);
		}
		else
		{
			return "Unknown";
		}
	}
}
