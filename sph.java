import java.io.*;
import java.net.*;
import java.util.*;

public class sph {
	//sph toimii sovelluksen X hallintasäikeenä
	
	private SummausPalvelu[]  summaukset;
	private ArrayList<ArrayList<Integer>> kokonaisLuvut;
	ServerSocket serveriSoketti;
	
	public void kaynnista() {
		//Luodaan soketti sovelluksen X porttiin 2000 joka ottaa yhteyden palvelimen Y porttiin 3126
		try {
			Socket soketti = luoYhteys(2000, "localhost", 3126);
			//luodaan oliovirrat
			InputStream inputS = soketti.getInputStream();
			OutputStream outputS = soketti.getOutputStream();
			ObjectInputStream objectIn = new ObjectInputStream(inputS);
			ObjectOutputStream objectOut = new ObjectOutputStream(outputS);
			int lukumaara = 0;
			//kokonaisluku keskustelu X:n ja Y:n välillä
			try {
				soketti.setSoTimeout(5000);
				lukumaara = objectIn.readInt();
			} catch (SocketTimeoutException e1) {
				objectOut.writeInt(-1);
				System.exit(0);
			}
			//luodaan uudet summauspalvelu taulukot
			summaukset = new SummausPalvelu[lukumaara];
			kokonaisLuvut = new ArrayList<ArrayList<Integer>>(lukumaara);
			
			//luodaan summauspalvelut
			for (int i = 0; i < lukumaara; i++) {
				summaukset[i] = new SummausPalvelu(i, 2001+i, this);
				kokonaisLuvut.add(new ArrayList<Integer>());
			}
			//käynnistetään summauspalvelijat
			for (int i = 0; i < lukumaara; i++) {
				summaukset[i].start();
			}
			//lähetetään portit palvelimelle Y
			for (int i = 0; i < lukumaara; i++) {
				objectOut.writeInt(2001+i);
				objectOut.flush();
			}
			long o = System.currentTimeMillis();
			while (true) {
				//Looppi, jossa X odottaa Y:n kyselyitä ja vastaa niihin Y:n haluamalla tavalla
				int input = objectIn.readInt();
				if (input == 1) {
					//Y:n ensimmäinen kysely
					annaSummauspalvelijoilleAikaa();
					objectOut.writeInt(annaSumma());
					System.out.println("kokonaissumma = " + annaSumma());
					o = System.currentTimeMillis();
				} else if (input == 2) {
					//Y:n toinen kysely
					annaSummauspalvelijoilleAikaa();
					objectOut.writeInt(suurimmanSummanPalvelu());
					System.out.println("suurimman summan palvelu = " + suurimmanSummanPalvelu());
					o = System.currentTimeMillis();
				} else if (input == 3) {
					//Y:n kolmas kysely
					annaSummauspalvelijoilleAikaa();
					System.out.println("lähetettyjen lukujen kokonaismaara = " + lukujenMaara());
					objectOut.writeInt(lukujenMaara());
					o = System.currentTimeMillis();
				}  
				else if (input == 0) {
					//sulkeminen jos saa arvon nolla
					System.out.println("Suljetaan yhteydet");
					//suljetaan yhteys sekä jäljellä olevat summauspalvelut
					soketti.close();
					suljeSummauspalvelut();
					break;
				}  
				if(System.currentTimeMillis() - o >= 60000){
					//sulkeminen jos kuluu yli minuutti aikaa
					System.out.println("Suljetaan yhteydet");
					//suljetaan yhteys sekä jäljellä olevat summauspalvelut
					soketti.close();
					suljeSummauspalvelut();
					break;
					
				}
				
				
				objectOut.flush();
			}
			for(int i = 0; i < summaukset.length; i++) {
				//tarkistus pyörimään jääneista summauspalveluista
				try {
					summaukset[i].join();
				} catch (InterruptedException e) {
	
					e.printStackTrace();
				}
			}
			System.out.println("Suljetaan sovellus");
			System.exit(0);
		} catch (IOException ioe) {
			System.err.print(ioe.toString());
		}
	}
	
