package model;

import service.FileBackedTaskManager;

public class ManagerCreateFileException extends Task {
    public ManagerCreateFileException(String title, String description, TaskStatus status) {
        super(title, description, status);
    }

    @Override
    public void create(FileBackedTaskManager manager) {

    }
}