import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {

  step = 1;

  usernameOrEmail = '';

  questions: string[] = [];
  answers: string[] = ['', '', ''];

  newPassword = '';
  confirmPassword = '';

  showPassword = false;
  showConfirmPassword = false;

  successMessage = '';
  errorMessage = '';

  constructor(
    private api: ApiService,
    private router: Router
  ) { }

  // ================= STEP 1 =================
  verifyUser() {

    if (!this.usernameOrEmail) {
      this.errorMessage = "Enter username/email";
      return;
    }

    this.api.getSecurityQuestions(this.usernameOrEmail)
      .subscribe({

        next: (q: string[]) => {

          if (!q || q.length === 0) {
            this.errorMessage = "Security questions not configured";
            return;
          }

          this.errorMessage = '';
          this.questions = q.slice(0, 3);
          this.step = 2;
        },

        error: (err) => {
          this.errorMessage = err?.error?.message || err?.error || "User not found";
        }
      });
  }

  // ================= STEP 2 =================
  verifyAnswer() {

    const valid = this.answers.some(a => a?.trim());

    if (!valid) {
      this.errorMessage = "Answer at least one question";
      return;
    }

    this.errorMessage = '';
    this.step = 3;
  }

  // ================= STEP 3 =================
  resetPassword() {

    if (!this.newPassword || !this.confirmPassword) {
      this.errorMessage = "Enter password";
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = "Passwords not matching";
      return;
    }

    const securityQuestions =
      this.questions.map((q, i) => ({
        question: q,
        answer: this.answers[i]
      }))
        .filter(a => a.answer?.trim());

    this.api.forgotPassword({
      usernameOrEmail: this.usernameOrEmail,
      newPassword: this.newPassword,
      securityQuestions
    })
      .subscribe({

        next: (res: any) => {

          this.errorMessage = '';
          this.successMessage = res.message || "Password reset successful!";

          this.step = 4;
        },

        error: (err) => {
          this.successMessage = '';
          const msg = err?.error?.message || err?.error || "Password reset failed";
          this.errorMessage = typeof msg === 'string' ? msg : JSON.stringify(msg);
        }
      });
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPassword() {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}