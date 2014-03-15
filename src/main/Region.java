package main;

import java.util.LinkedList;

public class Region
{
	private int id;
	private LinkedList<Region> neighbors;
	private LinkedList<SuperRegion> neighborSuperRegions;
	private SuperRegion superRegion;
	private int armies;
	private String playerName;

	public Region(int id, SuperRegion superRegion)
	{
		this(id, superRegion, "unknown", 0);
	}

	public Region(int id, SuperRegion superRegion, String playerName, int armies)
	{
		this.id = id;
		this.superRegion = superRegion;
		this.neighbors = new LinkedList<Region>();
		this.neighborSuperRegions = new LinkedList<SuperRegion>();
		this.playerName = playerName;
		this.armies = armies;

		superRegion.addSubRegion(this);
	}

	public void addNeighbor(Region neighbor)
	{
		if (!neighbors.contains(neighbor))
		{
			neighbors.add(neighbor);
			neighbor.addNeighbor(this);
			if (neighbor.getSuperRegion() != superRegion && !neighborSuperRegions.contains(neighbor.getSuperRegion()))
			{
				neighborSuperRegions.add(neighbor.getSuperRegion());
				superRegion.addBorderRegion(this);
			}
		}
	}

	/**
	 * @param region a Region object
	 * @return True if this Region is a neighbor of given Region, false otherwise
	 */
	public boolean isNeighbor(Region region)
	{
		return neighbors.contains(region) ;
	}

	/**
	 * @param playerName A string with a player's name
	 * @return True if this region is owned by given playerName, false otherwise
	 */
	public boolean ownedByPlayer(String playerName)
	{
		return playerName.equals(this.playerName);
	}

	/**
	 * @param armies Sets the number of armies that are on this Region
	 */
	public void setArmies(int armies)
	{
		this.armies = armies;
		superRegion.updateFullyGuarded();
	}

	/**
	 * @param playerName Sets the Name of the player that this Region belongs to
	 */
	public void setPlayerName(String playerName)
	{
		this.playerName = playerName;
	}

	/**
	 * @return The id of this Region
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @return A list of this Region's neighboring Regions
	 */
	public LinkedList<Region> getNeighbors()
	{
		return neighbors;
	}

	/**
	 * @return A list of SuperRegion's that this Regions is a neighbor of
	 */
	public LinkedList<SuperRegion> getNeighborSuperRegions()
	{
		return neighborSuperRegions;
	}

	/**
	 * @return The SuperRegion this Region is part of
	 */
	public SuperRegion getSuperRegion()
	{
		return superRegion;
	}

	/**
	 * @return The number of armies on this region
	 */
	public int getArmies()
	{
		return armies;
	}

	/**
	 * @return A string with the name of the player that owns this region
	 */
	public String getPlayerName()
	{
		return playerName;
	}


	public int compareTo(Region compareRegion)
	{
		int compareArmies = compareRegion.getArmies();

		//ascending order
		return this.armies - compareArmies;
	}
}
