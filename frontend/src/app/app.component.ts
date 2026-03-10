import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { EmailSimulatorComponent } from './core/components/email-simulator/email-simulator.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, EmailSimulatorComponent],
  template: `
    <router-outlet></router-outlet>
    <app-email-simulator></app-email-simulator>
  `
})
export class AppComponent {}
