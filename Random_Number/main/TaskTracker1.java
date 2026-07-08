package com.luv2code.springboot.demo.mycoolapp.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class TaskTracker1 {
    private class Task {
        private String task_description;
        private int task_id;
        private boolean done;
        private boolean in_progress;
        private int in_which_list;

        public Task(String task_description,  int id) {
            this.task_description = task_description;
            this.task_id = id;
            this.done = false;
            this.in_progress = false;
            in_which_list = 2; //undone
        }

        public void do_task() {
            in_which_list = 1; //done
            this.in_progress = false;
            this.done = true;
        }

        public void undo_task() {
            this.in_progress = false;
            this.done = false;
            in_which_list = 2; //undone
        }

        public void start_task() {
            this.in_progress = true;
            this.done = false;
            this.in_which_list = 3; //in progress
        }

        @Override
        public String toString() {
            return "{ Task : " + task_description + " Id : " + task_id + "} <br>";
        }

    }


    int contor = 0;
    List<Task> task_list = new ArrayList<>();

    @GetMapping("/tasks")
    public String get_intro() {
        return "For seeing the tasks go to /show?status=help for information. <br>" +
                "Go to /mark progress?id=.. to mark an task as in progress<br>";
    }

    @GetMapping("/show")
    public Object get_tasks(@RequestParam String status) {
        if(status.equalsIgnoreCase("help")) {
            return "status=done for tasks that are done <br>status=prog for tasks that are in progress <br>status=undone for tasks that are not started yet" +
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

    @GetMapping("/add")
    String add_task(@RequestParam String task) {
       for(Task t : task_list) {
           if(t.task_description.equalsIgnoreCase(task)) {
               return "Task already existent... Id = " + t.task_id;
           }
       }
       contor++;
       task_list.add(new Task(task, contor));
       return "Task added successfully.. Id = " + contor;
    }

    @GetMapping("/mark progress")
    public String mark_in_progress(@RequestParam int id) {
        if(id > contor) {
            return "Task not found..";
        }

        for(Task task : task_list) {
            if(task.task_id == id) {
                task.start_task();
                return "Task started successfully...";
            }
        }

        return "Task not found..";
    }

    @GetMapping("/done")
    public String mark_as_done(@RequestParam int id) {
        if(id > contor) {
            return "Task not found..";
        }

        for(Task task : task_list) {
            if(task.task_id == id) {
                task.do_task();
                return "Task marked as done...";
            }
        }

        return "Task not found..";
    }

    @GetMapping("/undo")
    public String undo_task(@RequestParam int id) {
        if(id > contor) {
            return "Task not found..";
        }

        for(Task task : task_list) {
            if(task.task_id == id) {
                task.undo_task();
                return "Task marked as undone...";
            }
        }

        return "Task not found..";
    }
}
