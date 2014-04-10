package move;

import main.Region;

/**
 * This Move is used in the first part of each round. It represents what Region is increased
 * with how many m_armies.
 */

public class PlaceArmiesMove extends Move
{
	private Region m_region;
	private int m_armies;

	public PlaceArmiesMove(String playerName, Region region, int armies)
	{
		super.setPlayerName(playerName);
		m_region = region;
		m_armies = armies;
	}

	/**
	 * @param armies Sets the number of armies this move will place on a Region
	 */
	public void setArmies(int armies)
	{
		m_armies = armies;
	}

	/**
	 * @return The Region this Move will be placing m_armies on
	 */
	public Region getRegion()
	{
		return m_region;
	}

	/**
	 * @return The number of armies this move will place
	 */
	public int getArmies()
	{
		return m_armies;
	}

	/**
	 * @return A string representation of this Move
	 */
	public String getString()
	{
		if (getIllegalMove().equals(""))
		{
			return getPlayerName() + " place_armies " + m_region.getId() + " " + m_armies;
		}
		else
		{
			return getPlayerName() + " illegal_move " + getIllegalMove();
		}
	}
}
