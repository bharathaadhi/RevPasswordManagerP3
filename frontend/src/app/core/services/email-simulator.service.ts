import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface SimulatedEmail {
  to: string;
  subject: string;
  body: string;
  code: string;
  expiresAt: Date;
}

@Injectable({
  providedIn: 'root'
})
export class EmailSimulatorService {

  private emailSubject = new BehaviorSubject<SimulatedEmail | null>(null);
  email$ = this.emailSubject.asObservable();

  showEmail(to: string, code: string) {
    const expiresAt = new Date();
    expiresAt.setMinutes(expiresAt.getMinutes() + 5);

    this.emailSubject.next({
      to,
      subject: 'Your Verification Code',
      body: `Your secure verification code is: ${code}. This code will expire in 5 minutes.`,
      code,
      expiresAt
    });

    // Auto-hide after 15 seconds
    setTimeout(() => {
      this.hideEmail();
    }, 15000);
  }

  hideEmail() {
    this.emailSubject.next(null);
  }
}
