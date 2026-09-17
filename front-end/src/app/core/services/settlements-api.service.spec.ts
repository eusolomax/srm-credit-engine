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

  it('sends all provided settlement filters as query parameters', () => {
    service
      .getAll({
        from: '2026-09-01',
        to: '2026-09-30',
        assignor: '12345678901',
        currency: 'BRL',
      })
      .subscribe();

    const request = http.expectOne(
      (pendingRequest) =>
        pendingRequest.url === '/api/settlements' &&
        pendingRequest.method === 'GET',
    );

    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('from')).toBe('2026-09-01');
    expect(request.request.params.get('to')).toBe('2026-09-30');
    expect(request.request.params.get('assignor')).toBe('12345678901');
    expect(request.request.params.get('currency')).toBe('BRL');

    request.flush([]);
  });
});
