import { Component } from '@angular/core';
import { Engineer, EngineerSend, TeamService } from '../service/team.service';
import { CommonModule } from '@angular/common';
import { RouterLink , RouterLinkActive , RouterOutlet ,Router, ActivatedRoute} from '@angular/router';
import { CalendarComponent } from "../calendar/calendar.component";
import { FormsModule, NgForm } from '@angular/forms';

@Component({
  selector: 'app-engineer-list',
  standalone: true,
  imports: [CommonModule, RouterLink, CalendarComponent, FormsModule],
  templateUrl: './engineer-list.component.html',
  styleUrl: './engineer-list.component.css'
})

export class EngineerListComponent {
  engineers: Engineer[] = [];
  newEngineer: EngineerSend = { name: '', teamId: 0, chat: false, p1: false, qm: false, stc: false };
  editEngineer: Engineer = { id: 0, name: '', teamId: 0, chat: false, p1: false, qm: false, stc: false };
  engineerIdToDelete: number | null = null;

  isAddEngineerModalOpen = false;
  isEditEngineerModalOpen = false;
  isDeleteConfirmModalOpen = false;
  isSuccessModalOpen = false;
  isErrorModalOpen = false;
  successMessage = '';
  errorMessage = '';

  teamId: number=0;

  constructor(private teamService: TeamService,private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.parent?.params.subscribe(params => {
      this.teamId = +params['id'];});
    this.loadEngineers();
  }

  loadEngineers(): void {
    this.teamService.getEngineersByTeamId(this.teamId).subscribe((data: Engineer[]) => {
        this.engineers = data;
    });
}

  getEngineerRoles(engineer: Engineer): string {
    let roles = [];
    if (engineer.qm) roles.push('QM');
    if (engineer.stc) roles.push('STC');
    if (engineer.p1) roles.push('P1');
    if (engineer.chat) roles.push('Chat');
    return roles.join(', ');
  }

  openAddEngineerModal(): void {
    this.isAddEngineerModalOpen = true;
  }

  closeAddEngineerModal(): void {
    this.isAddEngineerModalOpen = false;
  }

  openEditEngineerModal(engineer: Engineer): void {
    this.editEngineer = { ...engineer };
    this.isEditEngineerModalOpen = true;
  }

  closeEditEngineerModal(): void {
    this.isEditEngineerModalOpen = false;
  }

  openDeleteConfirmModal(engineerId: number): void {
    this.engineerIdToDelete = engineerId;
    this.isDeleteConfirmModalOpen = true;
  }

  closeDeleteConfirmModal(): void {
    this.isDeleteConfirmModalOpen = false;
    this.engineerIdToDelete = null;
  }

  addEngineer(form: NgForm): void {
    if (form.invalid) {
      this.errorMessage = 'Please fill out all required fields.';
      this.isErrorModalOpen = true;
      return;
    }
  
    // Set the teamId from the component to the new engineer
    this.newEngineer.teamId = this.teamId;
  
    this.teamService.createEngineer(this.newEngineer).subscribe({
      next: () => {
        this.successMessage = 'Engineer added successfully!';
        this.isSuccessModalOpen = true;
        this.loadEngineers();
        this.closeAddEngineerModal();
        form.resetForm();
      },
      error: () => {
        this.errorMessage = 'Failed to add engineer.';
        this.isErrorModalOpen = true;
      }
    });
  }
  updateEngineer(form: NgForm): void {
    if (form.invalid) {
      this.errorMessage = 'Please fill out all required fields.';
      this.isErrorModalOpen = true;
      return;
    }

    this.teamService.updateEngineer(this.editEngineer).subscribe({
      next: () => {
        this.successMessage = 'Engineer updated successfully!';
        this.isSuccessModalOpen = true;
        this.loadEngineers();
        this.closeEditEngineerModal();
      },
      error: () => {
        this.errorMessage = 'Failed to update engineer.';
        this.isErrorModalOpen = true;
      }
    });
  }

  confirmDeleteEngineer(): void {
    if (this.engineerIdToDelete !== null) {
      this.teamService.deleteEngineer(this.engineerIdToDelete).subscribe({
        next: () => {
          this.successMessage = 'Engineer deleted successfully!';
          this.isSuccessModalOpen = true;
          this.loadEngineers();
          this.closeDeleteConfirmModal();
        },
        error: () => {
          this.errorMessage = 'Failed to delete engineer.';
          this.isErrorModalOpen = true;
          this.closeDeleteConfirmModal();
        }
      });
    }
  }

  closeSuccessModal(): void {
    this.isSuccessModalOpen = false;
  }

  closeErrorModal(): void {
    this.isErrorModalOpen = false;
  }
}