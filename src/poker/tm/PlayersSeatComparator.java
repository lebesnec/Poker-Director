package poker.tm;

import java.util.Comparator;

import poker.tm.model.Player;

public class PlayersSeatComparator implements Comparator<Player> {

	public int compare(Player p1, Player p2) {
		if (p1.getTable() == p2.getTable()) {
			return p1.getSeat() - p2.getSeat();
			
		} else {
			return p1.getTable() - p2.getTable();
		}
	}

}
