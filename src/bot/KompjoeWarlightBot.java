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
import sun.print.resources.serviceui;

import java.util.*;

public class KompjoeWarlightBot implements Bot
{
	public enum Strategy
	{
		DEFAULT,
		CONTINENT_GET, // http://knowyourmeme.com/memes/get
		AGRO_MODE // attack ALL the regions!
	};

	private Comparator<Region> compareArmies;
	private Comparator<Region> compareArmiesDescending;
	private Comparator<SuperRegion> comparePreferredSuperRegions;
	private ArrayList<SuperRegion> preferredSuperRegions;

	private Strategy strategy;
	private SuperRegion superRegionGet; // used with Strategy.CONTINENT_GET

	private int noAttacksCounter;

	public KompjoeWarlightBot()
	{
		compareArmies = new Comparator<Region>()
		{
			public int compare(Region o1, Region o2) { return o2.compareTo(o1); }
		};

		compareArmiesDescending = new Comparator<Region>()
		{
			public int compare(Region o1, Region o2) { return 0-o2.compareTo(o1); }
		};

		comparePreferredSuperRegions = new Comparator<SuperRegion>()
		{
			public int compare(SuperRegion o1, SuperRegion o2) { return 0-o2.comparePreferredTo(o1); }
		};

		preferredSuperRegions = new ArrayList<SuperRegion>();

		strategy = Strategy.DEFAULT;
		superRegionGet = null;
		noAttacksCounter = 0;
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
		preferredSuperRegions.clear();
		preferredSuperRegions.addAll(state.getFullMap().getSuperRegions());
		Collections.sort(preferredSuperRegions, comparePreferredSuperRegions);

		for( SuperRegion superRegion : preferredSuperRegions)
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


	private void attack(ArrayList<AttackTransferMove> attackTransferMoves, String myName, String opponentName, Region fromRegion, Region toRegion)
	{
		int attackArmies = getAvailableArmies(fromRegion, myName);
		if (attackArmies > (toRegion.getArmies() * 3)+6)
		{
			// See if we can split up
			Region enemy = null;
			for (Region neighbor : fromRegion.getNeighbors())
			{
				if (neighbor != toRegion && neighbor.ownedByPlayer(opponentName))
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
				attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, enemy, otherAttackerEnemies));
			}
		}
		attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, attackArmies));
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
		LinkedList<Region> visibleRegions = state.getVisibleMap().getRegions();

		// Build list of owned Regions and one with owned Regions next to opponent
		ArrayList<Region> ownedRegions = new ArrayList<Region>();
		ArrayList<Region> ownedRegionsNextToOpponent = new ArrayList<Region>();
		ArrayList<Region> ownedRegionsNextToNobody = new ArrayList<Region>();
		for (Region region : visibleRegions)
		{
			if (region.ownedByPlayer(myName))
			{
				ownedRegions.add(region);
				for (Region neighbor : region.getNeighbors())
				{
					if (!neighbor.ownedByPlayer(myName))
					{
						if (neighbor.ownedByPlayer(opponentName))
						{
							if (!ownedRegionsNextToOpponent.contains(region))
							{
								ownedRegionsNextToOpponent.add(region);
							}
						}
						else
						{
							if (!ownedRegionsNextToNobody.contains(region))
							{
								ownedRegionsNextToNobody.add(region);
							}
						}
					}
				}
			}
		}

		Collections.sort(ownedRegionsNextToOpponent, compareArmies);
		Collections.sort(ownedRegionsNextToNobody, compareArmiesDescending);

		if (state.getRoundNumber() >= 50)
		{
			strategy = Strategy.AGRO_MODE;
		}
		else
		{
			strategy = Strategy.DEFAULT;
			superRegionGet = null;
			for ( SuperRegion superRegion : preferredSuperRegions.subList(0,3) )
			{
				if (superRegion.ownedByPlayer() != myName)
				{
					strategy = Strategy.CONTINENT_GET;
					superRegionGet = superRegion;

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

		if (strategy == Strategy.AGRO_MODE)
		{
			if (ownedRegionsNextToOpponent.size() > 0)
			{
				placeArmiesMoves.add(new PlaceArmiesMove(myName, ownedRegionsNextToOpponent.get(0), armiesLeft));
				ownedRegionsNextToOpponent.get(0).setArmies(ownedRegionsNextToOpponent.get(0).getArmies() + armiesLeft); // Update internal stuff
				//armiesLeft -= armiesLeft;
				return placeArmiesMoves;
			}
		}
		else if (strategy == Strategy.CONTINENT_GET && superRegionGet != null)
		{
			// Find regions in this superRegion we can take-over
			ArrayList<Region> regionsToTakeOver = new ArrayList<Region>();
			for ( Region subRegion : superRegionGet.getSubRegions() )
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
		if (ownedRegionsNextToNobody.size() > 0)
		{
			placeArmies( placeArmiesMoves, myName, ownedRegionsNextToNobody.get(0), armies );
			//placeArmiesMoves.add(new PlaceArmiesMove(myName, ownedRegionsNextToNobody.get(0), armies));
			ownedRegionsNextToNobody.get(0).setArmies(ownedRegionsNextToNobody.get(0).getArmies() + armies); // Update internal stuff
			armiesLeft -= armies;
		}

		// Prefer to place armies on Regions next to opponent, regions with less armies first
		int idxRegion = 0;
		while (armiesLeft > 0 && idxRegion < ownedRegionsNextToOpponent.size())
		{
			if (armies > armiesLeft) { armies = armiesLeft; }
			placeArmies( placeArmiesMoves, myName, ownedRegionsNextToOpponent.get(idxRegion), armies );
			//placeArmiesMoves.add(new PlaceArmiesMove(myName, ownedRegionsNextToOpponent.get(idxRegion), armies));
			ownedRegionsNextToOpponent.get(idxRegion).setArmies( ownedRegionsNextToOpponent.get(idxRegion).getArmies() + armies); // Update internal stuff
			armiesLeft -= armies;
			idxRegion++;
		}

		// If we cannot expand, put all near opponent
		if (ownedRegionsNextToNobody.size() == 0 && ownedRegionsNextToOpponent.size() > 0)
		{
			while(armiesLeft > 0)
			{
				for (Region region : ownedRegionsNextToOpponent)
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
			int r = (int) (rand * ownedRegions.size());
			Region region = ownedRegions.get(r);

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
		ArrayList<Region> regionsOwned = new ArrayList<Region>();

		boolean opponentIsVisible = false;

		//// First lets find the region with the most armies
		//int mostArmies = 0;
		//Region ownedRegionWithMostArmies = null;
		//
		for (Region region : state.getVisibleMap().getRegions())
		{
			if (region.ownedByPlayer(myName)) //do an attack
			{
				regionsOwned.add(region);
				//if (region.getArmies() > mostArmies)
				//{
				//	mostArmies = region.getArmies();
				//	ownedRegionWithMostArmies = region;
				//}
			}
			else if (region.ownedByPlayer(opponentName))
			{
				opponentIsVisible = true;
			}

			boolean nextToOpponent = false;
			for( Region neighbors : region.getNeighbors() )
			{
				if (neighbors.ownedByPlayer(opponentName))
				{
					nextToOpponent = true;
					break;
				}
			}
			region.setNextToOpponent(nextToOpponent);
		}

		////////////////////////////////////////////////////////////////////////////////////
		// Move armies from the region with the most armies towards the opponent. we just need to find the closes region
		////////////////////////////////////////////////////////////////////////////////////
		ArrayList<Region> regionsThatCanDoStuff = new ArrayList<Region>();
		regionsThatCanDoStuff.addAll( regionsOwned );
		Collections.sort(regionsThatCanDoStuff, compareArmies);

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
			if (threadCount >= fromRegion.getArmies() - 3 && strategy != Strategy.AGRO_MODE)
			{
				// fromRegion is defending :-)
				regionsThatDidStuff.add(fromRegion);
				continue;
			}

			boolean toMuchArmies = false;
			if (fromRegion.getArmies() > SuperRegion.MIN_GUARD_BORDER_REGION)
				{ toMuchArmies = true; }
			if (!toMuchArmies && fromRegion.getNeighborSuperRegions().size() > 0 && fromRegion.getArmies() > SuperRegion.MIN_GUARD_BORDER_REGION &&
				fromRegion.getSuperRegion().getFullyGuarded() && !fromRegion.isNextToOpponent()) { toMuchArmies = true; }
			if (!toMuchArmies && fromRegion.getSuperRegion().ownedByPlayer() == myName && fromRegion.getNeighborSuperRegions().size() == 0 &&
				fromRegion.getArmies() > SuperRegion.MIN_GUARD_REGION) { toMuchArmies = true; }

			if (toMuchArmies)
			{
				if (fromRegion.getSuperRegion().ownedByPlayer() == null)
				{
					// This SuperRegion is not owned by me yet!

					// Find nearest region not owned by me, inside the same SuperRegion
					boolean foundPath = false;
					ArrayList<Region> regionsVisited = new ArrayList<Region>();
					// Start Region, End Regions
					HashMap<Region,ArrayList<Region>> paths = new HashMap<Region,ArrayList<Region>>();

					// Cannot go via myself
					regionsVisited.add(fromRegion);
					// Visit neighbors to ensure the shortest route
					for (Region neighbor : fromRegion.getNeighbors())
					{
						regionsVisited.add( neighbor );
					}
					// Set-up starting paths
					for (Region neighbor : fromRegion.getNeighbors())
					{
						if (neighbor.getSuperRegion() == fromRegion.getSuperRegion() && !neighbor.ownedByPlayer(myName))
						{
							// Found target!
							regionsThatDidStuff.add( neighbor );
							foundPath = true;
							attack(attackTransferMoves, myName, opponentName, fromRegion, neighbor);
							break;
						}

						ArrayList<Region> endRegions = new ArrayList<Region>();
						for (Region neighborNeighbor : neighbor.getNeighbors())
						{
							if (!regionsVisited.contains(neighborNeighbor))
							{
								// Stay inside SuperRegion
								if (neighborNeighbor.getSuperRegion() == fromRegion.getSuperRegion())
								{
									endRegions.add( neighborNeighbor );
								}
								regionsVisited.add( neighborNeighbor );
							}
						}

						// add startRegion to go from, with all its neighbors that we visited.
						paths.put(neighbor, endRegions);
					}

					boolean newRegionsVisited = true;
					// This search will "fan-out" until it finds an enemy or all regions are visited
					while( !foundPath && newRegionsVisited )
					{
						newRegionsVisited = false;
						for( Region startRegion : paths.keySet() )
						{
							ArrayList<Region> newEndRegions = new ArrayList<Region>();
							for ( Region endRegion : paths.get(startRegion) )
							{
								for (Region endRegionNeighbor : endRegion.getNeighbors())
								{
									if (endRegionNeighbor.getSuperRegion() == fromRegion.getSuperRegion() && !endRegionNeighbor.ownedByPlayer(myName))
									{
										// Found target!
										regionsThatDidStuff.add( startRegion );
										foundPath = true;
										attack(attackTransferMoves, myName, opponentName, fromRegion, startRegion);
										break;
									}
									if (!regionsVisited.contains(endRegionNeighbor))
									{
										// Stay inside SuperRegion
										if (endRegionNeighbor.getSuperRegion() == fromRegion.getSuperRegion())
										{
											newEndRegions.add( endRegionNeighbor );
										}
										regionsVisited.add( endRegionNeighbor );
										newRegionsVisited = true;
									}
								}
								if (foundPath) { break; }
							}
							if (foundPath) { break; }
							// Overwrite old regions we visited, with the new regions have just visited and start from in the next loop.
							paths.put(startRegion, newEndRegions);
						}
					}
				}
				else
				{
					// Find nearest opponent
					boolean foundPath = false;
					ArrayList<Region> regionsVisited = new ArrayList<Region>();
					// Start Region, End Regions
					HashMap<Region,ArrayList<Region>> paths = new HashMap<Region,ArrayList<Region>>();

					// Cannot go via myself
					regionsVisited.add(fromRegion);
					// Visit neighbors to ensure the shortest route
					for (Region neighbor : fromRegion.getNeighbors())
					{
						regionsVisited.add( neighbor );
					}
					// Set-up starting paths
					for (Region neighbor : fromRegion.getNeighbors())
					{
						if (neighbor.ownedByPlayer(opponentName) || (!opponentIsVisible && !neighbor.ownedByPlayer(myName)))
						{
							// Found opponent!
							regionsThatDidStuff.add( neighbor );
							foundPath = true;
							attack(attackTransferMoves, myName, opponentName, fromRegion, neighbor);
							break;
						}

						ArrayList<Region> endRegions = new ArrayList<Region>();
						for (Region neighborNeighbor : neighbor.getNeighbors())
						{
							if (!regionsVisited.contains(neighborNeighbor))
							{
								endRegions.add( neighborNeighbor );
								regionsVisited.add( neighborNeighbor );
							}
						}

						// add startRegion to go from, with all its neighbors that we visited.
						paths.put(neighbor, endRegions);
					}

					boolean newRegionsVisited = true;
					// This search will "fan-out" until it finds an enemy or all regions are visited
					while( !foundPath && newRegionsVisited )
					{
						newRegionsVisited = false;
						for( Region startRegion : paths.keySet() )
						{
							ArrayList<Region> newEndRegions = new ArrayList<Region>();
							for ( Region endRegion : paths.get(startRegion) )
							{
								for (Region endRegionNeighbor : endRegion.getNeighbors())
								{
									if (endRegionNeighbor.ownedByPlayer(opponentName) || (!opponentIsVisible && !endRegionNeighbor.ownedByPlayer(myName)))
									{
										// Found opponent!
										regionsThatDidStuff.add( startRegion );
										foundPath = true;
										attack(attackTransferMoves, myName, opponentName, fromRegion, startRegion);
										break;
									}
									if (!regionsVisited.contains(endRegionNeighbor))
									{
										newEndRegions.add( endRegionNeighbor );
										regionsVisited.add( endRegionNeighbor );
										newRegionsVisited = true;
									}
								}
								if (foundPath) { break; }
							}
							if (foundPath) { break; }
							// Overwrite old regions we visited, with the new regions have just visited and start from in the next loop.
							paths.put(startRegion, newEndRegions);
						}
					}
				}
			}
		}

		for (Region fromRegion : regionsThatCanDoStuff)
		{
			if (regionsThatDidStuff.contains(fromRegion)) { continue; }

			boolean foundTarget = false;
			boolean nextToOpponent = false;
			if (/*!foundTarget &&*/ fromRegion.getArmies() > SuperRegion.MIN_GUARD_REGION + 5)
			{
				// Attack a opponent neighbor?
				for (Region neighbor : fromRegion.getNeighbors())
				{
					if (neighbor.ownedByPlayer(opponentName))
					{
						nextToOpponent = true;
						if (!fromRegion.isNextToOpponent())
						{
							boolean breakMe = true;
						}
						if (fromRegion.getArmies()-SuperRegion.MIN_GUARD_REGION > neighbor.getArmies()+3)
						{
							foundTarget = true;
							attack(attackTransferMoves, myName, opponentName, fromRegion, neighbor);
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
									attack(attackTransferMoves, myName, opponentName, gangMember, neighbor);
									regionsThatDidStuff.add(gangMember);
								}
							}
							break;
						}
					}
				}
			}
			if (fromRegion.isNextToOpponent() != nextToOpponent)
			{
				boolean breakMe = true;
			}
			if (!foundTarget && !nextToOpponent && fromRegion.getNeighborSuperRegions().size() == 0 && fromRegion.getArmies() > 4)
			{
				// Attack non-opponent neighbor?
				for (Region neighbor : fromRegion.getNeighbors())
				{
					if (!neighbor.ownedByPlayer(myName) && !neighbor.ownedByPlayer(opponentName))
					{
						foundTarget = true;
						attack(attackTransferMoves, myName, opponentName, fromRegion, neighbor);
						regionsThatDidStuff.add(fromRegion);
						break;
					}
				}
			}
			if (!foundTarget && !nextToOpponent && fromRegion.getArmies() > SuperRegion.MIN_GUARD_REGION)
			{
				// Try to get the borders of a super region guarded
				for (Region neighbor : fromRegion.getNeighbors())
				{
					if (fromRegion.getSuperRegion().ownedByPlayer() != null && // Try to guard SuperRegions
						fromRegion.getNeighborSuperRegions().size() == 0 &&
						neighbor.getNeighborSuperRegions().size() > 0)
					{
						// foundTarget = true;
						attack(attackTransferMoves, myName, opponentName, fromRegion, neighbor);
						regionsThatDidStuff.add(fromRegion);
						break;
					}
				}
			}
		}

		// Lame-ass stalemate detection
		if (attackTransferMoves.size() == 0)	{ noAttacksCounter++; }
		else                               		{ noAttacksCounter = 0; }

		if (noAttacksCounter > 5)
		{
			if (regionsThatCanDoStuff.size() > 0)
			{
				for (Region region : regionsThatCanDoStuff)
				{
					if (region.isNextToOpponent())
					{
						int maxOpponentArmies = 0;
						Region maxOpponent = null;
						// Collect all armies around me
						for (Region subRegion : region.getNeighbors())
						{
							//if (!regionsThatDidStuff.contains(subRegion) && subRegion.ownedByPlayer(myName))
							//{
							//	int armiesToMove = subRegion.getArmies()-SuperRegion.MIN_GUARD_REGION;
							//	attackTransferMoves.add(new AttackTransferMove(myName, subRegion, region, armiesToMove));
							//	regionsThatDidStuff.add(subRegion);
							//	region.setArmies(region.getArmies()+armiesToMove); // update internal stuff
							//}
							//else
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
							attackTransferMoves.add(new AttackTransferMove(myName, region, maxOpponent,
								region.getArmies() - SuperRegion.MIN_GUARD_REGION));
							regionsThatDidStuff.add(region);
							// Attack with
							for (Region neighbor : maxOpponent.getNeighbors())
							{
								if (neighbor.ownedByPlayer(myName))
								{
									attackTransferMoves.add(new AttackTransferMove(myName, neighbor, maxOpponent,
										neighbor.getArmies() - SuperRegion.MIN_GUARD_REGION));
									regionsThatDidStuff.add(region);
								}
							}

							noAttacksCounter = 0;
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
						//if (move.getToRegion().ownedByPlayer(opponentName))
						//{
						//	// Always attack with max force
						//	move.setArmies(move.getFromRegion().getArmies()-SuperRegion.MIN_GUARD_REGION);
						//}
						//else
						//{
						//	move.setArmies(getAvailableArmies(move.getFromRegion(), myName));
						//}
						sortedAttackTransferMoves.add(move);
						//move.getToRegion().setArmies(move.getToRegion().getArmies()+move.getArmies());
						changedSomething = true;
					}
				}

				if (!changedSomething)
				{
					for (AttackTransferMove move : attackTransferMoves)
					{
						//if (move.getToRegion().ownedByPlayer(opponentName))
						//{
						//	// Always attack with max force
						//	move.setArmies(move.getFromRegion().getArmies()-SuperRegion.MIN_GUARD_REGION);
						//}
						//else
						//{
						//	move.setArmies(getAvailableArmies(move.getFromRegion(), myName));
						//}
						sortedAttackTransferMoves.add(move);
						//move.getToRegion().setArmies(move.getToRegion().getArmies()+move.getArmies());
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


	private static int getAvailableArmies( Region region, String myName )
	{
		int attackArmies = region.getArmies()-SuperRegion.MIN_GUARD_REGION;
		if (region.getNeighborSuperRegions().size() > 0 && region.getSuperRegion().ownedByPlayer() != myName)
		{
			attackArmies = region.getArmies()-SuperRegion.MIN_GUARD_BORDER_REGION;
		}
		return attackArmies;
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
