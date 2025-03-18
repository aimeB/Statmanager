import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatTopJoueurComponent } from './stat-top-joueur.component';

describe('StatTopJoueurComponent', () => {
  let component: StatTopJoueurComponent;
  let fixture: ComponentFixture<StatTopJoueurComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StatTopJoueurComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(StatTopJoueurComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
