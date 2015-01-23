package main;

// This class was used to debug this bot for the Regions in the first Warlight challenge
import java.util.HashMap;

public class RegionName
{
	private static final HashMap<Integer, String> m_names;
	static
	{
		m_names = new HashMap<Integer, String>();
//		m_names.put(1,  "Alaska");
//		m_names.put(2,  "NorthwestTerritory");
//		m_names.put(3,  "Greenland");
//		m_names.put(4,  "Alberta");
//		m_names.put(5,  "Ontario");
//		m_names.put(6,  "Quebec");
//		m_names.put(7,  "WesternUnitedStates");
//		m_names.put(8,  "EasternUnitedStates");
//		m_names.put(9,  "CentralAmerica");
//		m_names.put(10, "Venezuela");
//		m_names.put(11, "Peru");
//		m_names.put(12, "Brazil");
//		m_names.put(13, "Argentina");
//		m_names.put(14, "Iceland");
//		m_names.put(15, "GreatBritain");
//		m_names.put(16, "Scandinavia");
//		m_names.put(17, "Ukraine");
//		m_names.put(18, "WesternEurope");
//		m_names.put(19, "NorthernEurope");
//		m_names.put(20, "SouthernEurope");
//		m_names.put(21, "NorthAfrica");
//		m_names.put(22, "Egypt");
//		m_names.put(23, "EastAfrica");
//		m_names.put(24, "Congo");
//		m_names.put(25, "SouthAfrica");
//		m_names.put(26, "Madagascar");
//		m_names.put(27, "Ural");
//		m_names.put(28, "Siberia");
//		m_names.put(29, "Yakutsk");
//		m_names.put(30, "Kamchatka");
//		m_names.put(31, "Irkutsk");
//		m_names.put(32, "Kazakhstan");
//		m_names.put(33, "China");
//		m_names.put(34, "Mongolia");
//		m_names.put(35, "Japan");
//		m_names.put(36, "MiddleEast");
//		m_names.put(37, "India");
//		m_names.put(38, "Siam");
//		m_names.put(39, "Indonesia");
//		m_names.put(40, "NewGuinea");
//		m_names.put(41, "WesternAustralia");
//		m_names.put(42, "EasternAustralia");
	}

	// Private constructor
	private RegionName()
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
			return "Region "+id;
//			return "Unknown";
		}
	}
}
