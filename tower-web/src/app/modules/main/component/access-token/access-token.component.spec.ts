import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AccessTokenComponent } from './access-token.component';

describe('AccessTokenComponent', () => {
  let component: AccessTokenComponent;
  let fixture: ComponentFixture<AccessTokenComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AccessTokenComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessTokenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
