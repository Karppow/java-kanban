package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.io.File;

public class Main {
    public static void main(String[] args) {
// Создаем временный файл для автосохранения
        File file = new File("tasks.csv");

        // Создаем экземпляр FileBackedTaskManager
        FileBackedTaskManager taskManager = new FileBackedTaskManager(file);
        // Создание задач и получение их ID
        int taskId1 = taskManager.createTask(new Task("Переезд", "Организовать переезд на новую квартиру", TaskStatus.NEW));
        int taskId2 = taskManager.createTask(new Task("Купить мебель", "Купить новую мебель для квартиры", TaskStatus.NEW));

        // Создание эпиков и подзадач
        Epic epic1 = taskManager.createEpic(new Epic("Организация праздника", "Подготовить семейный праздник"));
        int subtask1 = taskManager.createSubtask(new Subtask("Пригласить гостей", "Составить список гостей", TaskStatus.NEW, epic1.getId()));
        int subtask2 = taskManager.createSubtask(new Subtask("Купить торт", "Заказать торт для праздника", TaskStatus.NEW, epic1.getId()));

        Epic epic2 = taskManager.createEpic(new Epic("Покупка квартиры", "Подготовить документы для покупки квартиры"));
        int subtask3 = taskManager.createSubtask(new Subtask("Собрать документы", "Собрать все необходимые документы", TaskStatus.NEW, epic2.getId()));

        // Печать всех задач
        System.out.println("Все задачи:");
        taskManager.getAllTasks().forEach(task -> System.out.println(task.getTitle() + " - " + task.getStatus()));
        System.out.println("Все подзадачи:");
        taskManager.getAllSubtasks().forEach(subtask -> System.out.println(subtask.getTitle() + " - " + subtask.getStatus()));
        System.out.println("Все эпики:");
        taskManager.getAllEpics().forEach(epic -> System.out.println(epic.getTitle() + " - " + epic.getStatus()));

        // Изменение статусов
        Subtask subtask1Obj = taskManager.getSubtaskById(subtask1); // Retrieve the subtask object by ID
        subtask1Obj.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1Obj); // Ensure this method exists
        System.out.println("Статус эпика после завершения подзадачи: " + taskManager.getEpicById(epic1.getId()).getStatus());

        // Удаление подзадачи
        taskManager.deleteSubtask(subtask1); // Use subtask1 instead of subtaskId1
        System.out.println("После удаления подзадачи:");
        taskManager.getAllSubtasks().forEach(subtask -> System.out.println(subtask.getTitle() + " - " + subtask.getStatus()));

        // Удаление задачи
        taskManager.deleteTask(taskId1);
        System.out.println("После удаления задачи:");
        taskManager.getAllTasks().forEach(task -> System.out.println(task.getTitle()));

        // Загрузка данных из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        System.out.println("Загруженные задачи после восстановления:");
        loadedManager.getAllTasks().forEach(task -> System.out.println(task.getTitle() + " - " + task.getStatus()));
        loadedManager.getAllEpics().forEach(epic -> System.out.println(epic.getTitle() + " - " + epic.getStatus()));
        loadedManager.getAllSubtasks().forEach(subtask -> System.out.println(subtask.getTitle() + " - " + subtask.getStatus()));
    }
}