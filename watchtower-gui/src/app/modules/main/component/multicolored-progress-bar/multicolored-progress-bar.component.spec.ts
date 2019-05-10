import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MulticoloredProgressBarComponent } from './multicolored-progress-bar.component';

describe('MulticoloredProgressBarComponent', () => {
  let component: MulticoloredProgressBarComponent;
  let fixture: ComponentFixture<MulticoloredProgressBarComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MulticoloredProgressBarComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MulticoloredProgressBarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
