import {Component, Input, OnInit} from '@angular/core';
import {Task} from '../../entity/task/task';
import {groupBy, last, sumBy} from "lodash";
import {TaskStatus} from "../../entity/task/task-status.enum";
import {HumanizeDuration, HumanizeDurationLanguage, ILanguage} from "humanize-duration-ts";

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

  getNTotalProcessTasks(process: string): number {
    return this.tasksByProcess.get(process).length;
  }

  computeNProcessCompletedTasks(process: string): number {
    let tasks: Task[] = this.tasksByProcess.get(process);

    return tasks.filter((task: Task) => task.isCompleted).length
  }

  computePercentageProcessCompletedTasks(process: string): string {
    const nCompleted: number = this.computeNProcessCompletedTasks(process);
    const nTotal: number = this.getNTotalProcessTasks(process);

    const percentage: number = (nCompleted / nTotal) * 100;

    return `${percentage}%`;
  }

  computeTotalDurationProcessCompletedTasks(process: string): string {
    let tasks: Task[] = this.tasksByProcess.get(process).filter((task: Task) => task.isCompleted);
    let totalDuration: number = sumBy(tasks, (task: Task) => task.data.duration);

    let language: HumanizeDurationLanguage  = new HumanizeDurationLanguage();
    language.addLanguage('short', <ILanguage> {y: () => 'y', mo: () => 'mo', w: () => 'w', d: () => 'd', h: () => 'h', m: () => 'm', s: () => 's'});

    return new HumanizeDuration(language).humanize(totalDuration, {language: 'short', delimiter: ' '});
  }

  getProcessLastTask(process: string): Task {
    return last(this.tasksByProcess.get(process));
  }

}
