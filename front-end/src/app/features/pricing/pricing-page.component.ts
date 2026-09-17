import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import {
  FormField,
  FormRoot,
  form,
  min,
  required,
} from '@angular/forms/signals';
import { finalize } from 'rxjs';

import type { CurrencyCode } from '../../core/models/currency-code.model';
import type {
  CreatePricingSimulationRequest,
  PricingSimulationResponse,
} from '../../core/models/pricing.model';
import type { ReceivableType } from '../../core/models/receivable.model';
import { PricingApiService } from '../../core/services/pricing-api.service';

type PricingFormType = ReceivableType | '';
type PricingFormCurrency = CurrencyCode | '';

interface PricingFormModel {
  faceValue: number | null;
  type: PricingFormType;
  paymentCurrency: PricingFormCurrency;
  termMonths: number | null;
}

const emptyFormValue = (): PricingFormModel => ({
  faceValue: null,
  type: '',
  paymentCurrency: '',
  termMonths: null,
});

@Component({
  selector: 'app-pricing-page',
  imports: [FormField, FormRoot],
  templateUrl: './pricing-page.component.html',
  styleUrl: './pricing-page.component.css',
})
export class PricingPageComponent {
  private readonly pricingApi = inject(PricingApiService);

  protected readonly isSimulating = signal(false);
  protected readonly simulationResult = signal<PricingSimulationResponse | null>(null);
  protected readonly simulationError = signal<string | null>(null);

  protected readonly formModel = signal<PricingFormModel>(emptyFormValue());
  protected readonly pricingForm = form(this.formModel, (pricing) => {
    required(pricing.faceValue, {
      message: 'Informe o valor de face.',
    });
    min(pricing.faceValue, 0, {
      message: 'O valor de face deve ser maior que zero.',
    });

    required(pricing.type, {
      message: 'Selecione o tipo do recebível.',
    });
    required(pricing.paymentCurrency, {
      message: 'Selecione a moeda de pagamento.',
    });

    required(pricing.termMonths, {
      message: 'Informe o prazo em meses.',
    });
    min(pricing.termMonths, 1, {
      message: 'O prazo deve ser positivo.',
    });
  });

  protected readonly typeLabels: Record<ReceivableType, string> = {
    DUPLICATA: 'Duplicata mercantil',
    CHEQUE: 'Cheque pré-datado',
  };

  protected simulate(): void {
    this.pricingForm().markAsTouched();
    this.simulationError.set(null);

    if (!this.pricingForm().valid()) {
      return;
    }

    const value = this.formModel();

    if (
      value.faceValue === null ||
      value.termMonths === null ||
      value.type === '' ||
      value.paymentCurrency === ''
    ) {
      return;
    }

    const request: CreatePricingSimulationRequest = {
      faceValue: value.faceValue,
      type: value.type,
      paymentCurrency: value.paymentCurrency,
      termMonths: value.termMonths,
    };

    this.isSimulating.set(true);
    this.simulationResult.set(null);

    this.pricingApi
      .simulate(request)
      .pipe(finalize(() => this.isSimulating.set(false)))
      .subscribe({
        next: (result) => this.simulationResult.set(result),
        error: (error: unknown) => {
          this.simulationError.set(
            this.apiErrorMessage(error, 'Não foi possível realizar a simulação.'),
          );
        },
      });
  }

  protected formatMoney(value: number, currency: CurrencyCode): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency,
    }).format(value);
  }

  protected formatExchangeRate(exchangeRate: number): string {
    return new Intl.NumberFormat('pt-BR', {
      minimumFractionDigits: 4,
      maximumFractionDigits: 8,
    }).format(exchangeRate);
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
