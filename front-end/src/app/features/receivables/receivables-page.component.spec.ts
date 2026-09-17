import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Observable, of, Subject } from 'rxjs';

import type { Receivable } from '../../core/models/receivable.model';
import type { Settlement } from '../../core/models/settlement.model';
import { ReceivablesApiService } from '../../core/services/receivables-api.service';
import { SettlementsApiService } from '../../core/services/settlements-api.service';
import { ReceivablesPageComponent } from './receivables-page.component';

interface PendingSettlementRequest {
  receivableId: number;
  idempotencyKey: string;
  response: Subject<Settlement>;
}

class ReceivablesApiStub {
  getAll(): Observable<Receivable[]> {
    return of([
      createReceivable(1, 'AVAILABLE'),
      createReceivable(2, 'AVAILABLE'),
      createReceivable(3, 'SETTLED'),
    ]);
  }
}

class SettlementsApiStub {
  readonly requests: PendingSettlementRequest[] = [];

  settle(receivableId: number, idempotencyKey: string): Observable<Settlement> {
    const response = new Subject<Settlement>();
    this.requests.push({ receivableId, idempotencyKey, response });
    return response.asObservable();
  }
}

function createReceivable(
  id: number,
  status: Receivable['status'],
): Receivable {
  return {
    id,
    faceValue: 100000,
    assignor: '12345678901',
    type: 'DUPLICATA',
    paymentCurrency: 'BRL',
    termMonths: 3,
    dueDate: '2026-12-31',
    status,
    createdAt: '2026-09-17T12:00:00Z',
  };
}

function createSettlement(receivableId: number): Settlement {
  return {
    receivableId,
    assignor: '12345678901',
    presentValue: 92859.94,
    discount: 7140.06,
    paymentCurrency: 'BRL',
    exchangeRate: null,
    settledAt: '2026-09-17T12:00:00Z',
  };
}

describe('ReceivablesPageComponent settlement flow', () => {
  let fixture: ComponentFixture<ReceivablesPageComponent>;
  let settlementsApi: SettlementsApiStub;
  let confirmSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(async () => {
    settlementsApi = new SettlementsApiStub();

    await TestBed.configureTestingModule({
      imports: [ReceivablesPageComponent],
      providers: [
        { provide: ReceivablesApiService, useClass: ReceivablesApiStub },
        { provide: SettlementsApiService, useValue: settlementsApi },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ReceivablesPageComponent);
    confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
    vi.stubGlobal('crypto', {
      randomUUID: vi
        .fn()
        .mockReturnValueOnce('uuid-a')
        .mockReturnValueOnce('uuid-b'),
    });
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  afterEach(() => {
    confirmSpy.mockRestore();
    vi.unstubAllGlobals();
  });

  it('shows settlement action only for AVAILABLE receivables', () => {
    expect(actionButton(fixture, 1)).toBeTruthy();
    expect(actionButton(fixture, 2)).toBeTruthy();
    expect(actionButton(fixture, 3)).toBeNull();
  });

  it('confirms the operation and sends receivableId with an idempotencyKey', () => {
    actionButton(fixture, 1)?.click();

    expect(confirmSpy).toHaveBeenCalledWith(
      'Deseja liquidar o recebível #1?',
    );
    expect(settlementsApi.requests).toHaveLength(1);
    expect(settlementsApi.requests[0].receivableId).toBe(1);
    expect(settlementsApi.requests[0].idempotencyKey).toBe('uuid-a');
  });

  it('updates the receivable to SETTLED after a successful settlement', () => {
    actionButton(fixture, 1)?.click();
    settlementsApi.requests[0].response.next(createSettlement(1));
    settlementsApi.requests[0].response.complete();
    fixture.detectChanges();

    expect(actionButton(fixture, 1)).toBeNull();
    expect(fixture.nativeElement.textContent).toContain(
      'Recebível #1 liquidado com sucesso.',
    );
  });

  it('reuses the same idempotencyKey when retrying after an error', () => {
    actionButton(fixture, 1)?.click();
    settlementsApi.requests[0].response.error(new Error('temporary error'));
    fixture.detectChanges();

    expect(actionButton(fixture, 1)?.textContent).toContain('Tentar novamente');

    actionButton(fixture, 1)?.click();

    expect(settlementsApi.requests).toHaveLength(2);
    expect(settlementsApi.requests[1].receivableId).toBe(1);
    expect(settlementsApi.requests[1].idempotencyKey).toBe(
      settlementsApi.requests[0].idempotencyKey,
    );
  });

  it('generates a new idempotencyKey for a new settlement after success', () => {
    actionButton(fixture, 1)?.click();
    settlementsApi.requests[0].response.next(createSettlement(1));
    settlementsApi.requests[0].response.complete();
    fixture.detectChanges();

    actionButton(fixture, 2)?.click();

    expect(settlementsApi.requests[1].idempotencyKey).toBe('uuid-b');
    expect(settlementsApi.requests[1].idempotencyKey).not.toBe(
      settlementsApi.requests[0].idempotencyKey,
    );
  });
});

function actionButton(
  fixture: ComponentFixture<ReceivablesPageComponent>,
  id: number,
): HTMLButtonElement | null {
  return fixture.nativeElement.querySelector(
    `[data-receivable-id="${id}"]`,
  ) as HTMLButtonElement | null;
}
