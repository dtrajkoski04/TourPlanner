import { Component, AfterViewInit } from '@angular/core';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-root',
  standalone: true,
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  imports: [
    FormsModule,
    HttpClientModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ]
})
export class AppComponent implements AfterViewInit {
  map: any;
  searchTerm: string = '';

  constructor(private http: HttpClient) {}

  async ngAfterViewInit() {
    if (typeof window !== 'undefined') {
      const L = await import('leaflet');
      this.map = L.map('map').setView([48.2082, 16.3738], 13);

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors'
      }).addTo(this.map);
    }
  }

  async search() {
    if (!this.searchTerm.trim()) return;

    const encoded = encodeURIComponent(this.searchTerm);
    const result: any = await this.http
      .get(`https://nominatim.openstreetmap.org/search?q=${encoded}&format=json&limit=1`)
      .toPromise();

    if (result.length) {
      const { lat, lon } = result[0];
      this.map.setView([+lat, +lon], 14);

      const L = await import('leaflet');
      L.marker([+lat, +lon]).addTo(this.map);
    }
  }
}
