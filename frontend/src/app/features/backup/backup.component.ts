import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../core/services/api.service';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-backup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './backup.component.html',
  styleUrls: ['./backup.component.css']
})
export class BackupComponent {

  exportMasterPassword = '';
  exportVerificationCode = '';

  showExportPassword = false;
  showImportPassword = false;

  importMasterPassword = '';
  importVerificationCode = '';

  selectedFile: File | null = null;
  
  loadingCode = false;
  exporting = false;
  importing = false;

  private router = inject(Router);

  constructor(private api: ApiService) { }

  // ================= GENERATE CODE =================

  generateCode() {
    const email = this.api.getLoggedUser();
    if (!email || this.loadingCode) return;

    this.loadingCode = true;
    this.api.generateVaultCode(email).subscribe({
      next: () => {
        this.loadingCode = false;
      },
      error: (err) => {
        this.loadingCode = false;
        console.error('Failed to generate code:', err);
      }
    });
  }

  // ================= EXPORT =================

  exportVault() {
    const userId = localStorage.getItem('userId');
    if (!userId || this.exporting) return;

    if (!this.exportMasterPassword || !this.exportVerificationCode) {
      alert("Please enter both Master Password and Verification Code");
      return;
    }

    this.exporting = true;
    const payload = {
      userId: Number(userId),
      email: this.api.getLoggedUser(),
      masterPassword: this.exportMasterPassword,
      code: this.exportVerificationCode
    };

    this.api.exportVaultSecure(payload)
      .subscribe({
        next: (data: any[]) => {
          this.exporting = false;
          
          if (!data || data.length === 0) {
            alert("Your vault is empty. Nothing to export.");
            return;
          }

          // Create JSON blob
          const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
          const url = window.URL.createObjectURL(blob);

          const a = document.createElement('a');
          a.href = url;
          a.download = `rev-vault-backup-${new Date().toISOString().split('T')[0]}.json`;
          a.click();

          window.URL.revokeObjectURL(url);
          alert("Vault Exported Successfully");
          this.exportMasterPassword = '';
          this.exportVerificationCode = '';
        },
        error: (err) => {
          this.exporting = false;
          const msg = err?.error?.message || err?.error || "Export Failed: Invalid Credentials";
          alert(typeof msg === 'string' ? msg : JSON.stringify(msg));
        }
      });
  }

  // ================= IMPORT =================

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  importVault() {
    if (!this.selectedFile || this.importing) {
      alert("Select backup file");
      return;
    }

    if (!this.importMasterPassword || !this.importVerificationCode) {
      alert("Please enter both Master Password and Verification Code");
      return;
    }

    const userId = localStorage.getItem('userId');
    if (!userId) return;

    this.importing = true;
    const reader = new FileReader();

    reader.onload = () => {
      try {
        const data = JSON.parse(reader.result as string);

        const payload = {
          userId: Number(userId),
          email: this.api.getLoggedUser(),
          masterPassword: this.importMasterPassword,
          code: this.importVerificationCode,
          entries: data
        };

        this.api.importVaultSecure(payload).subscribe({
          next: () => {
            this.importing = false;
            alert("Import Successful");
            this.router.navigate(['/vault']);
          },
          error: (err) => {
            this.importing = false;
            const msg = err?.error?.message || err?.error || "Import Failed: Invalid Credentials";
            alert(typeof msg === 'string' ? msg : JSON.stringify(msg));
          }
        });
      } catch (e) {
        this.importing = false;
        alert("Invalid JSON file");
      }
    };

    reader.readAsText(this.selectedFile);
  }

  toggleExportPassword() {
    this.showExportPassword = !this.showExportPassword;
  }

  toggleImportPassword() {
    this.showImportPassword = !this.showImportPassword;
  }
}