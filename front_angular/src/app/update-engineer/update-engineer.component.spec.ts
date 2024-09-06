import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateEngineerComponent } from './update-engineer.component';

describe('UpdateEngineerComponent', () => {
  let component: UpdateEngineerComponent;
  let fixture: ComponentFixture<UpdateEngineerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdateEngineerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UpdateEngineerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
