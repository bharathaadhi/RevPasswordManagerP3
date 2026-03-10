import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';
import { EmailSimulatorService, SimulatedEmail } from '../../services/email-simulator.service';

@Component({
  selector: 'app-email-simulator',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="email-popup" *ngIf="email$ | async as email">
      <!-- GLOW EFFECT -->
      <div class="glow-bg"></div>
      
      <div class="email-header">
        <div class="header-main">
          <div class="app-icon">
            <i class="fa fa-envelope"></i>
          </div>
          <div class="email-info">
            <span class="label">SECURE NOTIFICATION</span>
            <span class="subject">{{ email.subject }}</span>
          </div>
        </div>
        <button class="close-btn" (click)="close()">
          <i class="fa fa-times"></i>
        </button>
      </div>

      <div class="email-body">
        <div class="recipient-line">
          <span class="to-text">To:</span> 
          <span class="email-address">{{ email.to }}</span>
        </div>
        
        <div class="content-box">
          <p class="instruction">Please use the following code to complete your verification:</p>
          
          <div class="code-wrapper">
            <div class="code-digits">
              {{ email.code }}
            </div>
            <div class="copy-hint" (click)="copyCode(email.code)">
              <i class="fa fa-clone"></i> COPY
            </div>
          </div>

          <div class="security-meta">
            <div class="expiry-pill">
              <i class="fa fa-clock-o"></i> Expires: {{ email.expiresAt | date:'shortTime' }}
            </div>
            <div class="security-badge">
              <i class="fa fa-shield"></i> End-to-End Encrypted
            </div>
          </div>
        </div>
      </div>

      <div class="progress-bar">
        <div class="progress-fill"></div>
      </div>
    </div>
  `,
  styles: [`
    @import url('https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;800&display=swap');

    .email-popup {
      position: fixed;
      top: 30px;
      right: 30px;
      width: 380px;
      background: rgba(13, 17, 23, 0.85);
      backdrop-filter: blur(20px) saturate(180%);
      -webkit-backdrop-filter: blur(20px) saturate(180%);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 20px;
      box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.7);
      z-index: 10000;
      color: #e2e8f0;
      font-family: 'Outfit', sans-serif;
      animation: slideInPremium 0.6s cubic-bezier(0.23, 1, 0.32, 1);
      overflow: hidden;
    }

    .glow-bg {
      position: absolute;
      top: -50%;
      left: -50%;
      width: 200%;
      height: 200%;
      background: radial-gradient(circle at center, rgba(124, 58, 237, 0.15) 0%, transparent 70%);
      pointer-events: none;
      z-index: -1;
    }

    @keyframes slideInPremium {
      from { transform: translateX(120%) scale(0.9); opacity: 0; }
      to { transform: translateX(0) scale(1); opacity: 1; }
    }

    .email-header {
      padding: 16px 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: rgba(255, 255, 255, 0.03);
      border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    }

    .header-main {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .app-icon {
      width: 32px;
      height: 32px;
      background: linear-gradient(135deg, #7c3aed 0%, #4f46e5 100%);
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;
      color: white;
      box-shadow: 0 4px 12px rgba(124, 58, 237, 0.4);
    }

    .email-info {
      display: flex;
      flex-direction: column;
    }

    .label {
      font-size: 10px;
      font-weight: 800;
      letter-spacing: 1.5px;
      color: #8b5cf6;
    }

    .subject {
      font-size: 15px;
      font-weight: 600;
      color: #f8fafc;
    }

    .close-btn {
      background: rgba(255, 255, 255, 0.05);
      border: none;
      width: 30px;
      height: 30px;
      border-radius: 50%;
      color: #94a3b8;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.2s;
    }

    .close-btn:hover {
      background: rgba(239, 44, 44, 0.2);
      color: #ef4444;
    }

    .email-body {
      padding: 20px;
    }

    .recipient-line {
      font-size: 13px;
      margin-bottom: 20px;
      padding: 8px 12px;
      background: rgba(0, 0, 0, 0.2);
      border-radius: 8px;
    }

    .to-text { color: #64748b; margin-right: 6px; }
    .email-address { color: #cbd5e1; font-weight: 500; }

    .content-box {
      display: flex;
      flex-direction: column;
      gap: 15px;
    }

    .instruction {
      font-size: 13px;
      color: #94a3b8;
      line-height: 1.5;
    }

    .code-wrapper {
      position: relative;
      background: rgba(255, 255, 255, 0.02);
      border: 1px solid rgba(124, 58, 237, 0.2);
      border-radius: 12px;
      padding: 24px;
      text-align: center;
      margin: 10px 0;
      cursor: pointer;
      transition: all 0.3s;
    }

    .code-wrapper:hover {
      background: rgba(124, 58, 237, 0.05);
      border-color: #7c3aed;
    }

    .code-digits {
      font-size: 42px;
      font-weight: 800;
      letter-spacing: 8px;
      color: #fff;
      text-shadow: 0 0 20px rgba(124, 58, 237, 0.5);
    }

    .copy-hint {
      position: absolute;
      bottom: 8px;
      right: 12px;
      font-size: 9px;
      font-weight: 700;
      color: #7c3aed;
      opacity: 0.6;
    }

    .security-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 10px;
    }

    .expiry-pill {
      font-size: 11px;
      font-weight: 600;
      color: #fb7185;
      background: rgba(251, 113, 133, 0.1);
      padding: 4px 10px;
      border-radius: 20px;
      display: flex;
      align-items: center;
      gap: 6px;
    }

    .security-badge {
      font-size: 11px;
      color: #10b981;
      display: flex;
      align-items: center;
      gap: 5px;
    }

    .progress-bar {
      height: 3px;
      background: rgba(255, 255, 255, 0.05);
      width: 100%;
    }

    .progress-fill {
      height: 100%;
      background: linear-gradient(90deg, #7c3aed, #4f46e5);
      width: 100%;
      animation: progressDrain 15s linear forwards;
    }

    @keyframes progressDrain {
      from { width: 100%; }
      to { width: 0%; }
    }
  `],
})
export class EmailSimulatorComponent {
  email$: Observable<SimulatedEmail | null>;

  constructor(private emailService: EmailSimulatorService) {
    this.email$ = this.emailService.email$;
  }

  close() {
    this.emailService.hideEmail();
  }

  copyCode(code: string) {
    navigator.clipboard.writeText(code);
    alert('Code copied to clipboard!');
  }
}
