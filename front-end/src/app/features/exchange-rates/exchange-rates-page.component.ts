import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import {
  FormField,
  form,
  required,
  validate,
} from '@angular/forms/signals';
import { finalize } from 'rxjs';

import type { CurrencyCode } from '../../core/models/currency-code.model';
import type {
  CreateExchangeRateRequest,
  ExchangeRate,
} from '../../core/models/exchange-rate.model';
import { ExchangeRatesApiService } from '../../core/services/exchange-rates-api.service';

type ExchangeRateFormCurrency = CurrencyCode | '';

interface ExchangeRateFormModel {
  fromCurrency: ExchangeRateFormCurrency;
  toCurrency: ExchangeRateFormCurrency;
  rate: number | null;
  effectiveAt: string;
}

const emptyFormValue = (): ExchangeRateFormModel => ({
  fromCurrency: '',
  toCurrency: '',
  rate: null,
  effectiveAt: '',
});

@Component({
  selector: 'app-exchange-rates-page',
  imports: [DatePipe, FormField],
  templateUrl: './exchange-rates-page.component.html',
  styleUrl: './exchange-rates-page.component.css',
})
export class ExchangeRatesPageComponent implements OnInit {
  private readonly exchangeRatesApi = inject(ExchangeRatesApiService);

  protected readonly exchangeRates = signal<ExchangeRate[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly loadError = signal<string | null>(null);
  protected readonly saveError = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);
  protected readonly formSubmitted = signal(false);

  protected readonly formModel = signal<ExchangeRateFormModel>(emptyFormValue());
  protected readonly exchangeRateForm = form(this.formModel, (rate) => {
    required(rate.fromCurrency, {
      message: 'Selecione a moeda de origem.',
    });
    required(rate.toCurrency, {
      message: 'Selecione a moeda de destino.',
    });
    validate(rate.toCurrency, ({ value }) => {
      const fromCurrency = this.formModel().fromCurrency;
      const toCurrency = value();

      if (
        fromCurrency !== '' &&
        toCurrency !== '' &&
        fromCurrency === toCurrency
      ) {
        return {
          kind: 'same-currency',
          message: 'A moeda de origem deve ser diferente da moeda de destino.',
        };
      }

      return undefined;
    });
    required(rate.rate, {
      message: 'Informe a taxa.',
    });
    validate(rate.rate, ({ value }) => {
      const currentRate = value();

      if (currentRate !== null && currentRate <= 0) {
        return {
          kind: 'positive',
          message: 'A taxa deve ser maior que zero.',
        };
      }

      return undefined;
    });
    required(rate.effectiveAt, {
      message: 'Informe a vigência da taxa.',
    });
  });

  ngOnInit(): void {
    this.loadExchangeRates();
  }

  protected loadExchangeRates(): void {
    this.isLoading.set(true);
    this.loadError.set(null);

    this.exchangeRatesApi
      .getAll()
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (exchangeRates) => this.exchangeRates.set(exchangeRates),
        error: (error: unknown) => {
          this.loadError.set(
            this.apiErrorMessage(error, 'Não foi possível carregar as taxas.'),
          );
        },
      });
  }

  protected submitForm(event: Event): void {
    event.preventDefault();
    this.formSubmitted.set(true);
    this.exchangeRateForm().markAsTouched();
    this.saveError.set(null);
    this.successMessage.set(null);

    if (!this.exchangeRateForm().valid()) {
      return;
    }

    const value = this.formModel();

    if (
      value.fromCurrency === '' ||
      value.toCurrency === '' ||
      value.rate === null ||
      value.effectiveAt === ''
    ) {
      return;
    }

    const effectiveAt = new Date(value.effectiveAt);

    if (Number.isNaN(effectiveAt.getTime())) {
      return;
    }

    const request: CreateExchangeRateRequest = {
      fromCurrency: value.fromCurrency,
      toCurrency: value.toCurrency,
      rate: value.rate,
      effectiveAt: effectiveAt.toISOString(),
    };

    this.isSaving.set(true);

    this.exchangeRatesApi
      .create(request)
      .pipe(finalize(() => this.isSaving.set(false)))
      .subscribe({
        next: () => {
          this.exchangeRateForm().reset(emptyFormValue());
          this.formSubmitted.set(false);
          this.successMessage.set('Taxa cadastrada com sucesso.');
          this.loadExchangeRates();
        },
        error: (error: unknown) => {
          this.saveError.set(
            this.apiErrorMessage(error, 'Não foi possível cadastrar a taxa.'),
          );
        },
      });
  }

  protected formatRate(rate: number): string {
    return new Intl.NumberFormat('pt-BR', {
      minimumFractionDigits: 4,
      maximumFractionDigits: 8,
    }).format(rate);
  }

  protected isSameCurrency(): boolean {
    const value = this.formModel();

    return (
      value.fromCurrency !== '' &&
      value.toCurrency !== '' &&
      value.fromCurrency === value.toCurrency
    );
  }

  private apiErrorMessage(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return 'Não foi possível conectar à API.';
      }

      const body = error.error as { detail?: unknown; message?: unknown } | null;

      if (typeof body?.detail === 'string') {
        return body.detail;
      }

      if (typeof body?.message === 'string') {
        return body.message;
      }
    }

    return fallback;
  }
}
