import { Component, NgModule, OnInit } from '@angular/core';
import { Engineer, StatResponse, Task, TeamService } from '../service/team.service';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-stats',
  standalone: true,
  imports: [CommonModule ,FormsModule],
  templateUrl: './stats.component.html',
  styleUrl: './stats.component.css'
})
export class StatsComponent implements OnInit{
  isCustomizeModalOpen = false;
  startDate: string = '';
  endDate: string = '';
  engineerStats: StatResponse[] = [];
  engineers: Engineer[] = [];
  teamId: number = 0;
  averageScore: number = 0;

  constructor(private teamService: TeamService, private route: ActivatedRoute) {}

  ngOnInit() {
    this.route.parent?.params.subscribe(params => {
      this.teamId = +params['id'];
      if (this.teamId) {
        this.fetchEngineers();
        this.fetchStats();
      } else {
        console.error('Team ID is not set');
      }
    });
  }

  fetchEngineers() {
    this.teamService.getEngineersByTeamId(this.teamId).subscribe(engineers => {
      this.engineers = engineers;
    });
  }

  fetchStats() {
    this.teamService.getStatsForCurrentQuarter(this.teamId).subscribe(
      stats => {
        this.engineerStats = stats;
        this.calculateAverageScore();
        this.calculateCredits();
      },
      error => {
        console.error('Error fetching stats:', error);
      }
    );
  }

  calculateAverageScore() {
    const totalScore = this.engineerStats.reduce((sum, stat) => sum + stat.score, 0);
    this.averageScore = +(totalScore / (this.engineerStats.length || 1)).toFixed(0);
  }

  calculateCredits() {
    this.engineerStats.forEach(stat => {
      stat.credit = +(this.averageScore - stat.score).toFixed(0);
    });
  }

  getEngineerName(engineerId: number): string {
    const engineer = this.engineers.find(e => e.id === engineerId);
    return engineer ? engineer.name : 'Unknown Engineer';
  }
  refreshStats() {
    this.teamService.updateStatsForCurrentQuarter(this.teamId).subscribe(
      () => {
        this.fetchStats(); // Re-fetch the stats after updating
      },
      error => {
        console.error('Error updating stats:', error);
      }
    );
  }
  // Open the customize modal
  openCustomizeModal() {
    this.isCustomizeModalOpen = true;
  }

  // Close the customize modal
  closeCustomizeModal() {
    this.isCustomizeModalOpen = false;
  }

  // Fetch customized stats based on selected dates
  fetchCustomizedStats(form: any) {
    if (form.invalid) {
      console.error('Please select both start and end dates.');
      return;
    }

    this.teamService.getStats(this.startDate, this.endDate, this.teamId).subscribe(
      stats => {
        this.engineerStats = stats;
        this.calculateAverageScore();
        this.calculateCredits();
        this.closeCustomizeModal();
      },
      error => {
        console.error('Error fetching customized stats:', error);
      }
    );
  }
}