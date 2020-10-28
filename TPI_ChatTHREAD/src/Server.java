import java.net.*;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) throws Exception {

        int porta = 2000;
        DatagramSocket portaAscolto = new DatagramSocket(porta);
        ArrayList<Client> listaUtenti = new ArrayList<>();
        boolean c=true;

        while (c) {
            byte[] buffer = new byte[1024]; //lunghezza buffer
            DatagramPacket packetRice = new DatagramPacket(buffer, buffer.length);
            portaAscolto.receive(packetRice); //riceve il pacchetto
            Thread t = new Thread(new SimpleRunner(packetRice,portaAscolto,listaUtenti));
            t.start();
        }
    }
}
