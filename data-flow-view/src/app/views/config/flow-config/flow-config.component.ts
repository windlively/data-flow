import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {FlowConfig} from '../../../model/flow-config';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {SelectionModel} from '@angular/cdk/collections';
import {AppService} from '../../../service/app.service';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {MatDialog} from '@angular/material/dialog';
import {FlowConfigEditDialogComponent} from './flow-config-edit-dialog.component';
import {ConfirmDialogComponent} from '../../../dialog/confirm-dialog.component';
import {HttpClient} from '@angular/common/http';

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
              public dialog: MatDialog,
              public http: HttpClient) { }

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
    this.selection.clear();
  }

  ngAfterViewInit(): void {
    this.app.allFlowConfigList.subscribe(s => this.initTable(s))
  }

  selection = new SelectionModel<FlowConfig>(true, []);

  getSelectFlowIdList = (): string[] => {
    return (this.selection? this.selection.selected : []).map(f => f['_id'])
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
    if(!!!flowIds || flowIds.length === 0){
      this.app.showSnackBar('未选择任何条目')
      return
    }
    this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: '确认删除以下flow？',
        msg: flowIds.join('\n')
      }
    }).afterClosed().subscribe(s => {
      if(s) {
        this.http.delete('flow-config', {
          params: {flowIds}
        }).subscribe(s => {
          this.app.showSnackBar(`删除${s}条配置`)
          this.app.refreshAllFlowConfigList()
        })
      }
    })
  }

  reloadServerFlow = () => {
    this.http.get('/flow-config/reload').subscribe(s => {
      if(s){
        this.app.showSnackBar('server reload flow success')
      }else {
        this.app.showSnackBar('server reload none flow')
      }
    })
  }

  editFlowConfig = (flowConfig: FlowConfig) => this.openEditor(flowConfig, 'update')

  addFlowConfig = () => {
    const flowConfig: FlowConfig = {
      _id: "",
      source: "__default",
      schema: "__default",
      name: "__default",
      node_list: [

      ]
    }
    this.openEditor(flowConfig, 'new')
  }

  private openEditor = (flowConfig: FlowConfig, type: string) => {
    this.dialog.open(FlowConfigEditDialogComponent, {
      width: '95vw',
      height: '90vh',
      maxWidth: '92vw',
      disableClose: true,
      autoFocus: false,
      data: {
        type: type,
        initFlowConfig: flowConfig
      },
      id: 'flow-config-edit-dialog'
    })
  }
}
