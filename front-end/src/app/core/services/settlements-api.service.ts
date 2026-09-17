import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import type {
  CreateSettlementRequest,
  Settlement,
  SettlementFilters,
} from '../models/settlement.model';

@Injectable({ providedIn: 'root' })
export class SettlementsApiService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = '/api/settlements';

  settle(receivableId: number, idempotencyKey: string): Observable<Settlement> {
    const request: CreateSettlementRequest = {
      receivableId,
      idempotencyKey,
    };

    return this.http.post<Settlement>(this.endpoint, request);
  }

  getAll(filters?: SettlementFilters): Observable<Settlement[]> {
    let params = new HttpParams();

    if (filters?.assignor) {
      params = params.set('assignor', filters.assignor);
    }

    if (filters?.currency) {
      params = params.set('currency', filters.currency);
    }

    if (filters?.from) {
      params = params.set('from', filters.from);
    }

    if (filters?.to) {
      params = params.set('to', filters.to);
    }

    return this.http.get<Settlement[]>(this.endpoint, { params });
  }
}
