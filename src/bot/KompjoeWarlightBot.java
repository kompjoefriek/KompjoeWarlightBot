package bot;

/**
 * Default bot with some modifications:
 *
 * During start selection it will:
 * - Prefer small super regions
 * - Then prefer super regions with less neighbors
 * - Then prefer super regions with biggest award
 *
 * During army placement it will:
 * - Prefer to place armies near opponents
 * - Avoid already guarded regions
 *
 * During moves it will:
 * - Try to guard SuperRegions
 *
 */

import main.Region;
import main.SuperRegion;
import move.AttackTransferMove;
import move.PlaceArmiesMove;

import java.util.*;

public class KompjoeWarlightBot implements Bot
{
	public enum Strategy
	{
		DEFAULT,
		CONTINENT_GET, // http://knowyourmeme.com/memes/get
		AGRO_MODE // attack ALL the regions!
	}

	private ArrayList<SuperRegion> m_preferredSuperRegions;

	private Strategy m_currentStrategy;
	private Strategy m_previousStrategy;
	private SuperRegion m_strategySuperRegionGet; // used with Strategy.CONTINENT_GET
	private int m_strategyMoveCounter;

	private int m_noAttacksCounter;


	public KompjoeWarlightBot()
	{
		m_preferredSuperRegions = new ArrayList<SuperRegion>();
		m_currentStrategy = Strategy.DEFAULT;
		m_previousStrategy = Strategy.CONTINENT_GET;
		m_strategySuperRegionGet = null;
		m_strategyMoveCounter = 0;
		m_noAttacksCounter = 0;
	}


	@Override
	/**
	 * A method used at the start of the game to decide which player start with what Regions. 6 Regions are required to be returned.
	 * This example randomly picks 6 regions from the pickable starting Regions given by the engine.
	 * @return : a list of m (m=6) Regions starting with the most preferred Region and ending with the least preferred Region to start with 
	 */
	public ArrayList<Region> getPreferredStartingRegions(BotState state, Long timeOut)
	{
		int max = 6;
		ArrayList<Region> pickableRegions = new ArrayList<Region>();
		pickableRegions.addAll(state.getPickableStartingRegions());
		ArrayList<Region> preferredStartingRegions = new ArrayList<Region>();

		////////////////////////////////////////////////////////////////////////////////////
		// Find preferred SuperRegions, and use them
		////////////////////////////////////////////////////////////////////////////////////
		m_preferredSuperRegions.clear();
		m_preferredSuperRegions.addAll(state.getFullMap().getSuperRegions());
		Collections.sort(m_preferredSuperRegions, BotState.comparePreferredSuperRegions);

		for( SuperRegion superRegion : m_preferredSuperRegions)
		{
			for( Region region : superRegion.getSubRegions())
			{
				if (pickableRegions.contains(region))
				{
					preferredStartingRegions.add(region);
					pickableRegions.remove(region);
					//if (preferredStartingRegions.size() >= max) { break; }
				}
			}
			if (preferredStartingRegions.size() >= max) { break; }
		}

		////////////////////////////////////////////////////////////////////////////////////
		// Randomly set remaining
		////////////////////////////////////////////////////////////////////////////////////
		while (preferredStartingRegions.size() < max)
		{
			double rand = Math.random();
			int r = (int) (rand * pickableRegions.size());
			int regionId = state.getPickableStartingRegions().get(r).getId();
			Region region = state.getFullMap().getRegion(regionId);

			if (!preferredStartingRegions.contains(region))
			{
				preferredStartingRegions.add(region);
				pickableRegions.remove(region);
			}
		}

		return preferredStartingRegions;
	}


	// Group placement commands together
	private void placeArmies(ArrayList<PlaceArmiesMove> placeArmiesMoves, String myName, Region place, int armies )
	{
		for (PlaceArmiesMove move : placeArmiesMoves)
		{
			if (move.getRegion().getId() == place.getId())
			{
				move.setArmies(move.getArmies() + armies);
				return;
			}
		}
		placeArmiesMoves.add(new PlaceArmiesMove(myName, place, armies));
	}


