import { Routes } from '@angular/router';
import { EngineerListComponent } from './engineer-list/engineer-list.component';
import { AddEngineerComponent } from './add-engineer/add-engineer.component';
import { TeamListComponent } from './team-list/team-list.component';
import { CalendarComponent } from './calendar/calendar.component';
import { TaskListComponent } from './task-list/task-list.component';
import { VacationComponent } from './vacation/vacation.component';
import { UpdateEngineerComponent } from './update-engineer/update-engineer.component';
import { HomeComponent } from './home/home.component';
import { StatsComponent } from './stats/stats.component';



export const routes: Routes= [
    { path: '', component:TeamListComponent },
    {
      path: 'home/:id',
      component: HomeComponent, // HomeComponent as the main component
      children: [ // Nested routes for the main content area
        { path: '', redirectTo: 'calendar', pathMatch: 'full' },
        { path: 'calendar', component: CalendarComponent },
        { path: 'engineer-list', component: EngineerListComponent },
        { path: 'vacation', component: VacationComponent },
        { path: 'team-list', component: TeamListComponent },
        { path: 'team/:id', component: HomeComponent },
        {path:'stats' , component :StatsComponent}
      ]
    },
    { path: '**', redirectTo: '' } // Wildcard route for a 404 page or redirection
  ];
