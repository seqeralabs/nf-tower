import {Component, Input, OnInit} from '@angular/core';
import {Task} from '../../entity/task/task';
import {groupBy} from "lodash";
import {TaskStatus} from "../../entity/task/task-status.enum";

@Component({
  selector: 'wt-tasks-processes',
  templateUrl: './tasks-processes.component.html',
  styleUrls: ['./tasks-processes.component.scss']
})
export class TasksProcessesComponent implements OnInit {

  @Input()
  tasks: Task[];

  tasksByProcess: Map<string, Task[]>;
  processes: string[];

  constructor() { }

  ngOnInit() {
    this.extractTasksByProcess();
  }


  extractTasksByProcess(): void {
    const tasksByProcessObject: any = groupBy(this.tasks, 'data.process');

    console.log('The tasks by process object', tasksByProcessObject);
    this.tasksByProcess = new Map(Object.entries(tasksByProcessObject));

    this.processes = Array.from(this.tasksByProcess.keys()).sort();
    console.log('The processes', this.processes);
  }

  computeNProcessCompletedTasks(process: string): number {
    let tasks: Task[] = this.tasksByProcess.get(process);

    return tasks.filter((task: Task) => task.data.status == TaskStatus.COMPLETED).length
  }

  computePercentageProcessCompletedTasks(process: string): string {
    const nCompleted: number = this.computeNProcessCompletedTasks(process);
    const nTotal: number = this.tasksByProcess.get(process).length;

    const percentage: number = (nCompleted / nTotal) * 100;

    return `${percentage}%`;
  }

}
