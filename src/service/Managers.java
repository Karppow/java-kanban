package service;

public class Managers {
    // Метод для получения стандартного экземпляра TaskManager
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(); // Возвращает стандартный экземпляр
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager(); // Возвращает стандартный экземпляр HistoryManager
    }
}
