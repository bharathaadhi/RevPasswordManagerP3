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

    this.errorMessage = '';
    this.step = 2;
  }

  previousStep(): void {
    this.step = 1;
  }

  register(): void {

    const hasEmpty = this.registerData.securityQuestions.some(
      q => !q.question || !q.answer?.trim()
    );

    if (hasEmpty) {
      this.errorMessage = 'Please complete all security questions';
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

    this.api.register(this.registerData).subscribe({

      next: (res: any) => {

        this.errorMessage = '';
        this.successMessage = res.message || "User registered successfully!";

        this.step = 3;   
      },

      error: (err) => {
        this.successMessage = '';
        this.errorMessage =
          err?.error?.message || "Registration failed";
      }

    });
  }
}