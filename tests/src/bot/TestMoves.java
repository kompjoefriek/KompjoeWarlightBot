package bot;

import junit.framework.Assert;
import main.Region;
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
		m_bot = new KompjoeWarlightBot();
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

							Assert.assertTrue("Must not move when next to an opponent", nextToEnemy && !move.getToRegion().ownedByPlayer(m_currentState.getOpponentPlayerName()));
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
							Assert.assertTrue("Must not split up", fromRegions.contains(move.getFromRegion()));
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
						Assert.assertTrue("Should move", attackTransferMoves.size() == 0);

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
		doNotSplitUp("tests/data/round_6_armies_should_not_split_up.txt", 6);
	}

	@Test
	public void testShouldMove() throws Exception
	{
		shouldMove("tests/data/round_11_armies_21_not_moving.txt", 11);
	}
}
