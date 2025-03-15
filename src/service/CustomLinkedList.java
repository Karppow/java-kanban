package service;
import model.Task;

public class CustomLinkedList {
    private Node head;
    private Node tail;

    public Node add(Task task) {
        Node newNode = new Node(task);
        if (head == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        return newNode; // Возвращаем добавленный узел
    }

    public void remove(Node node) {
        if (node == null) return;

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next; // Удаляем первый элемент
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev; // Удаляем последний элемент
        }
    }

    public Node find(Task task) {
        Node current = head;
        while (current != null) {
            if (current.task.equals(task)) {
                return current;
            }
            current = current.next;
        }
        return null;
    }

    public Node getHead() {
        return head;
    }

    public Node getTail() {
        return tail;
    }

    public int size() {
        int size = 0;
        Node current = head;
        while (current != null) {
            size++;
            current = current.next;
        }
        return size;
    }

    public class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task) {
            this.task = task;
        }
    }
}