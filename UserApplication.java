//Diktya Ypologistwn II  Auth Panagiotis Savvidis 8094 12-11-2020 panasavv@ece.auth.gr
package userapplication;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import javax.sound.sampled.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;



public class UserApplication {
 public static void main(String[] param) throws IOException,LineUnavailableException,ClassNotFoundException{
    
    int mode=0;
    int portS=0;
    int portC=0;
    int eCode=0;
    int iCode=0;
    int aCode=0;
    int cCode=0;
    int vCode=0;
    int yes=0;
    Scanner scan = new Scanner(System.in);
  
    System.out.print("Server listening port: ");
    portS = Integer.parseInt(scan.nextLine());
    
    System.out.print("Client listening port: ");
    portC = Integer.parseInt(scan.nextLine());

    System.out.print("Request code for Echo: E");
    eCode = InputInteger();
    echo(eCode,1,portS,portC);
    echo(eCode,2,portS,portC); 

    System.out.print("Request code for Image: M");
    iCode = InputInteger();
    image(iCode,1,portS,portC);
    image(iCode,2,portS,portC);
 
    System.out.print("Request code for Audio: A");
    aCode = InputInteger();
    soundDPCM(aCode,1,portS,portC);
    soundDPCM(aCode,2,portS,portC);
    soundAQDPCM(aCode,1,portS,portC,1);
    soundAQDPCM(aCode,2,portS,portC,1);
    soundAQDPCM(aCode,1,portS,portC,2);
    soundAQDPCM(aCode,2,portS,portC,2);

    System.out.print("Request code for Ithakicopter: Q");
    cCode = InputInteger();
    Ithakicopter(cCode,38078,48078,1);
    System.out.print("Are you ready for second flight? Press 1 for yes \n");
    yes = InputInteger();
    if (yes==1){    
    Ithakicopter(cCode,38078,48078,2);
    }else{
        return;
    }
   
    System.out.print("Request code for Vehicle OBD-II: V");
    vCode = InputInteger();
    vehicle(vCode,portS,portC,"1F");
    vehicle(vCode,portS,portC,"0F");
    vehicle(vCode,portS,portC,"11");
    vehicle(vCode,portS,portC,"0C");
    vehicle(vCode,portS,portC,"0D");
    vehicle(vCode,portS,portC,"05");

 }
  //End of Main

