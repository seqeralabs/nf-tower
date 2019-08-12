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
import {HumanizeDuration, HumanizeDurationLanguage, ILanguage} from "humanize-duration-ts";
import * as dateFormat from "date-fns/format";
import * as filesize from "file-size"

export abstract class FormatterUtil {

  private static durationHumanizer: HumanizeDuration;

  static initialize() {
    console.log('Initializing humanizer util');
    const language: HumanizeDurationLanguage  = new HumanizeDurationLanguage();
    language.addLanguage('short', <ILanguage> {y: () => 'y', mo: () => 'mo', w: () => 'w', d: () => 'd', h: () => 'h', m: () => 'm', s: () => 's'});
    this.durationHumanizer = new HumanizeDuration(language);
  }

  static humanizeDuration(durationMillis: number): string {
    return this.durationHumanizer.humanize(durationMillis, {language: 'short', delimiter: ' '});
  }

  static humanizeStorageCapacity(storageBytes: number, unit?: string): string {
    const humanizedStorage: string = unit ? `${filesize(storageBytes).to(unit, 'jedec')} ${unit}` : filesize(storageBytes).human('jedec');

    return humanizedStorage.replace('Bytes', 'B');
  }

  static formatDate(date: Date, format?: string): string {
    if (!format) {
      format = 'ddd MMM D YYYY hh:mm:ss'
    }
    return dateFormat(date, format);
  }

  static convertDurationToHours(durationMillis: number): string {
    return (durationMillis / (1000 * 60 * 60)).toFixed(2);
  }

}
FormatterUtil.initialize();
