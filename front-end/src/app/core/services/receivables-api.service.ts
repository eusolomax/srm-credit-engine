import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import type {
  CreateReceivableRequest,
  Receivable,
} from '../models/receivable.model';

@Injectable({ providedIn: 'root' })
export class ReceivablesApiService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = '/api/receivables';

  create(request: CreateReceivableRequest): Observable<Receivable> {
    return this.http.post<Receivable>(this.endpoint, request);
  }

  getById(id: number): Observable<Receivable> {
    return this.http.get<Receivable>(`${this.endpoint}/${id}`);
  }

  getAll(): Observable<Receivable[]> {
    return this.http.get<Receivable[]>(this.endpoint);
  }
}
