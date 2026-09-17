import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormField, form } from '@angular/forms/signals';
import { finalize } from 'rxjs';

import type { CurrencyCode } from '../../core/models/currency-code.model';
import type {
  Settlement,
  SettlementFilters,
} from '../../core/models/settlement.model';
import { SettlementsApiService } from '../../core/services/settlements-api.service';

type SettlementFormCurrency = CurrencyCode | '';

interface SettlementFiltersFormModel {
  from: string;
  to: string;
  assignor: string;
  currency: SettlementFormCurrency;
}

const emptyFilters = (): SettlementFiltersFormModel => ({
  from: '',
  to: '',
  assignor: '',
  currency: '',
});

@Component({
  selector: 'app-settlements-page',
  imports: [DatePipe, FormField],
  templateUrl: './settlements-page.component.html',
  styleUrl: './settlements-page.component.css',
})
export class SettlementsPageComponent {
  private readonly settlementsApi = inject(SettlementsApiService);

  protected readonly settlements = signal<Settlement[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly hasSearched = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly filtersModel = signal<SettlementFiltersFormModel>(
    emptyFilters(),
  );
  protected readonly filtersForm = form(this.filtersModel);

  protected readonly hasFilters = computed(() =>
    Object.values(this.filtersModel()).some(value => value !== ''),
  );

  protected submitFilters(event: Event): void {
    event.preventDefault();
    this.querySettlements();
  }

  protected querySettlements(): void {
    this.hasSearched.set(true);
    this.errorMessage.set(null);
    this.isLoading.set(true);

    this.settlementsApi
      .getAll(this.toSettlementFilters(this.filtersModel()))
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (settlements) => this.settlements.set(settlements),
        error: (error: unknown) => {
          this.errorMessage.set(
            this.apiErrorMessage(error, 'Não foi possível carregar o extrato.'),
          );
        },
      });
  }

  protected clearFilters(): void {
    this.filtersForm().reset(emptyFilters());
    this.querySettlements();
  }

  protected formatMoney(value: number, currency: CurrencyCode): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency,
    }).format(value);
  }

  protected formatExchangeRate(exchangeRate: number | null): string {
    if (exchangeRate === null) {
      return '-';
    }

    return new Intl.NumberFormat('pt-BR', {
      minimumFractionDigits: 4,
      maximumFractionDigits: 8,
    }).format(exchangeRate);
  }

  private toSettlementFilters(value: SettlementFiltersFormModel): SettlementFilters {
    const filters: SettlementFilters = {};

    if (value.from) {
      filters.from = value.from;
    }

    if (value.to) {
      filters.to = value.to;
    }

    if (value.assignor) {
      filters.assignor = value.assignor;
    }

    if (value.currency) {
      filters.currency = value.currency;
    }

    return filters;
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
