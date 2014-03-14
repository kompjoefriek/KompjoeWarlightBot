package bot;

/**
 * Default bot with some modifications:
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
import move.AttackTransferMove;
import move.PlaceArmiesMove;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class KompjoeConquestBot implements Bot
{
	@Override
	/**
	 * A method used at the start of the game to decide which player start with what Regions. 6 Regions are required to be returned.
	 * This example randomly picks 6 regions from the pickable starting Regions given by the engine.
	 * @return : a list of m (m=6) Regions starting with the most preferred Region and ending with the least preferred Region to start with 
	 */
	public ArrayList<Region> getPreferredStartingRegions(BotState state, Long timeOut)
	{
		int m = 6;
		ArrayList<Region> preferredStartingRegions = new ArrayList<Region>();

		// TODO: Do neat stuff here too...

		for (int i = 0; i < m; i++)
		{
			double rand = Math.random();
			int r = (int) (rand * state.getPickableStartingRegions().size());
			int regionId = state.getPickableStartingRegions().get(r).getId();
			Region region = state.getFullMap().getRegion(regionId);

			if (!preferredStartingRegions.contains(region)) preferredStartingRegions.add(region);
			else i--;
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

		// Build list of owned Regions and one with owned Regions net to opponent
		ArrayList<Region> ownedRegions = new ArrayList<Region>();
		ArrayList<Region> ownedRegionsNextToOpponent = new ArrayList<Region>();
		for (Region region : visibleRegions)
		{
			if (region.ownedByPlayer(myName))
			{
				ownedRegions.add(region);
				for (Region neighbor : region.getNeighbors())
				{
					if (region.ownedByPlayer(opponentName))
					{
						ownedRegionsNextToOpponent.add(region);
						break; // Break out of for-loop
					}
				}
			}
		}

		Collections.sort(ownedRegionsNextToOpponent, new Comparator<Region>()
		{
			public int compare(Region o1, Region o2) { return o2.compareTo(o1); }
		});

		// Prefer to place armies on Regions next to opponent
		int idxRegion = 0;
		while (armiesLeft > 0 && idxRegion < ownedRegionsNextToOpponent.size())
		{
			if (armies > armiesLeft) { armies = armiesLeft; }
			placeArmiesMoves.add(new PlaceArmiesMove(myName, ownedRegionsNextToOpponent.get(idxRegion), armies));
			armiesLeft -= armies;
			idxRegion++;
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

		for (Region fromRegion : state.getVisibleMap().getRegions())
		{
			if (fromRegion.ownedByPlayer(myName)) //do an attack
			{
				ArrayList<Region> possibleToRegions = new ArrayList<Region>();
				possibleToRegions.addAll(fromRegion.getNeighbors());

				while (!possibleToRegions.isEmpty())
				{
					double rand = Math.random();
					int r = (int) (rand * possibleToRegions.size());
					Region toRegion = possibleToRegions.get(r);

					if (fromRegion.getSuperRegion().ownedByPlayer() != null && // Try to guard SuperRegions
						fromRegion.getArmies() > 2 &&
						toRegion.getNeighborSuperRegions().size() > 0)
					{
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, fromRegion.getArmies()-2));
						break;
					}
    				else if (!toRegion.ownedByPlayer(myName) && fromRegion.getArmies() > 6) //do an attack
					{
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, armies));
						break;
					}
					else if (toRegion.ownedByPlayer(myName) && fromRegion.getArmies() > 1) //do a transfer
					{
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, armies));
						break;
					}
					else possibleToRegions.remove(toRegion);
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
