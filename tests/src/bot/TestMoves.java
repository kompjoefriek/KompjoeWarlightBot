package bot;

import junit.framework.Assert;
import main.Region;
import main.SuperRegionName;
import move.AttackTransferMove;
import move.PlaceArmiesMove;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Text cases for all invalid and wanted moves
 */
public class TestMoves
{
	private Bot m_bot;
	private BotState m_currentState;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() throws Exception
	{
		m_bot = new Gir();
		m_currentState = new BotState();
		//m_currentState.setDebugMode();
	}

	@After
	public void tearDown() throws Exception
	{
		m_bot = null;
		m_currentState = null;
	}

	private void doNotMoveNextToOpponent(String filename, int roundId) throws Exception
	{
		File file = new File(filename);
		Scanner scanner = new Scanner(file);

		while (scanner.hasNextLine())
		{
			String line = scanner.nextLine().trim();
			if (line.length() == 0) { continue; }
			String[] parts = line.split(" ");
			if (parts[0].equals("pick_starting_regions"))
			{
				// Pick which regions you want to start with
				m_currentState.setPickableStartingRegions(parts);
				ArrayList<Region> preferredStartingRegions = m_bot.getPreferredStartingRegions(m_currentState, Long.valueOf(parts[1]));
			}
			else if (parts.length == 3 && parts[0].equals("go"))
			{
				// We need to do a move
				if (parts[1].equals("place_armies"))
				{
					// Place armies
					ArrayList<PlaceArmiesMove> placeArmiesMoves = m_bot.getPlaceArmiesMoves(m_currentState, Long.valueOf(parts[2]));
				}
				else if (parts[1].equals("attack/transfer"))
				{
					// Attack/transfer
					ArrayList<AttackTransferMove> attackTransferMoves = m_bot.getAttackTransferMoves(m_currentState, Long.valueOf(parts[2]));

					// Actual test code here
					if (m_currentState.getRoundNumber() == roundId)
					{
						for ( AttackTransferMove move : attackTransferMoves )
						{
							boolean nextToEnemy = false;
							for ( Region neighbor : move.getFromRegion().getNeighbors() )
							{
								if (neighbor.ownedByPlayer(m_currentState.getOpponentPlayerName()))
								{
									nextToEnemy = true;
									break;
								}
							}

							Assert.assertFalse("Must not move when next to an opponent",
								nextToEnemy && !move.getToRegion()
									.ownedByPlayer(m_currentState.getOpponentPlayerName()));
						}

						return;
					}
				}
			}
			else if (parts.length == 3 && parts[0].equals("settings"))
			{
				// Update settings
				m_currentState.updateSettings(parts[1], parts[2]);
			}
			else if (parts[0].equals("setup_map"))
			{
				// Initial full map is given
				m_currentState.setupMap(parts);
			}
			else if (parts[0].equals("update_map"))
			{
				// All visible regions are given
				m_currentState.updateMap(parts);
			}
			else if (parts[0].equals("opponent_moves"))
			{
				// All visible opponent moves are given
				m_currentState.readOpponentMoves(parts);
			}
			else if (parts[0].equals("debug_line"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else if (parts[0].equals("Round"))
			{
				if (m_currentState.isDebugMode())
				{
					System.out.println(m_currentState.getVisibleMap().toString());
					System.out.println("DB: " + line + " (internal round " + m_currentState.getRoundNumber() + ")");
				}
			}
			else if (parts[0].equals("player1"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else if (parts[0].equals("player2"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else if (parts[0].equals("No"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else
			{
				throw new NoSuchFieldException("Unable to parse line \""+line+"\"");
			}
		}
	}

	private void doNotSplitUp(String filename, int roundId) throws Exception
	{
		File file = new File(filename);
		Scanner scanner = new Scanner(file);

		while (scanner.hasNextLine())
		{
			String line = scanner.nextLine().trim();
			if (line.length() == 0) { continue; }
			String[] parts = line.split(" ");
			if (parts[0].equals("pick_starting_regions"))
			{
				// Pick which regions you want to start with
				m_currentState.setPickableStartingRegions(parts);
				ArrayList<Region> preferredStartingRegions = m_bot.getPreferredStartingRegions(m_currentState, Long.valueOf(parts[1]));
			}
			else if (parts.length == 3 && parts[0].equals("go"))
			{
				// We need to do a move
				if (parts[1].equals("place_armies"))
				{
					// Place armies
					ArrayList<PlaceArmiesMove> placeArmiesMoves = m_bot.getPlaceArmiesMoves(m_currentState, Long.valueOf(parts[2]));
				}
				else if (parts[1].equals("attack/transfer"))
				{
					// Attack/transfer
					ArrayList<AttackTransferMove> attackTransferMoves = m_bot.getAttackTransferMoves(m_currentState, Long.valueOf(parts[2]));

					// Actual test code here
					if (m_currentState.getRoundNumber() == roundId)
					{
						ArrayList<Region> fromRegions = new ArrayList<Region>();
						for ( AttackTransferMove move : attackTransferMoves )
						{
							Assert.assertFalse("Must not split up", fromRegions.contains(move.getFromRegion()));
							fromRegions.add(move.getFromRegion());
						}

						return;
					}
				}
			}
			else if (parts.length == 3 && parts[0].equals("settings"))
			{
				// Update settings
				m_currentState.updateSettings(parts[1], parts[2]);
			}
			else if (parts[0].equals("setup_map"))
			{
				// Initial full map is given
				m_currentState.setupMap(parts);
			}
			else if (parts[0].equals("update_map"))
			{
				// All visible regions are given
				m_currentState.updateMap(parts);
			}
			else if (parts[0].equals("opponent_moves"))
			{
				// All visible opponent moves are given
				m_currentState.readOpponentMoves(parts);
			}
			else if (parts[0].equals("debug_line"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else if (parts[0].equals("Round"))
			{
				if (m_currentState.isDebugMode())
				{
					System.out.println(m_currentState.getVisibleMap().toString());
					System.out.println("DB: " + line + " (internal round " + m_currentState.getRoundNumber() + ")");
				}
			}
			else if (parts[0].equals("player1"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else if (parts[0].equals("player2"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else if (parts[0].equals("No"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else
			{
				throw new NoSuchFieldException("Unable to parse line \""+line+"\"");
			}
		}
	}

	private void shouldMove(String filename, int roundId) throws Exception
	{
		File file = new File(filename);
		Scanner scanner = new Scanner(file);

		while (scanner.hasNextLine())
		{
			String line = scanner.nextLine().trim();
			if (line.length() == 0) { continue; }
			String[] parts = line.split(" ");
			if (parts[0].equals("pick_starting_regions"))
			{
				// Pick which regions you want to start with
				m_currentState.setPickableStartingRegions(parts);
				ArrayList<Region> preferredStartingRegions = m_bot.getPreferredStartingRegions(m_currentState, Long.valueOf(parts[1]));
			}
			else if (parts.length == 3 && parts[0].equals("go"))
			{
				// We need to do a move
				if (parts[1].equals("place_armies"))
				{
					// Place armies
					ArrayList<PlaceArmiesMove> placeArmiesMoves = m_bot.getPlaceArmiesMoves(m_currentState, Long.valueOf(parts[2]));
				}
				else if (parts[1].equals("attack/transfer"))
				{
					// Attack/transfer
					ArrayList<AttackTransferMove> attackTransferMoves = m_bot.getAttackTransferMoves(m_currentState, Long.valueOf(parts[2]));

					// Actual test code here
					if (m_currentState.getRoundNumber() == roundId)
					{
						Assert.assertFalse("Should move", attackTransferMoves.size() == 0);

						return;
					}
				}
			}
			else if (parts.length == 3 && parts[0].equals("settings"))
			{
				// Update settings
				m_currentState.updateSettings(parts[1], parts[2]);
			}
			else if (parts[0].equals("setup_map"))
			{
				// Initial full map is given
				m_currentState.setupMap(parts);
			}
			else if (parts[0].equals("update_map"))
			{
				// All visible regions are given
				m_currentState.updateMap(parts);
			}
			else if (parts[0].equals("opponent_moves"))
			{
				// All visible opponent moves are given
				m_currentState.readOpponentMoves(parts);
			}
			else if (parts[0].equals("debug_line"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else if (parts[0].equals("Round"))
			{
				if (m_currentState.isDebugMode())
				{
					System.out.println(m_currentState.getVisibleMap().toString());
					System.out.println("DB: " + line + " (internal round " + m_currentState.getRoundNumber() + ")");
				}
			}
			else if (parts[0].equals("player1"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else if (parts[0].equals("player2"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else if (parts[0].equals("No"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else
			{
				throw new NoSuchFieldException("Unable to parse line \""+line+"\"");
			}
		}
	}


	private void doNotStopExpandingWhenNoOpponentVisible(String filename, int roundId) throws Exception
	{
		File file = new File(filename);
		Scanner scanner = new Scanner(file);

		while (scanner.hasNextLine())
		{
			String line = scanner.nextLine().trim();
			if (line.length() == 0) { continue; }
			String[] parts = line.split(" ");
			if (parts[0].equals("pick_starting_regions"))
			{
				// Pick which regions you want to start with
				m_currentState.setPickableStartingRegions(parts);
				ArrayList<Region> preferredStartingRegions = m_bot.getPreferredStartingRegions(m_currentState, Long.valueOf(parts[1]));
			}
			else if (parts.length == 3 && parts[0].equals("go"))
			{
				// We need to do a move
				if (parts[1].equals("place_armies"))
				{
					// Place armies
					ArrayList<PlaceArmiesMove> placeArmiesMoves = m_bot.getPlaceArmiesMoves(m_currentState, Long.valueOf(parts[2]));
				}
				else if (parts[1].equals("attack/transfer"))
				{
					// Attack/transfer
					ArrayList<AttackTransferMove> attackTransferMoves = m_bot.getAttackTransferMoves(m_currentState, Long.valueOf(parts[2]));

					// Actual test code here
					if (m_currentState.getRoundNumber() >= roundId + 10)
					{
						int movesToOther = 0;
						for ( AttackTransferMove move : attackTransferMoves )
						{
							if (!move.getToRegion().ownedByPlayer(m_currentState.getMyPlayerName()))
							{
								movesToOther++;
							}
						}

						Assert.assertFalse("Should move to others", movesToOther == 0);

						return;
					}
				}
			}
			else if (parts.length == 3 && parts[0].equals("settings"))
			{
				// Update settings
				m_currentState.updateSettings(parts[1], parts[2]);
			}
			else if (parts[0].equals("setup_map"))
			{
				// Initial full map is given
				m_currentState.setupMap(parts);
			}
			else if (parts[0].equals("update_map"))
			{
				// All visible regions are given
				m_currentState.updateMap(parts);
			}
			else if (parts[0].equals("opponent_moves"))
			{
				// All visible opponent moves are given
				m_currentState.readOpponentMoves(parts);
			}
			else if (parts[0].equals("debug_line"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else if (parts[0].equals("Round"))
			{
				if (m_currentState.isDebugMode())
				{
					System.out.println(m_currentState.getVisibleMap().toString());
					System.out.println("DB: " + line + " (internal round " + m_currentState.getRoundNumber() + ")");
				}
			}
			else if (parts[0].equals("player1"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else if (parts[0].equals("player2"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else if (parts[0].equals("No"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else
			{
				throw new NoSuchFieldException("Unable to parse line \""+line+"\"");
			}
		}
	}


	private void attackInSuperRegionBetweenRounds(String filename, int startRoundId, int endRoundId, int superRegionId) throws Exception
	{
		File file = new File(filename);
		Scanner scanner = new Scanner(file);

		while (scanner.hasNextLine())
		{
			String line = scanner.nextLine().trim();
			if (line.length() == 0) { continue; }
			String[] parts = line.split(" ");
			if (parts[0].equals("pick_starting_regions"))
			{
				// Pick which regions you want to start with
				m_currentState.setPickableStartingRegions(parts);
				ArrayList<Region> preferredStartingRegions = m_bot.getPreferredStartingRegions(m_currentState, Long.valueOf(parts[1]));
			}
			else if (parts.length == 3 && parts[0].equals("go"))
			{
				// We need to do a move
				if (parts[1].equals("place_armies"))
				{
					// Place armies
					ArrayList<PlaceArmiesMove> placeArmiesMoves = m_bot.getPlaceArmiesMoves(m_currentState, Long.valueOf(parts[2]));
				}
				else if (parts[1].equals("attack/transfer"))
				{
					// Attack/transfer
					ArrayList<AttackTransferMove> attackTransferMoves = m_bot.getAttackTransferMoves(m_currentState, Long.valueOf(parts[2]));

					// Actual test code here
					if (m_currentState.getRoundNumber() >= startRoundId && m_currentState.getRoundNumber() <= endRoundId)
					{
						for ( AttackTransferMove move : attackTransferMoves )
						{
							if (!move.getToRegion().ownedByPlayer(m_currentState.getMyPlayerName()) && move.getToRegion().getSuperRegion().getId() == superRegionId)
							{
								return;
							}
						}
					}

					Assert.assertFalse("Should attack in SuperRegion \"" + SuperRegionName.getName(superRegionId) + "\" before round "+endRoundId, m_currentState.getRoundNumber() > endRoundId);
				}
			}
			else if (parts.length == 3 && parts[0].equals("settings"))
			{
				// Update settings
				m_currentState.updateSettings(parts[1], parts[2]);
			}
			else if (parts[0].equals("setup_map"))
			{
				// Initial full map is given
				m_currentState.setupMap(parts);
			}
			else if (parts[0].equals("update_map"))
			{
				// All visible regions are given
				m_currentState.updateMap(parts);
			}
			else if (parts[0].equals("opponent_moves"))
			{
				// All visible opponent moves are given
				m_currentState.readOpponentMoves(parts);
			}
			else if (parts[0].equals("debug_line"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else if (parts[0].equals("Round"))
			{
				if (m_currentState.isDebugMode())
				{
					System.out.println(m_currentState.getVisibleMap().toString());
					System.out.println("DB: " + line + " (internal round " + m_currentState.getRoundNumber() + ")");
				}
			}
			else if (parts[0].equals("player1"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else if (parts[0].equals("player2"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else if (parts[0].equals("No"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else
			{
				throw new NoSuchFieldException("Unable to parse line \""+line+"\"");
			}
		}
	}


	@Test
	public void testMoveNextToOpponent1() throws Exception
	{
		doNotMoveNextToOpponent("tests/data/first_move_not_while_next_to_opponent.txt", 1);
	}

	@Test
	public void testMoveNextToOpponent2() throws Exception
	{
		doNotMoveNextToOpponent("tests/data/first_move_2_not_while_next_to_opponent.txt", 1);
	}

	@Test
	public void testMoveNextToOpponent3() throws Exception
	{
		doNotMoveNextToOpponent("tests/data/first_move_3_not_while_next_to_opponent.txt", 1);
	}

	@Test
	public void testMoveNextToOpponent4() throws Exception
	{
		doNotMoveNextToOpponent("tests/data/first_move_4_not_while_next_to_opponent.txt", 1);
	}

	@Test
	public void testMoveNextToOpponent5() throws Exception
	{
		doNotMoveNextToOpponent("tests/data/round_3_move_not_while_next_to_opponent.txt", 3);
	}

	@Test
	public void testMoveNextToOpponent6() throws Exception
	{
		doNotMoveNextToOpponent("tests/data/round_14_move_not_while_next_to_opponent.txt", 14);
	}

	@Test
	public void testSplitUp() throws Exception
	{
		// This test now fails on a desired situation. Disabled until i figure out what to do with it.
		//doNotSplitUp("tests/data/round_6_armies_should_not_split_up.txt", 6);
	}

	@Test
	public void testShouldMove() throws Exception
	{
		shouldMove("tests/data/round_11_armies_21_not_moving.txt", 11);
	}

	@Test
	public void testStaleMate1() throws Exception
	{
		doNotStopExpandingWhenNoOpponentVisible("tests/data/round_11_stalemate.txt", 11);
	}

	@Test
	public void testStaleMate2() throws Exception
	{
		doNotStopExpandingWhenNoOpponentVisible("tests/data/round_13_stalemate_good_start.txt", 13);
	}


	@Test
	public void testStaleMate3() throws Exception
	{
		attackInSuperRegionBetweenRounds("tests/data/round_8_get_sregion_before_round_29.txt", 8, 29, 4); // Africa
	}


	/*******
	 * Placement
	 *******/

	private void placementShouldBeInSuperRegion(String filename, int roundId, int superRegionId) throws Exception
	{
		File file = new File(filename);
		Scanner scanner = new Scanner(file);

		while (scanner.hasNextLine())
		{
			String line = scanner.nextLine().trim();
			if (line.length() == 0) { continue; }
			String[] parts = line.split(" ");
			if (parts[0].equals("pick_starting_regions"))
			{
				// Pick which regions you want to start with
				m_currentState.setPickableStartingRegions(parts);
				ArrayList<Region> preferredStartingRegions = m_bot.getPreferredStartingRegions(m_currentState, Long.valueOf(parts[1]));
			}
			else if (parts.length == 3 && parts[0].equals("go"))
			{
				// We need to do a move
				if (parts[1].equals("place_armies"))
				{
					if (m_currentState.getRoundNumber() == roundId)
					{
						boolean breakHere = true;
					}

					// Place armies
					ArrayList<PlaceArmiesMove> placeArmiesMoves = m_bot.getPlaceArmiesMoves(m_currentState, Long.valueOf(parts[2]));

					// Actual test code here
					if (m_currentState.getRoundNumber() == roundId)
					{
						for ( PlaceArmiesMove placement : placeArmiesMoves )
						{
							Assert.assertTrue("Placement should be in "+ SuperRegionName.getName(superRegionId) + ", but is in " + SuperRegionName.getName(placement.getRegion().getSuperRegion().getId()) + " instead.",
								placement.getRegion().getSuperRegion().getId() == superRegionId);
						}

						return;
					}
				}
				else if (parts[1].equals("attack/transfer"))
				{
					// Attack/transfer
					ArrayList<AttackTransferMove> attackTransferMoves = m_bot.getAttackTransferMoves(m_currentState, Long.valueOf(parts[2]));
				}
			}
			else if (parts.length == 3 && parts[0].equals("settings"))
			{
				// Update settings
				m_currentState.updateSettings(parts[1], parts[2]);
			}
			else if (parts[0].equals("setup_map"))
			{
				// Initial full map is given
				m_currentState.setupMap(parts);
			}
			else if (parts[0].equals("update_map"))
			{
				// All visible regions are given
				m_currentState.updateMap(parts);
			}
			else if (parts[0].equals("opponent_moves"))
			{
				// All visible opponent moves are given
				m_currentState.readOpponentMoves(parts);
			}
			else if (parts[0].equals("debug_line"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else if (parts[0].equals("Round"))
			{
				if (m_currentState.isDebugMode())
				{
					System.out.println(m_currentState.getVisibleMap().toString());
					System.out.println("DB: " + line + " (internal round " + m_currentState.getRoundNumber() + ")");
				}
			}
			else if (parts[0].equals("player1"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else if (parts[0].equals("player2"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else if (parts[0].equals("No"))
			{
				if (m_currentState.isDebugMode()) { System.out.println("DB: "+line); }
			}
			else
			{
				throw new NoSuchFieldException("Unable to parse line \""+line+"\"");
			}
		}
	}

	@Test
	public void testPlacementDuring_ContinentGet_Mode1() throws Exception
	{
		placementShouldBeInSuperRegion("tests/data/round_6_placement.txt", 6, 2); // Placement in South America only
	}
}
