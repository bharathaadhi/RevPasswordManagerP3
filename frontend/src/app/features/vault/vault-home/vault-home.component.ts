import { Component, OnInit, inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-vault-home',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './vault-home.component.html',
  styleUrls: ['./vault-home.component.css']
})
export class VaultHomeComponent implements OnInit {

  passwords: any[] = [];
  allPasswords: any[] = [];

  totalPasswords = 0;
  weakCount = 0;
  favoriteCount = 0;
  securityScore = 0;

  /* ================= TOAST ================= */

  toastMessage = '';
  toastType = '';

  /* ================= FORM ERROR ================= */

  formError = '';
  viewError = '';

  /* ================= MODALS ================= */

  showAdd = false;
  showViewModal = false;
  showEditModal = false;
  showDeleteModal = false;

  isVerifying = false;
  showPassword = false;
  showDeletePassword = false;
  showMasterPassword = false;
  showEmailModal = false;

  selectedEntryId: number | null = null;
  deleteEntryId: number | null = null;
  editingId: number | null = null;
  
  emailVerificationCode = '';

  masterPasswordInput = '';
  decryptedPassword = '';
  viewVerificationCode = '';

  deleteMasterPassword = '';
  deleteVerificationCode = '';
  deleteError = '';

  /* TRACKING FAVORITE UPDATE STATE */
  togglingIds: Set<number> = new Set();
  filterMode: 'all' | 'favorites' | 'weak' | 'search' | 'category' = 'all';

  searchKeyword = '';
  passwordStrength = '';

  categories = [
    'ALL',
    'SOCIAL_MEDIA',
    'BANKING',
    'EMAIL',
    'SHOPPING',
    'WORK',
    'OTHER'
  ];

  newPassword: any = {
    accountName: '',
    website: '',
    username: '',
    password: '',
    category: 'SOCIAL_MEDIA',
    notes: ''
  };

  editPassword: any = {
    accountName: '',
    website: '',
    username: '',
    password: '',
    category: 'SOCIAL_MEDIA',
    notes: ''
  };

