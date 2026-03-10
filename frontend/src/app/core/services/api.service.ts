import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { EmailSimulatorService } from './email-simulator.service';

@Injectable({ providedIn: 'root' })
export class ApiService {

  BASE_URL = 'http://localhost:8080';

  constructor(private http: HttpClient, private emailSim: EmailSimulatorService) { }

  private getLoggedUser(): string {
    return localStorage.getItem('username') || localStorage.getItem('email') || '';
  }

  // ================= AUTH =================
  login(data: any) {
    return this.http.post<any>(`${this.BASE_URL}/api/auth/login`, data);
  }

  register(data: any) {
    return this.http.post(`${this.BASE_URL}/api/auth/register`, data);
  }

  logout() {
    const email = this.getLoggedUser();
    return this.http.post(`${this.BASE_URL}/api/auth/logout?email=${email}`, {});
  }

  updateProfile(data: any) {
    return this.http.put(`${this.BASE_URL}/api/auth/profile`, data, { responseType: 'text' });
  }

  getProfile(usernameOrEmail: string) {
    return this.http.get(`${this.BASE_URL}/api/auth/profile?usernameOrEmail=${usernameOrEmail}`);
  }

  // ================= 2FA =================
  generate2FA(email: string) {
    return this.http.post(`${this.BASE_URL}/api/auth/generate-2fa?email=${email}`, {}, { responseType: 'text' })
      .pipe(tap(res => {
        const code = res.includes(': ') ? res.split(': ').pop() : res;
        this.emailSim.showEmail(email, (code || '').trim());
      }));
  }

  generateVerificationCode(usernameOrEmail: string) {
    return this.http.post(`${this.BASE_URL}/api/auth/generate-2fa?email=${usernameOrEmail}`, {}, { responseType: 'text' })
      .pipe(tap(res => {
        const code = res.includes(': ') ? res.split(': ').pop() : res;
        this.emailSim.showEmail(usernameOrEmail, (code || '').trim());
      }));
  }

  verify2FA(usernameOrEmailOrData: any, code?: string) {
    if (code !== undefined) {
      // Called with (username, code)
      return this.http.post(`${this.BASE_URL}/api/auth/verify-2fa`, {
        email: usernameOrEmailOrData,
        code: code
      }, { responseType: 'text' });
    }
    // Called with data object
    return this.http.post(`${this.BASE_URL}/api/auth/verify-2fa`, usernameOrEmailOrData, { responseType: 'text' });
  }

  get2FAStatus(usernameOrEmail: string) {
    return this.http.get(`${this.BASE_URL}/api/auth/2fa-status?usernameOrEmail=${usernameOrEmail}`);
  }

  toggle2FA(usernameOrEmail: string, enabled: boolean) {
    return this.http.post(`${this.BASE_URL}/api/auth/toggle-2fa`, null, {
      params: { usernameOrEmail, enabled }
    });
  }

