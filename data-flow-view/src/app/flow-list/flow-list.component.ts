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


  testOption = {
    title: {
      text: 'Flow '
    },
    tooltip: {},
    animationDurationUpdate: 1500,
    animationEasingUpdate: 'quinticInOut',
    series: [
      {
        type: 'graph',
        layout: 'none',
        symbolSize: 50,
        roam: true,
        label: {
          show: true
        },
        edgeSymbol: ['circle', 'arrow'],
        edgeSymbolSize: [4, 10],
        edgeLabel: {
          fontSize: 20
        },
        data: [{
          name: '节点1',
          x: 100,
          y: 200
        }, {
          symbol: 'rect',
          name: '节点2',
          x: 200,
          y: 200
        }, {
          name: '节点3',
          symbol: 'rect',
          x: 300,
          y: 200
        }, {
          name: '节点4',
          symbol: 'rect',
          x: 400,
          y: 200
        }],
        // links: [],
        links: [{
          source: 0,
          target: 1,
          symbolSize: [5, 10],
          label: {
            show: true
          },
          lineStyle: {
            width: 10,
            curveness: 0.2
          }
        }, {
          source: '节点2',
          target: '节点1',
          label: {
            show: true
          },
          lineStyle: {
            curveness: 0.2
          }
        }, {
          source: '节点1',
          target: '节点3'
        }, {
          source: '节点2',
          target: '节点3'
        }, {
          source: '节点2',
          target: '节点4'
        }, {
          source: '节点1',
          target: '节点4'
        }],
        lineStyle: {
          opacity: 0.9,
          width: 2,
          curveness: 0
        }
      }
    ]
  };

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
    const height = flows.length * 120 + 60
    let width = 0
    chartNodes.push({
      name: namespace,
      x: 0,
      y: height / 2,
      label: {
        show: true,
        position: 'bottom'
      },
      tooltip: {
        formatter: (params: Object|Array<any>, ticket: string, callback: (ticket: string, html: string) => {}) => {
          const arr = params['name'].split('.')
          return `source: ${arr[0]}<br>schema: ${arr[1]}<br>name: ${arr[2]}`
        },
      }
    });
    const chartOption: EChartsOption = {
      title: {
        text: 'Flow '
      },
      tooltip: {
        backgroundColor: 'rgba(50,50,50,0.7)',
        textStyle: {
          color: '#fff'
        }
      },
      animationDurationUpdate: 1500,
      animationEasingUpdate: 'quinticInOut',
      series: [
        {
          type: 'graph',
          symbolSize: 50,
          left: 100,
          roam: false,
          label: {
            show: true,
            position: 'bottom'
          },
          edgeSymbol: ['circle', 'arrow'],
          // edgeSymbolSize: [4, 10],
          edgeLabel: {
            fontSize: 20
          },
          data: [],
          links: []
        }],
      initOpts: {
        height: height,
        width: 0
      }
    };
    let yStartPos = 120;
    for (const flow of flows) {
      const flowNodes: object[] = flow['node_list'];
      let xStartPos = 100;
      let lastNodeName = '';
      width = chartOption['initOpts']['width'] = Math.max(600 + flowNodes.length * 300, width)
      for (const flowNode of flowNodes) {
        xStartPos += 300;
        const nodeName = `${flow['_id']}.${flowNode['node_name']}`;
        chartNodes.push({
          name: nodeName,
          fixed: true,
          x: xStartPos,
          y: yStartPos,
          label: {
            formatter: param => param.name.substring(param.name.lastIndexOf('.') + 1, param.name.length)
          },
          tooltip: {
            formatter: param => {
              return param.name.substring(param.name.lastIndexOf('.') + 1, param.name.length)
            }
          }
        });
        if (!!lastNodeName) {
          chartLinks.push({
            source: lastNodeName,
            target: nodeName,
            value: 100
          });
        } else {
          chartLinks.push({
            source: namespace,
            target: nodeName,
            symbolSize: [5, 20],
            lineStyle: {
              curveness: '0.1',
              width: 6
            }
          });
        }
        lastNodeName = nodeName;
      }
      yStartPos += 120;
    }

    chartOption['initOpts']['width'] = width

    chartOption.series[0].data = chartNodes;
    chartOption.series[0].links = chartLinks;

    return chartOption;
  };
}
