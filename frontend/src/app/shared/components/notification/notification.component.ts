import { Component, OnInit, OnDestroy, HostListener, ElementRef, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-notification',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.css']
})
export class NotificationComponent implements OnInit, OnDestroy {
  notifications: any[] = [];
  unreadCount = 0;
  showDropdown = false;
  private intervalId: any;

  private platformId = inject(PLATFORM_ID);

  constructor(private api: ApiService, private eRef: ElementRef) {}

  ngOnInit() {
    this.loadNotifications();
    if (typeof window !== 'undefined') {
      this.intervalId = setInterval(() => this.loadNotifications(), 10000);
    }
  }

  ngOnDestroy() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
  }

  @HostListener('document:click', ['$event'])
  clickout(event: any) {
    if (isPlatformBrowser(this.platformId) && this.eRef.nativeElement && !this.eRef.nativeElement.contains(event.target)) {
      this.showDropdown = false;
    }
  }

  toggleDropdown() {
    this.showDropdown = !this.showDropdown;
    if (this.showDropdown) {
      this.loadNotifications();
    }
  }

  loadNotifications() {
    if (!isPlatformBrowser(this.platformId)) return;
    
    const email = localStorage.getItem('username');
    if (!email) return;

    this.api.getUserNotifications(email).subscribe({
      next: (res: any) => {
        // Robust check for array
        this.notifications = Array.isArray(res) ? res : [];
        this.unreadCount = this.notifications.filter(n => n && !n.readStatus && !n.read).length;
      },
      error: () => {
        this.notifications = [];
        this.unreadCount = 0;
      }
    });
  }

  markAsRead(notification: any, event: Event) {
    event.stopPropagation();
    this.api.markNotificationAsRead(notification.id).subscribe(() => {
      notification.read = true;
      notification.readStatus = true;
      this.unreadCount = Math.max(0, this.unreadCount - 1);
    });
  }
}
