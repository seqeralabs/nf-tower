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

export class WorkflowRunRequest {
  runName: string;
  project: string;
  workDir: string;
  queueName: string;
  cloudRegion: string;
  accessKey: string;
  secretKey: string;
  endpoint: string;
  profile: string;
  config: string;
  jobRole: string;
  resume: boolean;
  revision: string;
  timestamp: Date;
}

export interface WorkflowRunResponse {
  message: string;
  workflowId: string;
}
