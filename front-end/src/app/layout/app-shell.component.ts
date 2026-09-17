import { Component } from '@angular/core';
import {
  RouterLink,
  RouterLinkActive,
  RouterOutlet,
} from '@angular/router';

interface NavigationItem {
  label: string;
  path: string;
}

@Component({
  selector: 'app-shell',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './app-shell.component.html',
  styleUrl: './app-shell.component.css',
})
export class AppShellComponent {
  protected readonly navigationItems: NavigationItem[] = [
    { label: 'Receivables', path: '/receivables' },
    { label: 'Pricing', path: '/pricing' },
    { label: 'Settlements', path: '/settlements' },
    { label: 'Exchange Rates', path: '/exchange-rates' },
  ];
}
