package server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class FileInfoList extends ArrayList<FileInfo> implements Serializable {

    private static final long serialVersionUID = 1L;

    public boolean exists(String name) {
        return this.stream().anyMatch(fileInfo -> fileInfo.getName().equals(name));
    }

    public boolean exists(long id) {
        return this.stream().anyMatch(fileInfo -> fileInfo.getId() == id);
    }

    public void removeById(long id) {
        FileInfo toRemove = this.stream().filter(fileInfo -> fileInfo.getId() == id).findFirst().orElseThrow();
        this.remove(toRemove);
    }

    public void removeByName(String name) {
        FileInfo toRemove = this.stream().filter(fileInfo -> fileInfo.getName().equals(name)).findFirst().orElseThrow();
        this.remove(toRemove);
    }

    public FileInfo getByName(String name) {
        return this.stream().filter(fileInfo -> fileInfo.getName().equals(name)).findFirst().orElseThrow();
    }

    public FileInfo getById(long id) {
        return this.stream().filter(fileInfo -> fileInfo.getId() == id).findFirst().orElseThrow();
    }
}
