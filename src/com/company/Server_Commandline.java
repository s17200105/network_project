package com.company;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Server_Commandline {
    String dierctor = "sharefile";
    File currentDir = new File(dierctor);
    String password;
    public Server_Commandline(int port,String password) throws IOException{
//        /str 127.0.0.1 8899 26151851
        this.password = password;
        System.out.println("Running ....");
        ServerSocket sSocket = new ServerSocket(port);
        System.out.println("Server start successful...in port"+port);
        while(true) {
            Socket cSocket=sSocket.accept();
            Thread t= new Thread() {
                @Override
                public void run(){
                try{
                    serve(cSocket);
                }catch(IOException e) {}
				}
            };
            t.start();
        }
    }

    private void serve(Socket socket) throws IOException {
        String [] cmd;
        System.out.print("Receive new request...");
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        String command = readMsg(in);
        cmd = command.split(" ");
        System.out.println(cmd[0]);
        switch(cmd[0])
        {
            case "004":
                sendFile(out, cmd);
                break;
            case "005":
                sendText(out,listFileName());
                break;
            case "002":
                changeDir(cmd[1]);
                sendText(out,dierctor);
                break;
            case "003":
                sendText(out,listFiles());
                break;
            case "007":
                checkPermission(out,cmd[1]);
        }
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
        return str;
    }

    private void sendFile(DataOutputStream out, String [] filenames) throws IOException {
        for (String filename:filenames) {
            if (!filename.equals("004")){
                System.out.println("Sending file...");
                byte[] buffer = new byte[1024];
                File file = new File(dierctor+"\\"+filename);
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
        }
    }

    private void sendText(DataOutputStream out,String sentence) throws IOException {
        byte[] buffer = new byte[1024];
        buffer = sentence.getBytes();
        out.writeInt(buffer.length);
        out.write(buffer);
    }

    private String listFiles() {
        File dir = currentDir;

        if (!dir.exists()) {
            System.out.println("File / directory does not exist.\n" + dir);
            return "File / directory does not exist.\n" + dir;
        }
        if (dir.isFile())
            return getInfo(dir);
        else {
            File[] fileList = dir.listFiles();
            String info = "";
            for (int i = 0; i < fileList.length; i++)
                info += getInfo(fileList[i]) + "\n";
            return info;
        }
    }

    private String listFileName(){
        File dir = currentDir;
        if (!dir.exists()) {
            System.out.println("File / directory does not exist.\n" + dir);
            return "File / directory does not exist.\n" + dir;
        }
        if (dir.isFile())
            return dir.getName();
        else
        {
            File[] fileList = dir.listFiles();
            String info = "";
            for (int i = 0; i < fileList.length; i++)
                info += fileList[i].getName()+" ";
            return info;
        }
    }

    private String getInfo(File f) {
        Date date = new Date(f.lastModified());
        String ld = new SimpleDateFormat("MMM dd, yyyy").format(date);
        if (f.isFile()) {
            return String.format("%dKB\t%s\t%s", (int) Math.ceil((float) f.length() / 1024), ld, f.getName());
        } else
            return String.format("<DIR>\t%s\t%s", ld, f.getName());
    }

    private void changeDir(String path) throws IOException {
        if (path == null) {
            System.out.println(currentDir.getCanonicalPath());
            return;
        }
        File dir;
        if (path.startsWith("/") || path.startsWith("\\") || path.contains(":")) {
            dierctor = path;
            dir = new File(path);
        }
        else{
            dir = new File(currentDir.getCanonicalPath() + "/" + path);
            dierctor = currentDir.getCanonicalPath() + "/" + path;
        }

        if (!dir.exists() || dir.isFile()) {
            System.out.println("The system cannot find the path specified.");
            return;
        }
        currentDir = dir;
    }

    public void checkPermission(DataOutputStream out,String password) throws IOException{
        if (this.password.equals(password)){
            sendText(out,"Success");
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        String sentence,ip,password;
        int port;
        String [] statement;

        System.out.println("Please enter connection command to start the server");
        sentence = scanner.nextLine();
        statement = sentence.split(" ");
        if(statement[0].equalsIgnoreCase("/str")){
            ip = statement [1];
            port = Integer.parseInt(statement[2]);
            password = statement[3].toString();
            Server_Commandline sc = new Server_Commandline(port,password);
        }
    }
}