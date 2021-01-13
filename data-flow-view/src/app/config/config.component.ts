import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {AppService} from '../app.service';
import {MatTableDataSource} from "@angular/material/table";
import {FlowConfig} from '../model/flow-config';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {SelectionModel} from '@angular/cdk/collections';
import {animate, state, style, transition, trigger} from '@angular/animations';


// export class FlowInfo{
//
//   flow_id: string;
//   source: string;
//   schema: string;
//   name: string
//
// }

@Component({
  selector: 'app-config',
  templateUrl: './config.component.html',
  styleUrls: ['./config.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class ConfigComponent implements OnInit, AfterViewInit {

  constructor(public app: AppService) { }

  ngOnInit(): void {

  }

  displayedColumns: string[] = ['_id', 'source', 'schema', 'name'];
  dataSource: MatTableDataSource<FlowConfig> = new MatTableDataSource<FlowConfig>([]);
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;

  tableExpandedRow: FlowConfig | null;

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  flowListToDataSource = (flowList: FlowConfig[]): MatTableDataSource<FlowConfig> => {
    // return new MatTableDataSource<FlowConfig>(flowList.map((c) => {
    //   const info: F = new FlowInfo();
    //   info.flow_id = c._id
    //   info.name = c.name
    //   info.source = c.source
    //   info.schema = c.schema
    //   return info
    // }))
    return new MatTableDataSource<FlowConfig>(flowList)
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

  selection = new SelectionModel<FlowConfig>(true, []);

  getSelectFlowIdList = (): string[] => {
    return (this.selection? this.selection.selected : []).map(f => f['flow_id'])
  }

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
  checkboxLabel(row?: FlowConfig): string {
    if (!row) {
      return `${this.isAllSelected() ? 'select' : 'deselect'} all`;
    }
    return `${this.selection.isSelected(row) ? 'deselect' : 'select'} row ${row._id}`;
  }

  deleteFlows = (flowIds: string[]) => {

  }

}
