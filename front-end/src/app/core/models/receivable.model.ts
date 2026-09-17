import { CurrencyCode } from './currency-code.model';

export type ReceivableType = 'DUPLICATA' | 'CHEQUE';

export type ReceivableStatus = 'AVAILABLE' | 'SETTLED';

export interface CreateReceivableRequest {
  faceValue: number;
  assignor: string;
  type: ReceivableType;
  paymentCurrency: CurrencyCode;
  termMonths: number;
  dueDate: string;
}

export interface Receivable {
  id: number;
  faceValue: number;
  assignor: string;
  type: ReceivableType;
  paymentCurrency: CurrencyCode;
  termMonths: number;
  dueDate: string;
  status: ReceivableStatus;
  createdAt: string;
}
