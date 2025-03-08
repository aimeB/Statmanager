import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetailTerrainComponent } from './detail-terrain.component';

describe('DetailTerrainComponent', () => {
  let component: DetailTerrainComponent;
  let fixture: ComponentFixture<DetailTerrainComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetailTerrainComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DetailTerrainComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