	private Socket luoYhteys(int omaPortti, String osoite, int portti) throws IOException {
		//metodi yhteyden luomiselle
		Socket soketti = null;
		serveriSoketti = new ServerSocket(omaPortti);//luodaan serveri soketti joka odottaa palvelimen Y vastausta
		int i = 0;
		for (; i < 5; i++) {
			try {
				
				serveriSoketti.setSoTimeout(5000);
				//lähetetään UDP-paketti
				pakettiLahetys(osoite, portti, Integer.toString(omaPortti));
				System.out.println(i+1 + ". yritys");
				//saadaan palvelimelta Y tiedot tyhjään sokettiin 
				soketti = serveriSoketti.accept();
				break;
			} catch (SocketTimeoutException ste) {
				if (i == 4) {
					//viidennen epäonnistuneen lähetyskerran jälkeen suljetaan sovellus
					System.out.println("Yhteytta ei saatu.");
					System.exit(0);
				}
			}
		}
		return soketti;
	}
	public void pakettiLahetys(String a, int portti, String viesti) throws IOException, SocketException {
		//metodi, jolla lähetettän UDP-paketti joka sisältää soketin tiedot
		InetAddress osoite = InetAddress.getByName(a);//haetaan palvelimen Y ip-osoite
		DatagramSocket soketti  = new DatagramSocket();//luodaan DatagramSoketti
		byte[] sisalto = viesti.getBytes();//tallennetaan portti 2000 tavuina taulukkoon sisalto
		DatagramPacket paketti = new DatagramPacket(sisalto, sisalto.length, osoite, portti);//luodaan Datagram paketti
		soketti.send(paketti);//lähetetään luotu paketti
		soketti.close();//suljetaan DatagramSoketti
	}
	private void annaSummauspalvelijoilleAikaa() {
		//Annetaan suoritusaika summauspalveluille
		for(int i = 0; i < summaukset.length; i++) {
			try {
				summaukset[i].join(0, 1);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
	}
	
	private void suljeSummauspalvelut() {
		//metodi jolla suljetaan kaikki summauspalvelut
		for (int i = 0; i < summaukset.length; i++) {
			summaukset[i].suljeYhteys();
		}
	}
	
	private int lukujenMaara() {
		//palauttaa välitettyjen lukujen kokonaismäärän
		int lukumaara = 0;
		for (int i = 0; i < annaSaadutLuvut().size(); i++) {
			lukumaara = lukumaara + annaSaadutLuvut().get(i).size();
		}
		return lukumaara;
	}
	
	private int suurimmanSummanPalvelu() {
		//metodi joka palauttaa suurimman summan omaavan palvelun indeksin
		int[] summat = new int[annaSaadutLuvut().size()];
		for (int i = 0; i < annaSaadutLuvut().size(); i++) {
			for (int j = 0; j < annaSaadutLuvut().get(i).size(); j++) {
				summat[i] = summat[i] + annaSaadutLuvut().get(i).get(j);
			}
		}
		int suurimmanIndeksi = 0;
		for (int i = 1; i < summat.length; i++) {
			if (summat[suurimmanIndeksi] < summat[i])
				suurimmanIndeksi = i;
		}
		return suurimmanIndeksi+1;
	}
	
	private int annaSumma() {
		//metodi välitettyjen lukujen kokonaissummalle
		int summa = 0;
		for (int i = 0; i < annaSaadutLuvut().size(); i++) {
			for (int j = 0; j < annaSaadutLuvut().get(i).size(); j++) {
				summa = annaSaadutLuvut().get(i).get(j) + summa;
			}
		}
		return summa;
	}
	public boolean lisaaLuku(int indeksi, int luku) {
		if (luku == 0)
			return false;
		annaSaadutLuvut().get(indeksi).add(luku);//lisätään kokonaisLuvut Arraylistin laskuri indeksissä olevaan Arraylistiin saatu luku
		System.out.println(indeksi + ". summauspalvelu lisasi luvun " + luku);
		return true;
	}
	
	public ArrayList<ArrayList<Integer>> annaSaadutLuvut() {
		//palauttaa Arraylistin kokonaisLuvut
		return kokonaisLuvut;
	}
}
