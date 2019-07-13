import {Directive, HostBinding, Self} from '@angular/core';
import {NgControl} from '@angular/forms';

/*
 * Reconciliate Angular `ng-invalid` css class
 * with Bootstrap 4 `is-invalid` css class.
 * See:
 * https://stackoverflow.com/a/48004959/395921
 */

@Directive({
    selector: '[formControlName],[ngModel],[formControl]',
})
export class BootstrapValidationCssDirective {
    constructor(@Self() private cd: NgControl) {}

    @HostBinding('class.is-invalid')
    get isInvalid(): boolean {
        const control = this.cd.control;
        return control ? control.invalid && control.touched : false;
    }
}
