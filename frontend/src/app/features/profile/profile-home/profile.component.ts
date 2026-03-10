import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { isPlatformBrowser } from '@angular/common';
import { Inject, PLATFORM_ID } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-profile-home',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileHomeComponent implements OnInit {

  activeTab: 'account' | 'security' | 'overview' = 'account';

  name = '';
  email = '';
  phone = '';

  currentMasterPassword = '';
  newMasterPassword = '';
  confirmMasterPassword = '';
  passwordStrength = 'Weak';

  showCurrent = false;
  showNew = false;
  showConfirm = false;
  twoFactorEnabled = false;

  securityQuestions: any[] = [];
  answers: string[] = ['', '', ''];

  weakPasswords = 0;
  reusedPasswords = 0;
  oldPasswords = 0;
  animatedScore = 0;

  constructor(private api: ApiService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object) { }

  ngOnInit() {
    this.loadProfile();
    this.loadQuestions();
    this.loadOverview();
    this.load2FA();
  }

  setTab(tab: any) {
    this.activeTab = tab;
  }

  /* ================= LOAD PROFILE ================= */
  loadProfile() {

    let user = '';

    if (isPlatformBrowser(this.platformId)) {
      user = localStorage.getItem('username') || '';
    }

    this.api.getProfile(user)
      .subscribe({
        next: (res: any) => {
          this.name = res.name;
          this.email = res.email;
          this.phone = res.phone;

          this.twoFactorEnabled =
            res.twoFactorEnabled;
        },
        error: () => {
          alert("Failed to load profile");
        }
      });
  }

  /* ================= SAVE PROFILE ================= */
  saveProfile() {

    const payload = {
      userId: Number(localStorage.getItem('userId')),
      name: this.name,
      email: this.email,
      phone: this.phone
    };

    this.api.updateProfile(payload)
      .subscribe({

        next: (res: any) => {

          console.log("PROFILE UPDATE RESPONSE:", res);

          const message =
            typeof res === 'string'
              ? res
              : res?.message || "Profile updated successfully";

          alert(message);

          this.loadProfile();
        },
        error: (err) => {

          console.error(err);

          alert(
            err?.error ||
            "Profile update failed"
          );
        }

      });
  }

  /* ================= TOGGLE 2FA ================= */

  load2FA() {

    const user =
      localStorage.getItem('username') || '';

    this.api.get2FAStatus(user)
      .subscribe((res: any) => {

        this.twoFactorEnabled = res.enabled;

      });
  }

  toggle2FA(event: any) {

    const enabled = event.target.checked;
    const username = localStorage.getItem('username') || '';

    this.api.toggle2FA(username, enabled)
      .subscribe({

        next: (res: any) => {
          this.twoFactorEnabled = res.enabled;
          
          const statusMsg = enabled ? "2fa enabled" : "2fa disabled";
          alert(statusMsg);
          
          alert("Security settings changed. Logging out for safety.");
          
          localStorage.clear();
          this.router.navigate(['/login']);
        },

        error: () => {
          alert("Failed to update 2FA configuration");
          this.twoFactorEnabled = !enabled;
        }
      });
  }

  /* ================= QUESTIONS ================= */
  loadQuestions() {

    const user = localStorage.getItem('username') || '';

    this.api.getSecurityQuestions(user)
      .subscribe(q => {
        this.securityQuestions =
          q.map((x: any) => ({ question: x }));

        this.answers =
          new Array(this.securityQuestions.length).fill('');
      });
  }

  updateAnswers() {

    const payload = {
      usernameOrEmail: localStorage.getItem('username'),
      securityQuestions:
        this.securityQuestions.map((q, i) => ({
          question: q.question,
          answer: this.answers[i]
        }))
    };

    this.api.updateSecurityAnswers(payload)
      .subscribe(() => {
        alert("Answers updated successfully");
      });
  }

  /* ================= PASSWORD ================= */
  updateMasterPassword() {

    if (!this.currentMasterPassword ||
      !this.newMasterPassword ||
      !this.confirmMasterPassword) {

      alert("All fields required");
      return;
    }

    if (this.newMasterPassword !== this.confirmMasterPassword) {
      alert("Passwords do not match");
      return;
    }

    const payload = {
      usernameOrEmail: localStorage.getItem('username'),
      currentPassword: this.currentMasterPassword,
      newPassword: this.newMasterPassword
    };

    this.api.updateMasterPassword(payload)
      .subscribe({

        next: () => {

          alert("Password updated successfully. Login again.");

          localStorage.clear();
          this.router.navigate(['/login']);
        },

        error: (err) => {
          alert(err.error || "Password update failed");
        }
      });
  }

  /* ================= OVERVIEW ================= */
  loadOverview() {
    this.api.getVault().subscribe({
      next: (res: any[]) => {
        const totalPasswords = res.length;
        this.weakPasswords = res.filter((p: any) => p.strength === 'Weak').length;
        
        let strongCount = res.filter((p: any) => p.strength === 'Strong').length;
        this.animatedScore = totalPasswords > 0 ? Math.round((strongCount / totalPasswords) * 100) : 0;
        
        let pwdCounts: { [key: string]: number } = {};
        res.forEach(p => {
           pwdCounts[p.encryptedPassword] = (pwdCounts[p.encryptedPassword] || 0) + 1;
        });
        
        this.reusedPasswords = Object.values(pwdCounts).filter(count => count > 1).reduce((acc, count) => acc + count, 0);
        this.oldPasswords = 0; // Keeping old logic consistent with dashboard
      },
      error: () => {
        this.weakPasswords = 0;
        this.reusedPasswords = 0;
        this.oldPasswords = 0;
        this.animatedScore = 0;
      }
    });
  }

  checkStrength() {

    const l = this.newMasterPassword.length;

    if (l > 12) this.passwordStrength = 'Strong';
    else if (l > 8) this.passwordStrength = 'Medium';
    else this.passwordStrength = 'Weak';
  }
}