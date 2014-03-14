package bot;

import main.Region;
import move.AttackTransferMove;
import move.PlaceArmiesMove;

import java.util.ArrayList;

public interface Bot
{
	public ArrayList<Region> getPreferredStartingRegions(BotState state, Long timeOut);
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut);
	public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut);
}
