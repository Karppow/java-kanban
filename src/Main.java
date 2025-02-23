import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import service.InMemoryTaskManager;

public class Main {
    public static void main(String[] args) {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        // Создание задач и получение их ID
        int taskId1 = taskManager.createTask(new Task("Переезд", "Организовать переезд на новую квартиру", TaskStatus.NEW));
        int taskId2 = taskManager.createTask(new Task("Купить мебель", "Купить новую мебель для квартиры", TaskStatus.NEW));

        // Получение объектов задач по ID
        Task task1 = taskManager.getTaskById(taskId1);
        Task task2 = taskManager.getTaskById(taskId2);

        // Создание эпиков и подзадач
        Epic epic1 = taskManager.createEpic(new Epic("Организация праздника", "Подготовить семейный праздник"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Пригласить гостей", "Составить список гостей", TaskStatus.NEW, epic1.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Купить торт", "Заказать торт для праздника", TaskStatus.NEW, epic1.getId()));

        Epic epic2 = taskManager.createEpic(new Epic("Покупка квартиры", "Подготовить документы для покупки квартиры"));
        Subtask subtask3 = taskManager.createSubtask(new Subtask("Собрать документы", "Собрать все необходимые документы", TaskStatus.NEW, epic2.getId()));

        // Печать всех задач
        System.out.println("Все задачи:");
        taskManager.getAllTasks().forEach(task -> System.out.println(task.getTitle() + " - " + task.getStatus()));
        System.out.println("Все подзадачи:");
        taskManager.getAllSubtasks().forEach(subtask -> System.out.println(subtask.getTitle() + " - " + subtask.getStatus()));
        System.out.println("Все эпики:");
        taskManager.getAllEpics().forEach(epic -> System.out.println(epic.getTitle() + " - " + epic.getStatus()));

        // Изменение статусов
        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1); // Ensure this method exists
        System.out.println("Статус эпика после завершения подзадачи: " + taskManager.getEpicById(epic1.getId()).getStatus());

        // Удаление подзадачи
        taskManager.deleteSubtask(subtask1.getId());
        System.out.println("После удаления подзадачи:");
        taskManager.getAllSubtasks().forEach(subtask -> System.out.println(subtask.getTitle() + " - " + subtask.getStatus()));

        // Удаление задачи
        taskManager.deleteTask(task1.getId());
        System.out.println("После удаления задачи:");
        taskManager.getAllTasks().forEach(task -> System.out.println(task.getTitle()));
    }
}
