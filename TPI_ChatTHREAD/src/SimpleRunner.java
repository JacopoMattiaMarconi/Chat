import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SimpleRunner implements Runnable {

    public DatagramPacket packetRice;
    public DatagramSocket portaAscolto;
    public ArrayList<Client> listaUtenti;
    public ArrayList<String> listauser=new ArrayList<>();

    public SimpleRunner(DatagramPacket packetRice, DatagramSocket portaAscolto, ArrayList<Client> listaUtenti) throws IOException {
        this.packetRice=packetRice;
        this.portaAscolto=portaAscolto;
        this.listaUtenti=listaUtenti;
    }

    public void log (String tolog) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("FILE LOG.txt",true));
        bw.write(tolog+"\n");
        bw.close();
    }

    public void run(){
        DatagramPacket packetSend;
        SimpleDateFormat formato = new SimpleDateFormat("dd,MM,yyyy HH:mm:ss");
        Date data = new Date();
        String dataora = formato.format(data);

        byte[] temp =packetRice.getData(); //ARRAY TEMP CONTIENE I BYTE DATI

        byte opcode=temp[0]; //OPCODE --> 1 BYTE
        int len=temp[1]*256+temp[2]; //LEN --> 2 BYTE

        InetAddress ip=packetRice.getAddress(); //IP
        int port=packetRice.getPort(); //PORTA
        int cont;
            switch (opcode){
                case 11: //PACCHETTO LOGIN + USERNAME
                    byte[] user=new byte[len];
                    cont=0;
                    for(int i=3; i<len+3;i++){
                        user[cont]=temp[i]; //USERMAME --> CONCATENAZIONE DI CARATTERI
                        cont++;
                    }
                    String username=new String(user);
                    if(listaUtenti.size()>0) {
                        if (username.length() > 5 && username.length() < 16 && !listauser.contains(username)) { //CONTROLLO USER CORRETTO
                            listauser.add(username);
                            Client c = new Client(username, ip, port); //AGGIUNTA USER
                            listaUtenti.add(c); //SI AGGIORNA LA LISTA UTENTI
                            System.out.println("LOGIN: "+username+", IP: "+ip);
                            try {
                                log("LOGIN: "+username+", IP: "+ip+" --> "+dataora);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            byte[] risposta = pacchettoConferma();
                                packetSend = new DatagramPacket(risposta, risposta.length, ip, port);
                                try {
                                    portaAscolto.send(packetSend);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else if (username.length() < 6 || username.length() > 15) {
                                byte[] risposta = pacchettoError1();
                                packetSend = new DatagramPacket(risposta, risposta.length, ip, port);
                                try {
                                    portaAscolto.send(packetSend);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                System.out.println(ip+": LOGIN FAILED");
                            try {
                                log(ip+": LOGIN FAILED --> "+dataora);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                                byte[] risposta = pacchettoError2();
                                packetSend = new DatagramPacket(risposta, risposta.length, ip, port);
                                try {
                                    portaAscolto.send(packetSend);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                System.out.println(ip+": LOGIN FAILED");
                                try {
                                    log(ip+": LOGIN FAILED --> "+dataora);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                        }
                    }
                    else if (username.length() > 5 && username.length() < 16) { //CONTROLLO USER CORRETTO
                        listauser.add(username);
                        Client c = new Client(username, ip, port); //AGGIUNTA USER
                        listaUtenti.add(c); //SI AGGIORNA LA LISTA UTENTI
                        System.out.println("LOGIN: "+username+", IP: "+ip);
                        try {
                            log("LOGIN: "+username+", IP: "+ip+" --> "+dataora);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        byte[] risposta = pacchettoConferma();
                        packetSend = new DatagramPacket(risposta, risposta.length, ip, port);
                        try {
                            portaAscolto.send(packetSend);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        byte[] risposta = pacchettoError1();
                        packetSend = new DatagramPacket(risposta, risposta.length, ip, port);
                        try {
                            portaAscolto.send(packetSend);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println(ip+": LOGIN FAILED");
                        try {
                            log(ip+": LOGIN FAILED --> "+dataora);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case 12: //PACCHETTO LOGOUT
                    byte[] risposta = pacchettoConferma();
                    packetSend = new DatagramPacket(risposta, risposta.length, ip, port);
                    try {
                        portaAscolto.send(packetSend);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    for (int i=0; i<listaUtenti.size(); i++) {
                        if(listaUtenti.get(i).getIp().equals(ip)){ //SE L'IP E' PRESENTE IN UN OGGETTO DELLA LISTA
                            System.out.println("LOGOUT: "+listaUtenti.get(i).getUsername()+", IP: "+ip);
                            try {
                                log("LOGOUT: "+listaUtenti.get(i).getUsername()+", IP: "+ip+" --> "+dataora);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            listaUtenti.remove(i); //AGGIORNAMENTO LISTA UTENTI
                        }
                    }
                    if(!listaUtenti.isEmpty()) {
                        byte[] lista = pacchettoListaUtenti(listaUtenti);
                        for (Client client : listaUtenti) {
                            packetSend = new DatagramPacket(lista, lista.length, client.getIp(), client.getPort()); //INVIATO A TUTTI GLI IP E LE PORTE CORRISPONDENTI
                            try {
                                portaAscolto.send(packetSend);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        System.out.println("UPDATED USERS LIST SENT TO ALL");
                        try {
                            log("UPDATED USERS LIST SENT TO ALL --> "+dataora);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case 20: //PACCHETTO PUBBLICO
                    byte[] mex=new byte[1024];
                    cont=0;
                    for(int i=3; i<len+3;i++){
                        mex[cont]=temp[i]; //MESSAGGIO --> CONCATENAZIONE DI CARATTERI
                        cont++;
                    }
                    String messaggiopubblico=new String(mex);
                    byte[] pacchettoBroadcast = pacchettoMessaggio(listaUtenti,ip,messaggiopubblico);
                    for (Client client : listaUtenti) {
                        packetSend = new DatagramPacket(pacchettoBroadcast, pacchettoBroadcast.length, client.getIp(), client.getPort()); //INVIATO A TUTTI GLI IP E LE PORTE CORRISPONDENTI
                        try {
                            portaAscolto.send(packetSend);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println(ip+" SENT :'"+messaggiopubblico+"' TO EVERYONE");
                    try {
                        log(ip+" SENT :'xxxx' TO EVERYONE --> "+dataora);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case 22: //PACCHETTO PRIVATO
                    int i=3; int lungh=0;
                    while(temp[i]!=(byte)0){ //PRENDERE LUNGHEZZA CAMPO DESTINATARIO
                        lungh++;
                        i++;
                    }
                    i++;
                    int lunghMex=0;
                    while(temp[i]!=(byte)0){ //PRENDERE LUNGHEZZA CAMPO DESTINATARIO
                        lunghMex++;
                        i++;
                    }
                    byte[] dest=new byte[lungh];
                    byte[] mexprivato=new byte[lunghMex];
                    cont=0;
                    i=3; //SALTA I PRIMI 3 BYTE --> OPCODE,LEN
                    while(temp[i]!=(byte)0){
                        dest[cont]=temp[i]; //BYTE DESTINATARIO |OPCODE|LEN|DESTINATARIO|...
                        i++; cont++;
                    }
                    String destinatario=new String(dest);
                    cont=0;
                    for(i=3+lungh+1; i<3+lungh+1+lunghMex; i++){ //SALTATO IL BYTE A 0
                        mexprivato[cont]=temp[i];
                        cont++;
                    }
                    String messaggioprivato=new String(mexprivato);
                    byte[] pacchettoPriv = pacchettoMessaggio(listaUtenti,ip,messaggioprivato);
                    for (i=0; i<listaUtenti.size(); i++) {
                        if(listaUtenti.get(i).getUsername().equals(destinatario)) {
                            packetSend = new DatagramPacket(pacchettoPriv, pacchettoPriv.length, listaUtenti.get(i).getIp(), listaUtenti.get(i).getPort()); //INVIO PACCHETTO AL DESTINATARIO RICEVUTO E ALLA SUA PORTA
                            try {
                                portaAscolto.send(packetSend);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            byte[] pacchettoConf = pacchettoConferma();
                            packetSend = new DatagramPacket(pacchettoConf, pacchettoConf.length, ip, port); //ERROR SE CLIENT NON ESISTE
                            try {
                                portaAscolto.send(packetSend);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            System.out.println(ip+" SENT :'"+messaggioprivato+"' TO "+listaUtenti.get(i).getIp());
                            try {
                                log(ip+" SENT :'xxxx' TO "+listaUtenti.get(i).getIp()+" --> "+dataora);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            byte[] pacchettoErr = pacchettoError1();
                            packetSend = new DatagramPacket(pacchettoErr, pacchettoErr.length, ip, port); //ERROR SE CLIENT NON ESISTE
                            try {
                                portaAscolto.send(packetSend);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;

                case 40: //PACCHETTO INFO
                    byte[] info = pacchettoInfo();
                    packetSend = new DatagramPacket(info, info.length, ip, port); //INVIO INFO A CHI L'HA RICHIESTO
                    try {
                        portaAscolto.send(packetSend);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("INFO SENT TO "+ip);
                    try {
                        log("INFO SENT TO "+ip+" --> "+dataora);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case 42: //PACCHETTO LISTA UTENTI
                    byte[] listaUt = pacchettoListaUtenti(listaUtenti);
                    packetSend = new DatagramPacket(listaUt, listaUt.length, ip, port); //INVIO LISTA A CHI L'HA RICHIESTO
                    try {
                        portaAscolto.send(packetSend);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("USERS LIST SENT TO "+ip);
                    try {
                        log("USERS LIST SENT TO "+ip+" --> "+dataora);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
    }

    public static byte[] pacchettoConferma() {
        return new byte[3];
    }

    public static byte[] pacchettoError1() {
        byte[] s ="ERRORE: USERNAME NON VALIDO. LUNGHEZZA ERRATA. min=6, max=15".getBytes();
        byte[] pacERR=new byte[3+s.length]; // LUNGHEZZA PACCHETTO ERRORE |1|LEN|S|

        if(s.length<255){ pacERR[0]=(byte)1; pacERR[1]=0; pacERR[2]=(byte)s.length;} //CONTROLLO ESATTEZZA BYTES LEN
        else{ pacERR[0]=(byte)1; pacERR[1]=(byte)(s.length-255); pacERR[2]=(byte)255;} // |1|LEN|...

        // ..|S|
        System.arraycopy(s, 0, pacERR, 3, s.length);
        return pacERR;
    }

    public static byte[] pacchettoError2() {
        byte[] s ="ERRORE: USERNAME NON VALIDO. USERNAME DUPLICATO.".getBytes();
        byte[] pacERR=new byte[3+s.length]; // LUNGHEZZA PACCHETTO ERRORE |1|LEN|S|

        if(s.length<255){ pacERR[0]=(byte)1; pacERR[1]=0; pacERR[2]=(byte) s.length;} //CONTROLLO ESATTEZZA BYTES LEN
        else{ pacERR[0]=(byte)1; pacERR[1]=(byte)(s.length-255); pacERR[2]=(byte) 255;} // |1|LEN|...

        // ..|S|
        System.arraycopy(s, 0, pacERR, 3, s.length);
        return pacERR;
    }

    public static byte[] pacchettoInfo() {
        SimpleDateFormat formato=new SimpleDateFormat("dd,MM,yyyy HH:mm:ss");
        Date data=new Date();
        String dataora=formato.format(data);
        byte[] s =(dataora+" VERSIONE CHAT:1.0.2020").getBytes(); //BYTE DEL MESSAGGIO
        byte[] pacInfo=new byte[3+s.length]; //LUNGHEZZA PACCHETTO INFO

        if(s.length<255){ pacInfo[0]=(byte)41; pacInfo[1]=0; pacInfo[2]=(byte)s.length;} //CONTROLLO ESATTEZZA BYTES LEN
        else{ pacInfo[0]=(byte)41; pacInfo[1]=(byte)(s.length-255); pacInfo[2]=(byte)255;}

        // ..|S|
        if (s.length - 2 >= 0) System.arraycopy(s, 0, pacInfo, 3, s.length - 2);
        return pacInfo;
    }

    public static byte[] pacchettoListaUtenti(ArrayList<Client> listaUtenti) {
        int cont=0;
        for (Client client : listaUtenti) {
            cont += client.getLunghezzaUser();
        }
        byte[] list=new byte[3+cont+listaUtenti.size()-1]; //listaUtenti.size() per gli zeri

        if(cont+listaUtenti.size()<255){ list[0]=(byte)43; list[1]=0; list[2]=(byte)(list.length-3);} //CONTROLLO ESATTEZZA BYTES LEN
        else{ list[0]=(byte)43; list[1]=(byte)((list.length-3)-255); list[2]=(byte)255;}

        int pos=3;
        for(int i=0; i<listaUtenti.size(); i++){
            if (i==listaUtenti.size()-1 || i==0) {
                for (int j = 0; j < listaUtenti.get(i).getUsername().length(); j++) {
                    list[pos] = (byte) listaUtenti.get(i).getUsername().charAt(j);
                    pos+=1;
                }
                if(i==0 && i!= listaUtenti.size()-1){
                    list[pos] = (byte) 0;
                    pos+=1;
                }
            }
            else {
                for (int j = 0; j < listaUtenti.get(i).getUsername().length(); j++) {
                    list[pos] = (byte) listaUtenti.get(i).getUsername().charAt(j);
                    pos+=1;
                }
                list[pos] = (byte) 0;
                pos+=1;
            }
        }
        return list;
    }

    public static byte[] pacchettoMessaggio(ArrayList<Client> listaUtenti, InetAddress mittente, String messaggio) {
        int cont=0;
        for (Client client : listaUtenti) {
            if (client.getIp().equals(mittente)) {
                cont = client.getLunghezzaUser(); //BYTE DI USERNAME "MITTENTE" PRESO TRAMITE LISTAUTENTI IP=IP SULLA LISTA
            }
        }
        byte[] mitt = new byte[cont];
        for (Client client : listaUtenti) {
            if (client.getIp().equals(mittente)) {
                mitt = client.getUsername().getBytes(); //BYTE DI USERNAME "MITTENTE" PRESO TRAMITE LISTAUTENTI IP=IP SULLA LISTA
            }
        }
        byte[] s=messaggio.getBytes(); //BYTE DEL MESSAGGIO
        byte[] pacMex=new byte[4+messaggio.length()+mitt.length]; //LUNGHEZZA PACCHETTO PUBBLICO

        if((messaggio.length()+mitt.length)<255){ pacMex[0]=(byte)21; pacMex[1]=0; pacMex[2]=(byte)(messaggio.length()+mitt.length);} //CONTROLLO ESATTEZZA BYTES LEN
        else{ pacMex[0]=(byte)21; pacMex[1]=(byte)((messaggio.length()+mitt.length)-255); pacMex[2]=(byte)255;}

        // ..|MITTENTE|..
        if (mitt.length >= 0) System.arraycopy(mitt, 0, pacMex, 3, mitt.length);
        pacMex[3+mitt.length]=0; //..|0|..

        for (int i=0; i<messaggio.length(); i++) {
            pacMex[i+4+mitt.length] = s[i]; // ..|MESSAGGIO|
        }
        return pacMex;
    }
}

