import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TimePlaySelectionDialogComponent } from './time-play-selection-dialog.component';

describe('TimePlaySelectionDialogComponent', () => {
  let component: TimePlaySelectionDialogComponent;
  let fixture: ComponentFixture<TimePlaySelectionDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TimePlaySelectionDialogComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TimePlaySelectionDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
