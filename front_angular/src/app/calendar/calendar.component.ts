import { CommonModule, NgIf } from '@angular/common';
import { Component, ElementRef, OnInit, Renderer2, ViewChild } from '@angular/core';
import { ActivatedRoute, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Engineer, SendTask, Task, TeamService } from '../service/team.service';
import { FormsModule, NgForm } from '@angular/forms';
import {MatIconModule} from '@angular/material/icon'
@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet ,CommonModule,FormsModule,MatIconModule],
  templateUrl: './calendar.component.html',
  styleUrl: './calendar.component.css'
})
export class CalendarComponent  implements OnInit{
  currentWeek: Date = new Date();
  week: Date[] = [];
  tasks: Task[] = [];
  engineers: Engineer[] = [];
  isAddTaskModalOpen = false;
  isSuccessModalOpen = false;
  isErrorModalOpen = false;
  isDeleteConfirmModalOpen = false;
  isDeleteSuccessModalOpen = false;
  taskToDelete: Task | null = null;
  isSettingsModalOpen = false;
  newTask: { name: string, engineerName: string,teamId: number, day: string, shift: string } = { name: '', engineerName: '',teamId: 0, day: '', shift: '' };
  teamId: number=0;
 
  isDistributeModalOpen = false;  // New state for distribute modal
  selectedDay: number | null = null;  // Store the selected day

  constructor(private teamService: TeamService,private route: ActivatedRoute) {}

  ngOnInit() {
    this.route.parent?.params.subscribe(params => {
      this.teamId = +params['id'];
      if (this.teamId) {
        this.setWeekDays();
        this.fetchTasks();
        this.fetchEngineers();
        this.newTask.teamId=this.teamId;
      } else {
        console.error('Team ID is not set');
      }
    });
  }

  setWeekDays() {
    this.week = [];
    const startOfWeek = this.getStartOfWeek(this.currentWeek);
    for (let i = 0; i < 7; i++) {
      const day = new Date(startOfWeek);
      day.setDate(startOfWeek.getDate() + i);
      this.week.push(day);
    }
  }

  getStartOfWeek(date: Date): Date {
    const start = new Date(date);
    const day = start.getDay();
    const diff = start.getDate() - day + (day === 0 ? -6 : 1); // Adjust when day is Sunday
    return new Date(start.setDate(diff));
  }

  goToPreviousWeek() {
    this.currentWeek.setDate(this.currentWeek.getDate() - 7);
    this.setWeekDays();
  }

  goToThisWeek() {
    this.currentWeek = new Date();
    this.setWeekDays();
  }

  goToNextWeek() {
    this.currentWeek.setDate(this.currentWeek.getDate() + 7);
    this.setWeekDays();
  }

  getFormattedDateRange(): string {
    const startOfWeek = this.getStartOfWeek(this.currentWeek);
    return `${startOfWeek.toLocaleString('default', { month: 'long' })}, ${startOfWeek.getFullYear()}`;
  }
  openSettingsModal() {
    this.isSettingsModalOpen = true;
  }
  equilibrateTasks(type: number) {
    this.teamService.equilibrateTasks(type, this.teamId).subscribe(
      response => {
        console.log('Tasks equilibrated successfully:', response);
        this.isSuccessModalOpen = true;  // Open success modal
        this.fetchTasks();
      },
      error => {
        console.error('Error equilibrating tasks:', error);
        this.isErrorModalOpen = true;  // Open error modal
      }
    );
    this.closeSettingsModal();  // Close the settings modal
  }

  closeSettingsModal() {
    this.isSettingsModalOpen = false;
  }
  openAddTaskModal() {
    this.isAddTaskModalOpen = true;
  }

  closeAddTaskModal() {
    this.isAddTaskModalOpen = false;
  }

  closeSuccessModal() {
    this.isSuccessModalOpen = false;
  }

  closeErrorModal() {
    this.isErrorModalOpen = false;
  }

  closeDeleteConfirmModal() {
    this.isDeleteConfirmModalOpen = false;
    this.taskToDelete = null;
  }

  closeDeleteSuccessModal() {
    this.isDeleteSuccessModalOpen = false;
  }

