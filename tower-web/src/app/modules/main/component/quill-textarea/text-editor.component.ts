import {Component, EventEmitter, Input, OnChanges, Output} from '@angular/core';
import {FormControl, Validators} from "@angular/forms";

@Component({
  selector: 'app-text-editor',
  templateUrl: './text-editor.component.html',
  styleUrls: ['./text-editor.component.scss'],
})
export class TextEditorComponent implements OnChanges{

  @Input() default_value = '';
  @Input() max_length: number;
  @Input() successPush: boolean;
  @Input() showError: boolean;
  @Output() textChanged = new EventEmitter();
  @Output() textInvalid = new EventEmitter();
  htmlFC: FormControl = new FormControl('', [
    Validators.required,
    Validators.minLength(5)
  ]);

  private text = '';

  constructor() {
  }

  set html(value) {
    if (value) {
      this.text = value.replace(/&nbsp;/g, ' ') || '';
    }
  }
​
  get html() {
    return this.text.replace(/&nbsp;/g, ' ') || '';
  }

  ngOnChanges(): void {
    if (this.default_value) {
      this.text = this.default_value.replace(/&nbsp;/g, ' ') || '';
    }
  }
​
  onEditText(event) {
    this.textChanged.emit(event);
    this.htmlFC.invalid
      ? this.textInvalid.emit(false)
      : this.textInvalid.emit(true);

    if (this.successPush) {
      this.htmlFC.setErrors(null);
    }
  }

}
