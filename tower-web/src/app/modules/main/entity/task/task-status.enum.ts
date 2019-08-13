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

export function toProgressTag(statusKey: number): string {
  if (statusKey == TaskStatus.NEW) {
    return 'pending'
  }
  if (statusKey == TaskStatus.COMPLETED) {
    return 'succeeded'
  }
  return TaskStatus[statusKey].toLowerCase()
}

export function getAllTaskStatusesKeys(): TaskStatus[] {
  return Object.keys(TaskStatus)
               .map(key => Number(key))
               .filter(key => !isNaN(key))

}
