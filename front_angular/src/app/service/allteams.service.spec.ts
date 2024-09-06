import { TestBed } from '@angular/core/testing';

import { AllteamsService } from './allteams.service';

describe('AllteamsService', () => {
  let service: AllteamsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AllteamsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
