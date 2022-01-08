package server;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    private static final int PORT = 23456;
    //private static Database database = new Database();

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            Database database = new Database();
            System.out.println("Server started");
            //System.out.println(database.dbSize());

            while (true) {
                try (
                        Socket socket = server.accept();
                        DataInputStream in = new DataInputStream(socket.getInputStream());
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                ) {
                    String[] request = in.readUTF().split("\\t");
                    String method = request[0];

                    if ("exit".equals(method)) {
                        break;
                    }

                    switch (method) {
                        case "GET":
                            String getFileMethod = request[1];
                            String filename = request[2];
                            //System.out.println(getFileMethod);
                            //System.out.println(filename);
                            byte[] data;
                            if ("1".equals(getFileMethod)) {
                                //System.out.println("getbyname");
                                data = database.getByName(filename);
                            } else {
                                //System.out.println("getbyid");
                                data = database.getById(Long.parseLong(filename));
                            }
                            //System.out.println("Server data length " + data.length);
                            out.writeInt(data.length);
                            out.write(data);
                            break;
                        case "PUT":
                            filename = request[1];
                            int length = in.readInt();
                            byte[] content = new byte[length];
                            in.readFully(content);

                            try {
                                long id = database.putFile(filename, content);
                                out.writeUTF("Response says that file is saved! ID = " + id);
                            } catch (IOException e) {
                                out.writeUTF(e.getMessage());
                            }

                            break;
                        case "DELETE":
                            String deleteFileMethod = request[1];
                            filename = request[2];

                            boolean deleteSuccess = false;
                            if ("1".equals(deleteFileMethod)) {
                                deleteSuccess = database.deleteByName(filename);
                            } else {
                                deleteSuccess = database.deleteById(Long.parseLong(filename));
                            }

                            if (deleteSuccess) {
                                out.writeUTF("The response says that this file was deleted successfully!");
                            } else {
                                out.writeUTF("The response says that this file is not found!");
                            }
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getFile(DataOutputStream out, String filepath) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        try (FileReader fileReader = new FileReader(filepath)) {
            int i = fileReader.read();
            while (i != -1) {
                contentBuilder.append((char) i);
                i = fileReader.read();
            }
            String response = "The content of the file is: " + contentBuilder.toString();
            out.writeUTF(response);
        } catch (FileNotFoundException e) {
            out.writeUTF("The response says that the file was not found!");
        }
    }

    public static void putFile(DataOutputStream out, String filepath, String content) throws IOException {
        if (Files.exists(Path.of(filepath))) {
            out.writeUTF("The response says that creating the file was forbidden!");
        } else {
            try (PrintWriter printWriter = new PrintWriter(filepath)) {
                printWriter.write(content);
                out.writeUTF("The response says that the file was created!");
            } catch (FileNotFoundException ignored) {
            }
        }
    }

    public static void deleteFile(DataOutputStream out, String filepath) throws IOException {
        File file = new File(filepath);
        boolean isDeleted = file.delete();
        if (isDeleted) {
            out.writeUTF("The response says that the file was successfully deleted!");
        } else {
            out.writeUTF("The response says that the file was not found!");
        }
    }

    public static void exit(Socket socket) throws IOException {
        socket.close();
    }
}