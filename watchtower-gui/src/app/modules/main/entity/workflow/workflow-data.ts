import {Manifest} from "./manifest";
import {Nextflow} from "./nextflow";
import {Stats} from "./stats";
import {WorkflowStatus} from "./workflow-status.enum";

export interface WorkflowData {

  workflowId: string | number;

  runName: string;
  sessionId: string;

  manifest: Manifest;
  nextflow: Nextflow;
  stats: Stats;

  submit: Date;
  start: Date;
  complete?: Date;
  duration: number;

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

  errorMessage?: string;
  errorReport?: string;

  params: any;
  configFiles: string[];
}
