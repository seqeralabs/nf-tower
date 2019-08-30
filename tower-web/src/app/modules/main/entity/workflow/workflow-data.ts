/*
 * Copyright (c) 2019, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
import {Manifest} from "./manifest";
import {Nextflow} from "./nextflow";
import {Stats} from "./stats";
import {WorkflowStatus} from "./workflow-status.enum";

export interface WorkflowData {

  workflowId: string;

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
  configText: string;

  peakLoadCpus: number;
  peakLoadTasks: number;
  peakLoadMemory: number;
}
