package move;

import main.Region;

/**
 * This Move is used in the second part of each round. It represents the attack or transfer of armies from
 * m_fromRegion to m_toRegion. If m_toRegion is owned by the player himself, it's a transfer. If m_toRegion is
 * owned by the opponent, this Move is an attack.
 */

public class AttackTransferMove extends Move
{
	private Region m_fromRegion;
	private Region m_toRegion;
	private int m_armies;

	public AttackTransferMove(String playerName, Region fromRegion, Region toRegion, int armies)
	{
		super.setPlayerName(playerName);
		m_fromRegion = fromRegion;
		m_toRegion = toRegion;
		m_armies = armies;
	}

	/**
	 * @param armies Sets the number of armies of this Move
	 */
	public void setArmies(int armies)
	{
		m_armies = armies;
	}

	/**
	 * @return The Region this Move is attacking or transferring from
	 */
	public Region getFromRegion()
	{
		return m_fromRegion;
	}

	/**
	 * @return The Region this Move is attacking or transferring to
	 */
	public Region getToRegion()
	{
		return m_toRegion;
	}

	/**
	 * @return The number of armies this Move is attacking or transferring with
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
			return getPlayerName() + " attack/transfer " + m_fromRegion.getId() + " " + m_toRegion.getId() + " " + m_armies;
		}
		else
		{
			return getPlayerName() + " illegal_move " + getIllegalMove();
		}
	}
}
