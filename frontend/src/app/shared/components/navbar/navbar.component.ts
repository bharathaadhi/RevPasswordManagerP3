import { Component, HostListener } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NotificationComponent } from '../notification/notification.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, NotificationComponent],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {

  menuOpen = false;

  constructor(private router: Router) {}

  toggleMenu() {
    this.menuOpen = !this.menuOpen;
  }

  @HostListener('document:click', ['$event'])
  closeMenu(event: Event) {
    const clicked = event.target as HTMLElement;

    if (!clicked.closest('.avatar-wrapper')) {
      this.menuOpen = false;
    }
  }

  logout() {
    localStorage.removeItem('token');
    sessionStorage.clear();
    this.router.navigateByUrl('/login', { replaceUrl: true });
  }
}