

import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class Packet {

        static int MaxAcPckSize=1024;
        static int MaxRawPckSize=950;
        static int PACKETCONSTANT=0x0eeeeeee;
        static int ACKCONSTANT=0x0fafafaf;

        int frameId;
        byte[] raw_bytes;
//      Bytes without packetization, checksum or headers
        byte[] ac_bytes;
//      Bytes with every wrapper, checksum etc ....
        int ac_size;
        int raw_size;
        boolean is_ack=false;

        static byte  getCheckSumForSegment(byte[] arr,int start ,int length){
           byte s=(byte)0;
           for(int i=0;i<length;i++){
               s=(byte)((int) s^(int)arr[start+i] & 0xff);
           }
           return s;
        }

        private void fillUpPacketWithStream(){
            ac_bytes=new byte[MaxAcPckSize];
            ByteBuffer wrapper=ByteBuffer.wrap(ac_bytes);
            wrapper.putInt(PACKETCONSTANT);
            wrapper.putInt(frameId);
            wrapper.putInt(raw_size);
            wrapper.put(getCheckSumForSegment(ac_bytes,0,12));

//            Load bytes into packet and add checksum
            int transferred=0;
            int start=0;int transfer=0;
            while(transferred<raw_size){
                if(raw_size-transferred<99) {
                    transfer = raw_size - transferred;
                }else{
                    transfer=99;
                }
                wrapper.put(raw_bytes,start,transfer);
//                System.out.println(transferred+": Transferred");
                wrapper.put(getCheckSumForSegment(raw_bytes,start,transfer));
                transferred=transferred+transfer;
                start=start+transfer;
            }
            assert start==raw_size;
            assert transferred==raw_size;
            ac_size=wrapper.position();
//            System.out.println("Noted array size"+ac_size+" "+raw_size);
        }

        static Packet createPacketFromRawByte(byte[] raw_bytes,int raw_size,int frameId){
            Packet pckt=new Packet();
            pckt.raw_bytes=raw_bytes;
            pckt.raw_size=raw_size;
            pckt.frameId=frameId;
            pckt.fillUpPacketWithStream();
            return pckt;

        }

        private Packet(){
            // Overridden
        }

        static Packet createPacketFromActualBytes(byte[] ac_bytes,int ac_size) throws Exception{
            if (ac_size<13)throw new Exception("Wrong packet header");
            ByteBuffer byteBuffer=ByteBuffer.wrap(ac_bytes);
            byteBuffer.wrap(ac_bytes);
            if(PACKETCONSTANT!=byteBuffer.getInt()){
                throw new Exception("Wrong packet header");
            }
            int frameId = byteBuffer.getInt();
            int raw_size = byteBuffer.getInt();
            if(byteBuffer.get()!=getCheckSumForSegment(ac_bytes,0,12)){
                throw new Exception("Payload header is corrupted");
            }
            byte[] raw_bytes=new byte[raw_size];
            int read = 0;
            while(read<raw_size){
                int transfer=raw_size-read;
                if(transfer>=99){
                    transfer=99;
                }
                byteBuffer.get(raw_bytes,read,transfer);
                byte checkSum=getCheckSumForSegment(raw_bytes,read,transfer);
                if(byteBuffer.get()!=checkSum){
                    throw new Exception("Transfer: "+transfer+" has been corrupted");
                }
                read=read+transfer;
            }
            Packet pckt=new Packet();
            pckt.raw_bytes=raw_bytes;
            pckt.raw_size=raw_size;
            pckt.ac_bytes=ac_bytes;
            pckt.ac_size=ac_size;
            pckt.frameId=frameId;
            return pckt;

        }

        static Packet createACKPacketFromRaw(int frameId){
                ByteBuffer byteBuffer=ByteBuffer.allocate(9);
                byteBuffer.putInt(ACKCONSTANT);
                byteBuffer.putInt(frameId);
                byteBuffer.put(getCheckSumForSegment(byteBuffer.array(),0,8));
                Packet pck=new Packet();
                pck.is_ack=true;
                pck.raw_bytes=null;
                pck.raw_size=0;
                pck.ac_size=9;
                pck.ac_bytes=byteBuffer.array();
                assert  pck.ac_bytes.length == 9;
                return pck;
        }

        static int getFrameIdFromActualACKPacket(byte[] ac_bytes,int ac_size) throws Exception{
            ByteBuffer byteBuffer=ByteBuffer.wrap(ac_bytes);
            if(ac_size!=9){throw new Exception("Packet of wrong size:"+ac_size);}
            if(byteBuffer.getInt()!=ACKCONSTANT){throw new Exception("Packet of wrong header");}
            int frameId=byteBuffer.getInt();
            byte checksum=byteBuffer.get();
            if(getCheckSumForSegment(ac_bytes,0,8)!=checksum){
                throw new Exception("CheckSum Does not match"+checksum+"    "+getCheckSumForSegment(ac_bytes,0,8));
            }
            return frameId;
        }

        void printPacketStats(){
            System.out.println("RAW_SIZE:"+this.raw_size+"    AC_SIZE:"+this.ac_size);
        }

        static boolean comparePackets(Packet p1,Packet p2){
            if(p1.ac_size!=p2.ac_size){return false;}
            if(p1.raw_size!=p2.raw_size){return false;}
            for(int i=0;i<p1.raw_size;i++){
                if(p1.raw_bytes[i]!=p2.raw_bytes[i]){
                    return false;
                }
            }
            return true;
        }



}

