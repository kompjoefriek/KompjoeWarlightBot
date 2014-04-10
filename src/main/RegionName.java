package main;

import java.util.HashMap;

public class RegionName
{
	private static final HashMap<Integer, String> m_regionNames;
	static
	{
		m_regionNames = new HashMap<Integer, String>();
		m_regionNames.put(1,  "Alaska");
		m_regionNames.put(2,  "NorthwestTerritory");
		m_regionNames.put(3,  "Greenland");
		m_regionNames.put(4,  "Alberta");
		m_regionNames.put(5,  "Ontario");
		m_regionNames.put(6,  "Quebec");
		m_regionNames.put(7,  "WesternUnitedStates");
		m_regionNames.put(8,  "EasternUnitedStates");
		m_regionNames.put(9,  "CentralAmerica");
		m_regionNames.put(10, "Venezuela");
		m_regionNames.put(11, "Peru");
		m_regionNames.put(12, "Brazil");
		m_regionNames.put(13, "Argentina");
		m_regionNames.put(14, "Iceland");
		m_regionNames.put(15, "GreatBritain");
		m_regionNames.put(16, "Scandinavia");
		m_regionNames.put(17, "Ukraine");
		m_regionNames.put(18, "WesternEurope");
		m_regionNames.put(19, "NorthernEurope");
		m_regionNames.put(20, "SouthernEurope");
		m_regionNames.put(21, "NorthAfrica");
		m_regionNames.put(22, "Egypt");
		m_regionNames.put(23, "EastAfrica");
		m_regionNames.put(24, "Congo");
		m_regionNames.put(25, "SouthAfrica");
		m_regionNames.put(26, "Madagascar");
		m_regionNames.put(27, "Ural");
		m_regionNames.put(28, "Siberia");
		m_regionNames.put(29, "Yakutsk");
		m_regionNames.put(30, "Kamchatka");
		m_regionNames.put(31, "Irkutsk");
		m_regionNames.put(32, "Kazakhstan");
		m_regionNames.put(33, "China");
		m_regionNames.put(34, "Mongolia");
		m_regionNames.put(35, "Japan");
		m_regionNames.put(36, "MiddleEast");
		m_regionNames.put(37, "India");
		m_regionNames.put(38, "Siam");
		m_regionNames.put(39, "Indonesia");
		m_regionNames.put(40, "NewGuinea");
		m_regionNames.put(41, "WesternAustralia");
		m_regionNames.put(42, "EasternAustralia");
	}

	// Private constructor
	private RegionName()
	{
	}


	public static String getRegionName(int id)
	{
		if (m_regionNames.containsKey(id))
		{
			return m_regionNames.get(id);
		}
		else
		{
			return "Unknown";
		}
	}
}
