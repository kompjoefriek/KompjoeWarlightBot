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

public class KompjoeConquestBot implements Bot
{
	private Comparator<Region> compareArmies;
	private Comparator<Region> compareArmiesDescending;
	private Comparator<SuperRegion> comparePreferredSuperRegions;

	public KompjoeConquestBot()
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
			public int compare(SuperRegion o1, SuperRegion o2)
			{
				return o2.comparePreferredTo(o1);
			}
		};
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
		ArrayList<SuperRegion> preferredSuperRegions = new ArrayList<SuperRegion>();
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
					if (preferredStartingRegions.size() >= max) { break; }
				}
			}
			if (preferredStartingRegions.size() >= max) { break; }
		}

		/*
		////////////////////////////////////////////////////////////////////////////////////
		// Find out if a complete SuperRegion can be filled with the PickableStartingRegions
		////////////////////////////////////////////////////////////////////////////////////
		HashMap<Integer,Integer> superRegionCount = new HashMap<Integer,Integer>();
		for (Region region : state.getPickableStartingRegions())
		{
			int value = 1;
			if (superRegionCount.containsKey(region.getSuperRegion().getId()))
			{
				value += superRegionCount.get(region.getSuperRegion().getId());
			}
			superRegionCount.put(region.getSuperRegion().getId(), value);
		}

		int highest = 0;
		for (Integer value : superRegionCount.values())
		{
			if (value > highest) { highest = value; }
		}

		int preferredSuperRegion = 0;
		for (Integer superRegion : superRegionCount.keySet())
		{
			if (superRegionCount.get( superRegion ) == highest)
			{
				preferredSuperRegion = superRegion;
				break;
			}
		}

		for (Region region : state.getPickableStartingRegions())
		{
			if (region.getSuperRegion().getId() == preferredSuperRegion)
			{
				preferredStartingRegions.add(region);
				count++;
			}
		}
		*/

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

		// Try to place armies on the region next to nobody with the most armies (to raise their numbers so it can expand)
		if (ownedRegionsNextToNobody.size() > 0)
		{
			placeArmiesMoves.add(new PlaceArmiesMove(myName, ownedRegionsNextToNobody.get(0), armies));
			armiesLeft -= armies;
		}

		// Prefer to place armies on Regions next to opponent, regions with less armies first
		int idxRegion = 0;
		while (armiesLeft > 0 && idxRegion < ownedRegionsNextToOpponent.size())
		{
			if (armies > armiesLeft) { armies = armiesLeft; }
			placeArmiesMoves.add(new PlaceArmiesMove(myName, ownedRegionsNextToOpponent.get(idxRegion), armies));
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
					placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armies));
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
				placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armies));
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
		ArrayList<AttackTransferMove> attackTransferMoves = new ArrayList<AttackTransferMove>();
		String myName = state.getMyPlayerName();
		int armies = 5;

		ArrayList<Region> regionsOwned = new ArrayList<Region>();

		// First lets find the region with the most armies
		int mostArmies = 0;
		Region regionWithMostArmies = null;

		for (Region region : state.getVisibleMap().getRegions())
		{
			if (region.ownedByPlayer(myName)) //do an attack
			{
				regionsOwned.add(region);
				if (region.getArmies() > mostArmies)
				{
					mostArmies = region.getArmies();
					regionWithMostArmies = region;
				}
			}
		}

		ArrayList<Region> regionsThatCanDoStuff = new ArrayList<Region>();
		regionsThatCanDoStuff.addAll( regionsOwned );

		if ((regionWithMostArmies.getNeighborSuperRegions().size() == 0 && mostArmies > 11) || mostArmies > 16)
		{
			// Find nearest opponent
			boolean foundPath = false;

			for (Region neighbor : regionWithMostArmies.getNeighbors())
			{
				if (neighbor.ownedByPlayer(state.getOpponentPlayerName()))
				{
					// Found opponent!
					regionsThatCanDoStuff.remove( neighbor );
					foundPath = true;
					int attackArmies = regionWithMostArmies.getArmies()-2;
					if (regionWithMostArmies.getNeighborSuperRegions().size() > 0)
					{
						attackArmies = regionWithMostArmies.getArmies()-7;
					}
					attackTransferMoves.add(new AttackTransferMove(myName, regionWithMostArmies, neighbor, attackArmies));
					break;
				}
			}

			if (!foundPath)
			{
				//HashMap<Region,Integer> stepsToOpponent = new HashMap<Region,Integer>();

				// TODO: Try dijkstra's to find nearest opponent
			}
		}


		for (Region fromRegion : regionsThatCanDoStuff)
		{
			boolean foundPath = false;
			if (!foundPath && fromRegion.getArmies() > 6)
			{
				// Attack a opponent neighbor?
				for (Region neighbor : fromRegion.getNeighbors())
				{
					if (!neighbor.ownedByPlayer(myName))
					{
						foundPath = true;
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, neighbor, fromRegion.getArmies()-2));
						break;
					}
				}
			}
			if (!foundPath && fromRegion.getArmies() > 4)
			{
				// Attack non-opponent neighbor?
				for (Region neighbor : fromRegion.getNeighbors())
				{
					if (!neighbor.ownedByPlayer(myName) && !neighbor.ownedByPlayer(state.getOpponentPlayerName()))
					{
						foundPath = true;
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, neighbor, fromRegion.getArmies()-2));
						break;
					}
				}
			}
			if (!foundPath && fromRegion.getArmies() > 2)
			{
				// Try to get the borders of a super region guarded
				for (Region neighbor : fromRegion.getNeighbors())
				{
					if (fromRegion.getSuperRegion().ownedByPlayer() != null && // Try to guard SuperRegions
						fromRegion.getNeighborSuperRegions().size() == 0 &&
						neighbor.getNeighborSuperRegions().size() > 0)
					{
						foundPath = true;
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, neighbor, fromRegion.getArmies()-2));
						break;
					}
				}
			}
		}

		return attackTransferMoves;
	}

	public static void main(String[] args)
	{
		BotParser parser = new BotParser(new KompjoeConquestBot());
		parser.run();
	}
}
