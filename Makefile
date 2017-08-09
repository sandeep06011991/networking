all:
	make c_recv
	make c_send

channel:
	javac  src/pa2/ChannelEmulator.java -d bin/
	java -cp bin/ pa2.ChannelEmulator

c_recv:src/ChatReceiver.java src/BaseTCP.java
	javac src/ChatReceiver.java src/BaseTCP.java src/Packet.java -d bin/

c_send:src/ChatSender.java src/BaseTCP.java
	javac src/ChatSender.java src/BaseTCP.java src/Packet.java -d bin/

receive:c_recv
	rm -f output_file.jpg
	java -cp bin/ ChatReceiver -s sandeep -p 9090 > output_files/output_file.jpg

send:c_send
	java -cp bin/ ChatSender -s sandeep -p 9090 < input_files/galgadot.jpg

make_udp:
	javac UDPTelnet.java -d bin/

run_udp:make_udp
	java -cp bin/ UDPTelnet
