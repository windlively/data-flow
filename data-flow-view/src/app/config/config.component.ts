import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {AppService} from '../app.service';
import {MatTableDataSource} from "@angular/material/table";
import {FlowConfig} from '../model/flow-config';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {SelectionModel} from '@angular/cdk/collections';

export interface PeriodicElement {
  name: string;
  position: number;
  weight: number;
  symbol: string;
}

export class FlowInfo{

  flow_id: string;
  source: string;
  schema: string;
  name: string

}

const ELEMENT_DATA: PeriodicElement[] = [
  {position: 1, name: 'Hydrogen', weight: 1.0079, symbol: 'H'},
  {position: 2, name: 'Helium', weight: 4.0026, symbol: 'He'},
  {position: 3, name: 'Lithium', weight: 6.941, symbol: 'Li'},
  {position: 4, name: 'Beryllium', weight: 9.0122, symbol: 'Be'},
  {position: 5, name: 'Boron', weight: 10.811, symbol: 'B'},
  {position: 6, name: 'Carbon', weight: 12.0107, symbol: 'C'},
  {position: 7, name: 'Nitrogen', weight: 14.0067, symbol: 'N'},
  {position: 8, name: 'Oxygen', weight: 15.9994, symbol: 'O'},
  {position: 9, name: 'Fluorine', weight: 18.9984, symbol: 'F'},
  {position: 10, name: 'Neon', weight: 20.1797, symbol: 'Ne'},
];

@Component({
  selector: 'app-config',
  templateUrl: './config.component.html',
  styleUrls: ['./config.component.css']
})
export class ConfigComponent implements OnInit, AfterViewInit {

  constructor(public app: AppService) { }

  ngOnInit(): void {

  }

  displayedColumns: string[] = ['flow_id', 'source', 'schema', 'name', 'select'];
  dataSource: MatTableDataSource<FlowInfo> = new MatTableDataSource<FlowInfo>([]);
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;


  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  flowListToDataSource = (flowList: FlowConfig[]): MatTableDataSource<FlowInfo> => {
    return new MatTableDataSource<FlowInfo>(flowList.map((c) => {
      const info: FlowInfo = new FlowInfo();
      info.flow_id = c._id
      info.name = c.name
      info.source = c.source
      info.schema = c.schema
      return info
    }))
  }

  initTable = (flowList: FlowConfig[]) => {
    this.dataSource = this.flowListToDataSource(flowList);
    this.dataSource.paginator = this.paginator
    this.dataSource.sort = this.sort
  }

  ngAfterViewInit(): void {
    this.initTable(this.app.allFlowConfigList)
    this.app.allFlowConfigListSubject.subscribe(s => this.initTable(s))
  }

  selection = new SelectionModel<FlowInfo>(true, []);

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.selection.select(row));
  }

  /** The label for the checkbox on the passed row */
  checkboxLabel(row?: FlowInfo): string {
    if (!row) {
      return `${this.isAllSelected() ? 'select' : 'deselect'} all`;
    }
    return `${this.selection.isSelected(row) ? 'deselect' : 'select'} row ${row.flow_id}`;
  }

}
