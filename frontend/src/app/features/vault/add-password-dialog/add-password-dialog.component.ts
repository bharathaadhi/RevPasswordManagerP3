import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select'; 

import { VaultService } from '../../../core/services/vault.service';

@Component({
  selector: 'app-add-password-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule   
  ],
  templateUrl: './add-password-dialog.component.html',
  styleUrls: ['./add-password-dialog.component.css']
})
export class AddPasswordDialogComponent {

  form = {
    account: '',
    username: '',
    password: '',
    category: '',
    notes: '' 
  };

  constructor(
    private vaultService: VaultService,
    private dialogRef: MatDialogRef<AddPasswordDialogComponent>
  ) {}

  save() {

    this.vaultService.addPassword(this.form).subscribe({
      next: () => {
        console.log('Password saved');
        this.dialogRef.close(true);
      },
      error: (err: any) => console.error(err)
    });

  }
}