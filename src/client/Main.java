package client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final int PORT = 23456;
    private static final String ADDRESS = "127.0.0.1";
    private static final Path DIRECTORY = Path.of(System.getProperty("user.dir"), "src", "client", "data");
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args){

        try (
                Socket socket = new Socket(ADDRESS, PORT);
                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream())
        ) {

            System.out.print("Enter action (1 - get a file, 2 - create a file, 3 - delete a file): ");
            String action = scanner.nextLine();

            if ("exit".equals(action)) {
                out.writeUTF("exit");
                System.out.println("The request was sent.");

                return;
            }

            Thread sendRequest = new Thread(() -> {
                try {
                    switch (action) {
                        case "1":
                            sendGet(out);
                            break;
                        case "2":
                            sendPut(out);
                            break;
                        case "3":
                            sendDelete(out);
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            Thread receiveResponse = new Thread(() -> {
                try {
                    switch (action) {
                        case "1":
                            receiveGet(in);
                            break;
                        case "2":
                            receivePut(in);
                            break;
                        case "3":
                            receiveDelete(in);
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            sendRequest.start();
            receiveResponse.start();
            sendRequest.join();
            receiveResponse.join();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sendGet(DataOutputStream out) throws IOException {
        System.out.print("Do you want to get the file by name or by id (1 - name, 2 - id): ");
        String getFileMethod = scanner.nextLine();

        if ("1".equals(getFileMethod)) {
            System.out.print("Enter name: ");
        } else {
            System.out.print("Enter id: ");
        }

        String filename = scanner.nextLine();
        out.writeUTF("GET\t" + getFileMethod + "\t" + filename);
        System.out.println("The request was sent.");
    }

    public static void receiveGet(DataInputStream in) throws IOException {
        int length = in.readInt();
        //System.out.println("data length: " + length);
        if (length == 0) {
            System.out.println("The response says that this file is not found!");
            return;
        }

        byte[] content = new byte[length];
        in.readFully(content);
        System.out.print("The file was downloaded! Specify a name for it: ");
        String filename = scanner.nextLine();
        String filepath = DIRECTORY + File.separator + filename;

        try (
                FileOutputStream fileOutputStream = new FileOutputStream(filepath);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)
                ) {

            bufferedOutputStream.write(content);
            bufferedOutputStream.flush();
            System.out.println("File saved on the hard drive!");
        } catch (FileNotFoundException e) {
            System.out.println("Saving failed.");
            e.printStackTrace();
        }
    }

    public static void sendPut(DataOutputStream out) throws IOException {
        System.out.print("Enter name of the file: ");
        String filename = scanner.nextLine();
        String filepath = DIRECTORY + File.separator + filename;

        System.out.print("Enter name of the file to be saved on server: ");
        String saveName = scanner.nextLine();

        if (saveName.isBlank()) {
            saveName = UUID.randomUUID().toString() + ".file";
        }

        try (
                FileInputStream fileInputStream = new FileInputStream(filepath);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                ) {

            byte[] data = bufferedInputStream.readAllBytes();
            out.writeUTF("PUT\t" + saveName);
            out.writeInt(data.length);
            out.write(data);

            System.out.println("The request was sent.");
        } catch (FileNotFoundException e) {
            System.out.println("No such file.");
        }
    }

    public static void receivePut(DataInputStream in) throws IOException {
        String response = in.readUTF();
        System.out.println(response);
    }

    public static void sendDelete(DataOutputStream out) throws IOException {
        System.out.println("Do you want to delete the file by name or by id (1 - name, 2 - id): ");
        String deleteFileMethod = scanner.nextLine();

        if ("1".equals(deleteFileMethod)) {
            System.out.print("Enter name: ");
        } else {
            System.out.println("Enter id: ");
        }

        String filename = scanner.nextLine();
        out.writeUTF("DELETE\t" + deleteFileMethod + "\t" + filename);
        System.out.println("The request was sent.");
    }

    public static void receiveDelete(DataInputStream in) throws IOException {
        String response = in.readUTF();
        System.out.println(response);
    }

    public static void exit(DataOutputStream out) throws IOException {
        out.writeUTF("exit");
        System.out.println("The request was sent.");

        System.exit(0);
    }
}
