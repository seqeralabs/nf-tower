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
export enum TaskStatus {

  NEW, SUBMITTED, RUNNING, CACHED, COMPLETED, FAILED, ABORTED

}

export function getAllTaskStatusesProgressStateTags(): string[] {
  return Object.keys(TaskStatus)
               .map(key => Number(key))
               .filter(key => !isNaN(key))
               .map(key => convertTaskStatusToProgressTag(key))
               .filter((key) => key != null);
}

export function convertTaskStatusToProgressTag(status: number | string): string {
  status = (typeof status === "string") ? TaskStatus[status] : status;

  if (status === TaskStatus.ABORTED) {
    return null;
  }
  if (status === TaskStatus.NEW) {
    return 'pending';
  }
  if (status === TaskStatus.COMPLETED) {
    return 'succeeded';
  }
  return TaskStatus[status].toLowerCase();
}

export function convertTaskStatusToProgressLabel(status: string): string {
  if( status === 'NEW')
    return 'PENDING';
  if( status === 'COMPLETED')
    return 'SUCCEEDED';
  else
    return status;
}

