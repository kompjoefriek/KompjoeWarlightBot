package bot;

import main.Map;
import main.Region;
import main.SuperRegion;
import move.AttackTransferMove;
import move.Move;
import move.PlaceArmiesMove;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class BotState
{
	private String m_myName = "";
	private String m_opponentName = "";
	private final Map m_fullMap = new Map(); // This map is known from the start, contains all the regions and how they are connected, doesn't change after initialization
	private Map m_visibleMap; // This map represents everything the player can see, updated at the end of each round.
	private ArrayList<Region> m_pickableStartingRegions; // 2 randomly chosen regions from each SuperRegion are given, which the bot can chose to start with
	private ArrayList<Move> m_opponentMoves; // List of all the opponent's moves, reset at the end of each round
	private int m_startingArmies; // Number of armies the player can place on map
	private int m_roundNumber;
	private boolean m_debugMode;

	public static final Comparator<Region> compareArmies = new Comparator<Region>()
	{
		public int compare(Region o1, Region o2) { return o2.compareTo(o1); }
	};

	public static final Comparator<Region> compareArmiesDescending = new Comparator<Region>()
	{
		public int compare(Region o1, Region o2) { return 0-o2.compareTo(o1); }
	};

	public static final Comparator<SuperRegion> comparePreferredSuperRegions = new Comparator<SuperRegion>()
	{
		public int compare(SuperRegion o1, SuperRegion o2) { return 0-o2.comparePreferredTo(o1); }
	};


	private ArrayList<Region> m_ownedRegions;
	private ArrayList<Region> m_ownedRegionsNextToOpponent;
	private ArrayList<Region> m_ownedRegionsNextToNobody;
	private boolean m_opponentVisible;

	public BotState()
	{
		m_pickableStartingRegions = new ArrayList<Region>();
		m_opponentMoves = new ArrayList<Move>();
		m_roundNumber = 0;
		m_debugMode = false;

		m_ownedRegions = new ArrayList<Region>();
		m_ownedRegionsNextToOpponent = new ArrayList<Region>();
		m_ownedRegionsNextToNobody = new ArrayList<Region>();
		m_opponentVisible = false;
	}

	public void updateSettings(String key, String value)
	{
		if (key.equals("your_bot")) // Bot's own name
		{
			m_myName = value;
		}
		else if (key.equals("opponent_bot")) // Opponent's name
		{
			m_opponentName = value;
		}
		else if (key.equals("starting_armies"))
		{
			m_startingArmies = Integer.parseInt(value);
			m_roundNumber++; // Next round
		}
	}

	//initial map is given to the bot with all the information except for player and armies info
	public void setupMap(String[] mapInput)
	{
		int i, regionId, superRegionId, reward;

		if (mapInput[1].equals("super_regions"))
		{
			for (i = 2; i < mapInput.length; i++)
			{
				try
				{
					superRegionId = Integer.parseInt(mapInput[i]);
					i++;
					reward = Integer.parseInt(mapInput[i]);
					m_fullMap.add(new SuperRegion(superRegionId, reward));
				}
				catch (Exception e)
				{
					System.err.println("Unable to parse SuperRegions");
				}
			}
		}
		else if (mapInput[1].equals("regions"))
		{
			for (i = 2; i < mapInput.length; i++)
			{
				try
				{
					regionId = Integer.parseInt(mapInput[i]);
					i++;
					superRegionId = Integer.parseInt(mapInput[i]);
					SuperRegion superRegion = m_fullMap.getSuperRegion(superRegionId);
					m_fullMap.add(new Region(regionId, superRegion));
				}
				catch (Exception e)
				{
					System.err.println("Unable to parse Regions " + e.getMessage());
				}
			}
		}
		else if (mapInput[1].equals("neighbors"))
		{
			for (i = 2; i < mapInput.length; i++)
			{
				try
				{
					Region region = m_fullMap.getRegion(Integer.parseInt(mapInput[i]));
					i++;
					String[] neighborIds = mapInput[i].split(",");
					for (int j = 0; j < neighborIds.length; j++)
					{
						Region neighbor = m_fullMap.getRegion(Integer.parseInt(neighborIds[j]));
						region.addNeighbor(neighbor);
					}
				}
				catch (Exception e)
				{
					System.err.println("Unable to parse Neighbors " + e.getMessage());
				}
			}
		}
	}

	// Regions from which a player is able to pick his preferred starting regions
	public void setPickableStartingRegions(String[] mapInput)
	{
		for (int i = 2; i < mapInput.length; i++)
		{
			int regionId;
			try
			{
				regionId = Integer.parseInt(mapInput[i]);
				Region pickableRegion = m_fullMap.getRegion(regionId);
				m_pickableStartingRegions.add(pickableRegion);
			}
			catch (Exception e)
			{
				System.err.println("Unable to parse pickable regions " + e.getMessage());
			}
		}
	}

	// Visible regions are given to the bot with player and armies info
	public void updateMap(String[] mapInput)
	{
		m_visibleMap = m_fullMap.getMapCopy();
		for (Region region : m_visibleMap.getRegions())
		{
			region.setUpdated(false);
		}

		for (int i = 1; i < mapInput.length; i++)
		{
			try
			{
				Region region = m_visibleMap.getRegion(Integer.parseInt(mapInput[i]));
				String playerName = mapInput[i + 1];
				int armies = Integer.parseInt(mapInput[i + 2]);

				region.setPlayerName(playerName);
				region.setArmies(armies);
				region.setUpdated(true);

				i += 2;
			}
			catch (Exception e)
			{
				System.err.println("Unable to parse Map Update " + e.getMessage());
			}
		}
		ArrayList<Region> unknownRegions = new ArrayList<Region>();

		// Remove regions which are not visible.
		for (Region region : m_visibleMap.getRegions())
		{
			if (!region.getUpdated())
			{
				region.setPlayerName("unknown");
				region.setArmies(2);

				unknownRegions.add(region);
			}
		}
		for (Region unknownRegion : unknownRegions)
		{
			m_visibleMap.getRegions().remove(unknownRegion);
		}

		// Custom preparations
		m_ownedRegions.clear();
		m_ownedRegionsNextToOpponent.clear();
		m_ownedRegionsNextToNobody.clear();
		m_opponentVisible = false;

		// Build list of owned Regions and one with owned Regions next to opponent

		for (Region region : m_visibleMap.getRegions())
		{
			if (region.ownedByPlayer(m_myName))
			{
				m_ownedRegions.add(region);
				for (Region neighbor : region.getNeighbors())
				{
					if (!neighbor.ownedByPlayer(m_myName))
					{
						if (neighbor.ownedByPlayer(m_opponentName))
						{
							m_opponentVisible = true;
							if (!m_ownedRegionsNextToOpponent.contains(region))
							{
								m_ownedRegionsNextToOpponent.add(region);
							}
						}
						else
						{
							if (!m_ownedRegionsNextToNobody.contains(region))
							{
								m_ownedRegionsNextToNobody.add(region);
							}
						}
					}
				}
			}
		}

		Collections.sort(m_ownedRegionsNextToOpponent, compareArmies);
		Collections.sort(m_ownedRegionsNextToNobody, compareArmiesDescending);
	}

	// Parses a list of the opponent's moves every round.
	// Clears it at the start, so only the moves of this round are stored.
	public void readOpponentMoves(String[] moveInput)
	{
		m_opponentMoves.clear();
		for (int i = 1; i < moveInput.length; i++)
		{
			try
			{
				Move move;
				if (moveInput[i + 1].equals("place_armies"))
				{
					Region region = m_visibleMap.getRegion(Integer.parseInt(moveInput[i + 2]));
					String playerName = moveInput[i];
					int armies = Integer.parseInt(moveInput[i + 3]);
					move = new PlaceArmiesMove(playerName, region, armies);
					i += 3;
				}
				else if (moveInput[i + 1].equals("attack/transfer"))
				{
					Region fromRegion = m_visibleMap.getRegion(Integer.parseInt(moveInput[i + 2]));
					if (fromRegion == null) // Might happen if the region isn't visible
					{
						fromRegion = m_fullMap.getRegion(Integer.parseInt(moveInput[i + 2]));
					}

					Region toRegion = m_visibleMap.getRegion(Integer.parseInt(moveInput[i + 3]));
					if (toRegion == null) // Might happen if the region isn't visible
					{
						toRegion = m_fullMap.getRegion(Integer.parseInt(moveInput[i + 3]));
					}

					String playerName = moveInput[i];
					int armies = Integer.parseInt(moveInput[i + 4]);
					move = new AttackTransferMove(playerName, fromRegion, toRegion, armies);
					i += 4;
				}
				else
				{
					continue; // Never happens
				}
				m_opponentMoves.add(move);
			}
			catch (Exception e)
			{
				System.err.println("Unable to parse Opponent moves " + e.getMessage());
			}
		}
	}

	public String getMyPlayerName()
	{
		return m_myName;
	}

	public String getOpponentPlayerName()
	{
		return m_opponentName;
	}

	public int getStartingArmies()
	{
		return m_startingArmies;
	}

	public int getRoundNumber()
	{
		return m_roundNumber;
	}

	public Map getVisibleMap()
	{
		return m_visibleMap;
	}

	public Map getFullMap()
	{
		return m_fullMap;
	}

	public ArrayList<Move> getOpponentMoves()
	{
		return m_opponentMoves;
	}

	public ArrayList<Region> getPickableStartingRegions()
	{
		return m_pickableStartingRegions;
	}

	public void setDebugMode()
	{
		m_debugMode = true;
	}

	public boolean isDebugMode()
	{
		return m_debugMode;
	}

	// Custom stats
	public ArrayList<Region> getOwnedRegions()
	{
		return m_ownedRegions;
	}

	public ArrayList<Region> getOwnedRegionsNextToOpponent()
	{
		return m_ownedRegionsNextToOpponent;
	}

	public ArrayList<Region> getOwnedRegionsNextToNobody()
	{
		return m_ownedRegionsNextToNobody;
	}

	public boolean isOpponentVisible()
	{
		return m_opponentVisible;
	}
}
