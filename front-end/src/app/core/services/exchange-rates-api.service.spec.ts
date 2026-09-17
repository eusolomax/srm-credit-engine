import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ExchangeRatesApiService } from './exchange-rates-api.service';

describe('ExchangeRatesApiService', () => {
  let service: ExchangeRatesApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ExchangeRatesApiService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(ExchangeRatesApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('gets all exchange rates', () => {
    service.getAll().subscribe();

    const request = http.expectOne('/api/exchange-rates');

    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('posts the exchange rate payload', () => {
    const payload = {
      fromCurrency: 'USD' as const,
      toCurrency: 'BRL' as const,
      rate: 5.4321,
      effectiveAt: '2026-09-17T12:00:00.000Z',
    };

    service.create(payload).subscribe();

    const request = http.expectOne('/api/exchange-rates');

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush({});
  });
});
