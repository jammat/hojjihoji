import java.io.*;

        import java.net.*;

        import java.nio.charset.Charset;

        import java.util.Scanner;


/**

 * Summauspalvelu

 *

 */

public class SummausPalvelu {


    private int port;

    private InetAddress ipAddress;

    private Socket clientSocket;

    private OutputStreamWriter objectOut;

    private InputStreamReader objectIn;

    private SummausPalvelija[] servants;

    private Summaaja[] summaajat;



    /**

     * Luo uuden summauspalvelun= 3126;

     * @throws IOException

     */

    public void Summauspalvelu() throws IOException {

        ipAddress = InetAddress.getLocalHost();

        port = 3126;

        tryTCPConnection(ipAddress);   //TCP-yhteys rivi 207

        setStreams();                    // rivi 262

        clientSocket.setSoTimeout(1000000); //

        setServants(); //rivi 57

        clientSocket.setSoTimeout(5000000);

        doTests(); //rivi 114

        objectOut.close();             //suljetaan setit

        clientSocket.close();

    }


    /**

     * Luo summaajat

     */

    public void setServants(){

        int number;

        try {

            number = objectIn.read() -48; // ascii eroaa 48 pythonissa joten -48 korjaa tämän


            servants = new SummausPalvelija[number];

            summaajat = new Summaaja[number];

            objectOut.flush();

            for(int i = 0; i < number; i++){

                Summaaja s = new Summaaja();

                summaajat[i] = s;

                servants[i] = new SummausPalvelija(port + i + 1, s);

                servants[i].start();



                String newPort = String.valueOf(port + i + 1);

                objectOut.write(newPort);

                objectOut.flush();

            }

            System.out.println("Set up calculators");

        } catch(SocketTimeoutException e){

            try {

                objectOut.write(-1);

                objectOut.close();

                objectIn.close();

                clientSocket.close();

                System.exit(0);

            } catch (IOException e1) {

                e1.printStackTrace();

            }

        }catch (IOException e) {

            e.printStackTrace();

        }

    }

    /**

     * Palauttaa yhden summajaan laskemien lukujen summan

     * @param number summaaja

     * @return

     */

    public synchronized int readSum(int number)

    {

        int i = summaajat[number].getSum();

        return i;

    }

    /**

     * Palauttaa yhden summajaan laskemien lukujen määrän

     * @param number summaaja

     * @return

     */

    public synchronized int readCount(int number)

    {

        int i = summaajat[number].getCount();

        return i;

    }

    /**

     * Vastaa palvelimen tekemiin testeihin

     * @throws InterruptedException

     */

    public void doTests(){



        try {



            int num = objectIn.read()-48; //ascii python-korjaus


            System.out.println(num+  " do tests number");



            while(num > 0 && num < 4)

            {

                switch(num){ // testit lähettävät kysytyt luvut

                    case 1:

                        objectOut.write(Integer.toString(entireSum()));

                        objectOut.flush();

                        break;

                    case 2:

                        objectOut.write(Integer.toString(highestSum()));

                        objectOut.flush();

                        break;

                    case 3:

                        objectOut.write(Integer.toString(numberCount()));

                        objectOut.flush();

                        break;

                    case 0:

                        return;

                    default:

                        objectOut.write(("-1"));

                        objectOut.flush();

                        break;

                }

                num = objectIn.read();

            }



        }

        catch(SocketTimeoutException e)

        {

            System.out.println("Did not recieve tests");

            return;

        } catch (IOException e) {

            System.err.println("Ei toimi");

            e.printStackTrace();

        }



    }

    /**

     * Palauttaa kaikkien summien summan

     */

    public int entireSum()

    {

        int sum = 0;

        for(int i = 0; i < servants.length; i++ )

        {

            sum += readSum(i);

        }

        System.out.println("Answer to sum query: " + sum);

        return sum;

    }

    /**

     * Palauttaa summaajan, jolla on suurin summa

     */

    public int highestSum()

    {

        int max = readSum(0);

        int num = 1;

        for(int i = 1; i < servants.length; i++ )

        {

            if(readSum(i) > max)

            {

                max = readSum(i);

                num = i + 1;

            }

        }

        System.out.println("Answer to highest sum query: " + num);

        return num;

    }

    /**

     * Palauttaa lähetettyjen lukujen lukumäärän

     */

    public int numberCount()

    {

        int sum = 0;

        for(int i = 0; i < servants.length; i++ )

        {

            sum += readCount(i);

        }

        System.out.println("Answer to count query: " + sum);

        return sum;

    }

    /**

     * Yrittää luoda TCP-yhteyden IP-osoitteeseen

     * @param i ip-osoite, johon yhteys yritetään luoda

     */

    public void tryTCPConnection(InetAddress i)

    {

        int connectionFails = 0;

        try{

            ServerSocket serverSocket = new ServerSocket(port);

            while(connectionFails < 5) // viisi yritystä

            {

                try {

                    sendUDPPacket(i);



                    serverSocket.setSoTimeout(5000);



                    Socket socket = serverSocket.accept();



                    clientSocket = socket;


                    System.out.println("TCP-connection set up");



                    return;

                }catch(SocketTimeoutException e)

                {


                    connectionFails++;


                }

            }

        } catch (IOException e) {

            e.printStackTrace();

        }

        System.out.println("Connection failed");

        System.exit(0);

    }

    /**

     * Lähettää UDP-paketin IP-osoitteeseen yhteyden muodostusta varten

     *  * @param i IP-osoite, johon paketti lähetetään

     */

    public void sendUDPPacket(InetAddress i)

    {

        String text = "" + port;

        byte[] data = text.getBytes();

        try{

            DatagramSocket socket = new DatagramSocket();

            DatagramPacket packet = new DatagramPacket(data, data.length, i, 3126 );

            socket.send(packet);

            socket.close();

            System.out.println("UDP-Packet sent");

        }

        catch(Exception e)

        {

            e.printStackTrace();

            System.exit(0);

        }

    }

    /**

     * Luo oliovirran

     */

    public void setStreams()

    {

        try {

            InputStream in = clientSocket.getInputStream();

            OutputStream out = clientSocket.getOutputStream();


//jätetään mahdollista käyttöä varten

//            BufferedReader inReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

//

//            String i = inReader.readLine();

//            System.out.println(i);


//            BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

//            outWriter.write("ok");


            objectIn = new InputStreamReader(in);

            objectOut = new OutputStreamWriter(out);

            System.out.println("Setting up object-streams");

        } catch (IOException e) {

            e.printStackTrace();

        }



    }



    /**

     * Luodaan palvelija, joka hoitaa summaamisen

     */







    /**

     * Luokka, johon summauspalvelijat voivat kerätä summaamansa luvut

     */




/**

 * Ohjelman main

 * @param args

 */




}