package com.company;
import java.io.*;
import java.net.*;
import java.lang.Thread;
import java.util.Scanner;

public class Server_Commandline {
    public Server_Commandline(int port,int password) throws IOException{
//        /str 127.0.0.1 8899 26151851
        System.out.println("Running ....");
        ServerSocket sSocket = new ServerSocket(port);
        System.out.println("Server start successful...in port"+port);
        while (true) {
            Socket cSocket = sSocket.accept();
            try{
                serve(cSocket);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private void serve(Socket socket) throws IOException {
        String filename;
        System.out.println("Receive new request...");
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        filename = readMsg(in);
        sendFile(out, filename);
        out.close();
    }

    private String readMsg(DataInputStream in) throws IOException {
        byte[] buffer = new byte[1024];
        String str = "";
        int size = in.readInt();
        int count = 0, len;

        while (count < size) {
            len = in.read(buffer, 0, Math.min(buffer.length, size - count));
            count += len;
            str += new String(buffer, 0, len);
        }

        System.out.println("Receive msg: "+str);

        return str;
    }

    private void sendFile(DataOutputStream out, String filename) throws IOException {
        System.out.println("Sending file...");
        byte[] buffer = new byte[1024];
        File file = new File(filename);
        FileInputStream fin = new FileInputStream(file);
        long size = file.length();
        long count = 0;
        int len;

        out.writeLong(size);

        while(count < size) {
            len = fin.read(buffer);
            out.write(buffer, 0, (int) Math.min(buffer.length, size - count));
            count += len;
        }
        fin.close();
        System.out.println("Out...");
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        String sentence,ip;
        int port,password;
        String [] statement;

        System.out.println("Please enter connection command to start the server");
        sentence = scanner.nextLine();
        statement = sentence.split(" ");
        if(statement[0].equalsIgnoreCase("/str")){
            ip = statement [1];
            port = Integer.parseInt(statement[2]);
            password = Integer.parseInt(statement[3]);
            Server_Commandline sc = new Server_Commandline(port,password);
        }
    }
}