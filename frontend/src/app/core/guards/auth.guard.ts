import { inject, PLATFORM_ID } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';

export const authGuard: CanActivateFn = () => {

  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);

  // Check if browser
  if (isPlatformBrowser(platformId)) {

    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');

    if (token && username) {
      return true;
    }

    router.navigate(['/login']);
    return false;
  }

  // If running on server, allow navigation
  return true;
};