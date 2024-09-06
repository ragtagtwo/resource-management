import { Component } from '@angular/core';
import { Task, TeamService } from '../service/team.service';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './task-list.component.html',
  styleUrl: './task-list.component.css'
})
export class TaskListComponent {

  tasks: Task[]=[]; // list of type Task
  isLoading = true;
  error: string | null = null; 
  engineerId: number=0;
  
  constructor(private teamService: TeamService, private route: ActivatedRoute) {}
  
  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.engineerId = +params['id'];
      this.teamService.getTask(this.engineerId).subscribe(
        (data: Task[]) => {
         
          this.tasks = data;
          this.isLoading = false;
        },
        (error) => {
          this.error = 'Failed to load tasks';
          this.isLoading = false;
        }
      );
    });
  }
  

}
