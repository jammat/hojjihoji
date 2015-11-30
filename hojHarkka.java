import java.io.*;
import java.net.*;

public class hojHarkka {
//Ohjelman käynnistys runko 
	public static void main(String[] args) throws SocketException, IOException {
		//luo uuden hallintaluokan
		sph ha = new sph();
		//käynnistää hallintaluokan
		ha.kaynnista();
	}

}
