import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { EmailSimulatorService } from './email-simulator.service';

@Injectable({ providedIn: 'root' })
export class ApiService {

  BASE_URL = 'http://localhost:8080';

  constructor(private http: HttpClient, private emailSim: EmailSimulatorService) { }

  public getLoggedUser(): string {
    const email = localStorage.getItem('email');
    const username = localStorage.getItem('username');
    if (email && email !== 'null' && email !== 'undefined') return email.toLowerCase().trim();
    if (username && username !== 'null' && username !== 'undefined') return username.trim();
    return '';
  }

  private getLoggedEmail(): string {
    const email = localStorage.getItem('email');
    return (email && email !== 'null' && email !== 'undefined') ? email : '';
  }

  public getUserId(): number | null {
    const id = localStorage.getItem('userId');
    if (!id || id === 'null' || id === 'undefined') return null;
    return Number(id);
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
    const params = new HttpParams().set('email', email);
    return this.http.post(`${this.BASE_URL}/api/auth/logout`, {}, { params });
  }

  updateProfile(data: any) {
    return this.http.put(`${this.BASE_URL}/api/auth/profile`, data, { responseType: 'text' });
  }

  getProfile(usernameOrEmail: string) {
    const params = new HttpParams().set('usernameOrEmail', usernameOrEmail);
    return this.http.get(`${this.BASE_URL}/api/auth/profile`, { params });
  }

  // ================= 2FA =================
  generate2FA(email: string) {
    const params = new HttpParams().set('email', email);
    return this.http.post(`${this.BASE_URL}/api/auth/generate-2fa`, {}, { params, responseType: 'text' })
      .pipe(tap(res => {
        const code = res.includes(': ') ? res.split(': ').pop() : res;
        this.emailSim.showEmail(email, (code || '').trim());
      }));
  }

  generateVerificationCode(usernameOrEmail: string) {
    const params = new HttpParams().set('email', usernameOrEmail);
    return this.http.post(`${this.BASE_URL}/api/auth/generate-2fa`, {}, { params, responseType: 'text' })
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
    const params = new HttpParams().set('usernameOrEmail', usernameOrEmail);
    return this.http.get(`${this.BASE_URL}/api/auth/2fa-status`, { params });
  }

  toggle2FA(usernameOrEmail: string, enabled: boolean) {
    return this.http.post(`${this.BASE_URL}/api/auth/toggle-2fa`, null, {
      params: { usernameOrEmail, enabled }
    });
  }

  showSimulatedNotification(to: string, title: string, message: string) {
    this.emailSim.showNotification(to, title, message);
  }

  // ================= SECURITY QUESTIONS =================
  getSecurityQuestions(usernameOrEmail: string): Observable<string[]> {
    const normalized = usernameOrEmail ? usernameOrEmail.toLowerCase().trim() : '';
    const params = new HttpParams().set('usernameOrEmail', normalized);
    return this.http.get<string[]>(`${this.BASE_URL}/api/auth/security-questions`, { params });
  }

  saveSecurityQuestion(data: any) {
    return this.http.post(`${this.BASE_URL}/api/auth/security-question`, data, { responseType: 'text' });
  }

  updateSecurityAnswers(payload: any) {
    return this.http.post(`${this.BASE_URL}/api/auth/security-question`, payload);
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
    const params = new HttpParams().set('usernameOrEmail', user);
    return this.http.get(`${this.BASE_URL}/api/auth/profile`, { params });
  }

  // ================= VAULT =================
  getVault() {
    const userId = this.getUserId();
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
      userId: this.getUserId(),
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
    const email = this.getLoggedUser();
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
    return this.http.put(`${this.BASE_URL}/vault/favorite/${id}?value=${value}`, {});
  }

  viewPassword(payload: any) {
    return this.http.post<any>(`${this.BASE_URL}/vault/reveal/${payload.entryId}`, { masterPassword: payload.masterPassword });
  }

  revealPassword(id: number | null, masterPassword?: string, code?: string) {
    const email = this.getLoggedUser();
    return this.http.post(`${this.BASE_URL}/vault/reveal/${id}`, { masterPassword, email, code }, { responseType: 'text' });
  }

  updatePassword(id: number, payload: any) {
    return this.updateVaultEntry(id, payload);
  }

  updateVaultEntry(id: number, data: any) {
    const payload = {
      userId: this.getUserId(),
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
    const userId = this.getUserId();
    return this.http.get<any[]>(`${this.BASE_URL}/vault/favorites?userId=${userId}`);
  }

  generateVaultCode(email: string) {
    const params = new HttpParams().set('email', email);
    return this.http.get(`${this.BASE_URL}/vault/generate-code`, { params, responseType: 'text' })
      .pipe(tap(code => {
        this.emailSim.showEmail(email, code);
      }));
  }

  searchVault(usernameOrEmail: string, keyword: string) {
    const userId = this.getUserId();
    return this.http.get<any[]>(`${this.BASE_URL}/vault/search?platform=${keyword}&userId=${userId}`);
  }

  filterVault(usernameOrEmail: string, category: string) {
    const userId = this.getUserId();
    return this.http.get<any[]>(`${this.BASE_URL}/vault/category/${category}?userId=${userId}`);
  }

  sortVault(usernameOrEmail: string, sortBy: string) {
    const userId = this.getUserId();
    return this.http.get<any[]>(`${this.BASE_URL}/vault/sort?userId=${userId}`);
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
    return this.http.get<any[]>(`${this.BASE_URL}/notifications/user/${encodeURIComponent(email)}`);
  }

  getUnreadNotifications(email: string) {
    return this.http.get<any[]>(`${this.BASE_URL}/notifications/user/${encodeURIComponent(email)}/unread`);
  }

  markNotificationAsRead(id: number) {
    return this.http.put(`${this.BASE_URL}/notifications/${id}/read`, {}, { responseType: 'text' });
  }
}