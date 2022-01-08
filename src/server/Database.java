package server;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class Database {

    private FileInfoList fileInfoList;

    public Database() {
        try (
                FileInputStream fileInputStream = new FileInputStream("D:/jetbrains-java/File Server/fileInfo.ser");
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream)
                ) {

            fileInfoList = (FileInfoList) objectInputStream.readObject();
        } catch (FileNotFoundException e) {
            fileInfoList = new FileInfoList();
            //System.out.println(e.getMessage());
        } catch (IOException | ClassNotFoundException e) {
            //System.out.println(e.getMessage());
        }
    }

    public int dbSize() {
        return fileInfoList.size();
    }

    public void saveDatabase(){
        try (
                FileOutputStream fileOutputStream = new FileOutputStream("D:/jetbrains-java/File Server/fileInfo.ser");
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)
                ) {

            objectOutputStream.writeObject(fileInfoList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFilePathById(long id) {
        Optional<FileInfo> toReturn = fileInfoList.stream().filter(fileInfo -> fileInfo.getId() == id).findFirst();
        return toReturn.orElseThrow().getPath();
    }

    public String getFilePathByName(String name) {
        Optional<FileInfo> toReturn = fileInfoList.stream().filter(fileInfo -> fileInfo.getName().equals(name)).findFirst();
        return toReturn.orElseThrow().getPath();
    }

    public byte[] getById(long id) {
        try (
                FileInputStream fileInputStream = new FileInputStream(getFilePathById(id));
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        ) {
            return bufferedInputStream.readAllBytes();
        } catch (IOException | NoSuchElementException e) {
            System.out.println(e.getMessage());
            return new byte[0];
        }

        /*try {
            return fileInfoList.getById(id).getData();
        } catch (NoSuchElementException e) {
            return new byte[0];
        }*/
    }

    public byte[] getByName(String name) {
        try (
                FileInputStream fileInputStream = new FileInputStream(getFilePathByName(name));
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        ) {
            return bufferedInputStream.readAllBytes();
        } catch (IOException | NoSuchElementException e) {
            System.out.println(e.getMessage());
            return new byte[0];
        }

        /*try {
            return fileInfoList.getByName(name).getData();
        } catch (NoSuchElementException e) {
            return new byte[0];
        }*/

    }

    public long putFile(String name, byte[] content) throws IOException {
        /*if (fileInfoList.exists(name)) {
            throw new FileAlreadyExistsException("File already exists");
        }*/

        FileInfo newFile = new FileInfo(name, content);
        fileInfoList.add(newFile);

        File file = new File(newFile.getPath());

        try (
                FileOutputStream fileOutputStream = new FileOutputStream(newFile.getPath());
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)
                ) {

            bufferedOutputStream.write(content);
            bufferedOutputStream.flush();
            saveDatabase();
            return newFile.getId();
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new IOException();
    }

    public boolean deleteById(long id) {
        try {
            File file = new File(getFilePathById(id));
            boolean deleteSuccess = file.delete();
            if (deleteSuccess) {
                fileInfoList.removeById(id);
                return true;
            } else {
                return false;
            }
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean deleteByName(String name) {
        try {
            File file = new File(getFilePathByName(name));
            boolean deleteSuccess = file.delete();
            if (deleteSuccess) {
                fileInfoList.removeByName(name);
                return true;
            } else {
                return false;
            }
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}
