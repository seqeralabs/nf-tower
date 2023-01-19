import { CommonModule } from '@angular/common';
import { Component, Inject, NgModule } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef, MatFormFieldModule, MatInputModule } from '@angular/material';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Observable } from 'rxjs';
import { map, startWith } from 'rxjs/operators';

@Component({
  selector: 'wt-confirm-delete-dialog',
  templateUrl: './confirm-delete-dialog.component.html',
  styleUrls: ['./confirm-delete-dialog.component.scss']
})
export class ConfirmDeleteDialogComponent {

  public control: FormControl = new FormControl();
  public message!: string;
  public noMatch$: Observable<boolean> = this.control.valueChanges.pipe(startWith([undefined]), map(value => value !== this.data.runName));

  constructor(@Inject(MAT_DIALOG_DATA) private data: {runName: string}, private dialogRef: MatDialogRef<ConfirmDeleteDialogComponent>) {
    this.message = `Please confirm the deletion of the workflow '${data.runName}' typing its name below (operation is not recoverable):`
  }

  public confirmDelete(): void {
    this.dialogRef.close(true);
  }
}


@NgModule({
  imports: [
    CommonModule,
    MatDialogModule,
    NoopAnimationsModule,
    ReactiveFormsModule,
    MatInputModule,
    MatFormFieldModule
  ],
  declarations: [ConfirmDeleteDialogComponent],
  exports: [ConfirmDeleteDialogComponent, MatDialogModule]
})
export class ConfirmDeleteDialogModule{

}
