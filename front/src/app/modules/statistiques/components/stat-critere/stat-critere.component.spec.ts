import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatCritereComponent } from './stat-critere.component';

describe('StatCritereComponent', () => {
  let component: StatCritereComponent;
  let fixture: ComponentFixture<StatCritereComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StatCritereComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StatCritereComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
