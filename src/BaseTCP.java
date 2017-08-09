/**
* Created by polises on 7/9/17.
*/

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

class BaseTCP{

    static String server_name=null;

    String sender_name=null;
    String receiver_name=null;

    static int port=0;
    final int MAX_ACK_MSG_SIZE = 1024;
    final int MAX_RAW_MSG_SIZE=950;

    private final String SERVER = "localhost";
    DatagramSocket udpsock = null;
    static Logger LOGGER= Logger.getLogger(BaseTCP.class.getName());

    BaseTCP(String server_name){
        sender_name=server_name+"_sender";
        receiver_name=server_name+"_receiver";
        try {
                udpsock = new DatagramSocket();
            }catch (Exception e){
                System.out.print("Socket creation error");
            }
    }

    public static void parse_args(String args[]) {
        int i = 0;
        while (i < args.length) {
            while (i < args.length) {
                switch (args[i]) {
                    case "-s":
                        server_name = args[i + 1];
                        break;
                    case "-p":
                        port = Integer.parseInt(args[i + 1]);
                        break;
                }
                i++;
            }
        }
    }
        
    public void send_data(byte[] bytes,int length) {
        try {
                DatagramPacket spck=new DatagramPacket(bytes,length,InetAddress.getByName(SERVER),port);
                udpsock.send(spck);
            }catch(Exception e){
            System.out.println("Caught exception while sending udp datagram");
            }
        }
    }
