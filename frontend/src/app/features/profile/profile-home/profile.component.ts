import { Component, OnInit } from '@angular/core';
import { finalize, tap } from 'rxjs';
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
  isSaving = false; // Add for button feedback

  securityQuestions: any[] = [];
  answers: string[] = ['', '', ''];
  isLoadingQuestions = false;

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

    const user = this.api.getLoggedUser();

    if (!user) {
      console.warn("No user found in localStorage");
      return;
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
        error: (err) => {
          console.error("Profile load error:", err);
          alert("Failed to load profile for: " + user);
        }
      });
  }

  /* ================= SAVE PROFILE ================= */
  saveProfile() {
    
    const emailRegex = /^[A-Za-z0-9+_.-]+@(.+)$/;
    if (!this.email || !emailRegex.test(this.email.trim())) {
      alert("Invalid email format");
      return;
    }

    const phoneRegex = /^\d{10}$/;
    if (!this.phone || !phoneRegex.test(this.phone.trim())) {
      alert("Phone number must be exactly 10 digits");
      return;
    }

    const payload = {
      userId: this.api.getUserId(),
      name: this.name,
      email: this.email,
      phone: this.phone
    };

    this.isSaving = true;

    this.api.updateProfile(payload)
      .pipe(finalize(() => this.isSaving = false))
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
    const user = this.api.getLoggedUser();
    if (!user) return;

    this.api.get2FAStatus(user)
      .subscribe((res: any) => {
        this.twoFactorEnabled = res.enabled;
      });
  }

  toggle2FA(event: any) {
    const enabled = event.target.checked;
    const username = this.api.getLoggedUser();
    if (!username) {
        alert("Session expired. Please login again.");
        return;
    }

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

        error: (err) => {
          const msg = err?.error?.message || err?.error || "Failed to update 2FA configuration";
          alert(typeof msg === 'string' ? msg : JSON.stringify(msg));
          this.twoFactorEnabled = !enabled;
        }
      });
  }

  /* ================= QUESTIONS ================= */
  loadQuestions() {
    const user = this.api.getLoggedUser();
    if (!user) return;

    this.isLoadingQuestions = true;
    this.api.getSecurityQuestions(user)
      .pipe(finalize(() => this.isLoadingQuestions = false))
      .subscribe({
        next: (q) => {
          if (!q || q.length === 0) {
            console.warn("No security questions found for user:", user);
          }
          this.securityQuestions = q.map((x: any) => ({ question: x }));
          this.answers = new Array(this.securityQuestions.length).fill('');
        },
        error: (err) => {
          console.error("Failed to load security questions:", err);
        }
      });
  }

  updateAnswers() {

    const usernameOrEmail = this.api.getLoggedUser();
    
    const payload = {
      usernameOrEmail: usernameOrEmail,
      securityQuestions:
        this.securityQuestions.map((q, i) => ({
          question: q.question,
          answer: this.answers[i]
        }))
    };

    this.isSaving = true;

    this.api.updateSecurityAnswers(payload)
      .pipe(finalize(() => this.isSaving = false))
      .subscribe({
        next: (res: any) => {
          alert(res?.message || "Answers updated successfully");
        },
        error: (err) => {
          const msg = err?.error?.message || err?.error || "Failed to update security answers";
          alert(typeof msg === 'string' ? msg : JSON.stringify(msg));
        }
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
      usernameOrEmail: this.api.getLoggedUser(),
      currentPassword: this.currentMasterPassword,
      newPassword: this.newMasterPassword
    };

    this.isSaving = true;

    this.api.updateMasterPassword(payload)
      .pipe(finalize(() => this.isSaving = false))
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