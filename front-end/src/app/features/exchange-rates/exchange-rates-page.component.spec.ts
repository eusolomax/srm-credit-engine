import { ComponentFixture, TestBed } from '@angular/core/testing';
import { WritableSignal } from '@angular/core';
import { Observable, Subject } from 'rxjs';

import type {
  CreateExchangeRateRequest,
  ExchangeRate,
} from '../../core/models/exchange-rate.model';
import { ExchangeRatesApiService } from '../../core/services/exchange-rates-api.service';
import { ExchangeRatesPageComponent } from './exchange-rates-page.component';

interface TestFormModel {
  fromCurrency: 'BRL' | 'USD' | '';
  toCurrency: 'BRL' | 'USD' | '';
  rate: number | null;
  effectiveAt: string;
}

class ExchangeRatesApiStub {
  readonly listResponses: Subject<ExchangeRate[]>[] = [];
  readonly createRequests: {
    request: CreateExchangeRateRequest;
    response: Subject<ExchangeRate>;
  }[] = [];

  getAll(): Observable<ExchangeRate[]> {
    const response = new Subject<ExchangeRate[]>();
    this.listResponses.push(response);
    return response.asObservable();
  }

  create(request: CreateExchangeRateRequest): Observable<ExchangeRate> {
    const response = new Subject<ExchangeRate>();
    this.createRequests.push({ request, response });
    return response.asObservable();
  }
}

function createExchangeRate(): ExchangeRate {
  return {
    fromCurrency: 'USD',
    toCurrency: 'BRL',
    rate: 5.4321,
    effectiveAt: '2026-09-17T12:00:00Z',
    createdAt: '2026-09-17T12:00:00Z',
  };
}

describe('ExchangeRatesPageComponent', () => {
  let fixture: ComponentFixture<ExchangeRatesPageComponent>;
  let exchangeRatesApi: ExchangeRatesApiStub;
  let page: { formModel: WritableSignal<TestFormModel> };

  beforeEach(async () => {
    exchangeRatesApi = new ExchangeRatesApiStub();

    await TestBed.configureTestingModule({
      imports: [ExchangeRatesPageComponent],
      providers: [{ provide: ExchangeRatesApiService, useValue: exchangeRatesApi }],
    }).compileComponents();

    fixture = TestBed.createComponent(ExchangeRatesPageComponent);
    page = fixture.componentInstance as unknown as {
      formModel: WritableSignal<TestFormModel>;
    };
    fixture.detectChanges();
  });

  it('renders the page and starts loading the list', () => {
    expect(fixture.nativeElement.querySelector('h1')?.textContent).toContain(
      'Taxas de câmbio',
    );
    expect(fixture.nativeElement.querySelector('#fromCurrency')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('#toCurrency')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('#rate')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('#effectiveAt')).toBeTruthy();
    expect(exchangeRatesApi.listResponses).toHaveLength(1);
    expect(fixture.nativeElement.textContent).toContain('Carregando taxas...');
  });

  it('shows rates returned by the API', () => {
    exchangeRatesApi.listResponses[0].next([createExchangeRate()]);
    exchangeRatesApi.listResponses[0].complete();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('USD');
    expect(fixture.nativeElement.textContent).toContain('BRL');
    expect(fixture.nativeElement.textContent).toContain('5,4321');
    expect(fixture.nativeElement.textContent).toContain('17/09/2026 12:00');
  });

  it('shows the empty state when no rates exist', () => {
    exchangeRatesApi.listResponses[0].next([]);
    exchangeRatesApi.listResponses[0].complete();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Nenhuma taxa cadastrada.');
  });

  it('shows an error when loading rates fails', () => {
    exchangeRatesApi.listResponses[0].error(new Error('request failed'));
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain(
      'Não foi possível carregar as taxas.',
    );
  });

  it('does not submit when required fields are empty or rate is invalid', () => {
    completeInitialList();

    submitForm();
    expect(exchangeRatesApi.createRequests).toHaveLength(0);

    page.formModel.set({
      fromCurrency: 'USD',
      toCurrency: 'BRL',
      rate: 0,
      effectiveAt: '2026-09-17T12:00:00',
    });
    fixture.detectChanges();
    submitForm();

    expect(exchangeRatesApi.createRequests).toHaveLength(0);
  });

  it('rejects equal source and destination currencies', () => {
    completeInitialList();
    page.formModel.set({
      fromCurrency: 'USD',
      toCurrency: 'USD',
      rate: 5.4321,
      effectiveAt: '2026-09-17T12:00:00',
    });
    fixture.detectChanges();

    submitForm();

    expect(exchangeRatesApi.createRequests).toHaveLength(0);
    expect(fixture.nativeElement.textContent).toContain(
      'A moeda de origem deve ser diferente da moeda de destino.',
    );
  });

  it('submits the valid exchange rate payload', () => {
    completeInitialList();
    setValidForm();

    submitForm();

    expect(exchangeRatesApi.createRequests).toHaveLength(1);
    expect(exchangeRatesApi.createRequests[0].request).toEqual({
      fromCurrency: 'USD',
      toCurrency: 'BRL',
      rate: 5.4321,
      effectiveAt: new Date('2026-09-17T12:00:00').toISOString(),
    });
  });

  it('disables the submit button while saving', () => {
    completeInitialList();
    setValidForm();
    submitForm();

    expect(submitButton().disabled).toBe(true);
    expect(submitButton().textContent).toContain('Cadastrando...');
  });

  it('clears the form and refreshes the list after success', () => {
    completeInitialList();
    setValidForm();
    submitForm();

    const createdRate = createExchangeRate();
    exchangeRatesApi.createRequests[0].response.next(createdRate);
    exchangeRatesApi.createRequests[0].response.complete();
    fixture.detectChanges();

    expect(exchangeRatesApi.listResponses).toHaveLength(2);
    exchangeRatesApi.listResponses[1].next([createdRate]);
    exchangeRatesApi.listResponses[1].complete();
    fixture.detectChanges();

    expect(page.formModel()).toEqual({
      fromCurrency: '',
      toCurrency: '',
      rate: null,
      effectiveAt: '',
    });
    expect(fixture.nativeElement.textContent).toContain(
      'Taxa cadastrada com sucesso.',
    );
    expect(fixture.nativeElement.textContent).toContain('5,4321');
  });

  it('keeps form data after a creation error', () => {
    completeInitialList();
    setValidForm();
    const submittedValue = page.formModel();
    submitForm();

    exchangeRatesApi.createRequests[0].response.error(new Error('failed'));
    fixture.detectChanges();

    expect(page.formModel()).toEqual(submittedValue);
    expect(fixture.nativeElement.textContent).toContain(
      'Não foi possível cadastrar a taxa.',
    );
  });

  function completeInitialList(): void {
    exchangeRatesApi.listResponses[0].next([]);
    exchangeRatesApi.listResponses[0].complete();
    fixture.detectChanges();
  }

  function setValidForm(): void {
    page.formModel.set({
      fromCurrency: 'USD',
      toCurrency: 'BRL',
      rate: 5.4321,
      effectiveAt: '2026-09-17T12:00:00',
    });
    fixture.detectChanges();
  }

  function submitForm(): void {
    const form = fixture.nativeElement.querySelector('form') as HTMLFormElement;
    form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    fixture.detectChanges();
  }

  function submitButton(): HTMLButtonElement {
    return fixture.nativeElement.querySelector(
      'button[type="submit"]',
    ) as HTMLButtonElement;
  }
});
