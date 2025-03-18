import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatCompositeComponent } from './stat-composite.component';

describe('StatCompositeComponent', () => {
  let component: StatCompositeComponent;
  let fixture: ComponentFixture<StatCompositeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StatCompositeComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(StatCompositeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
