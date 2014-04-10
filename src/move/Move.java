package move;

public class Move
{
	private String m_playerName; // Name of the player that did this move
	private String m_illegalMove = ""; // Gets the value of the error message if move is illegal, else remains empty

	/**
	 * @param playerName Sets the name of the Player that this Move belongs to
	 */
	public void setPlayerName(String playerName)
	{
		m_playerName = playerName;
	}

	/**
	 * @param illegalMove Sets the error message of this move. Only set this if the Move is illegal.
	 */
	public void setIllegalMove(String illegalMove)
	{
		m_illegalMove = illegalMove;
	}

	/**
	 * @return The Player's name that this Move belongs to
	 */
	public String getPlayerName()
	{
		return m_playerName;
	}

	/**
	 * @return The error message of this Move
	 */
	public String getIllegalMove()
	{
		return m_illegalMove;
	}
}
