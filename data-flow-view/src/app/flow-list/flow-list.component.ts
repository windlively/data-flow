import {Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Tools} from '../tools';
import {EChartsOption} from 'echarts';

@Component({
  selector: 'app-flow-list',
  templateUrl: './flow-list.component.html',
  styleUrls: ['./flow-list.component.css']
})
export class FlowListComponent implements OnInit {

  constructor(public http: HttpClient) {
  }

  dataSourceSvg=`<svg t="1610373003522" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="29817" width="200" height="200"><path d="M402.0991 506.928044l13.502107-13.500891 97.615089 97.613873-13.502107 13.502107z" p-id="29818" fill="#d42379"></path><path d="M307.358119 412.202874m-136.711724 0a136.711723 136.711723 0 1 0 273.423447 0 136.711723 136.711723 0 1 0-273.423447 0Z" p-id="29819" fill="#d42379"></path><path d="M569.470008 653.759354m-94.749495 0a94.749495 94.749495 0 1 0 189.498991 0 94.749495 94.749495 0 1 0-189.498991 0Z" p-id="29820" fill="#d42379"></path><path d="M602.278389 614.57393l-13.503323-13.503323 97.61509-97.61509 13.503323 13.503324z" p-id="29821" fill="#d42379"></path><path d="M727.831809 485.302292m-63.733935 0a63.733935 63.733935 0 1 0 127.46787 0 63.733935 63.733935 0 1 0-127.46787 0Z" p-id="29822" fill="#d42379"></path></svg>`

  chartOptionList = [];

  ngOnInit(): void {
    this.loadFlows();
  }

  loadFlows = () => {
    this.http.get('/assets/flow_config_example.json').subscribe((data: object[]) => {
      const flowGroupList = Tools.groupBy(data, o => [o['source'], o['schema'], o['name']].join('.'));
      const chartOptions = {};
      for (const namespace of Object.keys(flowGroupList)) {
        chartOptions[namespace] = this.drawOneNamespaceFlow(namespace, flowGroupList[namespace]);
        this.chartOptionList.push(chartOptions[namespace]);
      }
      console.log(this.chartOptionList);
    });
  };

