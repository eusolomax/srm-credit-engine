import { CurrencyCode } from './currency-code.model';
import { ReceivableType } from './receivable.model';

export interface CreatePricingSimulationRequest {
  faceValue: number;
  type: ReceivableType;
  paymentCurrency: CurrencyCode;
  termMonths: number;
}

export interface PricingSimulationResponse {
  faceValue: number;
  presentValue: number;
  discount: number;
  paymentCurrency: CurrencyCode;
  exchangeRate: number | null;
}
