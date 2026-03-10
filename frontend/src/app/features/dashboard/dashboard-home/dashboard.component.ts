import { Component, OnInit, inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router, NavigationEnd, RouterModule } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',

  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  totalPasswords: number = 0;
  weakPasswords: number = 0;
  message: string = '';
  recentEntries: any[] = [];
  favoritePasswords: any[] = [];
  loading: boolean = false;
  securityScore: number = 0;
  reusedPasswords: number = 0;
  oldPasswords: number = 0;

  private platformId = inject(PLATFORM_ID);

  constructor(
    private api: ApiService,
    private router: Router,
    private cd: ChangeDetectorRef
  ) { }

  ngOnInit(): void {

    if (!isPlatformBrowser(this.platformId)) return;

    this.loadDashboard();

    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.loadDashboard();
      });
  }

  loadDashboard(): void {

    const user = localStorage.getItem('username');
    if (!user) return;

    this.loading = true;

    this.api.getVault().subscribe({
      next: (res: any[]) => {

        this.totalPasswords = res.length;
        this.weakPasswords = res.filter((p: any) => p.strength === 'Weak').length;
        this.favoritePasswords = res.filter((p: any) => p.favorite);
        
        let strongCount = res.filter((p: any) => p.strength === 'Strong').length;
        this.securityScore = this.totalPasswords > 0 ? Math.round((strongCount / this.totalPasswords) * 100) : 0;
        
        let pwdCounts: { [key: string]: number } = {};
        res.forEach(p => {
           pwdCounts[p.encryptedPassword] = (pwdCounts[p.encryptedPassword] || 0) + 1;
        });
        
        this.reusedPasswords = Object.values(pwdCounts).filter(count => count > 1).reduce((acc, count) => acc + count, 0);

        this.oldPasswords = 0;

        this.recentEntries = [...res].sort((a: any, b: any) => b.id - a.id).slice(0, 5);

        this.loading = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cd.detectChanges();
      }
    });
  }

  viewPassword(id: number) {
    this.router.navigate(['/vault'], {
      queryParams: { highlight: id }
    });
  }

}