  // ================= SECURITY QUESTIONS =================
  getSecurityQuestions(usernameOrEmail: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.BASE_URL}/api/auth/security-questions/${usernameOrEmail}`);
  }

  saveSecurityQuestion(data: any) {
    return this.http.post(`${this.BASE_URL}/api/auth/security-question`, data, { responseType: 'text' });
  }

  updateSecurityAnswers(payload: any) {
    return this.http.post(`${this.BASE_URL}/api/auth/security-question`, payload, { responseType: 'text' });
  }

  forgotPassword(data: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/api/auth/recover`, data);
  }

  updateMasterPassword(data: any) {
    return this.http.post(`${this.BASE_URL}/api/auth/update-master-password`, data, { responseType: 'text' });
  }

  // ================= DASHBOARD =================
  dashboard() {
    const user = this.getLoggedUser();
    return this.http.get(`${this.BASE_URL}/api/auth/profile?usernameOrEmail=${user}`);
  }

  // ================= VAULT =================
  getVault() {
    const userId = localStorage.getItem('userId');
    if (userId) {
      return this.http.get<any[]>(`${this.BASE_URL}/vault/user/${userId}`);
    }
    return this.http.get<any[]>(`${this.BASE_URL}/vault/all`);
  }

  addPassword(data: any) {
    return this.addVaultEntry(data);
  }

  addVaultEntry(data: any) {
    const payload = {
      userId: localStorage.getItem('userId'),
      platform: data.platform || data.accountName,
      username: data.username,
      encryptedPassword: data.encryptedPassword || data.password,
      category: data.category,
      favorite: data.favorite || false,
      strength: data.strength || 'Weak'
    };
    return this.http.post(`${this.BASE_URL}/vault/save`, payload, { responseType: 'text' });
  }

  deletePassword(id: number) {
    return this.http.delete(`${this.BASE_URL}/vault/delete/${id}`, { responseType: 'text' });
  }

  secureDeletePassword(payload: any) {
    const email = localStorage.getItem('username');
    const masterPassword = payload.masterPassword;
    return this.http.post(`${this.BASE_URL}/vault/delete-secure/${payload.entryId}`, {
      code: payload.code,
      masterPassword,
      email
    }, { responseType: 'text' });
  }

  favoritePassword(id: number, value: boolean) {
    return this.http.put(`${this.BASE_URL}/vault/favorite/${id}`, {});
  }

  favoriteVaultEntry(id: number, value: boolean) {
    return this.http.put(`${this.BASE_URL}/vault/favorite/${id}`, {});
  }

  viewPassword(payload: any) {
    return this.http.post<any>(`${this.BASE_URL}/vault/reveal/${payload.entryId}`, { masterPassword: payload.masterPassword });
  }

  revealPassword(id: number | null, masterPassword?: string) {
    const email = localStorage.getItem('username');
    return this.http.post(`${this.BASE_URL}/vault/reveal/${id}`, { masterPassword, email }, { responseType: 'text' });
  }

  updatePassword(id: number, payload: any) {
    return this.updateVaultEntry(id, payload);
  }

  updateVaultEntry(id: number, data: any) {
    const payload = {
      userId: localStorage.getItem('userId'),
      platform: data.platform || data.accountName,
      username: data.username,
      encryptedPassword: data.encryptedPassword || data.password,
      category: data.category,
      favorite: data.favorite || false,
      strength: data.strength || 'Weak'
    };
    return this.http.put(`${this.BASE_URL}/vault/update/${id}`, payload, { responseType: 'text' });
  }

  getFavorites() {
    return this.http.get<any[]>(`${this.BASE_URL}/vault/favorites`);
  }

  generateVaultCode() {
    return this.http.get(`${this.BASE_URL}/vault/generate-code`, { responseType: 'text' });
  }

  searchVault(usernameOrEmail: string, keyword: string) {
    return this.http.get<any[]>(`${this.BASE_URL}/vault/search?platform=${keyword}`);
  }

  filterVault(usernameOrEmail: string, category: string) {
    return this.http.get<any[]>(`${this.BASE_URL}/vault/category/${category}`);
  }

  sortVault(usernameOrEmail: string, sortBy: string) {
    return this.http.get<any[]>(`${this.BASE_URL}/vault/sort`);
  }

  getOldPasswords(usernameOrEmail: string) {
    return this.http.get<any[]>(`${this.BASE_URL}/vault/all`);
  }

  // Secure export/import (maps to vault service)
  exportVaultSecure(payload: any) {
    return this.http.post<any[]>(`${this.BASE_URL}/vault/export-secure`, payload);
  }

  importVaultSecure(payload: any) {
    return this.http.post(`${this.BASE_URL}/vault/import-secure`, payload, { responseType: 'text' });
  }

  // ================= GENERATOR =================
  generatePassword(data: any) {
    return this.http.post<any>(`${this.BASE_URL}/generator/generate`, data);
  }

  generateMultiplePasswords(payload: any) {
    return this.http.post<string[]>(`${this.BASE_URL}/generator/generate-multiple`, payload);
  }

  // ================= SECURITY / AUDIT =================
  getSecurityAudit(passwords: string[]) {
    return this.http.post<any>(`${this.BASE_URL}/security/audit`, { passwords });
  }

  getSecurityReport(usernameOrEmail: string) {
    return this.http.post<any>(`${this.BASE_URL}/security/audit`, { passwords: [] });
  }

  getWeakPasswords(usernameOrEmail: string) {
    return this.http.post<any[]>(`${this.BASE_URL}/security/weak-passwords`, { passwords: [] });
  }

  getReusedPasswords(usernameOrEmail: string) {
    return this.http.post<any[]>(`${this.BASE_URL}/security/check-reuse`, { passwords: [] });
  }

  exportBackup(passwords: string[]) {
    return this.http.post<any>(`${this.BASE_URL}/security/export-backup`, { passwords });
  }

  // ================= NOTIFICATIONS =================
  getUserNotifications(email: string) {
    return this.http.get<any[]>(`${this.BASE_URL}/notifications/user/${email}`);
  }

  getUnreadNotifications(email: string) {
    return this.http.get<any[]>(`${this.BASE_URL}/notifications/user/${email}/unread`);
  }

  markNotificationAsRead(id: number) {
    return this.http.put(`${this.BASE_URL}/notifications/${id}/read`, {}, { responseType: 'text' });
  }
}