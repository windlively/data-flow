import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {FlowConfig} from '../../model/flow-config';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {SelectionModel} from '@angular/cdk/collections';
import {AppService} from '../../service/app.service';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {MatDialog} from '@angular/material/dialog';
import {FlowConfigEditDialogComponent} from './flow-config-edit-dialog.component';

@Component({
  selector: 'app-flow-config',
  templateUrl: './flow-config.component.html',
  styleUrls: ['./flow-config.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class FlowConfigComponent implements OnInit, AfterViewInit {

  constructor(public app: AppService,
              public dialog: MatDialog) { }

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

  initTable = (flowList: FlowConfig[]) => {
    this.dataSource = new MatTableDataSource<FlowConfig>(flowList)
    this.dataSource.paginator = this.paginator
    this.dataSource.sort = this.sort
  }

  ngAfterViewInit(): void {
    this.app.allFlowConfigList.subscribe(s => this.initTable(s))
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

  editFlowConfig = (flowConfig: FlowConfig) => {
    this.dialog.open(FlowConfigEditDialogComponent, {
      width: '95vw',
      height: '90vh',
      maxWidth: '92vw',
      disableClose: true,
      data: flowConfig,
      id: 'flow-config-edit-dialog'
    })
  }

}
