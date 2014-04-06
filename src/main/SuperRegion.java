package main;

import java.util.LinkedList;

public class SuperRegion
{
	public static final int MIN_GUARD_BORDER_REGION = 1; // Use this to enable guarding
	public static final int MIN_GUARD_REGION = 1;

	private int id;
	private int armiesReward;
	private LinkedList<Region> subRegions;
	private LinkedList<Region> borderRegions;
	private boolean fullyGuarded;

	public SuperRegion(int id, int armiesReward)
	{
		this.id = id;
		this.armiesReward = armiesReward;
		subRegions = new LinkedList<Region>();
		borderRegions = new LinkedList<Region>();
		fullyGuarded = false;
	}

	public void addSubRegion(Region subRegion)
	{
		if (!subRegions.contains(subRegion)) { subRegions.add(subRegion); }
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
					if (region.getArmies() < MIN_GUARD_BORDER_REGION) { fullyGuarded = false; }
				}
				else
				{
					if (region.getArmies() < MIN_GUARD_REGION) { fullyGuarded = false; }
				}
			}
		}
	}

	public boolean getFullyGuarded()
	{
		return fullyGuarded;
	}

	/**
	 * @return A list with the Regions that are part of this SuperRegion, and connect to another SuperRegion
	 */
	public LinkedList<Region> getBorderRegions()
	{
		return borderRegions;
	}


	public void addBorderRegion(Region region)
	{
		if (!borderRegions.contains(region)) { borderRegions.add(region); }
	}


	public int comparePreferredTo(SuperRegion compareSuperRegion)
	{
		// Exception for australia (http://webtrax.hu/myfacewhen/faces/lineart-memes/nothing-to-do-here-jet-pack-guy.jpg)
		if (this.getBorderRegions().size() == 1) { return 1; }
		if (compareSuperRegion.getBorderRegions().size() == 1) { return -1; }
		// Smaller super regions are preferred
		if (this.getSubRegions().size() != compareSuperRegion.getSubRegions().size())
		{
			return this.getSubRegions().size() - compareSuperRegion.getSubRegions().size();
		}
		// Less border regions are preferred
		if (this.getBorderRegions().size() != compareSuperRegion.getBorderRegions().size())
		{
			return this.getBorderRegions().size() - compareSuperRegion.getBorderRegions().size();
		}

		// More award is preferred, so i turned them around here :-)
		return compareSuperRegion.getArmiesReward() - this.getArmiesReward();
	}
}
