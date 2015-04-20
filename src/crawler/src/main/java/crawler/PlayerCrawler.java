package crawler;

import model.Player;
import model.Team;
import model.Transfer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.function.Consumer;

public class PlayerCrawler {
    private ArrayList<Consumer<Player>> onPlayerCrawledListeners = new ArrayList<>();

    public void onPlayerCrawled(Consumer<Player> listener) {
        onPlayerCrawledListeners.add(listener);
    }

    protected void emitPlayerCrawled(Player player) {
        for (Consumer<Player> listener : onPlayerCrawledListeners) {
            listener.accept(player);
        }
    }

	public void crawlAllPlayerPages(Collection<Player> players) {
        players.forEach(this::crawlPlayerPage);
	}

	public void crawlPlayerPage(Player player) {
		String uri = player.getUri();
		LinkedHashSet<Transfer> transferHistory = new LinkedHashSet<Transfer>();

		try {
			Thread.sleep(Utils.HTTP_SLEEP);
			System.err.println("Crawling player page: " + player.getName());
			Document doc = Jsoup
					.connect(uri)
					.userAgent(
							"Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0")
					.timeout(Utils.HTTP_TIMEOUT).get();

			// Get all the data by iterating through the player info
			Elements playerInfo = doc.getElementsByTag("tr");
			Iterator<Element> it = playerInfo.iterator();
			// while (it.hasNext()) {
			// Element e = it.next();
			// System.out.println(e.text());
			// System.out.println("----------------");
			// }

			// get player number
			Element e = it.next();
			if (e.text().indexOf(".") >= 0) {
				player.setNumber(Integer.parseInt(e.text().substring(0,
						e.text().indexOf("."))));
			} else {
				player.setNumber(-1);
			}
			System.out.println("Number: " + player.getNumber());
			System.out.println("Name: " + player.getName());

			// get player position
			e = it.next();
			player.setPosition(e.text().split(" ")[0]);
			System.out.println("Position: " + player.getPosition());

			// get age
			e = it.next();
			e = it.next();
			if (e.text().length() >= 4) {
				player.setAge(Integer.parseInt(e.text().substring(4,
						e.text().indexOf("(") - 1)));
			} else {
				player.setAge(-1);
			}
			System.out.println("Age: " + player.getAge());

			// get birthdate
			if (e.text().indexOf("(") >= 6) {
				player.setBirthdate(e.text().substring(
						e.text().indexOf("(") + 6, e.text().indexOf(")")));
			} else {
				player.setBirthdate("");
			}
			System.out.println("Date of Birth: " + player.getBirthdate());

			// get height
			e = it.next();
			if (e.text().indexOf("(") >= 0) {
				player.setHeight(e.text().substring(e.text().indexOf("(") + 1,
						e.text().indexOf(")")));
			} else {
				player.setHeight("");
			}
			System.out.println("Height: " + player.getHeight());

			// get weight
			e = it.next();
			if (e.text().indexOf("(") >= 0) {
				player.setWeight(e.text().substring(e.text().indexOf("(") + 1,
						e.text().indexOf(")")));
			} else {
				player.setWeight("");
			}
			System.out.println("Weight: " + player.getWeight());

			// get birthplace
			e = it.next();
			if (e.text().length() >= 15) {
				player.setBirthplace(e.text().substring(15));
			} else {
				player.setBirthplace("");
			}
			System.out.println("Birthplace: " + player.getBirthplace());

			// get nationality
			e = it.next();
			if (e.text().length() >= 12) {
				player.setNationality(e.text().substring(12));
			} else {
				player.setNationality("");
			}
			System.out.println("Nationality: " + player.getNationality());

			// get date signed
			e = it.next();
			if (e.text().length() >= 12) {
				player.setDateSigned(e.text().substring(12));
			} else {
				player.setDateSigned("");
			}
			System.out.println("Date Signed: " + player.getDateSigned());

			// get fee
			e = it.next();
			if (e.text().length() >= 4) {
				player.setFee(e.text().substring(4));
			} else {
				player.setFee("");
			}
			System.out.println("Fee: " + player.getFee());

			// Get transfer history
			e = it.next();
			e = it.next();
			e = it.next();
			System.out.println("Transfer History of " + player.getName());
			do {
				Transfer transfer = new Transfer();

				// get team
				Elements links = e.select("a[href*=/teams/team.sd?team_id]");
				for (Element link : links) {
					transfer.setTeam(new Team(link.attr("abs:href"), Utils
							.trim(link.text(), 35)));
				}
				System.out.println("\tTeam: " + transfer.getTeam().getName());

				// get date from
				String from = Utils.trim(
						e.text().substring(e.text().indexOf(",") - 6), 11);
				System.out.println("\tDate Joined: " + from);
				transfer.setFrom(from);

				// get date to (empty if actual club)
				String to = Utils.trim(
						e.text().substring(e.text().lastIndexOf(", ") - 6), 11);
				if (to.equals(from)) {
					transfer.setTo("");
				} else {
					transfer.setTo(to);
				}
				System.out.println("\tDate Left: " + transfer.getTo());

				if (e.text().indexOf("Loan") >= 0) {
					transfer.setFee("Loan");
					System.out.println("\tFee: Loan");
				} else {
					transfer.setFee("Signed");
					System.out.println("\tFee: Signed");
				}
				System.out.println("---");
				transferHistory.add(transfer);

				e = it.next();

			} while (!e.text().startsWith("Totals"));

			player.setTransferHistory(transferHistory);

            emitPlayerCrawled(player);

        } catch (IOException e) {
            System.err.println("TournamentCrawler failed");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Sleep failed");
            e.printStackTrace();
        }
    }

	public static void main(String[] args) {
		PlayerCrawler pc = new PlayerCrawler();
		// Player p = new Player(
		// "http://www.soccerbase.com/players/player.sd?player_id=46629",
		// "Brad Guzan");
		Player p = new Player(
				"http://www.soccerbase.com/players/player.sd?player_id=39937",
				"Gerard Pique");
		pc.crawlPlayerPage(p);
	}
}