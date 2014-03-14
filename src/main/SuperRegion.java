package main;

import java.util.LinkedList;

public class SuperRegion
{
	private int id;
	private int armiesReward;
	private LinkedList<Region> subRegions;
	private boolean fullyGuarded;

	public SuperRegion(int id, int armiesReward)
	{
		this.id = id;
		this.armiesReward = armiesReward;
		subRegions = new LinkedList<Region>();
		fullyGuarded = false;
	}

	public void addSubRegion(Region subRegion)
	{
		if (!subRegions.contains(subRegion)) subRegions.add(subRegion);
	}

	/**
	 * @return A string with the name of the player that fully owns this SuperRegion
	 */
	public String ownedByPlayer()
	{
		String playerName = subRegions.getFirst().getPlayerName();
		for (Region region : subRegions)
		{
			if (!playerName.equals(region.getPlayerName())) return null;
		}
		return playerName;
	}

	/**
	 * @return The id of this SuperRegion
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @return The number of armies a Player is rewarded when he fully owns this SuperRegion
	 */
	public int getArmiesReward()
	{
		return armiesReward;
	}

	/**
	 * @return A list with the Regions that are part of this SuperRegion
	 */
	public LinkedList<Region> getSubRegions()
	{
		return subRegions;
	}

	public void updateFullyGuarded()
	{
		fullyGuarded = false;
		if (ownedByPlayer() != null)
		{
			// All sub regions are owned by one player
			fullyGuarded = true;
			for (Region region : subRegions)
			{
				if (region.getNeighborSuperRegions().size() > 0)
				{
					if (region.getArmies() < 10) { fullyGuarded = false; }
				}
				else
				{
					if (region.getArmies() < 2) { fullyGuarded = false; }
				}
			}
		}
	}

	public boolean getFullyGuarded()
	{
		return fullyGuarded;
	}
}
