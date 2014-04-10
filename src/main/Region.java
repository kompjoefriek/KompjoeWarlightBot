package main;

import java.util.LinkedList;

public class Region
{
	private int m_id;
	private LinkedList<Region> m_neighbors;
	private LinkedList<SuperRegion> m_neighborSuperRegions;
	private SuperRegion m_superRegion;
	private int m_armies;
	private String m_playerName;
	private boolean m_updated;

	public Region(int id, SuperRegion superRegion)
	{
		this(id, superRegion, "unknown", 0);
	}

	public Region(int id, SuperRegion superRegion, String playerName, int armies)
	{
		m_id = id;
		m_superRegion = superRegion;
		m_neighbors = new LinkedList<Region>();
		m_neighborSuperRegions = new LinkedList<SuperRegion>();
		m_playerName = playerName;
		m_armies = armies;
		m_updated = false;

		superRegion.addSubRegion(this);
	}

	public void addNeighbor(Region neighbor)
	{
		if (!m_neighbors.contains(neighbor))
		{
			m_neighbors.add(neighbor);
			neighbor.addNeighbor(this);
			if (neighbor.getSuperRegion() != m_superRegion && !m_neighborSuperRegions.contains(neighbor.getSuperRegion()))
			{
				m_neighborSuperRegions.add(neighbor.getSuperRegion());
				m_superRegion.addBorderRegion(this);
			}
		}
	}

	/**
	 * @param region a Region object
	 * @return True if this Region is a neighbor of given Region, false otherwise
	 */
	public boolean isNeighbor(Region region)
	{
		return m_neighbors.contains(region);
	}

	/**
	 * @param playerName A string with a player's name
	 * @return True if this region is owned by given m_playerName, false otherwise
	 */
	public boolean ownedByPlayer(String playerName)
	{
		return playerName.equals(m_playerName);
	}

	/**
	 * @param armies Sets the number of m_armies that are on this Region
	 */
	public void setArmies(int armies)
	{
		m_armies = armies;
		m_superRegion.updateFullyGuarded();
	}

	/**
	 * @param playerName Sets the Name of the player that this Region belongs to
	 */
	public void setPlayerName(String playerName)
	{
		m_playerName = playerName;
	}

	/**
	 * @return The m_id of this Region
	 */
	public int getId()
	{
		return m_id;
	}

	/**
	 * @return A list of this Region's neighboring Regions
	 */
	public LinkedList<Region> getNeighbors()
	{
		return m_neighbors;
	}

	/**
	 * @return A list of SuperRegion's that this Regions is a neighbor of
	 */
	public LinkedList<SuperRegion> getNeighborSuperRegions()
	{
		return m_neighborSuperRegions;
	}

	/**
	 * @return The SuperRegion this Region is part of
	 */
	public SuperRegion getSuperRegion()
	{
		return m_superRegion;
	}

	/**
	 * @return The number of m_armies on this region
	 */
	public int getArmies()
	{
		return m_armies;
	}

	/**
	 * @return A string with the name of the player that owns this region
	 */
	public String getPlayerName()
	{
		return m_playerName;
	}

	/**
	 * Used in custom Comparators
	 * @param compareRegion Region to compare to
	 * @return A negative int when less m_armies than compareRegion, positive int when more m_armies then compareRegion
	 */
	public int compareTo(Region compareRegion)
	{
		int compareArmies = compareRegion.getArmies();

		//ascending order
		return m_armies - compareArmies;
	}

	/**
	 * Needs to be set to indicate changes (and is visible)
	 * @param updated Set to true when map update contains this region
	 */
	public void setUpdated(boolean updated)
	{
		m_updated = updated;
	}

	/**
	 * Needs to be m_updated manually before use, via setNextToOpponent
	 * @return value of m_updated
	 */
	public boolean getUpdated()
	{
		return m_updated;
	}

	public String toString()
	{
		return "\"id\":" +  m_id + "," +
			"\"name\":" + RegionName.getRegionName(m_id) + "," +
			"\"owner\":" +  m_playerName + "," +
			"\"armies\":" +  m_armies + "," +
			"\"neighbors\":" +  m_neighbors.size() + "," +
			"\"neighborSuperRegions\":" +  m_neighborSuperRegions.size() + "," +
			"\"updated\":" +  m_updated;
	}
}

