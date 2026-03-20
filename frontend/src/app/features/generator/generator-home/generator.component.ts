import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-generator-home',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './generator.component.html',
  styleUrls: ['./generator.component.css']
})
export class GeneratorHomeComponent {

  constructor(
    private router: Router,
    private api: ApiService
  ) { }

  length = 16;
  count = 5;

  includeUpper = true;
  includeLower = true;
  includeNumbers = true;
  includeSymbols = true;

  generatedPasswords: string[] = [];
  selectedPassword = '';
  copied = false;
  loading = false;
  toastMessage = '';
  toastType = '';

  generate() {
    this.loading = true;
    this.generatedPasswords = [];
    
    const payload = {
      count: this.count,
      length: this.length,
      upper: this.includeUpper,
      lower: this.includeLower,
      number: this.includeNumbers,
      special: this.includeSymbols,
      excludeSimilar: false
    };

    this.api.generateMultiplePasswords(payload).subscribe({
      next: (res: string[]) => {
        this.generatedPasswords = res;
        this.selectedPassword = '';
        this.loading = false;
        this.showToast('Passwords generated successfully!', 'success');
        
        // Scroll to results
        setTimeout(() => {
          document.querySelector('.generated-list-title')?.scrollIntoView({ behavior: 'smooth' });
        }, 100);
      },
      error: () => {
        this.loading = false;
        this.showToast('Error generating passwords. Please try again.', 'error');
      }
    });
  }

  showToast(msg: string, type: string) {
    this.toastMessage = msg;
    this.toastType = type;
    setTimeout(() => { this.toastMessage = ''; }, 3000);
  }

  selectPassword(password: string) {
    this.selectedPassword = password;
    this.copied = false;
  }

copyToClipboard() {

  if (!this.selectedPassword) return;

  const textarea = document.createElement('textarea');
  textarea.value = this.selectedPassword;

  textarea.style.position = 'fixed';
  textarea.style.left = '-9999px';

  document.body.appendChild(textarea);

  textarea.select();
  document.execCommand('copy');

  document.body.removeChild(textarea);

  this.copied = true;

  setTimeout(() => {
    this.copied = false;
  }, 2000);

}
  saveToVault() {

    this.router.navigate(['/vault'], {
      queryParams: {
        generatedPassword: this.selectedPassword
      }
    });

  }
  // ================= PREMIUM ANALYZER HELPERS =================

  get strengthPercent(): number {

    if (!this.selectedPassword) return 0;

    let score = 0;

    if (this.selectedPassword.length >= 8) score++;
    if (/[A-Z]/.test(this.selectedPassword)) score++;
    if (/[a-z]/.test(this.selectedPassword)) score++;
    if (/[0-9]/.test(this.selectedPassword)) score++;
    if (/[^A-Za-z0-9]/.test(this.selectedPassword)) score++;

    return score * 20;
  }

  hasUpper() {
    return /[A-Z]/.test(this.selectedPassword);
  }

  hasLower() {
    return /[a-z]/.test(this.selectedPassword);
  }

  hasNumber() {
    return /[0-9]/.test(this.selectedPassword);
  }

  hasSymbol() {
    return /[^A-Za-z0-9]/.test(this.selectedPassword);
  }
}