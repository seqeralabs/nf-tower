import {Manifest} from "./manifest";
import {Nextflow} from "./nextflow";
import {Stats} from "./stats";

export interface WorkflowData {

  workflowId: string;
  status: string;

  runName: string;
  sessionId: string;

  manifest: Manifest;
  nextflow: Nextflow;
  stats: Stats;

  submitTime: Date;
  startTime: Date;
  completeTime?: Date;

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
  duration: number;

  errorMessage?: any;
  errorReport?: any;

  params: any;
  configFiles: string[];
}
