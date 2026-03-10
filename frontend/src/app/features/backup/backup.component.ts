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

  private router = inject(Router);

  constructor(private api: ApiService) { }

  // ================= GENERATE CODE =================

  // ================= GENERATE CODE =================

  generateCode() {
    this.api.generateVaultCode()
      .subscribe((code: string) => {
        alert(`
📧 REV PASSWORD MANAGER EMAIL

Your verification code is:
👉 ${code}

This code expires in 5 minutes.
        `);
      });
  }

  // ================= EXPORT =================

  exportVault() {

    const userId = localStorage.getItem('userId');
    if (!userId) return;

    const payload = {
      userId: Number(userId),
      email: localStorage.getItem('username'),
      masterPassword: this.exportMasterPassword,
      code: this.exportVerificationCode
    };

    this.api.exportVaultSecure(payload)
      .subscribe({
        next: (data: any[]) => {
          // Create JSON blob
          const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
          const url = window.URL.createObjectURL(blob);

          const a = document.createElement('a');
          a.href = url;
          a.download = `rev-vault-backup-${new Date().toISOString().split('T')[0]}.json`;
          a.click();

          window.URL.revokeObjectURL(url);
          alert("Vault Exported Successfully");
        },
        error: (err) => {
          alert(err.error || "Export Failed: Invalid Credentials");
        }
      });
  }

  // ================= IMPORT =================

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  importVault() {

    if (!this.selectedFile) {
      alert("Select backup file");
      return;
    }

    const userId = localStorage.getItem('userId');
    if (!userId) return;

    const reader = new FileReader();

    reader.onload = () => {

      try {
        const data = JSON.parse(reader.result as string);

        const payload = {
          userId: Number(userId),
          email: localStorage.getItem('username'),
          masterPassword: this.importMasterPassword,
          code: this.importVerificationCode,
          entries: data
        };

        this.api.importVaultSecure(payload).subscribe({
          next: () => {
            alert("Import Successful");
            this.router.navigate(['/vault']);
          },
          error: (err) => alert(err.error || "Import Failed: Invalid Credentials")
        });
      } catch (e) {
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