import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Engineer, EngineerSend, TeamService } from '../service/team.service';

@Component({
  selector: 'app-update-engineer',
  standalone: true,
  imports: [FormsModule, CommonModule,RouterLink],
  templateUrl: './update-engineer.component.html',
  styleUrl: './update-engineer.component.css'
})
export class UpdateEngineerComponent implements OnInit {
  constructor(private router: Router, private route: ActivatedRoute) { }

  engineer: Engineer= {
    id:0,
    name: '',
    teamId: 1,
    chat: false,
    p1: false,
    qm: false,
    stc: false,
  };
  teamService = inject(TeamService);
  showToastSuccess = false;
  showToastError = false;
  errorMessage: string | null = null;

  ngOnInit(): void {
    const engineerId = this.route.snapshot.params['id'];
    this.loadEngineer(engineerId);
  }

  loadEngineer(id: number): void {
    this.teamService.getEngineer(id).subscribe({
      next: (res: Engineer) => {
        this.engineer = {
          id:res.id,
          name: res.name,
          teamId: res.teamId,
          chat: res.chat,
          p1: res.p1,
          qm: res.qm,
          stc: res.stc,
        };
      },
      error: (err: any) => {
        console.error('Error fetching engineer data', err);
      }
    });
  }

  validateName(name: string): boolean {
    const re = /^[a-zA-Z\s]*$/;
    return re.test(name);
  }

  updateEngineer(form: NgForm): void {
    if (!this.validateName(this.engineer.name)) {
      this.errorMessage = 'Name should contain only characters';
      this.showToastError = true;
      this.showToastSuccess = false;
      return;
    }
    if (!this.engineer.name || !this.engineer.teamId) {
      this.errorMessage = 'All fields are required';
      this.showToastError = true;
      this.showToastSuccess = false;
      return;
    }

    this.teamService.updateEngineer(this.engineer).subscribe({
      next: () => {
        this.showToastSuccess = true;
        setTimeout(() => this.showToastSuccess = false, 3000);
        
      },
      error: () => {
        this.showToastError = true;
        setTimeout(() => this.showToastError = false, 3000);
      }
    });
  }

  closeToast(): void {
    this.showToastSuccess = false;
    this.showToastError = false;
  }
}
