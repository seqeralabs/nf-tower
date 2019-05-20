import {TaskStatus} from "./task-status.enum";

export interface TaskData {

  taskId: number;
  status: TaskStatus;
  hash: string;
  name: string;
  exit: number;
  submit: number;
  start: number;
  process: string;
  tag?: any;
  module: any[];
  container: string;
  attempt: number;
  script: string;
  scratch?: any;
  workdir: string;
  queue?: any;
  cpus: number;
  memory?: any;
  disk?: any;
  time?: any;
  env?: any;
  errorAction: string;
  complete: number;
  duration: number;
  realtime: number;
  pcpu: number;
  rchar: number;
  wchar: number;
  syscr: number;
  syscw: number;
  readBytes: number;
  writeBytes: number;
  pmem: number;
  vmem: number;
  rss: number;
  peakVmem: number;
  peakRss: number;
  volCtxt: number;
  invCtxt: number;
  nativeId: number;
  workflowId: string;

}
