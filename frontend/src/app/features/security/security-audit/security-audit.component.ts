import { Component, OnInit, inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ApiService } from '../../../core/services/api.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-security-audit',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './security-audit.component.html',
  styleUrls: ['./security-audit.component.css']
})
export class SecurityAuditComponent implements OnInit {

  weakPasswords: any[] = [];
  reusedPasswords: any[] = [];
  securityScore: number = 0;
  alertMessage: string = '';
  oldPasswords: any[] = [];

  private platformId = inject(PLATFORM_ID);

  constructor(
    private api: ApiService,
    private router: Router,
    private cd: ChangeDetectorRef
  ) { }

  ngOnInit(): void {

    // SSR Protection
    if (!isPlatformBrowser(this.platformId)) return;

    const user = localStorage.getItem('username');
    if (!user) return;

    this.loadAudit(user);
  }

  loadAudit(username: string) {
    this.api.getVault().subscribe((res: any[]) => {
      // 1. Weak Passwords
      this.weakPasswords = res.filter(p => p.strength === 'Weak');

      // 2. Reused Passwords
      const pwdMap: { [key: string]: any[] } = {};
      res.forEach(p => {
        const pass = p.encryptedPassword;
        if (!pwdMap[pass]) pwdMap[pass] = [];
        pwdMap[pass].push(p);
      });
      
      this.reusedPasswords = [];
      Object.values(pwdMap).forEach(list => {
        if (list.length > 1) {
          this.reusedPasswords.push(...list);
        }
      });

      // 3. Old Passwords (90+ days)
      const ninetyDaysAgo = new Date();
      ninetyDaysAgo.setDate(ninetyDaysAgo.getDate() - 90);
      
      this.oldPasswords = res.filter(p => {
        if (!p.createdAt) return false;
        return new Date(p.createdAt) < ninetyDaysAgo;
      });

      // 4. Security Score
      const total = res.length;
      const strongCount = res.filter(p => p.strength === 'Strong').length;
      this.securityScore = total > 0 ? Math.round((strongCount / total) * 100) : 0;

      this.cd.detectChanges();
    });
  }

  // ===== FIX BUTTON =====
  fixWeakPasswords() {
    this.router.navigate(['/vault'], {
      queryParams: { filter: 'weak' }
    });
  }
}