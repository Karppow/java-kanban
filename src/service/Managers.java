package service;

import model.Task;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager() {
            @Override
            public Task getTask(int id) {
                return null;
            }

            @Override
            public void updateTask(int id, Task task) {

            }
        };
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}