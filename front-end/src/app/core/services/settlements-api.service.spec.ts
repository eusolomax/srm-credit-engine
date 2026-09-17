import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { SettlementsApiService } from './settlements-api.service';

describe('SettlementsApiService', () => {
  let service: SettlementsApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        SettlementsApiService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(SettlementsApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('posts only the receivable id and the supplied idempotency key', () => {
    service.settle(123, 'uuid-a').subscribe();

    const request = http.expectOne('/api/settlements');

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      receivableId: 123,
      idempotencyKey: 'uuid-a',
    });

    request.flush({});
  });
});
