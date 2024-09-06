import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AllteamsService, SendTeam, Team } from '../service/allteams.service';
import { FormsModule } from '@angular/forms';
@Component({
  selector: 'app-team-list',
  standalone: true,
  imports: [CommonModule,RouterLink,FormsModule],
  templateUrl: './team-list.component.html',
  styleUrl: './team-list.component.css'
})
export class TeamListComponent {
  teams: Team[] = [];
  showTeamModal = false;
  showSuccessModal = false;
  showFailureModal = false;
  currentTeamName = '';
  isEditing = false;
  editingTeam: Team | null = null;

  constructor(private allteamsService: AllteamsService,private router: Router) {}

  ngOnInit(): void {
    this.loadTeams();
  }

  loadTeams(): void {
    this.allteamsService.getTeams().subscribe(
      (data) => this.teams = data,
      (error) => console.error('Error fetching teams', error)
    );
  }

  openAddTeamModal(): void {
    this.isEditing = false;
    this.currentTeamName = '';
    this.showTeamModal = true;
  }

  openEditTeamModal(team: Team): void {
    this.isEditing = true;
    this.editingTeam = team;
    this.currentTeamName = team.name;
    this.showTeamModal = true;
  }

  closeTeamModal(): void {
    this.showTeamModal = false;
    this.currentTeamName = '';
  }

  onTeamSubmit(): void {
    if (this.currentTeamName.trim() === '') {
      // Show validation error if needed
      return;
    }

    if (this.isEditing && this.editingTeam) {
      const updatedTeam = { ...this.editingTeam, name: this.currentTeamName };
      this.allteamsService.updateTeam(updatedTeam).subscribe(
        () => {
          this.showTeamModal = false;
          this.showSuccessModal = true;
          this.loadTeams();
        },
        () => {
          this.showTeamModal = false;
          this.showFailureModal = true;
        }
      );
    } else {
      const newTeam: Team = { id: 0, name: this.currentTeamName };
      this.allteamsService.addTeam(newTeam).subscribe(
        () => {
          this.showTeamModal = false;
          this.showSuccessModal = true;
          this.loadTeams();
        },
        () => {
          this.showTeamModal = false;
          this.showFailureModal = true;
        }
      );
    }
  }

  closeSuccessModal(): void {
    this.showSuccessModal = false;
  }

  closeFailureModal(): void {
    this.showFailureModal = false;
  }
  viewTeamDetails(team: Team): void {
    this.router.navigate(['home', team.id, 'calendar']);
  }

  confirmDeleteTeam(team: Team): void {
    if (confirm(`Are you sure you want to delete the team: ${team.name}?`)) {
      this.deleteTeam(team);
    }
  }

  deleteTeam(team: Team): void {
    this.allteamsService.deleteTeam(team.id).subscribe(
      () => {
        this.showSuccessModal = true;
        this.loadTeams(); // Reload teams after deletion
      },
      () => {
        this.showFailureModal = true;
      }
    );
  }
}