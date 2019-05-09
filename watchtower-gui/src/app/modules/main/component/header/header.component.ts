import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'wt-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  title: string = 'Seqera';

  constructor() { }

  ngOnInit() {
  }

}
