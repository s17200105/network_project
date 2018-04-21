package com.company;
import java.io.*;
import java.rmi.server.ExportException;
import java.util.*;
import java.net.Socket;
import java.text.SimpleDateFormat;


public class CommandPrompt {
    private String ip,password,location;
    private int port;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    public CommandPrompt(String ip,int port,String password,String location){
        File currentDir = new File(".");
        this.ip = ip;
        this.port=port;
        this.password=password;
        this.location = location;
    }

    public CommandPrompt(){}

    private boolean check_permission() throws IOException{
        String status;
        try{
            socket = new Socket(ip, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            sendMsg(out,"007 "+password);
            status = receiveText(in);
            socket.close();
            if(!status.equals("Success")){
                System.out.println("*Please enter correct ip,port or password*");
                return false;
            }
            System.out.println("Server connect successful");
            return true;
        }catch (Exception e){
            return false;
        }
    }

    private void exec(String cmd,String sentence)throws IOException, InterruptedException {
        socket = new Socket(ip, port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        String [] statement = sentence.split(" ");
        String command=cmd;
        for (String s:statement) {
            if(!s.equals(statement[0])){
                command += " "+s;
            }
        }
        switch(cmd) {
            case "004":
                sendMsg(out,command);
                for (String filename:statement) {
                    if(!filename.equals("/dl")){
                        receiveFile(in,filename);
                        System.out.print(filename+" ");
                    }
                }
                System.out.println("download success");
                break;
            case "005":
                sendMsg(out,command);
                String files = receiveText(in);
                exec("004","/dl "+files);
                break;
            case "002":
                sendMsg(out,command);
                location=ip+"\\"+receiveText(in);
                break;
            case "003":
                sendMsg(out,command);
                System.out.println(receiveText(in));
                break;
        };
        socket.close();
    }

    private void sendMsg(DataOutputStream out,String cmd) throws IOException {
        int size = cmd.length();
        out.writeInt(size);
        out.write(cmd.getBytes());
        System.out.println("Processing...");
    }

    private void receiveFile(DataInputStream in, String filename) throws IOException {
        byte[] buffer = new byte[1024];
        long count = 0, size;
        int len;
        File file;
        FileOutputStream fout;
        file = new File("downloadfile/"+filename);
        fout = new FileOutputStream(file);

        size = in.readLong();
        count = 0;

        while (count < size) {
            len = in.read(buffer, 0, (int) Math.min(buffer.length, size - count));
            count += len;
            fout.write(buffer, 0, len);
        }
        fout.close();
    }

    private String receiveText(DataInputStream in) throws IOException {
        long count = 0;
        int len,size;
        size = in.readInt();
        byte[] buffer = new byte[size];
        count = 0;
        while (count < size) {
            len = in.read(buffer, buffer.length - size, size);;
            size -= len;
        }
        return new String(buffer);
    }

    private String getLocation(){
        return location;
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        String [] input;
        String statement,password="",ip="",cmd="";
        String location = "";
        CommandPrompt prompt = new CommandPrompt();
        int port=0;

        while (!prompt.check_permission()){
            System.out.println("*Please finish connection setting because using system*");
            statement = scanner.nextLine();
            if(statement.split(" ")[0].equals("/conn")){
                input = statement.split(" ");
                ip = input[1];
                port = Integer.parseInt(input[2]);
                password = input[3];
            }
            location = ip+"\\sharefile";
            prompt = new CommandPrompt(ip,port,password,location);
        }

        while (true) {
            try {
                location = prompt.getLocation();
                System.out.print(location + "\\>");
                statement = scanner.nextLine().trim();
                if (statement.equalsIgnoreCase("/exit")) {
                    System.out.println("Disconnecting...");
                    System.out.println("Exit the program,see you again.");
                    break;
                }
                switch (statement.split(" ")[0]) {
                    case "/cd":
                        cmd = "002";
                        break;
                    case "/ls":
                        cmd = "003";
                        break;
                    case "/dl":
                        cmd = "004";
                        break;
                    case "/dla":
                        cmd = "005";
                        break;
                }
                prompt.exec(cmd, statement);
            }catch(Exception e){
                return;
            }
        }
        scanner.close();
    }

}

