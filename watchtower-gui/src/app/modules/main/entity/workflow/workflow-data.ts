import {Manifest} from "./manifest";
import {Nextflow} from "./nextflow";
import {Stats} from "./stats";
import {Duration, Moment} from "moment";
import {WorkflowStatus} from "./workflow-status.enum";

export interface WorkflowData {

  workflowId: string;
  status: WorkflowStatus;

  runName: string;
  sessionId: string;

  manifest: Manifest;
  nextflow: Nextflow;
  stats: Stats;

  submitTime: Moment;
  startTime: Moment;
  completeTime?: Moment;

  projectDir: string;
  profile: string;
  homeDir: string;
  workDir: string;
  container: string;
  commitId: string;
  repository: string;
  containerEngine?: any;
  scriptFile: string;
  userName: string;
  launchDir: string;
  scriptId: string;
  revision: string;
  exitStatus: number;
  commandLine: string;
  resume: boolean;
  success: boolean;
  projectName: string;
  scriptName: string;
  duration: Duration;

  errorMessage?: any;
  errorReport?: any;

  params: any;
  configFiles: string[];
}
