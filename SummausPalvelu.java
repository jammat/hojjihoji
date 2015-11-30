import java.net.*;
import java.io.*;


public class SummausPalvelu extends Thread{
	//Summauspalvelun attribuutit
	private int laskuri;
	private boolean yhteydenTila;
	private ServerSocket serveriSoketti;
	private Socket soketti;
	private sph hallinta;
	
	public SummausPalvelu(int laskuri, int portti, sph hallinta) throws IOException {
		//Summauspalvelun konstruktorit
		super();
		this.laskuri = laskuri;
		this.hallinta = hallinta;
		yhteydenTila = false;
		serveriSoketti = new ServerSocket(portti);
		soketti = null;
	}
	
	public void run() {
		//summauspalvelun runko
		try {
			soketti = serveriSoketti.accept();
			yhteydenTila = true;
			InputStream inputS = soketti.getInputStream();
			ObjectInputStream objectIn = new ObjectInputStream((inputS));
			while (yhteydenTila) {
				//jos oliovirrasta saatu luku on 0 lopeta while ja sulje yhteys
				if (!hallinta.lisaaLuku(laskuri, objectIn.readInt())) {				
					suljeYhteys();
					break;
				} else {
									
				}
			}
		} catch (Exception e) {
			
		}
		System.out.println(laskuri  + ". summauspalvelu suljettu!");
	}
	public void suljeYhteys() {
		//Summauspalvelun sulkemis metodi
		try {
			soketti.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		yhteydenTila = false;
	}
}