  private platformId = inject(PLATFORM_ID);

  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
    private cd: ChangeDetectorRef
  ) { }

  ngOnInit() {

    if (!isPlatformBrowser(this.platformId)) return;

    const user = localStorage.getItem('username');
    if (!user) return;

    this.loadVault();

    this.route.queryParams.subscribe(params => {

      /* GENERATED PASSWORD FROM GENERATOR */

      if (params['generatedPassword']) {

        setTimeout(() => {

          this.openAdd();
          this.newPassword.password = params['generatedPassword'];

        });

      }

      /* WEAK PASSWORD FILTER */

      if (params['filter'] === 'weak') {
        this.loadWeakPasswords(user);
      }

      /* FAVORITES FILTER */

      if (params['filter'] === 'favorite') {
        this.loadFavorites();
      }

    });

  }

  /* ================= TOAST FUNCTION ================= */

  showToast(message: string, type: string) {

    this.toastMessage = message;
    this.toastType = type;

    setTimeout(() => {
      this.toastMessage = '';
    }, 3000);
  }

  /* ================= LOAD VAULT ================= */

  loadVault() {

    const user = localStorage.getItem('username');
    if (!user) return;

    this.filterMode = 'all';

    this.api.getVault().subscribe({
      next: (res: any[]) => {
        this.allPasswords = [...res];
        this.passwords = [...res];

        /* update stats */

        this.totalPasswords = this.allPasswords.length;

        this.favoriteCount =
          this.allPasswords.filter(p => p.favorite).length;

        this.weakCount =
          this.allPasswords.filter(p => p.strength === 'Weak').length;

        /* calculate security score */

        if (this.totalPasswords > 0) {

          const strongCount =
            this.allPasswords.filter(p => p.strength === 'Strong').length;

          this.securityScore =
            Math.round((strongCount / this.totalPasswords) * 100);

        } else {

          this.securityScore = 0;

        }

        this.cd.detectChanges();

      }

    });
  }

  /* ================= CATEGORY FILTER ================= */
  onCategoryChange(event: Event) {

    const category = (event.target as HTMLSelectElement).value;
    this.filterMode = category === 'ALL' ? 'all' : 'category';

    if (category === 'ALL') {

      this.passwords = [...this.allPasswords];
      return;

    }

    this.passwords = this.allPasswords.filter(p =>
      p.category === category
    );

  }
  /* ================= SORT ================= */

  onSortChange(event: Event) {

    const sortBy = (event.target as HTMLSelectElement).value;

    if (sortBy === 'name') {

      this.passwords = [...this.passwords].sort((a, b) =>
        a.platform.localeCompare(b.platform)
      );

    }

    if (sortBy === 'created') {

      this.passwords = [...this.passwords].sort((a, b) =>
        new Date(b.createdAt).getTime() -
        new Date(a.createdAt).getTime()
      );

    }

    if (sortBy === 'updated') {

      this.passwords = [...this.passwords].sort((a, b) =>
        new Date(b.updatedAt).getTime() -
        new Date(a.updatedAt).getTime()
      );

    }

  }

  /* ================= SEARCH ================= */

  search() {
    this.filterMode = 'search';

    if (!this.searchKeyword) {

      this.passwords = [...this.allPasswords];
      return;

    }

    const keyword = this.searchKeyword.toLowerCase();

    this.passwords = this.allPasswords.filter(p =>
      p.platform?.toLowerCase().includes(keyword) ||
      p.website?.toLowerCase().includes(keyword) ||
      p.username?.toLowerCase().includes(keyword)
    );

  }

  /* ================= FAVORITES ================= */

  loadFavorites() {
    this.filterMode = 'favorites';
    if (this.allPasswords.length > 0) {
      this.passwords = this.allPasswords.filter(p => p.favorite);
      this.cd.detectChanges();
    } else {
      this.api.getVault().subscribe(res => {
        this.allPasswords = [...res];
        this.passwords = this.allPasswords.filter(p => p.favorite);
        this.cd.detectChanges();
      });
    }
  }

  /* ================= WEAK PASSWORDS ================= */

  loadWeakPasswords(user: string) {
    this.filterMode = 'weak';
    if (this.allPasswords.length > 0) {
      this.passwords = this.allPasswords.filter(p => p.strength === 'Weak');
      this.cd.detectChanges();
    } else {
      this.api.getVault().subscribe(res => {
        this.allPasswords = res;
        this.passwords = res.filter(p => p.strength === 'Weak');
        this.cd.detectChanges();
      });
    }
  }

  /* ================= ADD PASSWORD ================= */

  openAdd() {

    this.showAdd = true;
    this.formError = '';

    this.newPassword = {
      accountName: '',
      website: '',
      username: '',
      password: '',
      category: 'SOCIAL_MEDIA',
      notes: ''
    };

  }

  closeAdd() {

    this.showAdd = false;
    this.formError = '';
    this.passwordStrength = '';

  }

  savePassword() {

    this.formError = '';

    if (!this.newPassword.accountName ||
      !this.newPassword.username ||
      !this.newPassword.password) {

      this.formError =
        "Account Name, Username and Password are required";

      return;
    }

    const user = localStorage.getItem('username');
    if (!user) return;

    let calculatedStrength = 'Weak';
    const pwd = this.newPassword.password;
    if (pwd.length >= 8 && /[A-Z]/.test(pwd) && /[0-9]/.test(pwd)) {
      calculatedStrength = 'Strong';
    }

    this.isVerifying = true;

    this.api.addVaultEntry({
      ...this.newPassword,
      strength: calculatedStrength
    }).pipe(finalize(() => this.isVerifying = false))
      .subscribe({

      next: () => {

        this.showAdd = false;

        this.showToast(
          "Password added successfully",
          "toast-success"
        );

        this.loadVault();

      },

      error: (err) => {

        const errorMsg = typeof err?.error === 'string' ? err.error : 
                         (err?.error?.message || "Failed to add password");

        this.showToast(errorMsg, "toast-error");

      }

    });

  }

  /* ================= DELETE ================= */

  openDelete(id: number) {

    this.deleteEntryId = id;

    this.deleteMasterPassword = '';
    this.deleteVerificationCode = '';
    this.deleteError = '';

    this.showDeleteModal = true;

  }

  confirmDelete() {

    this.deleteError = '';

    if (!this.deleteMasterPassword || !this.deleteVerificationCode) {

      this.deleteError = "Master password and verification code are required";
      return;

    }

    const user = localStorage.getItem('username');
    if (!user || !this.deleteEntryId) return;

    this.isVerifying = true; // STARTS VERIFICATION

    this.api.secureDeletePassword({
      entryId: this.deleteEntryId,
      code: this.deleteVerificationCode,
      masterPassword: this.deleteMasterPassword
    }).pipe(finalize(() => this.isVerifying = false))
      .subscribe({

      next: () => {

        this.showDeleteModal = false;

        this.deleteMasterPassword = '';
        this.deleteVerificationCode = '';
        this.deleteError = '';

        this.showToast(
          "Password deleted successfully",
          "toast-success"
        );

        this.loadVault();

      },

      error: (err) => {

        this.deleteError = typeof err?.error === 'string' ? err.error : 
                          (err?.error?.message || "Invalid verification code");

      }

    });

  }

  /* ================= VIEW PASSWORD ================= */

  openView(id: number) {

    this.selectedEntryId = id;

    this.masterPasswordInput = '';
    this.viewVerificationCode = '';
    this.decryptedPassword = '';
    this.showPassword = false;

    this.showViewModal = true;

  }

  closeView() {

    this.showViewModal = false;

    this.masterPasswordInput = '';
    this.viewVerificationCode = '';
    this.decryptedPassword = '';
    this.showEmailModal = false;

  }

  verifyAndView() {

    const user = localStorage.getItem('username');
    if (!user || !this.selectedEntryId) return;

    this.viewError = '';

    /* FRONTEND VALIDATION */

    if (!this.masterPasswordInput || !this.viewVerificationCode) {

      this.viewError = "Master password and verification code required";
      return;

    }

    this.isVerifying = true; // FIX: Set to true BEFORE request

    this.api.revealPassword(this.selectedEntryId, this.masterPasswordInput, this.viewVerificationCode)
      .pipe(finalize(() => this.isVerifying = false))
      .subscribe({
        next: (decryptedPassword: string) => {
          this.decryptedPassword = decryptedPassword;
          this.showPassword = true;
          this.cd.detectChanges();
        },

        error: (err) => {

          this.viewError = typeof err?.error === 'string' ? err.error : 
                           (err?.error?.message || "Invalid master password or verification code");

        }

      });

  }

  /* ================= GENERATE CODE ================= */

  generateViewCode() {
    const email = localStorage.getItem('email') || localStorage.getItem('username');
    if (!email) return;

    this.api.generateVaultCode(email).subscribe({
      next: (code: string) => {
        // No alert needed, simulation will pop up
      },
      error: () => {
        this.showToast("Failed to generate verification code", "toast-error");
      }
    });
  }

  closeEmailModal() {
    this.showEmailModal = false;
  }

  /* ================= EDIT ================= */

  openEdit(p: any) {

    this.editingId = p.id;

    this.editPassword = {
      accountName: p.platform,
      website: p.website || '',
      username: p.username,
      password: '',
      category: p.category,
      notes: p.notes || ''
    };

    this.showEditModal = true;

  }

  updatePassword() {

    if (!this.editingId) return;

    const user = localStorage.getItem('username');

    let calculatedStrength = 'Weak';
    const pwd = this.editPassword.password;
    if (pwd && pwd.length >= 8 && /[A-Z]/.test(pwd) && /[0-9]/.test(pwd)) {
      calculatedStrength = 'Strong';
    } else if (!pwd) {
      // If password field is empty (user didn't change it), find original password strength or default
      const originalEntry = this.passwords.find(p => p.id === this.editingId);
      calculatedStrength = originalEntry?.strength || 'Weak';
    }

    this.isVerifying = true;

    this.api.updateVaultEntry(this.editingId, {
      ...this.editPassword,
      strength: calculatedStrength
    }).pipe(finalize(() => this.isVerifying = false))
      .subscribe({

      next: () => {

        this.showEditModal = false;

        this.showToast(
          "Password updated successfully",
          "toast-success"
        );

        this.loadVault();

      },

      error: (err) => {

        const errorMsg = typeof err?.error === 'string' ? err.error : 
                         (err?.error?.message || "Update failed");

        this.showToast(errorMsg, "toast-error");

      }

    });

  }

  /* ================= PASSWORD STRENGTH ================= */

  checkStrength(password: string) {

    let score = 0;

    if (!password) {
      this.passwordStrength = '';
      return;
    }

    if (password.length >= 8) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[a-z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;

    if (score <= 2) this.passwordStrength = 'Weak';
    else if (score <= 4) this.passwordStrength = 'Medium';
    else this.passwordStrength = 'Strong';

  }

  togglePassword() {
    this.showMasterPassword = !this.showMasterPassword;
  }

  toggleFavorite(p: any) {

    if (this.togglingIds.has(p.id)) return; // PREVENT DOUBLE CLICK

    const newValue = !p.favorite;
    this.togglingIds.add(p.id);

    this.api.favoriteVaultEntry(p.id, newValue)
      .pipe(finalize(() => {
        this.togglingIds.delete(p.id);
        this.cd.detectChanges();
      }))
      .subscribe({

        next: () => {

          /* update master list */
          const index = this.allPasswords.findIndex(x => x.id === p.id);
          if (index !== -1) {
            this.allPasswords[index].favorite = newValue;
          }

          /* update local entry in view list (prevents jumping) */
          const viewIndex = this.passwords.findIndex(x => x.id === p.id);
          if (viewIndex !== -1) {
            if (this.filterMode === 'favorites' && !newValue) {
              // Remove if unfavorited while in favorites view
              this.passwords.splice(viewIndex, 1);
            } else {
              this.passwords[viewIndex].favorite = newValue;
            }
          }

          /* update favorite count */
          this.favoriteCount =
            this.allPasswords.filter(x => x.favorite).length;

          this.showToast(
            newValue ? "Added to favorites" : "Removed from favorites",
            "toast-success"
          );

        },

        error: () => {

          this.showToast(
            "Favorite update failed",
            "toast-error"
          );

        }

      });

  }

  toggleDeletePassword() {
    this.showDeletePassword = !this.showDeletePassword;
  }

}