 public static int InputInteger() throws IOException{
  Scanner scan= new Scanner(System.in);
  String s = scan.nextLine();
  s = s.replaceAll("[^0-9.]", "");
  int temp = 0;
  try {
    temp = Integer.parseInt(s);
  } catch (NumberFormatException x) {
    System.out.println("Please choose a valid option");
  }
  return temp;
 }


  
  //Start of Echos
 public static void echo(int eCode,int mode,int server,int client) throws IOException,SocketException,UnknownHostException{
   String modeName="";
   String packetName ="";
   if(mode==1){
     modeName+="Delay";
     packetName+="E" + Integer.toString(eCode) +"\r";
   }else{
     modeName+="noDelay";
     packetName+="E0000\r";
   }

   
   byte[] hostIP = { (byte)155,(byte)207,18,(byte)208 };
   InetAddress hostAdr = InetAddress.getByAddress(hostIP);

   DatagramSocket receiver = new DatagramSocket(client);

   byte[] txB = packetName.getBytes();
   DatagramPacket packet = new DatagramPacket(txB,txB.length, hostAdr,server);

   DatagramSocket socket = new DatagramSocket();
   
   receiver.setSoTimeout(3600);

   byte[] rxB = new byte[2048];
   DatagramPacket receiverPacket = new DatagramPacket(rxB,rxB.length);

   int packetCount=0;
   double startingTime=0;
   double endingTime=0;
   double totalTime=0;
   double average=0;
   double loopBegin=0;
   double loopFin=0;

   String msg="";

   ArrayList<String> writes = new ArrayList<String>();
   ArrayList<Float> counterNumber = new ArrayList<Float>();
   ArrayList<Double> timings = new ArrayList<Double>();

   loopBegin = System.nanoTime();

   while(loopFin<(300000)){
   socket.send(packet);
   packetCount++;
   startingTime = System.nanoTime();
   while (true) {
     try {
         receiver.receive(receiverPacket);
         endingTime=(System.nanoTime()- startingTime)/1000000;
         msg = new String(rxB,0,receiverPacket.getLength());
         System.out.println(msg);
         System.out.print("Time: "+endingTime+"\n");
         break;
     } catch (Exception x) {
       System.out.println(x);
       break;
     }
   }


   writes.add("Time: "+endingTime+"\n");
   timings.add(endingTime);

   totalTime+=endingTime;
   loopFin=(System.nanoTime()-loopBegin)/1000000;

  }



  average=totalTime/packetCount;
  writes.add("Packets : "+String.valueOf((double)packetCount));
  writes.add("Average time : "+String.valueOf(average));
  writes.add("Communication : "+(totalTime/60000)+" minutes\n");
  writes.add("Test : "+(loopFin/60000)+" minutes\n");
    double intervSum=0;
    float intervCount=0;

  for(int i = 0; i < timings.size();i++){
    int j = i;
    while((intervSum < 16000)&&(j < timings.size())){
      intervSum += timings.get(j);
      intervCount++;
      j++;
    }
    intervCount = intervCount/16;
    counterNumber.add(intervCount);
    intervCount = 0;
    intervSum = 0;
  }

  BufferedWriter bw = null;
  try{
    File file =new File(("EchoPackets"+eCode+modeName+".txt"));
    bw = new BufferedWriter(new FileWriter(("EchoPackets"+eCode+modeName+".txt"), false));
    file.createNewFile();
    
    for (int i=0; i <writes.size(); i++){

      bw.write(String.valueOf(writes.get(i)));
      bw.newLine();
    }
    bw.newLine();
  }catch(IOException ioe){
    ioe.printStackTrace();
  }
  finally{
    try{
      if(bw != null) bw.close();
    }catch(Exception ex){
      System.out.println("BufferedWriter couldn't close" + ex);
    }
  }

  bw = null;
  try{
    File file =new File(("EchoPackets(p16)"+eCode+modeName+".txt"));
    bw = new BufferedWriter(new FileWriter(("EchoPackets(p16)"+eCode+modeName+".txt"), false));

    file.createNewFile();

    for (int i=0; i <counterNumber.size(); i++){

      bw.write(String.valueOf(counterNumber.get(i)));
      bw.newLine();
    }
    bw.newLine();
  }catch(IOException ioe){
    ioe.printStackTrace();
  }
  finally{
    try{
      if(bw != null) bw.close();
    }catch(Exception ex){
      System.out.println(ex);
    }
  }


  System.out.println("Temperatures: ");
  loopBegin = System.nanoTime();

  for(int i=0;i<=9;i++){
  packetName="E"+Integer.toString(eCode)+"T0"+i+"\r";
  txB = packetName.getBytes();
  
  packetCount++;
  packet = new DatagramPacket(txB,txB.length, hostAdr,server);
  socket.send(packet);
  
  startingTime = System.nanoTime();
  while (true) {
    try {
        receiver.receive(receiverPacket);
        endingTime=(System.nanoTime()- startingTime)/1000000;
        msg = new String(rxB,0,receiverPacket.getLength());
        System.out.println(msg);
        System.out.println("Time: " + endingTime);
        break;
    } catch (Exception x) {
      System.out.println(x);
      break;
    }
  }
  totalTime+=endingTime;
  loopFin=(System.nanoTime()-loopBegin)/1000000;
  
  }

  receiver.close();
  socket.close();

  System.out.println( packetCount + " packets received with average time: "+average+" ms");
}
  //End of Echos
  //Start of Image
 public static void image(int code,int mode,int server,int client) throws IOException,SocketException,UnknownHostException{
      String packet="M";
      String modeName="CAM";
      
      
      byte[] hostIP = { (byte)155,(byte)207,18,(byte)208 };
      byte[] rxB = new byte[2048];
      InetAddress hostAdr = InetAddress.getByAddress(hostIP);

      DatagramSocket Socket = new DatagramSocket();
      DatagramSocket rSocket = new DatagramSocket(client);
      rSocket.setSoTimeout(3600);
      DatagramPacket rPacket = new DatagramPacket(rxB,rxB.length);

    if(mode == 1) {
        modeName += Integer.toString(code);
        packet += Integer.toString(code) +"\r";
     }else if(mode == 2) {
        modeName += "=PTZ" + Integer.toString(code);
        packet +=  Integer.toString(code) + " CAM=PTZ\r";
     }

      byte[] txB = packet.getBytes();
      DatagramPacket Packet = new DatagramPacket(txB,txB.length, hostAdr,server);       
      
      Socket.send(Packet);
  		rSocket.setSoTimeout(3200);
      String fileName = ("Image"+code+modeName+".jpeg");
  		FileOutputStream fout = new FileOutputStream(fileName);
  		while (true){
  			try{
  				rSocket.receive(rPacket);
  				if (rxB == null) break;
  				for(int i = 0 ; i <= 127 ; i++){
  				  fout.write(rxB[i]);
  				}
  			}catch (IOException ex) {
  				System.out.println(ex);
  				break;
  			}
  		}
  		fout.close();

      System.out.print("Image saved\n");
      rSocket.close();
      Socket.close();

}
  //End of Image
  //Start of SoundDPCM
 public static void soundDPCM(int code,int mode,int server,int client) throws IOException,SocketException,UnknownHostException,LineUnavailableException{
  int packetsCount = 975;
  int firstMask = 15;
  int secondMask = 240;
  int bi = 5;
  int rx;
  int i=0;
  int nib = 0;
  int nib2 = 0;
  int sub = 0;
  int sub2 = 0;
  int x1 = 0;
  int x2 = 0;
  int count = 0;
  String packet = "A";
  String info="song";
  ArrayList<Integer> subs = new ArrayList<Integer>();
  ArrayList<Integer> samples = new ArrayList<Integer>();
  byte[] hostIP = { (byte)155,(byte)207,18,(byte)208 };
  InetAddress hostAdr = InetAddress.getByAddress(hostIP);
  DatagramSocket Socket = new DatagramSocket();
  DatagramSocket rSocket = new DatagramSocket(client);
  rSocket.setSoTimeout(3600);

  byte[] rxB = new byte[128];
  DatagramPacket rPacket = new DatagramPacket(rxB,rxB.length);
  BufferedWriter mw = null;

  if(mode == 1){
    packet += Integer.toString(code) + "F975";
  }else if(mode == 2){
    packet += Integer.toString(code) + "T975";
  }
  
  byte[] txB = packet.getBytes();
  DatagramPacket Packet = new DatagramPacket(txB,txB.length, hostAdr,server);

  Socket.send(Packet);

    byte[] song = new byte[256*packetsCount];
    
    while ( i < packetsCount) {
      try{
        rSocket.receive(rPacket);
        for (int j = 0;j <= 127;j++){
          rx = rxB[j];
          nib = rx & firstMask; 
          nib2 = (rx & secondMask)>>4; 
          sub = nib-8;
          subs.add(sub);
          sub = sub*bi;
          sub2 = nib2-8;
          subs.add(sub2);
          sub2 = sub2*bi;
          x1 = x2 + sub;
          samples.add(x1);
          x2 = x1 + sub2;
          samples.add(x2);
          song[count] = (byte)x1;
          song[count + 1] = (byte)x2;
          count += 2;
        }
    }catch (Exception ex){
      System.out.println(ex);
    }
    i++;
  }

  if(mode==1){
    System.out.println("Have fun with this song :)");

    AudioFormat pcm = new AudioFormat(8000,8,1,true,false);
    SourceDataLine playsong = AudioSystem.getSourceDataLine(pcm);
    playsong.open(pcm,32000);
    playsong.start();
    playsong.write(song,0,256*packetsCount);
    playsong.stop();
    playsong.close();
  } else if(mode == 2){
    info="frequency";
  }

  BufferedWriter bw = null;
  try{
    File file = new File("DPCM_sub_F"+code+info+".txt");

    file.createNewFile();
    
    FileWriter fw = new FileWriter(file,false);
    bw = new BufferedWriter(fw);
    i=0;
    while (i < subs.size()) {
      bw.write("" + subs.get(i) + " " + subs.get(i+1));
      bw.newLine();
      i+=2;
    }

   }catch(IOException ioe){
     ioe.printStackTrace();
   }finally{
    try{
      if(bw != null) bw.close();
    }catch(Exception ex){
      System.out.println("BufferedWriter couldn't close" + ex);
    }
   }

   
   try{
    File file = new File("DPCM_samples_F"+code+info+".txt");
    file.createNewFile();
  
    FileWriter fw = new FileWriter(file,false);
    mw = new BufferedWriter(fw);
    i=0;
    while (i < samples.size()) {
      mw.write("" + samples.get(i) + " " + samples.get(i+1));
      mw.newLine();
      i += 2;
    }

   }catch(IOException ioe){
     ioe.printStackTrace();
   }finally{
     try{
       if(mw != null) mw.close();
     }catch(Exception ex){
       System.out.println("BufferedWriter couldn't close" + ex);
     }
   }

   rSocket.close();
   Socket.close();


 }
  //End of SoundDPCM
  //Start of SoundAQDPCM
 public static void soundAQDPCM(int code,int mode,int server,int client,int run) throws IOException,SocketException,UnknownHostException,LineUnavailableException{
  int packetNumber = 975;
  int rx;
  int nib1 = 0;
  int nib2 = 0;
  int sub = 0;
  int sub2 = 0;
  int x1 = 0;
  int x2 = 0;
  int count = 4;
  int mean;
  int bi;
  int temp = 0;
  String packet = "A";
  String info="song";

  byte[] hostIP = { (byte)155,(byte)207,18,(byte)208 };
  InetAddress hostAdr = InetAddress.getByAddress(hostIP);

  DatagramSocket Socket = new DatagramSocket();
  DatagramSocket rSocket = new DatagramSocket(client);
  byte[] rxB = new byte[132];
  DatagramPacket recievePacket = new DatagramPacket(rxB,rxB.length);
  rSocket.setSoTimeout(5000);

  if(mode == 1){
    info="song";
    packet += Integer.toString(code) + "AQF975";
  }else if(mode == 2){
    info="frequency";
    packet += Integer.toString(code) + "AQT975";
  }

  byte[] txB = packet.getBytes();
  
  DatagramPacket Packet = new DatagramPacket(txB,txB.length, hostAdr,server);
  
  Socket.send(Packet);
  byte[] meanB = new byte[4];
  byte[] biB = new byte[4];
  byte sign;
  byte[] song = new byte[512*packetNumber];

  ArrayList<Integer> means = new ArrayList<Integer>();
  ArrayList<Integer> bis = new ArrayList<Integer>();
  ArrayList<Integer> subs = new ArrayList<Integer>();
  ArrayList<Integer> samples = new ArrayList<Integer>();

  for(int i = 1;i < packetNumber;i++){

    try{
      rSocket.receive(recievePacket);
      sign = (byte)( ( rxB[1] & 0x80) !=0 ? 0xff : 0x00);
      meanB[3] = sign;
      meanB[2] = sign;
      meanB[1] = rxB[1];
      meanB[0] = rxB[0];
      mean = ByteBuffer.wrap(meanB).order(ByteOrder.LITTLE_ENDIAN).getInt();
      means.add(mean);
      sign = (byte)( ( rxB[3] & 0x80) !=0 ? 0xff : 0x00);
      biB[3] = sign;
      biB[2] = sign;
      biB[1] = rxB[3];
      biB[0] = rxB[2];
      bi = ByteBuffer.wrap(biB).order(ByteOrder.LITTLE_ENDIAN).getInt();
      bis.add(bi);

      for (int j = 4;j <= 131;j++){ 
        count += 4;

        rx = rxB[j];
        nib1 = (int)(rx & 0x0000000F);
        nib2 = (int)((rxB[j] & 0x000000F0)>>4);
        sub = (nib2-8);
        subs.add(sub);
        sub2 = (nib1-8);
        subs.add(sub2);
        sub = sub*bi;
        sub2 = sub2*bi;

        x1 = temp + sub + mean;
        samples.add(x1);
        
        song[count] = (byte)(x1 & 0x000000FF);
        song[count + 1] = (byte)((x1 & 0x0000FF00)>>8);

        x2 = sub + sub2 + mean;
        temp = sub2;
        samples.add(x2);
        
        song[count + 2] = (byte)(x2 & 0x000000FF);
        song[count + 3] = (byte)((x2 & 0x0000FF00)>>8);


      }
    }catch (Exception ex){
      System.out.println(ex);
    }
  }
  if(mode==1){
    System.out.println("Have fun with this song :) ");

    AudioFormat aqpcm = new AudioFormat(8000,16,1,true,false);
    SourceDataLine playsong = AudioSystem.getSourceDataLine(aqpcm);
    playsong.open(aqpcm,32000);
    playsong.start();
    playsong.write(song,0,512*packetNumber);
    playsong.stop();
    playsong.close();
  } else if(mode == 2){
    info="frequency";
  }

  BufferedWriter bw = null;
  try{
    File file = new File("AQDPCM_subs_F"+code+info+"_"+run+".txt");
    file.createNewFile();
    
    FileWriter fw = new FileWriter(file,false);
    bw = new BufferedWriter(fw);
    for(int i = 0 ; i < subs.size() ; i += 2){
      bw.write("" + subs.get(i) + " " + subs.get(i+1));
      bw.newLine();
    }

  }catch(IOException ioe){
    ioe.printStackTrace();
  }finally{
    try{
      if(bw != null) bw.close();
    }catch(Exception ex){
      System.out.println("BufferedWriter couldn't close" + ex);
    }
  }

  BufferedWriter mw = null;
  try{
    File file = new File("AQDPCM_samples_F"+code+info+"_"+run+".txt");
    file.createNewFile();
    
    FileWriter fw = new FileWriter(file,false);
    mw = new BufferedWriter(fw);
    for(int i = 0 ; i < samples.size() ; i += 2){
      mw.write("" + samples.get(i) + " " + samples.get(i+1));
      mw.newLine();
    }

  }catch(IOException ioe){
    ioe.printStackTrace();
  }finally{
    try{
      if(mw != null) mw.close();
    }catch(Exception ex){
      System.out.println("BufferedWriter couldn't close" + ex);
    }
  }

  BufferedWriter pw = null;
  try{
    File file = new File("AQDPCM_mean_F"+code+info+"_"+run+".txt");
    
    file.createNewFile();

    FileWriter fw = new FileWriter(file,false);
    pw = new BufferedWriter(fw);
    for(int i = 0 ; i < means.size() ; i += 2){
      pw.write("" + means.get(i));
      pw.newLine();
    }

  }catch(IOException ioe){
    ioe.printStackTrace();
  }finally{
    try{
      if(pw != null) pw.close();
    }catch(Exception ex){
      System.out.println("BufferedWriter couldn't close" + ex);
    }
  }

  BufferedWriter kw = null;
  try{
    File file = new File("AQDPCM_betas_F"+code+info+"_"+run+".txt");
    
    file.createNewFile();
    
    FileWriter fw = new FileWriter(file,false);
    kw = new BufferedWriter(fw);
    for(int i = 0 ; i < bis.size() ; i ++){
      kw.write("" + bis.get(i));
      kw.newLine();
    }

  }catch(IOException ioe){
    ioe.printStackTrace();
  }finally{
    try{
      if(kw != null) kw.close();
    }catch(Exception ex){
      System.out.println("BufferedWriter couldn't close" + ex);
    }
  }

  rSocket.close();
  Socket.close();

 }
  //End of SoundAQDPCM
  //Start of IthakiCopter
 public static void Ithakicopter(int code,int server,int client,int run) throws IOException,SocketException,UnknownHostException,LineUnavailableException,ClassNotFoundException{
  String msg="";
  ArrayList<String> msgs = new ArrayList<String>();
  BufferedWriter bw = null;

  DatagramSocket Socket = new DatagramSocket();
  DatagramSocket rSocket = new DatagramSocket(client);
  byte[] rxB = new byte[5000];
  DatagramPacket rPacket = new DatagramPacket(rxB,rxB.length);

  String info = "Q" + Integer.toString(code)+"\r";
  byte[] txB = info.getBytes();

  byte[] hostIP = { (byte)155,(byte)207,18,(byte)208 };
  InetAddress hostAdr = InetAddress.getByAddress(hostIP);
  
  DatagramPacket Packet = new DatagramPacket(txB,txB.length, hostAdr,server);
  
  Calendar calend = Calendar.getInstance();
  SimpleDateFormat simpleDate = new SimpleDateFormat("HH:mm:ss");
  msgs.add("Session's Code: "+code+" at "+simpleDate.format(calend.getTime())+"\n");

  int msgCounter = 0;
  rSocket.setSoTimeout(5000);
  for (int i = 1;i <= 60 ; i++){
    try{
      Socket.send(Packet);
      rSocket.receive(rPacket);
      msg = new String(rxB,0,rPacket.getLength());
      msgs.add(msg);
      System.out.println(msg);
      msgCounter++;
    }catch(Exception ex){
      System.out.println(ex);
    }

  }
  try{
    File file = new File("IthakiCopter"+code+"_"+run+".txt");
    file.createNewFile();
    
    FileWriter fw = new FileWriter(file,true);
    bw = new BufferedWriter(fw);
    for(int i = 0 ; i < msgCounter; i++){
      bw.write("" + msgs.get(i));
      bw.newLine();
    }

  }catch(IOException ex){
    ex.printStackTrace();
  }finally{
    try{
      if(bw != null) bw.close();
    }catch(Exception ex){
      System.out.println("BufferedWriter couldn't close" + ex);
    }
  }

  rSocket.close();
  Socket.close();
 }
  //End of IthakiCopter
  //Start of Vehicle
 public static void vehicle(int code,int server,int client,String pid) throws IOException,SocketException,UnknownHostException,LineUnavailableException,ClassNotFoundException{
   ArrayList<String> msgs = new ArrayList<String>();
   double loopBegin=0;
   double loopFin=0;


   byte[] hostIP = { (byte)155,(byte)207,18,(byte)208 };
   InetAddress hostAdr = InetAddress.getByAddress(hostIP);

   DatagramSocket Socket = new DatagramSocket();
   DatagramSocket rSocket = new DatagramSocket(client);
   byte[] rxB = new byte[5000];
   DatagramPacket rPacket = new DatagramPacket(rxB,rxB.length);

    rSocket.setSoTimeout(5000);
    loopBegin = System.nanoTime();
    int msgCounter = 0;
    while(loopFin<240000){

         String info = "V"+Integer.toString(code)+"OBD=01 "+ pid ;
         byte[] txB = info.getBytes();
         DatagramPacket sendPacket = new DatagramPacket(txB,txB.length, hostAdr,server);
         try{
            Socket.send(sendPacket);
        		rSocket.receive(rPacket);
        		String msg = new String(rxB,0,rPacket.getLength());
        		msgs.add(msg);
            System.out.println(msg);
            msgCounter++;
         }catch(Exception ex){
           System.out.println(ex);
         }


      loopFin=(System.nanoTime()-loopBegin)/1000000;
    }

 	BufferedWriter bw = null;
 		try{
 			File file = new File("OBDII_Vehicle"+code+"PID_"+pid+".txt");
 			file.createNewFile();
 			
 			FileWriter fw = new FileWriter(file,true);
 			bw = new BufferedWriter(fw);
 			for(int i = 0 ; i < msgCounter; i++){
 				bw.write("" + msgs.get(i));
 				bw.newLine();
 			}

 		}catch(IOException ex){
 			ex.printStackTrace();
 		}finally{
 			try{
 				if(bw != null) bw.close();
 			}catch(Exception ex){
 				System.out.println("BufferedWriter couldn't close" + ex);
 			}
 		}

    rSocket.close();
    Socket.close();
 }
  //End of Vehicle

}
