package com.luv2code.springboot.demo.mycoolapp.rest;

import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@RestController
public class TaskTracker1 {

    private final String file_name = "tasks_saved.txt";
//    private final ObjectMapper mapper = new ObjectMapper();

    public static class Task {
        private String task_description;
        private int task_id;
        private boolean done;
        private boolean in_progress;
        private int in_which_list;
        private LocalDateTime created;
        private LocalDateTime updated;

        public Task(String task_description,  int id) {
            created = LocalDateTime.now();
            updated = created;
            this.task_description = task_description;
            this.task_id = id;
            this.done = false;
            this.in_progress = false;
            in_which_list = 2; //undone
        }

        public Task(int id, String description, boolean done, boolean in_progress, int in_which_list, LocalDateTime created, LocalDateTime updated) {
            this.task_id = id;
            this.task_description = description;
            this.done = done;
            this.in_progress = in_progress;
            this.in_which_list = in_which_list;
            this.created = created;
            this.updated = updated;
        }

        private void do_task() {
            in_which_list = 1; //done
            this.in_progress = false;
            this.done = true;
        }

        private void undo_task() {
            this.in_progress = false;
            this.done = false;
            in_which_list = 2; //undone
        }

        private void start_task() {
            this.in_progress = true;
            this.done = false;
            this.in_which_list = 3; //in progress
        }

        private void update_task(String new_desciption) {
            this.task_description = new_desciption;
            updated = LocalDateTime.now();
        }

        @Override
        public String toString() {
            return " Task : " + task_description + " Id : " + task_id + " Created : " + created + " Updated : " + updated +
                    " <br>";
        }

        public String getTask_description() {
            return task_description;
        }

        public int getTask_id() {
            return task_id;
        }

        public boolean isDone() {
            return done;
        }

        public boolean isIn_progress() {
            return in_progress;
        }

        public int getIn_which_list() {
            return in_which_list;
        }

        public LocalDateTime getCreated() {
            return created;
        }

        public LocalDateTime getUpdated() {
            return updated;
        }
    }

    int contor = 0;
    List<Task> task_list = new ArrayList<>();

    private void save_data() {
       try(PrintWriter writer = new PrintWriter(new FileWriter(file_name, false))) {
           for(Task t : task_list) {
               writer.println(t.task_id + "|" + t.task_description + "|" + t.in_which_list + "|"
               + t.done + "|" + t.in_progress + "|" + t.created + "|" + t.updated);
           }
       } catch(Exception e) {
           System.out.println("Error at saving data in teh text file : " +e.getMessage() + "\n");
       }
    }

    private void load_data() {
        try {
            File file = new File(file_name);
            if (file.exists()) {
                Scanner scanner = new Scanner(file);
                task_list.clear();
                int maximum_id = 0;

                while(scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if(line.trim().isEmpty()) {
                        continue;
                    }

                    String[] info = line.split("\\|");
                    int id = Integer.parseInt(info[0]);
                    String description = info[1];
                    int in_which_list = Integer.parseInt(info[2]);
                    boolean done = Boolean.parseBoolean(info[3]);
                    boolean in_progress = Boolean.parseBoolean(info[4]);
                    LocalDateTime created = LocalDateTime.parse(info[5]);
                    LocalDateTime updated = LocalDateTime.parse(info[6]);

                    Task task = new Task(id, description, done, in_progress,  in_which_list, created, updated);
                    task_list.add(task);

                    maximum_id = Math.max(maximum_id, id);
                }

                contor = maximum_id;
            }
        } catch(Exception e) {
            System.out.println("Error at loading data in teh text file : " +e.getMessage() + "\n");
        }
    }

    TaskTracker1() {
        load_data();
    }

    @GetMapping("/tasks")
    public String get_intro() {
        return "For seeing the tasks per cathegories go to /list?status=help for information. <br>" +
                "Go to /mark-in-progress?id=.. to mark an task as in progress<br>" +
                "Go to /mark-done?id=.. to mark a task as done <br>" +
                "Go to /add?task=.. to add a new task to the list<br>" +
                "Go to /list to list all the tasks and their status<br>" +
                "Go to /update?id=..&task=.. to update an task.";
    }

    @GetMapping("/list")
    public Object get_tasks(@RequestParam(required = false) String status) {
        if(status == null) {
            return this.task_list;
        }

        if(status.equalsIgnoreCase("help")) {
            return "status=done for tasks that are done <br>status=prog for tasks that are in progress <br>status=todo for tasks that are not started yet" +
                    " <br>status=help for help";
        }

        int st = switch(status) {
            case "done" : {yield 1; }
            case "undone" : {yield 2;}
            case "prog" : {yield 3;}
            default: {yield 0;}
        };
        if(st == 0) {
            return "Try help..";
        }
        List<Task> return_list = new ArrayList<>();
        for(Task task : task_list) {
            if(task.in_which_list == st) {
                return_list.add(task);
            }
        }

        if(return_list.isEmpty()) {
            return "List empty...";
        }

        return return_list;
    }

    @GetMapping("/add") //PostMapping
    public String add_task(@RequestParam String task) {
       for(Task t : task_list) {
           if(t.task_description.equalsIgnoreCase(task)) {
               return "Task already existent... Id = " + t.task_id;
           }
       }
       contor++;
       task_list.add(new Task(task, contor));
        save_data();
        return "Task added successfully.. Id = " + contor;
    }

    @GetMapping("/mark-in-progress") //PatchMapping
    public String mark_in_progress(@RequestParam int id) {
        if(id > contor) {
            return "There is no task with such id..";
        }

        for(Task task : task_list) {
            if(task.task_id == id) {
                task.start_task();
                save_data();
                return "Task started successfully...";
            }
        }

        return "There is no task with such id..";
    }

    @GetMapping("/mark-done") //PatchMapping
    public String mark_as_done(@RequestParam int id) {
        if(id > contor) {
            return "There is no task with such id..";
        }

        for(Task task : task_list) {
            if(task.task_id == id) {
                task.do_task();
                save_data();
                return "Task marked as done...";
            }
        }

        return "There is no task with such id..";
    }

    @GetMapping("/update") //PutMapping
    public String update_task(@RequestParam int id, @RequestParam String task) {
        if(id > contor) {
            return "There is no task with such id..";
        }

        for(Task t : task_list) {
            if(t.task_id == id) {
                t.update_task(task);
                save_data();
                return "Task updated successfully!";
            }
        }

        return "There is no task with such id..";
    }

    @GetMapping("/delete") //DeleteMapping
    public String delete_task(@RequestParam int id) {
        boolean was_deleted = task_list.removeIf(t -> t.task_id == id);
        if(was_deleted ) {
            save_data();
            return "Task deleted successfully";
        }
        return "There is no id with such id..";
    }
}
