import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClotureCotationDialogComponent } from './cloture-cotation-dialog.component';

describe('ClotureCotationDialogComponent', () => {
  let component: ClotureCotationDialogComponent;
  let fixture: ComponentFixture<ClotureCotationDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClotureCotationDialogComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ClotureCotationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
