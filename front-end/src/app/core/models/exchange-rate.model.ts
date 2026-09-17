import { CurrencyCode } from './currency-code.model';

export interface CreateExchangeRateRequest {
  fromCurrency: CurrencyCode;
  toCurrency: CurrencyCode;
  rate: number;
  effectiveAt: string;
}

export interface ExchangeRate {
  fromCurrency: CurrencyCode;
  toCurrency: CurrencyCode;
  rate: number;
  effectiveAt: string;
  createdAt: string;
}
