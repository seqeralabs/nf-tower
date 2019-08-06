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

export abstract class DurationUtil {

  private static durationHumanizer: HumanizeDuration;

  public static initialize() {
    console.log('Initializing duration util');
    const language: HumanizeDurationLanguage  = new HumanizeDurationLanguage();
    language.addLanguage('short', <ILanguage> {y: () => 'y', mo: () => 'mo', w: () => 'w', d: () => 'd', h: () => 'h', m: () => 'm', s: () => 's'});
    this.durationHumanizer = new HumanizeDuration(language);
  }

  public static humanizeDuration(durationMillis: number): string {
    return this.durationHumanizer.humanize(durationMillis, {language: 'short', delimiter: ' '});
  }

}
DurationUtil.initialize();
