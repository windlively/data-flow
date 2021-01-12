import { Component, OnInit } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: 'app-tool-button-group',
  templateUrl: './tool-button-group.component.html',
  styleUrls: ['./tool-button-group.component.css']
})
export class ToolButtonGroupComponent implements OnInit {

  openToolButtonClass = '';
  toolButtonOpened: boolean;

  toolButtonGroup: object[] = [
    {animationStyle: '', name: 'toggle-control', icon: 'plus', color: '', ngClass: this.openToolButtonClass, description: '菜单'},
    {animationStyle: '', name: 'flow-list', icon: 'partition', color: '', ngClass: '', description: 'Flow列表', routeLink: 'flow-list'},
    {animationStyle: '', name: 'config', icon: 'setting', color: '', ngClass: '', description: '配置管理'},
    {animationStyle: '', name: 'profile', icon: 'profile', color: '', ngClass: '', description: '个人资料'}
  ];

  constructor(public dialog: MatDialog,
              public httpClient: HttpClient,
              public router: Router) { }

  ngOnInit() {
    // 记录工具按钮的状态（是否打开）
    this.toolButtonOpened = false;
    const style = document.getElementsByTagName('style')[0];
    let keyframes = '';
    // 动态生成关键帧，用于右下角工具按钮的出现/消失动画
    for (let i = 1; i < this.toolButtonGroup.length; i++) {
      keyframes += `
          @keyframes hideToolButton-${i} {
              from {
                  visibility: visible;
                  bottom: ${(20 + i * 50)}px;
                  right: 20px;
                  opacity: 1;
              }to{
                  visibility: hidden;
                  bottom: 2%;
                  right: 20px;
                  opacity: 0;
               }
          }`;
      keyframes += `
          @keyframes showToolButton-${i} {
              from {
                  visibility: visible;
                  bottom: 2%;
                  right: 20px;
                  opacity: 0;
              }to{
                  visibility: visible;
                  bottom: ${(20 + i * 50)}px;
                  right: 20px;
                  opacity: 1;
               }
          }
          `;
    }
    style.innerHTML = keyframes;
  }

  // 工具按钮点击
  toggleToolGroup = () => {
    if(this.toolButtonOpened){
      // 关闭时的动画
      this.openToolButtonClass = 'tool-button-close';
      for ( let i = 0; i < this.toolButtonGroup.length; i++) {
        if (i === 0) { continue; }
        this.toolButtonGroup[i]['animationStyle'] = `hideToolButton-${i} .6s ease-out forwards`;
      }
    }else {
      //打开时的动画
      this.openToolButtonClass = 'tool-button-open';
      for(let i = 0; i < this.toolButtonGroup.length;i++) {
        if(i == 0) continue;
        this.toolButtonGroup[i]['animationStyle'] = `showToolButton-${i} .6s ease-out forwards`;
      }
    }
    this.toolButtonOpened = !this.toolButtonOpened;
  };



  toolGroupButtonClick = (buttonName: string) => {
    switch (buttonName) {
      case 'toggle-control': this.toggleToolGroup(); break;
      default: this.router.navigateByUrl(buttonName);
    }
  }

  logout = () => {

  }

  editProfile = () => {

  };

  newEssay = () => {

  };
}