	private void attack(ArrayList<AttackTransferMove> attackTransferMoves, BotState state, Region fromRegion, Region toRegion)
	{
		if (!toRegion.ownedByPlayer(state.getMyPlayerName()) && fromRegion.getArmies() < 6) { return; } // Don't attack with so little armies

		int attackArmies = getAvailableArmies(fromRegion, state.getMyPlayerName());
		if (attackArmies > (toRegion.getArmies() * 3)+6)
		{
			// See if we can split up
			Region enemy = null;
			for (Region neighbor : fromRegion.getNeighbors())
			{
				if (neighbor != toRegion && neighbor.ownedByPlayer(state.getOpponentPlayerName()))
				{
					if (enemy == null || neighbor.getArmies() > enemy.getArmies())
					{
						enemy = neighbor;
					}
				}
			}

			if (enemy != null)
			{
				int otherAttackerEnemies = attackArmies/2;
				attackArmies -= otherAttackerEnemies;
				attackTransferMoves.add(new AttackTransferMove(state.getMyPlayerName(), fromRegion, enemy, otherAttackerEnemies));
			}
		}
		attackTransferMoves.add(new AttackTransferMove(state.getMyPlayerName(), fromRegion, toRegion, attackArmies));
	}


	@Override
	/**
	 * This method is called for at first part of each round. This example puts two armies on random regions
	 * until he has no more armies left to place.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut)
	{
		ArrayList<PlaceArmiesMove> placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
		String myName = state.getMyPlayerName();
		String opponentName = state.getOpponentPlayerName();
		int armies = 2;
		int armiesLeft = state.getStartingArmies();

		if (m_previousStrategy == m_currentStrategy)
		{
			m_strategyMoveCounter++;
		}
		else
		{
			m_strategyMoveCounter = 1;
		}
		m_previousStrategy = m_currentStrategy;

		// TODO: try to capture the center of the world (something with neighborSuperRegions > 1)

		m_strategySuperRegionGet = null;
		if ((state.getRoundNumber() <= 5 && state.isOpponentVisible() && !(m_strategyMoveCounter >= 2 && m_currentStrategy == Strategy.AGRO_MODE)) ||
			state.isOpponentVisible() && m_strategyMoveCounter >= 5 && m_currentStrategy != Strategy.AGRO_MODE ||
			state.getRoundNumber() >= 60)
		{
			m_currentStrategy = Strategy.AGRO_MODE;
		}
		else
		{
			m_currentStrategy = Strategy.DEFAULT;
			for ( SuperRegion superRegion : m_preferredSuperRegions.subList(0,3) )
			{
				if (superRegion.ownedByPlayer() != null && superRegion.ownedByPlayer().equals(myName))
				{
					m_currentStrategy = Strategy.CONTINENT_GET;
					m_strategySuperRegionGet = superRegion;

					boolean someOwnedByMe = false;
					for ( Region subRegion : superRegion.getSubRegions() )
					{
						if (subRegion.ownedByPlayer(myName))
						{
							someOwnedByMe = true;
							break;
						}
					}

					if (someOwnedByMe)
					{
						// Ok, lets get this superRegion
						break;
					}
				}
			}
		}

		if (m_currentStrategy == Strategy.AGRO_MODE)
		{
			if (state.getOwnedRegionsNextToOpponent().size() > 0)
			{
				placeArmiesMoves.add(new PlaceArmiesMove(myName, state.getOwnedRegionsNextToOpponent().get(0), armiesLeft));
				state.getOwnedRegionsNextToOpponent().get(0).setArmies(state.getOwnedRegionsNextToOpponent().get(0).getArmies() + armiesLeft); // Update internal stuff
				//armiesLeft -= armiesLeft;
				return placeArmiesMoves;
			}
		}
		else if (m_currentStrategy == Strategy.CONTINENT_GET && m_strategySuperRegionGet != null)
		{
			// Find regions in this superRegion we can take-over
			//ArrayList<Region> regionsToTakeOver = new ArrayList<Region>();
			for ( Region subRegion : m_strategySuperRegionGet.getSubRegions() )
			{
				if (!subRegion.ownedByPlayer(myName))
				{
					for (Region subRegionNeighbor : subRegion.getNeighbors())
					{
						if (subRegionNeighbor.ownedByPlayer(myName))
						{
							placeArmiesMoves.add(new PlaceArmiesMove(myName, subRegionNeighbor, armiesLeft));
							subRegionNeighbor.setArmies(subRegionNeighbor.getArmies() + armiesLeft); // Update internal stuff
							//armiesLeft -= armiesLeft;
							return placeArmiesMoves;
						}
					}
				}
			}
		}


		// Try to place armies on the region next to nobody with the most armies (to raise their numbers so it can expand)
		if (state.getOwnedRegionsNextToNobody().size() > 0)
		{
			placeArmies( placeArmiesMoves, myName, state.getOwnedRegionsNextToNobody().get(0), armies );
			//placeArmiesMoves.add(new PlaceArmiesMove(myName, ownedRegionsNextToNobody.get(0), armies));
			state.getOwnedRegionsNextToNobody().get(0).setArmies(state.getOwnedRegionsNextToNobody().get(0).getArmies() + armies); // Update internal stuff
			armiesLeft -= armies;
		}

		// Prefer to place armies on Regions next to opponent, regions with less armies first
		int idxRegion = 0;
		while (armiesLeft > 0 && idxRegion < state.getOwnedRegionsNextToOpponent().size())
		{
			if (armies > armiesLeft) { armies = armiesLeft; }
			placeArmies( placeArmiesMoves, myName, state.getOwnedRegionsNextToOpponent().get(idxRegion), armies );
			//placeArmiesMoves.add(new PlaceArmiesMove(myName, ownedRegionsNextToOpponent.get(idxRegion), armies));
			state.getOwnedRegionsNextToOpponent().get(idxRegion).setArmies( state.getOwnedRegionsNextToOpponent().get(
				idxRegion).getArmies() + armies); // Update internal stuff
			armiesLeft -= armies;
			idxRegion++;
		}

		// If we cannot expand, put all near opponent
		if (state.getOwnedRegionsNextToNobody().size() == 0 && state.getOwnedRegionsNextToOpponent().size() > 0)
		{
			while(armiesLeft > 0)
			{
				for (Region region : state.getOwnedRegionsNextToOpponent())
				{
					if (armies > armiesLeft) { armies = armiesLeft; }
					placeArmies( placeArmiesMoves, myName, region, armies );
					//placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armies));
					region.setArmies(region.getArmies() + armies); // Update internal stuff
					armiesLeft -= armies;
				}
			}
		}

		// Try to place the remaining armies randomly
		int tries = 0;
		while (armiesLeft > 0)
		{
			double rand = Math.random();
			if (state.getOwnedRegions().size() == 0)
			{
				System.err.println("Wow. Internal check failed! (no owned regions found)");
			}
			int r = (int) (rand * state.getOwnedRegions().size());
			Region region = state.getOwnedRegions().get(r);

			if (armies > armiesLeft) { armies = armiesLeft; }

			// Do not award armies to fully guarded SuperRegions (unless we have to)
			if (!region.getSuperRegion().getFullyGuarded() || tries > state.getFullMap().getRegions().size())
			{
				placeArmies( placeArmiesMoves, myName, region, armies );
				//placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armies));
				region.setArmies(region.getArmies() + armies); // Update internal stuff
				armiesLeft -= armies;
			}
			tries++;
		}

		return placeArmiesMoves;
	}

	@Override
	/**
	 * This method is called for at the second part of each round. This example attacks if a region has
	 * more than 6 armies on it, and transfers if it has less than 6 and a neighboring owned region.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut)
	{
		long deadline = (System.currentTimeMillis() % 1000) + (timeOut - 10);
		String myName = state.getMyPlayerName();
		String opponentName = state.getOpponentPlayerName();
		ArrayList<AttackTransferMove> attackTransferMoves = new ArrayList<AttackTransferMove>();

		//if (state.getRoundNumber() >= 43)
		//{
		//	boolean debugMe = true;
		//}

		////////////////////////////////////////////////////////////////////////////////////
		// Move armies from the region with the most armies towards the opponent. we just need to find the closes region
		////////////////////////////////////////////////////////////////////////////////////
		ArrayList<Region> regionsThatCanDoStuff = new ArrayList<Region>();
		regionsThatCanDoStuff.addAll( state.getOwnedRegions() );
		Collections.sort(regionsThatCanDoStuff, BotState.compareArmies);

		ArrayList<Region> regionsThatDidStuff = new ArrayList<Region>();
		for( Region fromRegion : regionsThatCanDoStuff )
		{
			// First check if this region is not next to an opponent (and is a thread)
			int threadCount = 0;
			for ( Region neighbor : fromRegion.getNeighbors() )
			{
				if (neighbor.ownedByPlayer(opponentName))
				{
					threadCount += neighbor.getArmies();
				}
			}
			if (threadCount > 0 && threadCount >= fromRegion.getArmies() - 3 && m_currentStrategy != Strategy.AGRO_MODE)
			{
				// fromRegion is defending :-)
				regionsThatDidStuff.add(fromRegion);
				continue;
			}

			boolean toMuchArmies = false;
			if (fromRegion.getArmies() > SuperRegion.MIN_GUARD_BORDER_REGION)
				{ toMuchArmies = true; }
			if (!toMuchArmies && fromRegion.getNeighborSuperRegions().size() > 0 && fromRegion.getArmies() > SuperRegion.MIN_GUARD_BORDER_REGION &&
				fromRegion.getSuperRegion().getFullyGuarded() && !state.getOwnedRegionsNextToOpponent().contains(
				fromRegion)) { toMuchArmies = true; }
			if (!toMuchArmies && fromRegion.getSuperRegion().ownedByPlayer() != null && fromRegion.getSuperRegion().ownedByPlayer().equals(myName) &&
				fromRegion.getNeighborSuperRegions().size() == 0 && fromRegion.getArmies() > SuperRegion.MIN_GUARD_REGION) { toMuchArmies = true; }

			if (toMuchArmies)
			{
				if (fromRegion.getSuperRegion().ownedByPlayer() == null && !state.getOwnedRegionsNextToOpponent().contains(fromRegion) && m_currentStrategy != Strategy.AGRO_MODE)
				{
					// This SuperRegion is not owned by me yet!
					try
					{
						Region toRegion = getPath(fromRegion, state, SEARCH_FLAG_FIND_ANY | SEARCH_FLAG_WITHIN_SUPER_REGION );
						if (toRegion != null)
						{
							attack(attackTransferMoves, state, fromRegion, toRegion);
							regionsThatDidStuff.add( fromRegion );
						}
						else
						{
							debugLog(state, "getPath did not find anything!");
						}
					}
					catch(Exception e)
					{
					}
				}
				else
				{
					if (state.isOpponentVisible())
					{
						// Find nearest opponent
						try
						{
							Region toRegion = getPath(fromRegion, state, SEARCH_FLAG_FIND_OPPONENT );
							if (toRegion != null)
							{
								attack(attackTransferMoves, state, fromRegion, toRegion);
								regionsThatDidStuff.add( fromRegion );
							}
							else
							{
								debugLog(state, "getPath did not find anything!");
							}
						}
						catch(Exception e)
						{
						}
					}
					else
					{
						// No opponent visible, but we have the entire super region
						try
						{
							Region toRegion = getPath(fromRegion, state, SEARCH_FLAG_FIND_REGION_ID, 0 );
							if (toRegion != null)
							{
								attack(attackTransferMoves, state, fromRegion, toRegion);
								regionsThatDidStuff.add( fromRegion );
							}
							else
							{
								debugLog(state, "getPath did not find anything!");
							}
						}
						catch(Exception e)
						{
						}
					}
				}
			}
		}

		for (Region fromRegion : regionsThatCanDoStuff)
		{
			if (regionsThatDidStuff.contains(fromRegion)) { continue; }

			boolean foundTarget = false;
			if (/*!foundTarget &&*/ fromRegion.getArmies() > SuperRegion.MIN_GUARD_REGION + 5)
			{
				// Attack a opponent neighbor?
				for (Region neighbor : fromRegion.getNeighbors())
				{
					if (neighbor.ownedByPlayer(opponentName))
					{
						if (fromRegion.getArmies()-SuperRegion.MIN_GUARD_REGION > neighbor.getArmies()+3)
						{
							foundTarget = true;
							attack(attackTransferMoves, state, fromRegion, neighbor);
							regionsThatDidStuff.add(fromRegion);
							break;
						}
						else
						{
							// Try to gang-up
							int armiesAvailableInGang = 0;
							ArrayList<Region> myGang = new ArrayList<Region>();
							for (Region neighborNeighbor : regionsThatCanDoStuff)
							{
								if (neighborNeighbor.ownedByPlayer(myName) && !regionsThatDidStuff.contains(neighborNeighbor))
								{
									int availableArmies = getAvailableArmies(neighborNeighbor, myName);
									if (availableArmies > 3)
									{
										myGang.add(neighborNeighbor);
										armiesAvailableInGang += availableArmies;
									}
								}
							}

							if (armiesAvailableInGang > neighbor.getArmies()+6)
							{
								foundTarget = true;
								for (Region gangMember : myGang)
								{
									attack(attackTransferMoves, state, gangMember, neighbor);
									regionsThatDidStuff.add(gangMember);
								}
							}
							break;
						}
					}
				}
			}
			if (!foundTarget && !state.getOwnedRegionsNextToOpponent().contains(fromRegion) && fromRegion.getNeighborSuperRegions().size() == 0 && fromRegion.getArmies() > 4)
			{
				// Attack non-opponent neighbor?
				for (Region neighbor : fromRegion.getNeighbors())
				{
					if (!neighbor.ownedByPlayer(myName) && !neighbor.ownedByPlayer(opponentName))
					{
						foundTarget = true;
						attack(attackTransferMoves, state, fromRegion, neighbor);
						regionsThatDidStuff.add(fromRegion);
						break;
					}
				}
			}
			if (!foundTarget && !state.getOwnedRegionsNextToOpponent().contains(fromRegion) && fromRegion.getArmies() > SuperRegion.MIN_GUARD_REGION)
			{
				// Try to get the borders of a super region guarded
				for (Region neighbor : fromRegion.getNeighbors())
				{
					if (fromRegion.getSuperRegion().ownedByPlayer() != null && // Try to guard SuperRegions
						fromRegion.getNeighborSuperRegions().size() == 0 &&
						neighbor.getNeighborSuperRegions().size() > 0)
					{
						// foundTarget = true;
						attack(attackTransferMoves, state, fromRegion, neighbor);
						regionsThatDidStuff.add(fromRegion);
						break;
					}
				}
			}
		}

		// Lame-ass stalemate detection
		if (attackTransferMoves.size() == 0)	{ m_noAttacksCounter++; }
		else                               		{ m_noAttacksCounter = 0; }

		if (m_noAttacksCounter > 10)
		{
			if (regionsThatCanDoStuff.size() > 0)
			{
				for (Region region : regionsThatCanDoStuff)
				{
					if (state.getOwnedRegionsNextToOpponent().contains(region))
					{
						int maxOpponentArmies = 0;
						Region maxOpponent = null;

						for (Region subRegion : region.getNeighbors())
						{
							if (subRegion.ownedByPlayer(opponentName))
							{
								if (subRegion.getArmies() > maxOpponentArmies)
								{
									maxOpponentArmies = subRegion.getArmies();
									maxOpponent = subRegion;
								}
							}
						}

						if (maxOpponent != null)
						{
							attack(attackTransferMoves, state, region, maxOpponent);
							regionsThatDidStuff.add(region);
							// Attack with
							for (Region neighbor : maxOpponent.getNeighbors())
							{
								if (neighbor.ownedByPlayer(myName))
								{
									attack(attackTransferMoves, state, neighbor, maxOpponent);
									regionsThatDidStuff.add(region);
								}
							}

							m_noAttacksCounter = 0;
						}
					}
				}
			}
		}

		////////////////////////////////////////////////////////////////////////////////////
		// Sort the moves the way we want them
		////////////////////////////////////////////////////////////////////////////////////
		ArrayList<AttackTransferMove> sortedAttackTransferMoves = new ArrayList<AttackTransferMove>();
		try
		{
			while (attackTransferMoves.size() > 0)
			{
				if (System.currentTimeMillis() % 1000 >= deadline)
				{
					break;
				}

				boolean changedSomething = false;
				for (AttackTransferMove move : attackTransferMoves)
				{
					boolean isDestination = false;
					for (AttackTransferMove moveTo : attackTransferMoves)
					{
						if (move.getFromRegion() == moveTo.getToRegion())
						{
							isDestination = true;
							break;
						}
					}

					if (!isDestination)
					{
						sortedAttackTransferMoves.add(move);
						changedSomething = true;
					}
				}

				if (!changedSomething)
				{
					for (AttackTransferMove move : attackTransferMoves)
					{
						sortedAttackTransferMoves.add(move);
					}
				}

				attackTransferMoves.removeAll( sortedAttackTransferMoves );
			}
		}
		catch(Exception e)
		{
			// Something went wrong... return old stuff
			return attackTransferMoves;
		}

		if (sortedAttackTransferMoves.size() == 0 || (System.currentTimeMillis() % 1000 >= deadline))
		{
			return attackTransferMoves;
		}
		else
		{
			return sortedAttackTransferMoves;
		}
	}


	protected static int getAvailableArmies( Region region, String myName )
	{
		int attackArmies = region.getArmies()-SuperRegion.MIN_GUARD_REGION;
		if (region.getNeighborSuperRegions().size() > 0 && region.getSuperRegion().ownedByPlayer() != myName)
		{
			attackArmies = region.getArmies()-SuperRegion.MIN_GUARD_BORDER_REGION;
		}
		return attackArmies;
	}


	protected static int SEARCH_FLAG_FIND_OPPONENT = 1;
	protected static int SEARCH_FLAG_FIND_ANY = 2;
	protected static int SEARCH_FLAG_FIND_REGION_ID = 4;

	protected static int SEARCH_FLAG_WITHIN_SUPER_REGION = 8;

	protected static Region getPath(Region fromRegion, BotState state, int searchFlags) throws Exception
	{
		if ((searchFlags >> 2 & 1) != 0)
		{
			throw new FindPathException("Must supply a regionId when using SEARCH_FLAG_FIND_REGION_ID");
		}
		return getPath(fromRegion,state,searchFlags,0);
	}

	protected static Region getPath(Region fromRegion, BotState state, int searchFlags, int regionId) throws Exception
	{
		boolean findOpponent = (searchFlags >> 0 & 1) != 0;
		boolean findAny = (searchFlags >> 1 & 1) != 0;
		boolean findRegionId = (searchFlags >> 2 & 1) != 0;

		if (!findOpponent && !findAny && !findRegionId) { throw new FindPathException("Must specify one FIND search flag (found none)"); }
		if ((findOpponent && findAny) || (findOpponent && findRegionId) || (findAny && findRegionId)) { throw new FindPathException("Must specify only one FIND search flag (found multiple)"); }

		boolean flagWithinSuperRegion = (searchFlags >> 3 & 1) != 0;


		// Find nearest region not owned by me, inside the same SuperRegion
		ArrayList<Region> regionsVisited = new ArrayList<Region>();
		// Start Region, End Regions
		HashMap<Region,ArrayList<Region>> paths = new HashMap<Region,ArrayList<Region>>();

		// Cannot go via myself
		regionsVisited.add(fromRegion);
		// Visit neighbors to ensure the shortest route
		for (Region neighbor : fromRegion.getNeighbors())
		{
			if (!flagWithinSuperRegion || neighbor.getSuperRegion() == fromRegion.getSuperRegion())
			{
				if (findOpponent && neighbor.ownedByPlayer(state.getOpponentPlayerName()))
				{
					return neighbor; // Found target!
				}
				if (findAny && !neighbor.ownedByPlayer(state.getMyPlayerName()))
				{
					return neighbor; // Found target!
				}
				if (findRegionId && neighbor.getId() == regionId)
				{
					return neighbor; // Found target!
				}
			}

			regionsVisited.add( neighbor );
		}
		// Set-up starting paths
		for (Region neighbor : fromRegion.getNeighbors())
		{
			ArrayList<Region> endRegions = new ArrayList<Region>();
			for (Region neighborNeighbor : neighbor.getNeighbors())
			{
				if (!regionsVisited.contains(neighborNeighbor))
				{
					// Stay inside SuperRegion when needed
					if (!flagWithinSuperRegion || neighborNeighbor.getSuperRegion() == fromRegion.getSuperRegion())
					{
						if (findOpponent && neighborNeighbor.ownedByPlayer(state.getOpponentPlayerName()))
						{
							return neighbor; // Found target!
						}
						if (findAny && !neighborNeighbor.ownedByPlayer(state.getMyPlayerName()))
						{
							return neighbor; // Found target!
						}
						if (findRegionId && neighborNeighbor.getId() == regionId)
						{
							return neighbor; // Found target!
						}

						endRegions.add( neighborNeighbor );
					}
					regionsVisited.add( neighborNeighbor );
				}
			}

			// Add startRegion to go from, with all its neighbors that we visited.
			paths.put(neighbor, endRegions);
		}

		boolean newRegionsVisited = true;
		// This search will "fan-out" until it finds an enemy or all regions are visited
		while( newRegionsVisited )
		{
			newRegionsVisited = false;
			for( Region startRegion : paths.keySet() )
			{
				ArrayList<Region> newEndRegions = new ArrayList<Region>();
				for ( Region endRegion : paths.get(startRegion) )
				{
					for (Region endRegionNeighbor : endRegion.getNeighbors())
					{
						if (!flagWithinSuperRegion || endRegionNeighbor.getSuperRegion() == fromRegion.getSuperRegion())
						{
							if (findOpponent && endRegionNeighbor.ownedByPlayer(state.getOpponentPlayerName()))
							{
								return startRegion; // Found target!
							}
							if (findAny && !endRegionNeighbor.ownedByPlayer(state.getMyPlayerName()))
							{
								return startRegion; // Found target!
							}
							if (findRegionId && endRegionNeighbor.getId() == regionId)
							{
								return startRegion; // Found target!
							}
						}

						if (!regionsVisited.contains(endRegionNeighbor))
						{
							// Stay inside SuperRegion when needed
							if (!flagWithinSuperRegion || endRegionNeighbor.getSuperRegion() == fromRegion.getSuperRegion())
							{
								newEndRegions.add( endRegionNeighbor );
							}
							regionsVisited.add( endRegionNeighbor );
							newRegionsVisited = true;
						}
					}
				}
				// Overwrite old regions we visited, with the new regions have just visited and start from in the next loop.
				paths.put(startRegion, newEndRegions);
			}
		}

		// Not found
		return null;
	}


	protected void debugLog( BotState state, String string )
	{
		if (state.isDebugMode())
		{
			StackTraceElement userTrace = Thread.currentThread().getStackTrace()[2];
			System.err.println(string + " " + userTrace.getFileName() + "(" + userTrace.getLineNumber() + ")" );
		}
	}


	public static void main(String[] args)
	{
		BotParser parser;
		if (args.length > 0)
		{
			parser = new BotParser(new KompjoeWarlightBot(), args[0]);
		}
		else
		{
			parser = new BotParser(new KompjoeWarlightBot());
		}
		parser.run();
	}
}


class FindPathException extends Exception
{
	FindPathException(String message)
	{
		super(message);
	}
}
