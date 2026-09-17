import { Routes } from '@angular/router';
import { AppShellComponent } from './layout/app-shell.component';
import { PlaceholderPageComponent } from './features/placeholder/placeholder-page.component';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'receivables',
  },
  {
    path: '',
    component: AppShellComponent,
    children: [
      {
        path: 'receivables',
        component: PlaceholderPageComponent,
        data: {
          eyebrow: 'Operação',
          title: 'Recebíveis',
          description: 'Consulte e cadastre os recebíveis da operação.',
        },
      },
      {
        path: 'pricing',
        component: PlaceholderPageComponent,
        data: {
          eyebrow: 'Operação',
          title: 'Pricing',
          description: 'Simule o valor líquido de um recebível.',
        },
      },
      {
        path: 'settlements',
        component: PlaceholderPageComponent,
        data: {
          eyebrow: 'Acompanhamento',
          title: 'Settlements',
          description: 'Consulte o extrato de liquidações realizadas.',
        },
      },
      {
        path: 'exchange-rates',
        component: PlaceholderPageComponent,
        data: {
          eyebrow: 'Configuração',
          title: 'Exchange Rates',
          description: 'Consulte e cadastre taxas de câmbio.',
        },
      },
      {
        path: 'not-found',
        component: PlaceholderPageComponent,
        data: {
          eyebrow: 'Erro 404',
          title: 'Página não encontrada',
          description: 'A página solicitada não existe.',
          notFound: true,
        },
      },
      {
        path: '**',
        redirectTo: 'not-found',
      },
    ],
  },
];
