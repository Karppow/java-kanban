public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        // Создание задач
        Task task1 = taskManager.createTask("Переезд", "Организовать переезд на новую квартиру", TaskStatus.NEW);
        Task task2 = taskManager.createTask("Купить мебель", "Купить новую мебель для квартиры", TaskStatus.NEW);

        // Создание эпиков и подзадач
        Epic epic1 = taskManager.createEpic("Организация праздника", "Подготовить семейный праздник");
        Subtask subtask1 = taskManager.createSubtask("Пригласить гостей", "Составить список гостей", TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = taskManager.createSubtask("Купить торт", "Заказать торт для праздника", TaskStatus.NEW, epic1.getId());

        Epic epic2 = taskManager.createEpic("Покупка квартиры", "Подготовить документы для покупки квартиры");
        Subtask subtask3 = taskManager.createSubtask("Собрать документы", "Собрать все необходимые документы", TaskStatus.NEW, epic2.getId());

        // Печать всех задач
        System.out.println("Все задачи:");
        taskManager.getAllTasks().forEach(task -> System.out.println(task.getTitle() + " - " + task.getStatus()));
        System.out.println("Все подзадачи:");
        taskManager.getAllSubtasks().forEach(subtask -> System.out.println(subtask.getTitle() + " - " + subtask.getStatus()));
        System.out.println("Все эпики:");
        taskManager.getAllEpics().forEach(epic -> System.out.println(epic.getTitle() + " - " + epic.getStatus()));

        // Изменение статусов
        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);
        System.out.println("Статус эпика после завершения подзадачи: " + taskManager.getEpicById(epic1.getId()).getStatus());

        // Удаление задачи
        taskManager.deleteTask(task1.getId());
        System.out.println("После удаления задачи:");
        taskManager.getAllTasks().forEach(task -> System.out.println(task.getTitle()));
    }
}