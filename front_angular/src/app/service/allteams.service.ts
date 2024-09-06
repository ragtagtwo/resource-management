import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
export interface Team {
  id: number;
  name: string;
}
export interface SendTeam{
  name: string;
}
@Injectable({
  providedIn: 'root'
})
export class AllteamsService {

  private apiTeams = 'http://localhost:8082/api/teams';

  constructor(private http: HttpClient) {}

  getTeams(): Observable<Team[]> {
    return this.http.get<Team[]>(this.apiTeams);
  }

  addTeam(team: SendTeam): Observable<Team> {
    return this.http.post<Team>(this.apiTeams, team);
  }

  updateTeam(team: Team): Observable<Team> {
    return this.http.put<Team>(`${this.apiTeams}/${team.id}`, team);
  }

  deleteTeam(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiTeams}/${id}`);
  }
}