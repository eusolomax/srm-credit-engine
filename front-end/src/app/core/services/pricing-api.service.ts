import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import type {
  CreatePricingSimulationRequest,
  PricingSimulationResponse,
} from '../models/pricing.model';

@Injectable({ providedIn: 'root' })
export class PricingApiService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = '/api/pricing/simulate';

  simulate(
    request: CreatePricingSimulationRequest,
  ): Observable<PricingSimulationResponse> {
    return this.http.post<PricingSimulationResponse>(this.endpoint, request);
  }
}
