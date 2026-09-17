import { CurrencyCode } from './currency-code.model';

export interface CreateSettlementRequest {
  receivableId: number;
  idempotencyKey: string;
}

export interface Settlement {
  receivableId: number;
  assignor: string;
  faceValue: number;
  presentValue: number;
  discount: number;
  paymentCurrency: CurrencyCode;
  exchangeRate: number | null;
  settledAt: string;
}

export interface SettlementFilters {
  assignor?: string;
  currency?: CurrencyCode;
  from?: string;
  to?: string;
}
