package bot;

import main.Region;
import move.AttackTransferMove;
import move.PlaceArmiesMove;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class BotParser
{
	private final Scanner m_scan;
	private final Bot m_bot;
	private BotState m_currentState;


	public BotParser(Bot bot)
	{
		m_scan = new Scanner(System.in);
		m_bot = bot;
		m_currentState = new BotState();
	}


	public BotParser(Bot bot, String fileName )
	{
		File file = new File(fileName);
		Scanner scanner;
		try
		{
			scanner = new Scanner(file);
		}
		catch(Exception e)
		{
			scanner = new Scanner(System.in);
		}
		m_scan = scanner;
		m_bot = bot;
		m_currentState = new BotState();
		m_currentState.setDebugMode();
	}


	public void run()
	{
		while (m_scan.hasNextLine())
		{
			String line = m_scan.nextLine().trim();
			if (line.length() == 0) { continue; }
			String[] parts = line.split(" ");
			// Warlight2 function.
			if (parts[0].equals("pick_starting_region"))
			{
				// Note: the starting regions are set before this happens
				// In Warlight2, this will be called repeatedly between bots until there are no more starting regions left.
				m_currentState.setPickableStartingRegions(parts);
				// Pick which regions you want to start with.
				ArrayList<Region> preferredStartingRegions = m_bot.getPreferredStartingRegions(m_currentState, Long.valueOf(parts[1]));
				// Output only one region ID
				if (preferredStartingRegions.size() > 0)
				{
					System.out.println("" + preferredStartingRegions.get(0).getId());
				}
				else
				{
					// No regions? just output 0
					System.out.println("0");
				}
			}
			// Warlight1 function. Warlight2 does not use this anymore
			else if (parts[0].equals("pick_starting_regions"))
			{
				// Note: the starting regions are set before this happens
				// In Warlight2, this will be called repeatedly between bots until there are no more starting regions left.
				// parts[1] is a timeout value
				m_currentState.setPickableStartingRegions(parts);
				// Pick which regions you want to start with.
				ArrayList<Region> preferredStartingRegions = m_bot.getPreferredStartingRegions(m_currentState, Long.valueOf(parts[1]));
				String output = "";
				for (Region region : preferredStartingRegions)
				{
					output = output.concat(region.getId() + " ");
				}

				System.out.println(output);
			}
			else if (parts.length == 3 && parts[0].equals("go"))
			{
				// We need to do a move
				String output = "";
				if (parts[1].equals("place_armies"))
				{
					// Place armies
					ArrayList<PlaceArmiesMove> placeArmiesMoves = m_bot.getPlaceArmiesMoves(m_currentState, Long.valueOf(parts[2]));
					for (PlaceArmiesMove move : placeArmiesMoves)
					{
						if (output.length() > 0) { output += ","; }
						output += move.getString();
					}
				}
				else if (parts[1].equals("attack/transfer"))
				{
					// Attack/transfer
					ArrayList<AttackTransferMove> attackTransferMoves = m_bot.getAttackTransferMoves(m_currentState, Long.valueOf(parts[2]));
					for (AttackTransferMove move : attackTransferMoves)
					{
						if (output.length() > 0) { output += ","; }
						output += move.getString();
					}
				}
				if (output.length() > 0)	{ System.out.println(output); }
				else						{ System.out.println("No moves"); }
			}
			else if (parts.length == 3 && parts[0].equals("settings"))
			{
				// Update settings
				m_currentState.updateSettings(parts[1], parts[2]);
			}
			else if (parts.length > 2 && parts[0].equals("settings"))
			{
				// Update settings
				m_currentState.updateSettings(parts[1], parts);
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
			else if (parts[0].equals("Output"))
			{
				if (m_currentState.isDebugMode())
				{
					System.out.println("DB: "+line);
				}
			}
			// Debug stuff
			else if (parts[0].equals("debug_line") && m_currentState.isDebugMode())
			{
				System.out.println("DB: "+line);
			}
			else if (parts[0].equals("Round") && m_currentState.isDebugMode())
			{
				System.out.println(m_currentState.getVisibleMap().toString());

				System.out.println("DB: " + line + " (internal round " + m_currentState.getRoundNumber() + ")");
			}
			else if (parts[0].equals("player1") && m_currentState.isDebugMode())
			{
				System.out.println("DB: "+line);
			}
			else if (parts[0].equals("player2") && m_currentState.isDebugMode())
			{
				System.out.println("DB: "+line);
			}
			else if (parts[0].equals("No") && m_currentState.isDebugMode())
			{
				System.out.println("DB: "+line);
			}
			else
			{
				System.err.printf("Unable to parse line \"%s\"\n", line);
			}
		}
	}
}
