
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by polises on 7/9/17.
 */
public class ChatSender extends BaseTCP{

    final int WINDOW_SIZE=10;
    Semaphore available_window=new Semaphore(WINDOW_SIZE);
    Lock lock=new ReentrantLock();
    int UNackedframeId=0;
    int ackedFrame=0;

    ACKReceiver receiver;

//    Packet lasttransmitted;
    Timer retry= new Timer();
    List<Packet> transmitted=new ArrayList<>();

    void retransmit(){
        if(transmitted.size()==0)return;
        lock.lock();
        for(int i=0;i<transmitted.size();i++){
            Packet pck=transmitted.get(i);
            send_data(pck.ac_bytes,pck.ac_size);
        }
        lock.unlock();

    }

    void removeAckedPackets(int i){
        lock.lock();
        while(i>0){
            Packet pckt=transmitted.remove(0);
            LOGGER.info("Acknowledged frame"+pckt.frameId);
            i--;
            ackedFrame++;
            available_window.release();
        }
        lock.unlock();
    }

    public class ACKReceiver extends Thread {
        public void run(){
            while (true) {
                    byte[] msg = new byte[MAX_ACK_MSG_SIZE];
                    DatagramPacket recvDgram = new DatagramPacket(msg, msg.length);
                    try {
                        udpsock.receive(recvDgram);
                        int recvId=Packet.getFrameIdFromActualACKPacket(recvDgram.getData(),recvDgram.getLength());
                        if((recvId>ackedFrame) &&(recvId<=UNackedframeId)){
                            removeAckedPackets(recvId-ackedFrame);
                        }else{
                            LOGGER.info("Wierd frameId received"+recvId);
                            retransmit();
                        }
                    } catch (IOException e) {
                        continue;
                    } catch (Exception e){
                        LOGGER.info("Invalid ACK Packet recieved:"+e.getMessage());
                        continue;
                    }

            }
        }
    }





    ChatSender(String server_name,int port){
//      set my name and connect with counter part
        super(server_name);
        String setNAME="NAME "+sender_name;
        send_data(setNAME.getBytes(),setNAME.length());
        String setCONN="CONN "+receiver_name;
        send_data(setCONN.getBytes(),setCONN.length());
        receiver = new ACKReceiver();
        receiver.start();
        retry.schedule(new TimerTask() {
            @Override
            public void run() {
                retransmit();
            }
        },0,1000);
    }

    synchronized private void send_method(){

    }


    void sender(){
//      Reads data from stdin and each packet at a time and attempts to send
        byte[] buffer=new byte[MAX_RAW_MSG_SIZE];
        int read;Packet pckt;int transmit=0;
        try {
            while((read=System.in.read(buffer,0,MAX_RAW_MSG_SIZE))!=-1){
                available_window.acquire();
                lock.lock();
                UNackedframeId++;
                pckt=Packet.createPacketFromRawByte(buffer,read,UNackedframeId);
                transmitted.add(pckt);
                lock.unlock();
////
                transmit=transmit+read;
                LOGGER.info("So far transmitted "+transmit);
                send_data(pckt.ac_bytes,pckt.ac_size);
            };
//            available_window.acquire();
            while(UNackedframeId!=ackedFrame){
                Thread.sleep(1000);
            }
            LOGGER.info("Complete File send "+transmit+"bytes");
            available_window.release();
            receiver.interrupt();
        }catch (Exception ex){
            ex.printStackTrace();

        }
    }

    public static void main(String args[]) throws Exception{
        BaseTCP.parse_args(args);
        ChatSender rcv=new ChatSender(server_name,port);
        rcv.sender();
    }

}
