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
