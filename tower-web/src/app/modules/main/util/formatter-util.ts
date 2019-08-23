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
    return this.durationHumanizer.humanize(durationMillis, {language: 'short', delimiter: ' ', round: true});
  }

  static humanizeStorageCapacity(storageBytes: number, decimals: number = 2, unit?: string): string {
    if( storageBytes == null || storageBytes == 0 )
      return '';
    const filesizeHandler = filesize(storageBytes, {fixed: decimals});
    const humanizedStorage: string = unit ? `${filesizeHandler.to(unit, 'jedec')} ${unit}` : filesizeHandler.human('jedec');
    return humanizedStorage.replace('Bytes', 'B');
  }

  static formatDate(date: string | number | Date, format?: string): string {
    if (date==null || date==0) {
      return '';
    }
    const dateInstance: Date = new Date(date);
    if (!format) {
      format = 'YYYY-MM-DD hh:mm:ss'
    }
    return dateFormat(dateInstance, format);
  }

  static convertDurationToHours(durationMillis: number): string {
    return (durationMillis / (1000 * 60 * 60)).toFixed(1);
  }

}
FormatterUtil.initialize();
