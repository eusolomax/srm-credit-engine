import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import {
  FormField,
  FormRoot,
  form,
  maxLength,
  min,
  minLength,
  required,
} from '@angular/forms/signals';
import { finalize } from 'rxjs';

import type { CurrencyCode } from '../../core/models/currency-code.model';
import type {
  CreateReceivableRequest,
  Receivable,
  ReceivableType,
} from '../../core/models/receivable.model';
import { ReceivablesApiService } from '../../core/services/receivables-api.service';

type ReceivableFormType = ReceivableType | '';
type ReceivableFormCurrency = CurrencyCode | '';

interface ReceivableFormModel {
  faceValue: number | null;
  assignor: string;
  type: ReceivableFormType;
  paymentCurrency: ReceivableFormCurrency;
  termMonths: number | null;
  dueDate: string;
}

const emptyFormValue = (): ReceivableFormModel => ({
  faceValue: null,
  assignor: '',
  type: '',
  paymentCurrency: '',
  termMonths: null,
  dueDate: '',
});

@Component({
  selector: 'app-receivables-page',
  imports: [DatePipe, FormField, FormRoot],
  templateUrl: './receivables-page.component.html',
  styleUrl: './receivables-page.component.css',
})
export class ReceivablesPageComponent implements OnInit {
  private readonly receivablesApi = inject(ReceivablesApiService);

  protected readonly receivables = signal<Receivable[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly loadError = signal<string | null>(null);
  protected readonly saveError = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);

  protected readonly formModel = signal<ReceivableFormModel>(emptyFormValue());
  protected readonly receivableForm = form(this.formModel, (receivable) => {
    required(receivable.faceValue, {
      message: 'Informe o valor de face.',
    });
    min(receivable.faceValue, 0, {
      message: 'O valor de face deve ser maior que zero.',
    });

    required(receivable.assignor, {
      message: 'Informe o cedente.',
    });
    minLength(receivable.assignor, 11, {
      message: 'O cedente deve ter entre 11 e 14 caracteres.',
    });
    maxLength(receivable.assignor, 14, {
      message: 'O cedente deve ter entre 11 e 14 caracteres.',
    });

    required(receivable.type, {
      message: 'Selecione o tipo do recebível.',
    });
    required(receivable.paymentCurrency, {
      message: 'Selecione a moeda de pagamento.',
    });

    required(receivable.termMonths, {
      message: 'Informe o prazo em meses.',
    });
    min(receivable.termMonths, 1, {
      message: 'O prazo deve ser positivo.',
    });

    required(receivable.dueDate, {
      message: 'Informe o vencimento.',
    });
  });

  protected readonly typeLabels: Record<ReceivableType, string> = {
    DUPLICATA: 'Duplicata mercantil',
    CHEQUE: 'Cheque pré-datado',
  };

  protected readonly statusLabels: Record<Receivable['status'], string> = {
    AVAILABLE: 'Disponível',
    SETTLED: 'Liquidado',
  };

  ngOnInit(): void {
    this.loadReceivables();
  }

  protected loadReceivables(): void {
    this.isLoading.set(true);
    this.loadError.set(null);

    this.receivablesApi
      .getAll()
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (receivables) => this.receivables.set(receivables),
        error: (error: unknown) => {
          this.loadError.set(
            this.apiErrorMessage(error, 'Não foi possível carregar os recebíveis.'),
          );
        },
      });
  }

  protected createReceivable(): void {
    this.receivableForm().markAsTouched();
    this.saveError.set(null);
    this.successMessage.set(null);

    if (!this.receivableForm().valid()) {
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

    const request: CreateReceivableRequest = {
      faceValue: value.faceValue,
      assignor: value.assignor,
      type: value.type,
      paymentCurrency: value.paymentCurrency,
      termMonths: value.termMonths,
      dueDate: value.dueDate,
    };

    this.isSaving.set(true);

    this.receivablesApi
      .create(request)
      .pipe(finalize(() => this.isSaving.set(false)))
      .subscribe({
        next: () => {
          this.successMessage.set('Recebível cadastrado com sucesso.');
          this.receivableForm().reset(emptyFormValue());
          this.loadReceivables();
        },
        error: (error: unknown) => {
          this.saveError.set(
            this.apiErrorMessage(error, 'Não foi possível cadastrar o recebível.'),
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
