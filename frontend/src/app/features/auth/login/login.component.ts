import { Component, ChangeDetectorRef } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  styles: [`
    .otp-backdrop {
      position: fixed;
      top: 0; left: 0; 
      width: 100vw; height: 100vh;
      background: rgba(0, 0, 0, 0.9);
      backdrop-filter: blur(10px);
      z-index: 10000;
      display: flex;
      align-items: center;
      justify-content: center;
      animation: fadeIn 0.3s ease-out;
    }

    .otp-modal-container {
      position: relative;
      width: 440px;
      background: #0f172a;
      border-radius: 24px;
      border: 1px solid rgba(124, 58, 237, 0.3);
      box-shadow: 0 50px 100px -20px rgba(0, 0, 0, 0.6);
      overflow: hidden;
      animation: zoomIn 0.4s cubic-bezier(0.16, 1, 0.3, 1);
    }

    .modal-glow {
      position: absolute;
      top: -100px;
      left: 50%;
      transform: translateX(-50%);
      width: 300px;
      height: 200px;
      background: radial-gradient(circle, rgba(124, 58, 237, 0.2) 0%, transparent 70%);
      pointer-events: none;
    }

    .modal-inner {
      padding: 40px;
      position: relative;
      z-index: 2;
    }

    .modal-header {
      text-align: center;
      margin-bottom: 30px;
    }

    .icon-circle {
      width: 64px;
      height: 64px;
      background: rgba(124, 58, 237, 0.1);
      border: 1px solid rgba(124, 58, 237, 0.3);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 20px;
      font-size: 24px;
      color: #7c3aed;
      box-shadow: 0 0 20px rgba(124, 58, 237, 0.2);
    }

    .modal-header h2 {
      font-size: 24px;
      font-weight: 800;
      color: #fff;
      margin-bottom: 8px;
    }

    .header-subtext {
      font-size: 14px;
      color: #94a3b8;
    }

    .error-msg-premium {
      background: rgba(239, 68, 68, 0.1);
      border: 1px solid rgba(239, 68, 68, 0.2);
      color: #f87171;
      padding: 12px 16px;
      border-radius: 12px;
      font-size: 13px;
      margin-bottom: 25px;
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .input-container-premium {
      margin-bottom: 30px;
    }

    .input-container-premium label {
      display: block;
      font-size: 11px;
      font-weight: 800;
      color: #64748b;
      letter-spacing: 2px;
      margin-bottom: 15px;
    }

    .otp-input-wrapper input {
      width: 100%;
      background: rgba(0, 0, 0, 0.3);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 14px;
      padding: 18px;
      font-size: 28px;
      font-weight: 800;
      color: #fff;
      text-align: center;
      letter-spacing: 12px;
      transition: all 0.3s;
    }

    .otp-input-wrapper input:focus {
      outline: none;
      border-color: #7c3aed;
      background: rgba(124, 58, 237, 0.05);
    }

    .verify-btn-premium {
      width: 100%;
      background: linear-gradient(135deg, #7c3aed 0%, #4f46e5 100%);
      border: none;
      padding: 18px;
      border-radius: 14px;
      color: #fff;
      font-size: 15px;
      font-weight: 700;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12px;
      box-shadow: 0 10px 25px rgba(124, 58, 237, 0.4);
    }

    .verify-btn-premium:disabled {
      opacity: 0.7;
    }

    .modal-footer {
      text-align: center;
      margin-top: 25px;
      font-size: 13px;
      color: #64748b;
    }

    .modal-footer a {
      color: #8b5cf6;
      font-weight: 700;
      cursor: pointer;
    }

    .cyber-loader {
      width: 18px;
      height: 18px;
      border: 2px solid rgba(255, 255, 255, 0.3);
      border-top-color: #fff;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
      display: inline-block;
    }

    @keyframes spin { to { transform: rotate(360deg); } }
    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
    @keyframes zoomIn { from { transform: scale(0.95); opacity: 0; } to { transform: scale(1); opacity: 1; } }
  `]
})
export class LoginComponent {

  usernameOrEmail = '';
  password = '';

  showPassword = false;
  loading = false;

  showOtpModal = false;
  enteredOtp = '';
  otpUser = '';
  otpEmail = '';

  errorMessage = '';
  successMessage = '';

  verifyLoading = false;
  loginResponse: any = null;

  constructor(
    public api: ApiService,
    private router: Router,
    private cd: ChangeDetectorRef
  ) {}

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  /* ================= LOGIN ================= */

  login() {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.usernameOrEmail || !this.password) {
      this.errorMessage = "Enter credentials";
      return;
    }

    this.loading = true;
    this.cd.detectChanges();

    this.api.login({
      usernameOrEmail: this.usernameOrEmail,
      masterPassword: this.password
    })
    .subscribe({
      next: (res: any) => {
        this.loading = false;
        this.cd.detectChanges();

        if (res.twoFactorRequired) {
          this.loginResponse = res;
          this.otpUser = res.username;
          this.otpEmail = res.email || this.otpUser;
          
          this.api.generateVerificationCode(this.otpEmail).subscribe();
          
          this.showOtpModal = true;
          this.cd.detectChanges();
          return;
        }

        this.successMessage = "Login successful";
        this.loginSuccess(res);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err?.error?.message || "Invalid credentials";
        this.cd.detectChanges();
      }
    });
  }

  /* ================= VERIFY OTP ================= */

  verifyOtp() {
    this.errorMessage = '';

    if (!this.enteredOtp) {
      this.errorMessage = "Enter OTP";
      return;
    }

    if (this.verifyLoading) return;

    this.verifyLoading = true;
    this.cd.detectChanges();

    this.api.verify2FA(this.otpEmail, this.enteredOtp)
      .subscribe({
        next: (res: any) => {
          this.verifyLoading = false;
          this.showOtpModal = false;
          this.loginSuccess(this.loginResponse);
        },
        error: (err) => {
          this.verifyLoading = false;
          // 🛡️ Robust error parsing for descriptive backend errors
          this.errorMessage = err?.error?.message || err?.error || "Invalid OTP";
          
          if (typeof this.errorMessage === 'object') {
            this.errorMessage = JSON.stringify(this.errorMessage);
          }
          
          this.cd.detectChanges();
        }
      });
  }

  /* ================= SUCCESS ================= */

  loginSuccess(res: any) {
    if (!res) return;
    localStorage.setItem('token', res.token);
    localStorage.setItem('username', res.username);
    localStorage.setItem('userId', res.userId);
    this.router.navigate(['/dashboard']);
  }
}