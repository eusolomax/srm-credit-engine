import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import type {
  CreateExchangeRateRequest,
  ExchangeRate,
} from '../models/exchange-rate.model';

@Injectable({ providedIn: 'root' })
export class ExchangeRatesApiService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = '/api/exchange-rates';

  create(request: CreateExchangeRateRequest): Observable<ExchangeRate> {
    return this.http.post<ExchangeRate>(this.endpoint, request);
  }

  getAll(): Observable<ExchangeRate[]> {
    return this.http.get<ExchangeRate[]>(this.endpoint);
  }
}
