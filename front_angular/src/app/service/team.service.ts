import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import { MatSnackBar} from '@angular/material/snack-bar';

export interface Engineer {
  id: number;
  name: string;
  teamId:number;
  chat:Boolean ;
  p1:Boolean ;
  qm:Boolean ;
  stc:Boolean ;
}
export interface EngineerSend {
  name: string;
  teamId:number;
  chat:Boolean ;
  p1:Boolean ;
  qm:Boolean ;
  stc:Boolean ;
}
export interface Task{
  id :number;
  name:string;
  engineerId:number;
  createdDate:any;
  teamId:number;
  shift:string;
}
export interface SendTask {
  name: string;
  engineerId: number;
  createdDate: string;
  shift:string;
  teamId:number;
}
export interface Vacation {
  id: number;
  engineerId: number;
  startDate: string; // ISO 8601 string format (e.g., "2024-07-24")
  endDate: string;   // ISO 8601 string format (e.g., "2024-07-31")
  shift:string;
  teamId:number;
}

export interface StatResponse {
  engineerId: number;
  teamID: number;
  p1: number;
  stc: number;
  chat: number;
  qm: number;
  score: number;
  credit?: number; // Optional property to store the calculated credit
}
@Injectable({
  providedIn: 'root'
})
export class TeamService {
  
  private apiGetEngineers = 'http://localhost:8081/api/engineers';

  constructor(private http: HttpClient, private snackBar: MatSnackBar) {}

  getTeams(): Observable<Engineer[]> {
    return this.http.get<Engineer[]>(this.apiGetEngineers);
  }

  private apiPostEngineer = 'http://localhost:8081/api/engineers';
  
  createEngineer(engineer: EngineerSend): Observable<Engineer> {
    return this.http.post<Engineer>(this.apiPostEngineer, engineer);
  }

  private apiGetTask = 'http://localhost:8083/api/tasks/engineer';
  
  getTask(engineerId: number): Observable<Task[]> {
    const url = `${this.apiGetTask}/${engineerId}`;
    return this.http.get<Task[]>(url);
  }

  private apiDeleteEngineer = 'http://localhost:8081/api/engineers';
  
  deleteEngineer(engineerId: number) {
    const url = `${this.apiDeleteEngineer}/${engineerId}`;
    return this.http.delete<Engineer>(url);
  }

  private apiGetEngineer = 'http://localhost:8081/api/engineers';
  
  getEngineer(engineerId: number) {
    const url = `${this.apiGetEngineer}/${engineerId}`;
    return this.http.get<Engineer>(url);
  }

  private apiGetEngineersByTeam = 'http://localhost:8081/api/engineers/team';

  getEngineersByTeamId(teamId: number): Observable<Engineer[]> {
    const url = `${this.apiGetEngineersByTeam}/${teamId}`;
    return this.http.get<Engineer[]>(url);
  }

  private apiUpdateEngineer = 'http://localhost:8081/api/engineers';
  
  updateEngineer(engineer: Engineer) {
    const url = `${this.apiUpdateEngineer}/${engineer.id}`;
    return this.http.put<Engineer>(url, engineer);
  }

  private apiVacation = 'http://localhost:8083/api/vacations';

  getVacationsByTeamId(teamId: number): Observable<Vacation[]> {
    return this.http.get<Vacation[]>(`${this.apiVacation}/team/${teamId}`);
  }

  getVacationById(id: number): Observable<Vacation> {
    return this.http.get<Vacation>(`${this.apiVacation}/${id}`);
  }

  createVacation(vacation: Vacation): Observable<Vacation> {
    return this.http.post<Vacation>(this.apiVacation, vacation);
  }

  updateVacation(id: number, vacation: Vacation): Observable<Vacation> {
    return this.http.put<Vacation>(`${this.apiVacation}/${id}`, vacation);
  }

  deleteVacation(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiVacation}/${id}`);
  }

  private apiDeleteTask = 'http://localhost:8083/api/tasks';
  
  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiDeleteTask}/${id}`);
  }

  private apiPostTask = 'http://localhost:8083/api/tasks';
  
  addTask(task: SendTask): Observable<Task> {
    return this.http.post<Task>(this.apiPostTask, task);
  }

  private apiGetTaskbyTeamId = 'http://localhost:8083/api/tasks/team';
  
  getTasks(teamId: number): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiGetTaskbyTeamId}/${teamId}`);
  }

  private apiPlan = 'http://localhost:8083/api/distribute';

  planTasks(teamId: number, stc: number): Observable<any> {
    return this.http.post(`${this.apiPlan}?teamId=${teamId}&number=1&stc=${stc}`, {});
  }

  resetTasks(teamId: number): Observable<any> {
    return this.http.post(`${this.apiPlan}?number=2&teamId=${teamId}`, {});
  }

  private apiStats = 'http://localhost:8083/api/stats/current-quarter';
  
  getStatsForCurrentQuarter(teamId: number): Observable<StatResponse[]> {
    return this.http.get<StatResponse[]>(`${this.apiStats}?teamId=${teamId}`);
  }

  private apiUpdateStats = 'http://localhost:8083/api';
  
  updateStatsForCurrentQuarter(teamId: number): Observable<void> {
    return this.http.put<void>(`${this.apiUpdateStats}/stats/current-quarter/update`, null, {
      params: { teamId: teamId.toString() }
    });
  }

  private apiGetCustomStats = 'http://localhost:8083/api';
  
  getStats(startDate: string, endDate: string, teamId: number): Observable<StatResponse[]> {
    return this.http.get<StatResponse[]>(`${this.apiGetCustomStats}/stats`, {
      params: {
        startDate,
        endDate,
        teamId: teamId.toString()
      }
    });
  }

  private apiEquilibrate = 'http://localhost:8083/api'; 
  
  equilibrateTasks(type: number, teamId: number): Observable<any> {
    return this.http.post(`${this.apiEquilibrate}/equilibrate?type=${type}&teamId=${teamId}`, {});
  }
}