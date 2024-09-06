import { Component } from '@angular/core';
import { Engineer, TeamService, Vacation } from '../service/team.service';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule, NgForm } from '@angular/forms';

@Component({
  selector: 'app-vacation',
  standalone: true,
  imports: [CommonModule, RouterLink , FormsModule],
  templateUrl: './vacation.component.html',
  styleUrl: './vacation.component.css'
})
export class VacationComponent {
  vacations: Vacation[] = [];
  engineers: Engineer[] = [];
  newVacation: Vacation = { id: 0, engineerId: 0, shift: '',teamId:0, startDate: '', endDate: '' };
  editVacation: Vacation = { id: 0, engineerId: 0, shift: '', startDate: '',teamId:0, endDate: '' };
  vacationIdToDelete: number | null = null;
  teamId:number=0;

  isAddVacationModalOpen = false;
  isEditVacationModalOpen = false;
  isDeleteConfirmModalOpen = false;
  isSuccessModalOpen = false;
  isErrorModalOpen = false;
  successMessage = '';
  errorMessage = '';
  isSingleDayVacation = false; 

  constructor(private vacationService: TeamService,private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.parent?.params.subscribe(params => {
      this.teamId = +params['id'];});
    this.loadVacations();
    this.loadEngineers();
  }

  loadVacations(): void {
    this.vacationService.getVacationsByTeamId(this.teamId).subscribe((data: Vacation[]) => {
      this.vacations = data;
    });
  }

  loadEngineers(): void {
    this.vacationService.getTeams().subscribe((data: Engineer[]) => {
      this.engineers = data;
    });
  }

  getEngineerName(engineerId: number): string {
    const engineer = this.engineers.find(e => e.id === engineerId);
    return engineer ? engineer.name : 'Unknown';
  }

  openAddVacationModal(): void {
    this.isAddVacationModalOpen = true;
  }

  closeAddVacationModal(): void {
    this.isAddVacationModalOpen = false;
  }

  openEditVacationModal(vacation: Vacation): void {
    this.editVacation = { ...vacation };
    this.isEditVacationModalOpen = true;
  }

  closeEditVacationModal(): void {
    this.isEditVacationModalOpen = false;
  }

  openDeleteConfirmModal(vacationId: number): void {
    this.vacationIdToDelete = vacationId;
    this.isDeleteConfirmModalOpen = true;
  }

  closeDeleteConfirmModal(): void {
    this.isDeleteConfirmModalOpen = false;
    this.vacationIdToDelete = null;
  }

  addVacation(form: any): void {
    if (form.invalid) {
      this.errorMessage = 'Please fill out all required fields.';
      this.isErrorModalOpen = true;
      return;
    }
    this.newVacation.teamId=this.teamId;
    this.vacationService.createVacation(this.newVacation).subscribe({
      next: () => {
        this.successMessage = 'Vacation added successfully!';
        this.isSuccessModalOpen = true;
        this.loadVacations();
        this.closeAddVacationModal();
      },
      error: () => {
        this.errorMessage = 'Failed to add vacation.';
        this.isErrorModalOpen = true;
      }
    });
  }

  updateVacation(form: any): void {
    if (form.invalid) {
      this.errorMessage = 'Please fill out all required fields.';
      this.isErrorModalOpen = true;
      return;
    }

    this.vacationService.updateVacation(this.editVacation.id,this.editVacation).subscribe({
      next: () => {
        this.successMessage = 'Vacation updated successfully!';
        this.isSuccessModalOpen = true;
        this.loadVacations();
        this.closeEditVacationModal();
      },
      error: () => {
        this.errorMessage = 'Failed to update vacation.';
        this.isErrorModalOpen = true;
      }
    });
  }

  confirmDeleteVacation(): void {
    if (this.vacationIdToDelete !== null) {
      this.vacationService.deleteVacation(this.vacationIdToDelete).subscribe({
        next: () => {
          this.successMessage = 'Vacation deleted successfully!';
          this.isSuccessModalOpen = true;
          this.loadVacations();
          this.closeDeleteConfirmModal();
        },
        error: () => {
          this.errorMessage = 'Failed to delete vacation.';
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
  determineShiftOptions(): boolean {
    this.isSingleDayVacation = this.newVacation.startDate === this.newVacation.endDate;
  return this.isSingleDayVacation;
  }
}