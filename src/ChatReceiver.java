
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Created by polises on 7/9/17.
 */

public class ChatReceiver extends BaseTCP{

    boolean connected=false;
    Map<Integer,Packet> buffer_map=new HashMap<>();
    //Packed Ids begin with 1
    int ackedPacketId=0;
    final int MAX_BUFFER=10;

    ChatReceiver(String server_name,int port){
//      Establish connection identifying the name
        super(server_name);
        String setName="NAME "+receiver_name;
        send_data(setName.getBytes(),setName.length());
    }

    void sendAckPacket(Packet pckt){
        if(!connected){
            String setCONN="CONN "+sender_name;
            send_data(setCONN.getBytes(),setCONN.length());
            connected=true;
        }
        send_data(pckt.ac_bytes,pckt.ac_size);
    }




    void write_recv(){
        byte[] msg = new byte[MAX_ACK_MSG_SIZE];
        int s=0;
        DatagramPacket recvDgram = new DatagramPacket(msg, msg.length);
        while(true) {
            try {

                udpsock.receive(recvDgram);
                Packet pct=Packet.createPacketFromActualBytes(recvDgram.getData(),recvDgram.getLength());
                if(pct.frameId<=ackedPacketId+MAX_BUFFER){
                      buffer_map.put(new Integer(pct.frameId),pct);
                      //move window to fill holes
                      while(buffer_map.containsKey(new Integer(ackedPacketId+1))){
                          ackedPacketId=ackedPacketId+1;
                          Packet p=buffer_map.remove(ackedPacketId);
                          s=s+p.raw_size;
                          System.out.write(p.raw_bytes);
                    }
                    if(buffer_map.size()!=0){LOGGER.info("Hole with last acknowledged frame"+ackedPacketId);}
//                    if(temp){temp=false;continue;}
//                    temp=true;

                    LOGGER.info("Acknowledge frame"+ackedPacketId);
                }else{
                    LOGGER.info("Acknowledge previous"+ackedPacketId);
                };
                Packet ack_pck=Packet.createACKPacketFromRaw(ackedPacketId);
                sendAckPacket(ack_pck);
            } catch (IOException e) {
                continue;
            } catch (Exception e){
                    Packet ack_pck=Packet.createACKPacketFromRaw(ackedPacketId);
                    LOGGER.info("Corrupt packet recieved for"+(ackedPacketId));
                    sendAckPacket(ack_pck);

            }

        }
    }
    public static void main(String args[]){
       BaseTCP.parse_args(args);
       ChatReceiver rcv=new ChatReceiver(server_name,port);
       rcv.write_recv();

    }
}

