import {FormControl} from "@angular/forms";

export  class NoSpaceValidator {

  static noSpace(control: FormControl): {[key: string]: boolean} {
    if (control.value && control.value.replace(/ +/g, "").length < 5) {
      return {noSpace: true};
    }
    return null;
  }

  static noNewLine(control: FormControl): {[key: string]: boolean} {
    if (control.value && control.value.replace(/\n+/g, "").length < 5) {
      return {noNewLine: true};
    }
    return null;
  }
}
