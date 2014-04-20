package main;

import java.util.LinkedList;

public class GameMap
{
	private LinkedList<Region> m_regions;
	private LinkedList<SuperRegion> m_superRegions;

	public GameMap()
	{
		this(new LinkedList<Region>(), new LinkedList<SuperRegion>());
	}

	public GameMap(LinkedList<Region> regions, LinkedList<SuperRegion> superRegions)
	{
		m_regions = regions;
		m_superRegions = superRegions;
	}

	/**
	 * add a Region to the map
	 *
	 * @param region : Region to be added
	 */
	public void add(Region region)
	{
		for (Region r : m_regions)
		{
			if (r.getId() == region.getId())
			{
				System.err.println("Region cannot be added: id already exists.");
				return;
			}
		}
		m_regions.add(region);
	}

	/**
	 * add a SuperRegion to the map
	 *
	 * @param superRegion : SuperRegion to be added
	 */
	public void add(SuperRegion superRegion)
	{
		for (SuperRegion s : m_superRegions)
		{
			if (s.getId() == superRegion.getId())
			{
				System.err.println("SuperRegion cannot be added: id already exists.");
				return;
			}
		}
		m_superRegions.add(superRegion);
	}

	/**
	 * @return : a new GameMap object exactly the same as this one
	 */
	public GameMap getMapCopy()
	{
		// Why would you make copies.. WHY!!! WHY MY PIGGY!!! https://www.youtube.com/watch?v=KpbQZOEKsGI
		GameMap newMap = new GameMap();
		for (SuperRegion sr : m_superRegions) // Copy m_superRegions
		{
			//SuperRegion newSuperRegion = new SuperRegion(sr.getId(), sr.getArmiesReward());
			//newMap.add(newSuperRegion);
			newMap.add(sr);
		}
		for (Region r : m_regions) // Copy regions
		{
			//Region newRegion = new Region(r.getId(), newMap.getSuperRegion(r.getSuperRegion().getId()), r.getPlayerName(), r.getArmies());
			//newMap.add(newRegion);
			newMap.add(r);
		}
		//for (Region r : m_regions) // Add neighbors to copied regions
		//{
		//	//Region newRegion = newMap.getRegion(r.getId());
		//	//for (Region neighbor : r.getNeighbors())
		//	//{
		//	//	newRegion.addNeighbor(newMap.getRegion(neighbor.getId()));
		//	//}
		//}
		return newMap;
	}

	/**
	 * @return : the list of all Regions in this map
	 */
	public LinkedList<Region> getRegions()
	{
		return m_regions;
	}

	/**
	 * @return : the list of all SuperRegions in this map
	 */
	public LinkedList<SuperRegion> getSuperRegions()
	{
		return m_superRegions;
	}

	/**
	 * @param id : a Region id number
	 * @return : the matching Region object
	 */
	public Region getRegion(int id)
	{
		for (Region region : m_regions)
		{
			if (region.getId() == id) { return region; }
		}
		return null;
	}

	/**
	 * @param id : a SuperRegion id number
	 * @return : the matching SuperRegion object
	 */
	public SuperRegion getSuperRegion(int id)
	{
		for (SuperRegion superRegion : m_superRegions)
		{
			if (superRegion.getId() == id) { return superRegion; }
		}
		return null;
	}

	public String toString()
	{
		String mapString = "GameMap [";
		for (Region region : m_regions)
		{
			mapString = mapString.concat(region.toString());
			if (m_regions.getLast() != region)
			{
				mapString = mapString.concat(","+System.lineSeparator());
			}
		}
		mapString.concat("]");
		return mapString;
	}
}
