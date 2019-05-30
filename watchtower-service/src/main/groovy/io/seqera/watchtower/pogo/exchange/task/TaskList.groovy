package io.seqera.watchtower.pogo.exchange.task

class TaskList {

    List<TaskGet> tasks

    static TaskList of(List<TaskGet> tasks) {
        new TaskList(tasks: tasks)
    }

}
