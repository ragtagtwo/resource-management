import { Component, inject, NgModule, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Engineer,  EngineerSend,  TeamService } from '../service/team.service';
import { FormsModule, NgForm } from '@angular/forms';
import { CommonModule } from '@angular/common';



@Component({
  selector: 'app-add-engineer',
  standalone: true,
  imports: [RouterLink , FormsModule , CommonModule ],
  templateUrl: './add-engineer.component.html',
  styleUrl: './add-engineer.component.css'
})



export class AddEngineerComponent  {

constructor( private router: Router){}
  

engineer: EngineerSend = {
  name: '',
  teamId: 1,
  chat:false,
  p1:false ,
  qm:false ,
  stc:false ,
};
teamService = inject(TeamService);

showToastSuccess = false;
showToastError = false;
errorMessage: string | null = null;

validateEmail(email: string): boolean {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(email);
}

validateName(name: string): boolean {
  const re = /^[a-zA-Z\s]*$/;
  return re.test(name);
}


createEngineer(form: NgForm) {
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

  this.teamService.createEngineer(this.engineer).subscribe({
    next: (res: Engineer) => {
      console.log('Engineer created successfully', res);
      this.showToastSuccess = true;
      this.showToastError = false;
      this.errorMessage = null;
      form.resetForm();
    },
    error: (err: any) => {
      console.error('Error creating engineer', err);
      this.showToastSuccess = false;
      this.showToastError = true;
      this.errorMessage = null;
    }
  });
}

closeToast() {
  this.showToastSuccess = false;
  this.showToastError = false;
}
}
