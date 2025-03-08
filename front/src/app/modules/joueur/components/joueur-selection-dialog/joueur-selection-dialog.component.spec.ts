import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JoueurSelectionDialogComponent } from './joueur-selection-dialog.component';

describe('JoueurSelectionDialogComponent', () => {
  let component: JoueurSelectionDialogComponent;
  let fixture: ComponentFixture<JoueurSelectionDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [JoueurSelectionDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(JoueurSelectionDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
