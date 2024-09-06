import { Component, OnInit } from '@angular/core';
import { CalendarComponent } from "../calendar/calendar.component";
import { AppComponent } from "../app.component";
import { ActivatedRoute, Router, RouterLink, RouterModule } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CalendarComponent, AppComponent,RouterLink,RouterModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit{
  teamId: number=0;
  constructor(private router:Router,private route: ActivatedRoute){}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.teamId = +params['id'];
    });
  }
navigateToTeamList() :void {
  this.router.navigate(['team-list']);
}


}
