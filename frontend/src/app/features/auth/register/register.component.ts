import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {

  step = 1;
  showPassword = false;

  successMessage = '';
  errorMessage = '';
  isRegistering = false;

  securityQuestionOptions = [
    'What is your first school name?',
    'What is your mother’s maiden name?',
    'What is your favorite movie?',
    'What was your childhood nickname?',
    'What is your birth city?',
    'What is your favorite food?'
  ];

  registerData = {
    username: '',
    email: '',
    phone: '',
    masterPassword: '',
    securityQuestions: [
      { question: '', answer: '' },
      { question: '', answer: '' },
      { question: '', answer: '' }
    ]
  };

  constructor(
    private api: ApiService,
    private router: Router
  ) { }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  goToLogin(): void {
    this.router.navigateByUrl('/login');
  }

  nextStep(): void {

    const { username, email, masterPassword } = this.registerData;

    if (!username?.trim() || !email?.trim() || !masterPassword?.trim()) {
      this.errorMessage = 'Please fill all required fields';
      return;
    }

    if (username.trim().length < 2) {
      this.errorMessage = 'Name must be at least 2 characters';
      return;
    }

    const emailRegex = /^[A-Za-z0-9+_.-]+@(.+)$/;
    if (!emailRegex.test(email.trim())) {
      this.errorMessage = 'Invalid email format';
      return;
    }

    if (masterPassword.length < 8) {
      this.errorMessage = 'Password must be at least 8 characters';
      return;
    }

    this.errorMessage = '';
    this.step = 2;
  }

  previousStep(): void {
    this.step = 1;
  }

  register(): void {
    if (this.isRegistering) return;

    const hasEmpty = this.registerData.securityQuestions.some(
      q => !q.question || !q.answer?.trim()
    );

    if (hasEmpty) {
      this.errorMessage = 'Please complete all security questions';
      return;
    }

    const phoneRegex = /^\d{10}$/;
    if (!this.registerData.phone || !phoneRegex.test(this.registerData.phone)) {
      this.errorMessage = 'Phone number must be exactly 10 digits';
      return;
    }

    const selectedQuestions =
      this.registerData.securityQuestions.map(q => q.question);

    const hasDuplicate =
      new Set(selectedQuestions).size !== selectedQuestions.length;

    if (hasDuplicate) {
      this.errorMessage = 'Please select different security questions';
      return;
    }

    this.isRegistering = true;
    const payload = {
      ...this.registerData,
      name: this.registerData.username,
      password: this.registerData.masterPassword
    };

    this.api.register(payload).subscribe({

      next: (res: any) => {
        console.log('Registration success:', res);
        this.isRegistering = false;
        this.errorMessage = '';
        this.successMessage = "User registered successfully! Please log in to your secure vault.";
        this.step = 3;   
      },

      error: (err) => {
        this.isRegistering = false;
        this.successMessage = '';
        const msg = err?.error?.message || err?.error || "Registration failed";
        this.errorMessage = typeof msg === 'string' ? msg : JSON.stringify(msg);
      }

    });
  }
}