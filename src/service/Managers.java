package service;

import model.Task;

public class Managers {
    // Метод для получения стандартного экземпляра TaskManager
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(); // Возвращает стандартный экземпляр
    }

    // Метод для получения специального экземпляра TaskManager для тестирования
    public static TaskManager getTestInstance() {
        return new InMemoryTaskManager() {
            @Override
            public Task getTask(int id) {
                return null; // Логика для тестирования
            }

            public Task getSubtask(int id) {
                return null; // Логика для тестирования
            }

            @Override
            public void updateTask(int id, Task task) {
                // Логика для тестирования
            }
        };
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager(); // Возвращает стандартный экземпляр HistoryManager
    }
}
