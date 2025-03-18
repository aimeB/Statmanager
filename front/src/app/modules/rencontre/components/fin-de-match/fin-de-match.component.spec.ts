import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FinDeMatchComponent } from './fin-de-match.component';

describe('FinDeMatchComponent', () => {
  let component: FinDeMatchComponent;
  let fixture: ComponentFixture<FinDeMatchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FinDeMatchComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(FinDeMatchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
