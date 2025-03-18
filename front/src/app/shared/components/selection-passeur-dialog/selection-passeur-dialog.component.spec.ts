import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectionPasseurDialogComponent } from './selection-passeur-dialog.component';

describe('SelectionPasseurDialogComponent', () => {
  let component: SelectionPasseurDialogComponent;
  let fixture: ComponentFixture<SelectionPasseurDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SelectionPasseurDialogComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(SelectionPasseurDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
