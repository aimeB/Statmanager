import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RencontreSelectionComponent } from './rencontre-selection.component';

describe('RencontreSelectionComponent', () => {
  let component: RencontreSelectionComponent;
  let fixture: ComponentFixture<RencontreSelectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RencontreSelectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RencontreSelectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