  addTask(form: NgForm) {
    if (form.valid) {
      const engineer = this.engineers.find(e => e.id === +this.newTask.engineerName);
      
      if (engineer) {
        const task: SendTask = {
          name: this.newTask.name,
          engineerId: engineer.id,
          teamId: this.teamId,
          createdDate: new Date(this.newTask.day).toISOString(), // Ensure this is in UTC
          shift: this.newTask.shift
        };

        // Debugging output
        console.log('Task to send:', task);
        
        this.teamService.addTask(task).subscribe(
          response => {
            this.fetchTasks();
            this.closeAddTaskModal();
            this.isSuccessModalOpen = true;
            form.resetForm();
          },
          error => {
            console.error('Error adding task:', error);
            this.isErrorModalOpen = true;
          }
        );
      }
    }
  }

  fetchTasks() {
    this.teamService.getTasks(this.teamId).subscribe(tasks => {
      this.tasks = tasks;
    });
  }

  fetchEngineers() {
    this.teamService.getEngineersByTeamId(this.teamId).subscribe(engineers => {
      this.engineers = engineers;
    });
  }

// change the color of day depending on task 

  getTasksForDay(day: Date): Task[] {
    const dayStr = day.toISOString().split('T')[0];
    return this.tasks.filter(task => task.createdDate.split('T')[0] === dayStr);
  }
  
  getDayClass(day: Date): string {
    const dayOfWeek = day.getDay();
  if (dayOfWeek === 0 || dayOfWeek === 6) {
    return 'bg-white';
  }
    const tasksForDay = this.getTasksForDay(day);
    const p1Tasks = tasksForDay.filter(task => task.name === 'p1').length;
    const chatTasks = tasksForDay.filter(task => task.name === 'chat').length;
  
    if (p1Tasks === 0 || chatTasks < 4) {
      return 'bg-[#DA3D27]'; // Customize this class to your desired color
    }
    return 'bg-white'; // Default background color
  }

  getEngineerName(engineerId: number): string {
    const engineer = this.engineers.find(e => e.id === engineerId);
    return engineer ? engineer.name : 'Unknown Engineer';
  }

  confirmDeleteTask(task: Task) {
    this.taskToDelete = task;
    this.isDeleteConfirmModalOpen = true;
  }

  deleteTask() {
    if (this.taskToDelete) {
      this.teamService.deleteTask(this.taskToDelete.id).subscribe(
        response => {
          this.fetchTasks();
          this.isDeleteConfirmModalOpen = false;
          this.isDeleteSuccessModalOpen = true;
        },
        error => {
          this.isErrorModalOpen = true;
        }
      );
    }
  }
  resetTasks(): void {
    this.teamService.resetTasks(this.teamId).subscribe(
      response => {
        console.log('Tasks reset successfully');
        this.isSuccessModalOpen = true;
        this.fetchTasks();
      },
      error => {
        console.error('Error resetting tasks', error);
        this.isErrorModalOpen = true;
      }
    );
  }



  openDistributeModal() {
    this.isDistributeModalOpen = true;
    // Ensure selectedDay is set to Wednesday (3) when the modal opens
    this.selectedDay = 3;
    this.closeSettingsModal();  // Close the settings modal when opening the distribute modal
  }

  closeDistributeModal() {
    this.isDistributeModalOpen = false;
  }

  distributeTasks(): void {
    if (this.selectedDay !== null) {
      this.teamService.planTasks(this.teamId, this.selectedDay).subscribe(
        response => {
          console.log('Tasks distributed successfully');
          this.isSuccessModalOpen = true;
          this.fetchTasks();
        },
        error => {
          console.error('Error distributing tasks', error);
          this.isErrorModalOpen = true;
        }
      );
    }
    this.closeDistributeModal();  // Close the distribute modal after submission
  }

  getTaskClass(taskName: string): string {
    switch (taskName) {
      case 'p1':
        return 'bg-[#FD9B6C]';
      case 'chat':
        return 'bg-[#5999AF]';
      case 'qm':
        return 'bg-[#ffc145]';
      case 'stc':
        return 'bg-[#5b5f97]';
      default:
        return 'bg-gray-200';
    }
  }
 
 
}