  drawOneNamespaceFlow = (namespace: string, flows: object[]): object => {
    const chartNodes = [];
    const chartLinks = [];
    // 根据当前namespace下flow的个数计算高度
    const height = (flows.length + 1) * 100
    let width = 0
    // 源节点
    chartNodes.push({
      name: namespace,
      x: 0,
      y: height / 2,
      label: {
        show: true,
        position: 'bottom',
        fontSize: '12px',
        fontWeight: 'bolder'
      },
      tooltip: {
        formatter: (params: Object | Array<any>, ticket: string, callback: (ticket: string, html: string) => {}) => {
          const arr = params['name'].split('.')
          return `source: ${arr[0]}<br>schema: ${arr[1]}<br>name: ${arr[2]}`
        },
      },
      symbol: 'image://data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4KPCEtLSBHZW5lcmF0b3I6IEFkb2JlIElsbHVzdHJhdG9yIDI1LjAuMSwgU1ZHIEV4cG9ydCBQbHVnLUluIC4gU1ZHIFZlcnNpb246IDYuMDAgQnVpbGQgMCkgIC0tPgo8c3ZnIHZlcnNpb249IjEuMSIgaWQ9IuWbvuWxgl8xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiB4PSIwcHgiIHk9IjBweCIKCSB2aWV3Qm94PSIwIDAgMjAwIDIwMCIgc3R5bGU9ImVuYWJsZS1iYWNrZ3JvdW5kOm5ldyAwIDAgMjAwIDIwMDsiIHhtbDpzcGFjZT0icHJlc2VydmUiPgo8c3R5bGUgdHlwZT0idGV4dC9jc3MiPgoJLnN0MHtmaWxsOiM1NUI5RkY7fQo8L3N0eWxlPgo8cGF0aCBjbGFzcz0ic3QwIiBkPSJNNzguNSw5OWwyLjYtMi42bDE5LjEsMTkuMWwtMi42LDIuNkw3OC41LDk5eiIvPgo8cGF0aCBjbGFzcz0ic3QwIiBkPSJNMzMuMyw4MC41YzAsMTQuNywxMiwyNi43LDI2LjcsMjYuN3MyNi43LTEyLDI2LjctMjYuN1M3NC44LDUzLjgsNjAsNTMuOFMzMy4zLDY1LjgsMzMuMyw4MC41TDMzLjMsODAuNXoiLz4KPHBhdGggY2xhc3M9InN0MCIgZD0iTTkyLjcsMTI3LjdjMCwxMC4yLDguMywxOC41LDE4LjUsMTguNWMxMC4yLDAsMTguNS04LjMsMTguNS0xOC41YzAsMCwwLDAsMCwwYzAtMTAuMi04LjMtMTguNS0xOC41LTE4LjUKCUMxMDEsMTA5LjIsOTIuNywxMTcuNSw5Mi43LDEyNy43eiIvPgo8cGF0aCBjbGFzcz0ic3QwIiBkPSJNMTE3LjYsMTIwbC0yLjYtMi42bDE5LjEtMTkuMWwyLjYsMi42TDExNy42LDEyMHoiLz4KPHBhdGggY2xhc3M9InN0MCIgZD0iTTEyOS43LDk0LjhjMCw2LjksNS42LDEyLjQsMTIuNCwxMi40czEyLjQtNS42LDEyLjQtMTIuNGMwLTYuOS01LjYtMTIuNC0xMi40LTEyLjRTMTI5LjcsODcuOSwxMjkuNyw5NC44eiIvPgo8L3N2Zz4K',
      symbolSize: 160
    });
    const chartOption: EChartsOption = {
      tooltip: {
        backgroundColor: 'rgba(50,50,50,0.7)',
        textStyle: {
          color: '#fff'
        },
        borderWidth: 0
      },
      animationDurationUpdate: 1500,
      animationEasingUpdate: 'quinticInOut',
      series: [
        {
          type: 'graph',
          symbolSize: 50,
          left: 120,
          top: 100,
          roam: false,
          label: {
            show: true,
            position: 'bottom'
          },
          edgeSymbol: 'circle',
          // edgeSymbolSize: [4, 10],
          edgeLabel: {
            fontSize: 20
          },
          data: [],
          links: [],

        }],
      initOpts: {
        height: height,
        width: 0
      }
    };
    let yStartPos = 100;
    for (const flow of flows) {
      const flowNodes: object[] = flow['node_list'];
      let xStartPos = 100;
      let lastNodeName = '';
      // 图表宽度
      width = chartOption['initOpts']['width'] = Math.max(600 + flowNodes.length * 300, width)
      for (const flowNode of flowNodes) {
        xStartPos += 300;
        const nodeName = `${flow['_id']}.${flowNode['node_name']}`;
        // 普通的流节点
        chartNodes.push({
          name: nodeName,
          fixed: true,
          x: xStartPos,
          y: yStartPos,
          symbol: 'image://data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/PjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+PHN2ZyB0PSIxNjEwMzgyNjk2NTEwIiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjY4NzAxIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgd2lkdGg9IjEyOCIgaGVpZ2h0PSIxMjgiPjxkZWZzPjxzdHlsZSB0eXBlPSJ0ZXh0L2NzcyI+PC9zdHlsZT48L2RlZnM+PHBhdGggZD0iTTk4MS4yMTYxMTcgNzM0LjQ5NjkzM2wtODQuNjcxNjUzLTQ4Ljg5ODU5OWMtMC42NDc0MTctMC4zNjk1MjEtMS4zNzUzODMtMC4zNTg0NDUtMi4wMzc5MDMtMC42Nzc2MjMtMC40NTEwNzctMC4yOTYwMi0wLjY5Njc1My0wLjc3MDI1NS0xLjE2Njk2MS0xLjA0NzE0NC0xNC43ODk5MDEtOC43ODE5MTMtMzQuMzI1MTc1LTMuODgyNDg3LTQzLjE5ODcxMyAxMS4xMjI4ODMtODguODYyMjQyIDE1MC40NTY0NTQtMjc2LjIwMTMxOSAyMDkuMzk5NTgtNDM1LjYyMjkzNSAxMzcuMTc1ODVDMzMxLjI2Mjk2NCA3OTQuNDU2OTk3IDI2Ny43Mjg1MTUgNzI2LjU3NzkwNyAyMzUuNDk4NjM0IDY0MS4wMTIxNTVjLTMyLjE2ODQ2Mi04NS41MzQ1MzktMjkuMDg2NDM1LTE3OC40OTU3NDUgOC42Mjc4NjItMjYxLjcxOTUyIDM3LjcxNDI5Ny04My4yNTQ5ODggMTA1LjYyNDU5OS0xNDYuODIwNjQ5IDE5MS4xNTkxMzgtMTc4Ljk4ODEwNCA4NS41MzQ1MzktMzIuMTk4NjY4IDE3OC40NjQ1MzItMjkuMTE3NjQ4IDI2MS43MTk1MiA4LjU5NjY0OSA2Mi40MjU4ODYgMjguMjU0NzYxIDExNC4zMTM4OCA3My45NDk1MDMgMTUwLjExNzEzOSAxMzIuMTIyMzc0IDAuMjExNDQzIDAuMzQyMzM2IDAuNTY5ODg4IDAuNTEyNDk2IDAuNzkzNDEzIDAuODQ3Nzg0IDAuNDE1ODM3IDAuOTAxMTQ4IDAuNDIzODkyIDEuODgzODUyIDAuOTMyMzYxIDIuNzU3ODE1IDUuNzkyNTE4IDEwLjEwNTk0NSAxNi40NTMyNDggMTUuNzc1NjI2IDI3LjI5OTI0MyAxNS43NzU2MjYgNS4zNjE1NzggMCAxMC43ODQ1NzUtMS4zNTUyNDYgMTUuNzc1NjI2LTQuMjIwNzk1bDg4Ljk4NjA4Ny01MS4zNjQ0MjJjMTUuMDk4MDAzLTguNzE5NDg3IDIwLjI3NDMxNy0yOC4wMDgwNzkgMTEuNTIzNjE3LTQzLjEwNjA4MS04LjY4OTI4MS0xNS4xNTk0MjItMjguMDM5MjkxLTIwLjI0NDExMS00My4wNzU4NzUtMTEuNTU0ODNsLTYzLjcwODYzNyAzNi43NzQ4ODhjLTI5LjMyMjA0My00MS40ODUwMjItNjUuMjU2MTk1LTc3LjM4MDkxMy0xMDcuNTY5ODctMTA1LjA4MDg5bDM3LjYxMzYxLTY1LjA3ODk4NmM4Ljc1MDctMTUuMDY2NzkgMy41NzQzODUtMzQuMzg2NTk0LTExLjUyMzYxNy00My4xMDYwODEtMTQuODUxMzItOC41OTY2NDktMzQuMzI1MTc1LTMuNjA0NTkxLTQzLjA3NTg3NSAxMS41NTQ4M2wtMzguMjE4NzM4IDY2LjEzMTE2NGMtNDYuNDEwNjI2LTIxLjAzNzUyNC05NS4zOTQ4MDktMzIuNjE4NTMyLTE0NC44MDU5MDQtMzUuMzQ5MTYxTDU3OC4wNjc3MzIgNDcuOTk5NDY1YzAtMTcuNDM5OTgtMTQuMTEyMjc4LTMxLjU1MTI1MS0zMS41NTEyNTEtMzEuNTUxMjUxUzUxNC45NjUyMyAzMC41NjA0OTIgNTE0Ljk2NTIzIDQ3Ljk5OTQ2NWwwIDY5LjYxNTkzOGMtMzQuNDI0ODU2IDMuNTQ3Mi02OC42NTczOTkgMTEuMTExODA4LTEwMS45MjczNzYgMjMuNjIxMTUtMTMuMzkxMzU5IDUuMDM3MzY2LTI1LjM1MDk1MSAxMi4zNzg0NDktMzcuOTc2MDgzIDE4LjY4MzQ2NGwtNDMuMTIyMTkxLTc0LjcwMDYyOGMtOC42ODkyODEtMTUuMTkwNjM1LTI4LjAzOTI5MS0yMC4yNDQxMTEtNDMuMDc1ODc1LTExLjU1NDgzLTE1LjA5ODAwMyA4LjcxOTQ4Ny0yMC4yNzQzMTcgMjguMDA4MDc5LTExLjUyMzYxNyA0My4xMDYwODFsNDMuNTIxOTE4IDc1LjM5NzM4MWMtMzkuNDEyODg1IDI4LjExNTgxNC03My42MTAxODggNjIuNDIxODU4LTEwMC42OTA5NCAxMDIuOTI4MjA0bC03Ni44NjUzOTYtNDQuMzU0NTk5Yy0xNS4yMjA4NDEtOC43NTA3LTM0LjQ0ODAxMy0zLjU0MzE3Mi00My4wNzU4NzUgMTEuNTU0ODMtOC43NTA3IDE1LjA5ODAwMy0zLjU3NDM4NSAzNC4zODY1OTQgMTEuNTIzNjE3IDQzLjEwNjA4MWw3Ni45MTQ3MzMgNDQuMzg0ODA1Yy0wLjU2MTgzMyAxLjIxMzI3Ny0xLjQyNDcyIDIuMjE0MTA1LTEuOTc5NTA1IDMuNDM1NDM3LTE5LjUwODA4OSA0Mi45OTAyOTEtMzAuNzE1NTUgODguMTk1Njk0LTM0LjUzNjYxOCAxMzMuODc5MzYxTDYzLjQ0MjgyNCA0ODcuMTAyMTRjLTE3LjQzOTk4IDAtMzEuNTUxMjUxIDE0LjExMjI3OC0zMS41NTEyNTEgMzEuNTUxMjUxIDAgMTcuNDM5OTggMTQuMTEyMjc4IDMxLjU1MTI1MSAzMS41NTEyNTEgMzEuNTUxMjUxbDg4LjEwODA5NyAwYzIuODU0NDc0IDM4LjE4NzUyNSAxMS4wNDIzMzQgNzYuMjAyODc2IDI0LjkxMTk1NiAxMTMuMDUwMjU5IDMuNTAwODg0IDkuMzAxNDU3IDguODY2NDkgMTcuNDQ0MDA4IDEyLjk4MzU3NyAyNi4zNzg5NjRsLTc3LjY4OTAxNSA0NC44NTkwNGMtMTUuMDk4MDAzIDguNzE5NDg3LTIwLjI3NDMxNyAyOC4wMDgwNzktMTEuNTIzNjE3IDQzLjEwNjA4MSA1Ljc5MjUxOCAxMC4xMzcxNTggMTYuNDUzMjQ4IDE1Ljc3NTYyNiAyNy4yOTkyNDMgMTUuNzc1NjI2IDUuMzYxNTc4IDAgMTAuNzg0NTc1LTEuMzU1MjQ2IDE1Ljc3NTYyNi00LjIyMTgwMmw3Ni4yMjIwMDctNDQuMDExMjU2YzI3LjAxMTI3OCA0MC41NzU4MTkgNjAuMjkyMzMgNzYuNDA2MjY0IDEwMC4wNTQ1OTkgMTA0Ljc5NTk0NmwtNDIuMjQzMTk0IDczLjE1NjA5Yy04Ljc1MDcgMTUuMDk4MDAzLTMuNTc0Mzg1IDM0LjM4NjU5NCAxMS41MjM2MTcgNDMuMTA2MDgxIDQuOTkyMDU3IDIuODY1NTUgMTAuNDE0MDQ3IDQuMjIwNzk1IDE1Ljc3NTYyNiA0LjIyMDc5NSAxMC44NDU5OTQgMCAyMS41MDY3MjUtNS42Njk2OCAyNy4yOTkyNDMtMTUuNzc1NjI2bDQyLjI3ODQzNS03My4yMjE1MzdjNC45NTI3ODkgMi40ODc5NzQgOS4yMTI4NTMgNS45MzU0OTQgMTQuMjkzNTE0IDguMjM4MjAzIDQxLjEwNzQ0NiAxOC42MTgwMTcgODMuNzYzNDU3IDI5LjgxODQyOSAxMjYuNDUzNzAxIDM0LjE5NzMwM2wwIDY4LjAzNzE2OGMwIDE3LjQzOTk4IDE0LjExMjI3OCAzMS41NTEyNTEgMzEuNTUxMjUxIDMxLjU1MTI1MXMzMS41NTEyNTEtMTQuMTEyMjc4IDMxLjU1MTI1MS0zMS41NTEyNTFsMC02Ny4yODYwNDNjNTAuMzc3NjktMi43NDI3MTIgOTkuNTA3ODY5LTE0Ljc0NzYxMiAxNDUuMTg3NTA4LTM1LjQ4MDA1NGwzNy44MzcxMzUgNjUuNTE0OTZjNS43OTI1MTggMTAuMTM3MTU4IDE2LjQ1MzI0OCAxNS43NzU2MjYgMjcuMjk5MjQzIDE1Ljc3NTYyNiA1LjM2MTU3OCAwIDEwLjc4NDU3NS0xLjM1NTI0NiAxNS43NzU2MjYtNC4yMjA3OTUgMTUuMDk4MDAzLTguNzE5NDg3IDIwLjI3NDMxNy0yOC4wMDgwNzkgMTEuNTIzNjE3LTQzLjEwNjA4MWwtMzcuMjYzMjItNjQuNTI4MjI5YzQxLjY4MTM2MS0yNy40NjUzNzYgNzguNTEzNjQxLTYyLjkwMzE0MiAxMDguNDI0NzAyLTEwNS42ODYwMThsNjIuODEwNTEgMzYuMjczNDY3YzQuOTkyMDU3IDIuODY1NTUgMTAuNDE1MDU0IDQuMjIxODAyIDE1Ljc3NTYyNiA0LjIyMTgwMiAxMC44NDU5OTQgMCAyMS41MDY3MjUtNS42Mzg0NjcgMjcuMjk5MjQzLTE1Ljc3NTYyNkMxMDAxLjQ5MDQzNCA3NjIuNTA1MDEyIDk5Ni4zMTQxMiA3NDMuMjE2NDIgOTgxLjIxNjExNyA3MzQuNDk2OTMzeiIgcC1pZD0iNjg3MDIiIGZpbGw9IiM4OTkyYzgiPjwvcGF0aD48cGF0aCBkPSJNMzg0Ljg1NDU4IDUyMy4xOTAzNDNjLTUxLjg4Nzk5NCAwLTk0LjEwMDk4MiA0Mi4yMTI5ODgtOTQuMTAwOTgyIDk0LjA2OTc2OSAwIDUxLjg4Nzk5NCA0Mi4yMTI5ODggOTQuMTAwOTgyIDk0LjEwMDk4MiA5NC4xMDA5ODIgNDAuMzQxMjE5IDAgNzQuNTQ2NTc3LTI1LjY0Njk3IDg3Ljg5OTY3NS02MS4zODU3OWwxOTkuMTEyMzk4LTExLjEzODk5M2MxNi43MDc5ODYgMjYuMjc5Mjg0IDQ1LjkwMjE1NyA0My44NjkyODggNzkuMjgzODk2IDQzLjg2OTI4OCA1MS44ODc5OTQgMCA5NC4xMDA5ODItNDIuMjEyOTg4IDk0LjEwMDk4Mi05NC4xMDA5ODIgMC01MS44NTY3ODEtNDIuMjEyOTg4LTk0LjA2OTc2OS05NC4xMDA5ODItOTQuMDY5NzY5LTQ3LjcxMjUwNyAwLTg2LjgxNzI5MSAzNS44MTkzNjktOTIuODY4NTc0IDgxLjg3OTYwNGwtMTg0Ljg0MzA0OCAxMC4zNDE1NTNjLTQuMjQzOTUzLTEyLjI1OTYzOS0xMC44MDc3MzMtMjMuMzQ0MjYxLTE5LjM4ODI3Mi0zMi43MjIyNGw3Ny43NTg0ODktMTMzLjYyNDYyM2M3Ljg3NjczNyAyLjE0NTYzOCAxNi4wMDMxNzggMy42NTg5NjIgMjQuNTQ1NDU2IDMuNjU4OTYyIDUxLjg4Nzk5NCAwIDk0LjEwMDk4Mi00Mi4yMTI5ODggOTQuMTAwOTgyLTk0LjA2OTc2OSAwLTUxLjg4Nzk5NC00Mi4yMTI5ODgtOTQuMTAwOTgyLTk0LjEwMDk4Mi05NC4xMDA5ODJzLTk0LjEwMDk4MiA0Mi4yMTI5ODgtOTQuMTAwOTgyIDk0LjEwMDk4MmMwIDIwLjI0NzEzMSA2LjU3ODg4MyAzOC45MTI0NzEgMTcuNTE2NTAyIDU0LjI3OTMwOGwtODEuNjIxODQ2IDE0MC4yNTY4N0MzOTMuNzcwNDA2IDUyMy45MDYyMjcgMzg5LjQwNjYzNiA1MjMuMTkwMzQzIDM4NC44NTQ1OCA1MjMuMTkwMzQzek0zODQuODU0NTggNjQ4LjI1NjU3OWMtMTcuMDcwNDU5IDAtMzAuOTk3NDczLTEzLjg5NjgwOC0zMC45OTc0NzMtMzAuOTk3NDczIDAtMTcuMDcwNDU5IDEzLjkyNzAxNC0zMC45NjYyNiAzMC45OTc0NzMtMzAuOTY2MjYgMTcuMDcwNDU5IDAgMzAuOTk3NDczIDEzLjg5NjgwOCAzMC45OTc0NzMgMzAuOTY2MjZDNDE1Ljg1MjA1MyA2MzQuMzYwNzc4IDQwMS45MjUwMzkgNjQ4LjI1NjU3OSAzODQuODU0NTggNjQ4LjI1NjU3OXpNNzUxLjE1MDU0OSA1NTcuNjM4MzU3YzE3LjA2OTQ1MiAwIDMwLjk5NzQ3MyAxMy44OTY4MDggMzAuOTk3NDczIDMwLjk2NjI2IDAgMTcuMTAwNjY1LTEzLjkyNzAxNCAzMC45OTc0NzMtMzAuOTk3NDczIDMwLjk5NzQ3My0xNy4wNzA0NTkgMC0zMC45OTc0NzMtMTMuODk2ODA4LTMwLjk5NzQ3My0zMC45OTc0NzNDNzIwLjE1MzA3NiA1NzEuNTM0MTU4IDczNC4wODAwODkgNTU3LjYzODM1NyA3NTEuMTUwNTQ5IDU1Ny42MzgzNTd6TTU1Ni4zNTU2MDYgMjk4Ljk5OTg1NmMxNy4wNzA0NTkgMCAzMC45OTc0NzMgMTMuODk2ODA4IDMwLjk5NzQ3MyAzMC45OTc0NzMgMCAxNy4wNzA0NTktMTMuOTI3MDE0IDMwLjk2NjI2LTMwLjk5NzQ3MyAzMC45NjYyNi0xNy4wNjk0NTIgMC0zMC45OTc0NzMtMTMuODk2ODA4LTMwLjk5NzQ3My0zMC45NjYyNkM1MjUuMzU4MTMzIDMxMi44OTY2NjQgNTM5LjI4NTE0NyAyOTguOTk5ODU2IDU1Ni4zNTU2MDYgMjk4Ljk5OTg1NnoiIHAtaWQ9IjY4NzAzIiBmaWxsPSIjODk5MmM4Ij48L3BhdGg+PC9zdmc+',
          symbolSize: 60,
          label: {
            formatter: param => param.name.substring(param.name.lastIndexOf('.') + 1),
            position: 'bottom',
            color: '#000'
          },
          itemStyle: {
            color: '#9b8bba'
          },
          tooltip: {
            formatter: param => {
              // return param.name.substring(param.name.lastIndexOf('.') + 1)
              let html = '';
              let resolver: string[] = Object.keys(flowNode).filter(k => ['skip_if_exception','node_name', 'resolve_order'].indexOf(k) === -1)
              const resolverOrder: string[] = flowNode['resolve_order'];
              if(!!resolverOrder && resolverOrder instanceof Array && resolverOrder.length > 0) {
                resolver = resolver.sort((a, b) => {
                  let iA, iB;
                  return ((iA = resolverOrder.indexOf(a)) == -1 ? Number.MAX_VALUE : iA)
                    - ((iB = resolverOrder.indexOf(b)) == -1 ? Number.MAX_VALUE : iB)
                })
              }

              return resolver.map(s => s.match(/export_to_\w+/) ? `<span style="color: #7fff00; ">${s}</span>` : s)
                .map(s => s.match(/filter/) ? `<span style="color: red; ">${s}</span>` : s)
                .join(' --> ')
            },

          }
        });
        // 普通流节点之间的连接线
        if (!!lastNodeName) {
          chartLinks.push({
            source: lastNodeName,
            target: nodeName,
            value: 100,
            name: `${lastNodeName.substring(lastNodeName.lastIndexOf('.') + 1)} ==> ${flowNode['node_name']}`,
            tooltip: {
              textStyle: {
                color: '#fff'
              },
              formatter: (param, data) => param.data['name']
            },
            emphasis: {
              lineStyle: {
                width: 10
              }
            },
            lineStyle: {
              width: 8
            },
            symbol: 'circle'
          });
        } else {
          // 数据源节点与流的第一个节点的连接线
          chartLinks.push({
            source: namespace,
            target: nodeName,
            name: `==> ${flow['_id']}`,
            symbolSize: [0, 0],
            lineStyle: {
              curveness: '0.08',
              width: 10
            },
            emphasis: {
              lineStyle: {
                width: 12
              }
            },
            label: {
              show: true,
              formatter: flow['_id'],
              fontSize: 12
            },
            tooltip: {
              textStyle: {
                color: '#fff'
              },
              formatter: (param, data) => param.data['name']
            }
          });
        }
        lastNodeName = nodeName;
      }
      yStartPos += 100;
    }

    chartOption['initOpts']['width'] = width

    chartOption.series[0].data = chartNodes;
    chartOption.series[0].links = chartLinks;

    return chartOption;
  };
}
