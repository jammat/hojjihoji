import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


public class SummausPalvelija extends Thread{

       

        private int port;

        private InputStreamReader in;

        Summaaja sum;

       

        /**

        * Luo uuden summauspalvelijan

        * @param port Portti johon otetaan yhteyttä

        * @param s mihin tähän summauspalvelija laittaa luvut

        */

        public SummausPalvelija(int port, Summaaja s) throws IOException

        {

            this.port = port;

            this.sum = s;

        }

       

        /**

        * Aloittaa lukujen vastaanottamisen ja summaamisen

        */

        public void run()

        {

            //Alustaa yhteyden palvelimeen

            ServerSocket serverSocket;

            try {

                serverSocket = new ServerSocket(port);


                Socket s = serverSocket.accept();

                System.out.println("socket accepted connection in port " + s.getLocalPort() );

                s.setSoTimeout(100000);

                InputStream input = s.getInputStream();

                in = new InputStreamReader(input);

            } catch (IOException e1) {

                e1.printStackTrace();

            }

            //Aloittaa summaamisen

            int num = -1;

            while(num != 0){

                try {

                    synchronized(sum){

                        while(in.ready()){

                            num = in.read();

                            num = num -48; //ascii python-korjaus

                           

                            if(num == 0)

                                break;

                            else

                                sum.addNumber(num);   

                        }

                    }

                }catch (EOFException e) {

                    System.exit(0);

                }

                catch (IOException e) {

                    e.printStackTrace();

                }   

            }

           

            System.out.println("Thread closing in ");

            try {

                in.close();//Sulkee oliovirran

            } catch (IOException e) {

                e.printStackTrace();

            }

        }
}
