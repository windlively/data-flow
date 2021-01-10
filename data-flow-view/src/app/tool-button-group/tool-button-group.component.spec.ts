import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ToolButtonGroupComponent } from './tool-button-group.component';

describe('ToolButtonGroupComponent', () => {
  let component: ToolButtonGroupComponent;
  let fixture: ComponentFixture<ToolButtonGroupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ToolButtonGroupComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ToolButtonGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
