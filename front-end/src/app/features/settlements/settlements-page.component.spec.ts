import { ComponentFixture, TestBed } from '@angular/core/testing';
import { WritableSignal } from '@angular/core';
import { Observable, Subject } from 'rxjs';

import type {
  Settlement,
  SettlementFilters,
} from '../../core/models/settlement.model';
import { SettlementsApiService } from '../../core/services/settlements-api.service';
import { SettlementsPageComponent } from './settlements-page.component';

interface TestFiltersModel {
  from: string;
  to: string;
  assignor: string;
  currency: 'BRL' | 'USD' | '';
}

interface PendingQuery {
  filters: SettlementFilters | undefined;
  response: Subject<Settlement[]>;
}

class SettlementsApiStub {
  readonly requests: PendingQuery[] = [];

  getAll(filters?: SettlementFilters): Observable<Settlement[]> {
    const response = new Subject<Settlement[]>();
    this.requests.push({ filters, response });
    return response.asObservable();
  }
}

function createSettlement(receivableId: number): Settlement {
  return {
    receivableId,
    assignor: '12345678901',
    faceValue: 100000,
    presentValue: 92859.94,
    discount: 7140.06,
    paymentCurrency: 'BRL',
    exchangeRate: null,
    settledAt: '2026-09-17T12:00:00Z',
  };
}

describe('SettlementsPageComponent', () => {
  let fixture: ComponentFixture<SettlementsPageComponent>;
  let settlementsApi: SettlementsApiStub;
  let page: { filtersModel: WritableSignal<TestFiltersModel> };

  beforeEach(async () => {
    settlementsApi = new SettlementsApiStub();

    await TestBed.configureTestingModule({
      imports: [SettlementsPageComponent],
      providers: [{ provide: SettlementsApiService, useValue: settlementsApi }],
    }).compileComponents();

    fixture = TestBed.createComponent(SettlementsPageComponent);
    page = fixture.componentInstance as unknown as {
      filtersModel: WritableSignal<TestFiltersModel>;
    };
    fixture.detectChanges();
  });

  it('renders the statement controls without querying automatically', () => {
    expect(fixture.nativeElement.querySelector('h1')?.textContent).toContain(
      'Extrato de liquidações',
    );
    expect(fixture.nativeElement.querySelector('#from')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('#to')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('#assignor')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('#currency')).toBeTruthy();
    expect(settlementsApi.requests).toHaveLength(0);
  });

  it('queries all settlements without filters and shows loading and results', () => {
    submitQuery();

    expect(settlementsApi.requests[0].filters).toEqual({});
    expect(fixture.nativeElement.textContent).toContain(
      'Carregando liquidações...',
    );

    settlementsApi.requests[0].response.next([createSettlement(10)]);
    settlementsApi.requests[0].response.complete();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('#10');
    expect(fixture.nativeElement.textContent).toContain('100.000,00');
    expect(fixture.nativeElement.textContent).toContain('92.859,94');
  });

  it('sends the filled filters to the API service', () => {
    page.filtersModel.set({
      from: '2026-09-01',
      to: '2026-09-30',
      assignor: '12345678901',
      currency: 'BRL',
    });
    fixture.detectChanges();

    submitQuery();

    expect(settlementsApi.requests[0].filters).toEqual({
      from: '2026-09-01',
      to: '2026-09-30',
      assignor: '12345678901',
      currency: 'BRL',
    });
  });

  it('shows an empty state when the API returns no settlements', () => {
    submitQuery();
    settlementsApi.requests[0].response.next([]);
    settlementsApi.requests[0].response.complete();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain(
      'Nenhuma liquidação encontrada.',
    );
  });

  it('shows an API error', () => {
    submitQuery();
    settlementsApi.requests[0].response.error(new Error('request failed'));
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain(
      'Não foi possível carregar o extrato.',
    );
  });

  it('clears filters and queries all settlements again', () => {
    page.filtersModel.set({
      from: '2026-09-01',
      to: '2026-09-30',
      assignor: '12345678901',
      currency: 'USD',
    });
    fixture.detectChanges();
    submitQuery();

    settlementsApi.requests[0].response.next([]);
    settlementsApi.requests[0].response.complete();
    fixture.detectChanges();

    clearFilters();

    expect(page.filtersModel()).toEqual({
      from: '',
      to: '',
      assignor: '',
      currency: '',
    });
    expect(settlementsApi.requests[1].filters).toEqual({});
  });

  function submitQuery(): void {
    const button = fixture.nativeElement.querySelector(
      'button[type="submit"]',
    ) as HTMLButtonElement;
    button.click();
    fixture.detectChanges();
  }

  function clearFilters(): void {
    const button = fixture.nativeElement.querySelector(
      'button[type="button"]',
    ) as HTMLButtonElement;
    button.click();
    fixture.detectChanges();
  }
});
