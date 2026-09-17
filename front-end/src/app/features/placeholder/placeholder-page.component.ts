import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

interface PlaceholderPageData {
  eyebrow: string;
  title: string;
  description: string;
  notFound?: boolean;
}

@Component({
  selector: 'app-placeholder-page',
  imports: [RouterLink],
  templateUrl: './placeholder-page.component.html',
  styleUrl: './placeholder-page.component.css',
})
export class PlaceholderPageComponent {
  protected readonly page = inject(ActivatedRoute).snapshot
    .data as PlaceholderPageData;
}
