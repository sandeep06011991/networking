# TCP over UDP


Problem statement of the project:http://people.cs.umass.edu/~arun/453/PA2/PA2.html


The purpose of the project is to implement TCP properties given UDP sockets and an unreliable channel
The unreliable channel can corrupt packets , delay packets , reorder packets and drop packets.
The maximum size of a packet is limited at 1024 bytes
The size of the transmission window is 10 bytes


The above problems are addressed as follows
1. corrupt packets: Add checksum every 99 bytes (As the channel might flip one byte in 100 bytes)
2. drop packets: on Timer expire : attempt retransmission
3. reorder packets: add packet id to header.
4. transmission window: to minimize latency caused due to rtt delay


I have not implemented the following.
1. Circular frameId: frameId starting back at 0 after crossing over max


How to Run
1. open terminal
   make channel
2. open another terminal
   make receive
3. open another terminal
   make send




