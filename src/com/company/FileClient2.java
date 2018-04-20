package com.company;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class FileClient2 {
    public FileClient2(String ip, int port) throws IOException {
        Socket socket = new Socket(ip, port);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        String filename = sendMsg(out);
        receiveFile(in, filename);

        in.close();
        socket.close();
    }

    String sendMsg(DataOutputStream out) throws IOException {
        System.out.print("Input a file name: ");
        Scanner scanner = new Scanner(System.in);
        String msg = scanner.nextLine();
        int size = msg.length();
        out.writeInt(size);
        out.write(msg.getBytes());
        scanner.close();
        return msg;
    }

    void receiveFile(DataInputStream in, String filename) throws IOException {
        byte[] buffer = new byte[1024];
        long count = 0, size;
        int len;
        File file;
        FileOutputStream fout;
        file = new File(filename);
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

    public static void main(String[] args) throws IOException {
        FileClient2 client = new FileClient2("127.0.0.1", 8899);
    }
}
