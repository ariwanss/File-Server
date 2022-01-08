package server;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Objects;

public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Path DIRECTORY = Path.of(System.getProperty("user.dir"), "src", "server", "data");
    private static long counter = 0;

    private long id;
    private String name;
    private String path;
    private byte[] data;

    public FileInfo(String name, byte[] data) {
        this.id = ++counter;
        this.name = name;
        this.path = DIRECTORY + File.separator + name;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileInfo fileInfo = (FileInfo) o;
        return Objects.equals(name, fileInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
