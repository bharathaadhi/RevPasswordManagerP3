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
  private lastNotifId = -1;

  private platformId = inject(PLATFORM_ID);

  constructor(private api: ApiService, private eRef: ElementRef) {}

  ngOnInit() {
    this.loadNotifications();
    if (isPlatformBrowser(this.platformId)) {
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
    
    const email = this.api.getLoggedUser();
    if (!email) return;

    this.api.getUserNotifications(email).subscribe({
      next: (res: any) => {
        const oldLastId = this.lastNotifId;
        this.notifications = Array.isArray(res) ? res : [];
        
        // Defensive count: check both possible field names from backend
        this.unreadCount = this.notifications.filter(n => {
          if (!n) return false;
          // Check for readStatus (JsonProperty) or isRead or read
          const isRead = n.readStatus === true || n.isRead === true || n.read === true;
          return !isRead;
        }).length;

        console.log(`[Notification] Loaded for ${email}: ${this.notifications.length} total, ${this.unreadCount} unread.`);

        if (this.notifications.length > 0) {
          const latest = this.notifications[0];
          this.lastNotifId = Math.max(this.lastNotifId, latest.id);
          
          if (oldLastId !== -1 && latest.id > oldLastId) {
            this.api.showSimulatedNotification(email, latest.title, latest.message);
          }
        }
      },
      error: (err) => {
        console.error('Failed to load notifications:', err);
        this.notifications = [];
        this.unreadCount = 0;
      }
    });
  }

  markAsRead(notification: any, event: Event) {
    event.stopPropagation();
    if (!notification || !notification.id) return;

    this.api.markNotificationAsRead(notification.id).subscribe({
      next: () => {
        notification.read = true;
        notification.readStatus = true;
        notification.isRead = true;
        this.unreadCount = Math.max(0, this.unreadCount - 1);
      },
      error: (err) => console.error('Failed to mark notification as read:', err)
    });
  }
}
