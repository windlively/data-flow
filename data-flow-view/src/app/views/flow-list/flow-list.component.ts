import {Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Tools} from '../../tools';
import {EChartsOption} from 'echarts';
import {AppService} from '../../service/app.service';


const svgIcons = {
  dataSource: 'data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4KPCEtLSBHZW5lcmF0b3I6IEFkb2JlIElsbHVzdHJhdG9yIDI1LjAuMSwgU1ZHIEV4cG9ydCBQbHVnLUluIC4gU1ZHIFZlcnNpb246IDYuMDAgQnVpbGQgMCkgIC0tPgo8c3ZnIHZlcnNpb249IjEuMSIgaWQ9IuWbvuWxgl8xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiB4PSIwcHgiIHk9IjBweCIKCSB2aWV3Qm94PSIwIDAgMjAwIDIwMCIgc3R5bGU9ImVuYWJsZS1iYWNrZ3JvdW5kOm5ldyAwIDAgMjAwIDIwMDsiIHhtbDpzcGFjZT0icHJlc2VydmUiPgo8c3R5bGUgdHlwZT0idGV4dC9jc3MiPgoJLnN0MHtmaWxsOiM1NUI5RkY7fQo8L3N0eWxlPgo8cGF0aCBjbGFzcz0ic3QwIiBkPSJNNzguNSw5OWwyLjYtMi42bDE5LjEsMTkuMWwtMi42LDIuNkw3OC41LDk5eiIvPgo8cGF0aCBjbGFzcz0ic3QwIiBkPSJNMzMuMyw4MC41YzAsMTQuNywxMiwyNi43LDI2LjcsMjYuN3MyNi43LTEyLDI2LjctMjYuN1M3NC44LDUzLjgsNjAsNTMuOFMzMy4zLDY1LjgsMzMuMyw4MC41TDMzLjMsODAuNXoiLz4KPHBhdGggY2xhc3M9InN0MCIgZD0iTTkyLjcsMTI3LjdjMCwxMC4yLDguMywxOC41LDE4LjUsMTguNWMxMC4yLDAsMTguNS04LjMsMTguNS0xOC41YzAsMCwwLDAsMCwwYzAtMTAuMi04LjMtMTguNS0xOC41LTE4LjUKCUMxMDEsMTA5LjIsOTIuNywxMTcuNSw5Mi43LDEyNy43eiIvPgo8cGF0aCBjbGFzcz0ic3QwIiBkPSJNMTE3LjYsMTIwbC0yLjYtMi42bDE5LjEtMTkuMWwyLjYsMi42TDExNy42LDEyMHoiLz4KPHBhdGggY2xhc3M9InN0MCIgZD0iTTEyOS43LDk0LjhjMCw2LjksNS42LDEyLjQsMTIuNCwxMi40czEyLjQtNS42LDEyLjQtMTIuNGMwLTYuOS01LjYtMTIuNC0xMi40LTEyLjRTMTI5LjcsODcuOSwxMjkuNyw5NC44eiIvPgo8L3N2Zz4K',
  in: 'data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/PjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+PHN2ZyB0PSIxNjEwNDIyNTIzNDE0IiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjYyNDk1IiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgd2lkdGg9IjIwMCIgaGVpZ2h0PSIyMDAiPjxkZWZzPjxzdHlsZSB0eXBlPSJ0ZXh0L2NzcyI+PC9zdHlsZT48L2RlZnM+PHBhdGggZD0iTTUxMiAwQzI5Mi41NzE0MjkgMCAxMDkuNzE0Mjg2IDEzOC45NzE0MjkgMzYuNTcxNDI5IDMyOS4xNDI4NTdoODAuNDU3MTQyYzIxLjk0Mjg1Ny00My44ODU3MTQgNTEuMi04Ny43NzE0MjkgODcuNzcxNDI5LTEyNC4zNDI4NTdDMjg1LjI1NzE0MyAxMTcuMDI4NTcxIDM5NC45NzE0MjkgNzMuMTQyODU3IDUxMiA3My4xNDI4NTdzMjI2Ljc0Mjg1NyA0My44ODU3MTQgMzA3LjIgMTMxLjY1NzE0M0M4OTkuNjU3MTQzIDI4NS4yNTcxNDMgOTUwLjg1NzE0MyAzOTQuOTcxNDI5IDk1MC44NTcxNDMgNTEycy00My44ODU3MTQgMjI2Ljc0Mjg1Ny0xMzEuNjU3MTQzIDMwNy4yQzczOC43NDI4NTcgODk5LjY1NzE0MyA2MjkuMDI4NTcxIDk1MC44NTcxNDMgNTEyIDk1MC44NTcxNDNzLTIyNi43NDI4NTctNDMuODg1NzE0LTMwNy4yLTEzMS42NTcxNDNjLTM2LjU3MTQyOS0zNi41NzE0MjktNjUuODI4NTcxLTgwLjQ1NzE0My04Ny43NzE0MjktMTI0LjM0Mjg1N0gzNi41NzE0MjlDMTA5LjcxNDI4NiA4ODUuMDI4NTcxIDI5Mi41NzE0MjkgMTAyNCA1MTIgMTAyNGMyODUuMjU3MTQzIDAgNTEyLTIyNi43NDI4NTcgNTEyLTUxMnMtMjM0LjA1NzE0My01MTItNTEyLTUxMnpNNDAyLjI4NTcxNCA2NjUuNmw1MS4yIDUxLjJMNjU4LjI4NTcxNCA1MTIgNDUzLjQ4NTcxNCAzMDcuMmwtNTEuMiA1MS4yIDExNy4wMjg1NzIgMTE3LjAyODU3MUgwdjczLjE0Mjg1OGg1MTkuMzE0Mjg2TDQwMi4yODU3MTQgNjY1LjZ6IiBmaWxsPSIjOWI4YmJhIiBwLWlkPSI2MjQ5NiI+PC9wYXRoPjwvc3ZnPg==',
  out: 'data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/PjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+PHN2ZyB0PSIxNjEwNDIyNDYzMDgzIiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjYxMjE5IiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgd2lkdGg9IjIwMCIgaGVpZ2h0PSIyMDAiPjxkZWZzPjxzdHlsZSB0eXBlPSJ0ZXh0L2NzcyI+PC9zdHlsZT48L2RlZnM+PHBhdGggZD0iTTUxMiAxMDI0YzIxNy42IDAgNDA1LjMzMzMzMy0xMzYuNTMzMzMzIDQ3Ny44NjY2NjctMzI4LjUzMzMzM2gtNzguOTMzMzM0Yy0yMS4zMzMzMzMgNDYuOTMzMzMzLTUxLjIgODkuNi04OS42IDEyOC04My4yIDgzLjItMTkyIDEyOC0zMDkuMzMzMzMzIDEyOHMtMjI4LjI2NjY2Ny00NC44LTMwOS4zMzMzMzMtMTI4Yy04My4yLTgzLjItMTI4LTE5Mi0xMjgtMzA5LjMzMzMzNHM0NC44LTIyOC4yNjY2NjcgMTI4LTMwOS4zMzMzMzNjODMuMi04My4yIDE5Mi0xMjggMzA5LjMzMzMzMy0xMjhzMjI2LjEzMzMzMyA0NC44IDMwOS4zMzMzMzMgMTI4YzM4LjQgMzguNCA2OC4yNjY2NjcgODEuMDY2NjY3IDg5LjYgMTI4aDc4LjkzMzMzNEM5MTcuMzMzMzMzIDEzNi41MzMzMzMgNzI5LjYgMCA1MTIgMCAyMzAuNCAwIDAgMjMwLjQgMCA1MTJzMjMwLjQgNTEyIDUxMiA1MTJ6IG0yNTMuODY2NjY3LTM1Ni4yNjY2NjdsNTEuMiA1MS4yTDEwMjQgNTEybC0yMDYuOTMzMzMzLTIwNi45MzMzMzMtNTEuMiA1MS4yIDExOS40NjY2NjYgMTE5LjQ2NjY2NkgzNjQuOHY3Mi41MzMzMzRoNTE4LjRsLTExNy4zMzMzMzMgMTE5LjQ2NjY2NnoiIGZpbGw9IiM5YjhiYmEiIHAtaWQ9IjYxMjIwIj48L3BhdGg+PC9zdmc+',
  dataProcess: 'data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/PjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+PHN2ZyB0PSIxNjEwNDI4NTI2OTAxIiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9Ijk3MDAzIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgd2lkdGg9IjIwMCIgaGVpZ2h0PSIyMDAiPjxkZWZzPjxzdHlsZSB0eXBlPSJ0ZXh0L2NzcyI+PC9zdHlsZT48L2RlZnM+PHBhdGggZD0iTTgxMC4xODkgODY2LjE0M0w2NjAuOTQ4IDcxNi45MDJoMTExLjkzMVYzNDMuNzc5YzAtNjEuODMyLTUwLjA5OS0xMTEuOTMyLTExMS45MzEtMTExLjkzMnMtMTExLjkzMSA1MC4xLTExMS45MzEgMTExLjkzMlY2NzkuNTljMCAxMDMuMDQtODMuNTExIDE4Ni41NTItMTg2LjU1MyAxODYuNTUyLTEwMy4wNCAwLTE4Ni41NTItODMuNTEtMTg2LjU1Mi0xODYuNTUyVjMwNi40NjlINjMuOTgxbDE0OS4yNDItMTQ5LjI0MiAxNDkuMjQgMTQ5LjI0MmgtMTExLjkzVjY3OS41OWMwIDYxLjc5NSA1MC4xIDExMS45MzEgMTExLjkzIDExMS45MzEgNjEuODMzIDAgMTExLjkzMy01MC4xMzYgMTExLjkzMy0xMTEuOTMxVjM0My43NzljMC0xMDMuMDQgODMuNTEtMTg2LjU1MiAxODYuNTUyLTE4Ni41NTJTODQ3LjUgMjQwLjczNyA4NDcuNSAzNDMuNzc5djM3My4xMjNoMTExLjkzMmwtMTQ5LjI0MyAxNDkuMjR6IiBwLWlkPSI5NzAwNCIgZmlsbD0iIzEyOTZkYiI+PC9wYXRoPjwvc3ZnPg==',
  filter: 'data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/PjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+PHN2ZyB0PSIxNjEwNDI5MjM0Mjg4IiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjUgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9Ijk4MzcyIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgd2lkdGg9IjIwMC4xOTUzMTI1IiBoZWlnaHQ9IjIwMCI+PGRlZnM+PHN0eWxlIHR5cGU9InRleHQvY3NzIj48L3N0eWxlPjwvZGVmcz48cGF0aCBkPSJNNjY0Ljk1MzA3NCA4MDEuNDE3NDZsLTE2MS41NzE4IDkxLjMwMjc4NlYxMzMuMTU3ODYxbDE2MS41NzE4IDkzLjYwNjY4OHY3MS4yNTAzMDVoNzUuOTQzNDM5VjE4MS42MjUxMzRMNDI3LjQzNzgzNSAwLjUxMjgzMlYxMDI0bDMxMy44NDI2NjEtMTc2LjkzMTE0N3YtMTI2LjY3MTk1aC03Ni4zNzAwODd2ODEuMDYzMjIyek0yMjEuMjM4NTk4IDE0Ni4yNTU5NzFhNzMuMTI3NTU5IDczLjEyNzU1OSAwIDEgMC03Mi42NTgyNDUtNzIuNjU4MjQ2YzAgNDAuMTA0OTYyIDMyLjUxMDYxOCA3Mi42NTgyNDUgNzIuNjE1NTggNzIuNjU4MjQ2eiBtLTc1Ljk0MzQzOSAxNTAuMzkzNjA4YTcyLjY1ODI0NSA3Mi42NTgyNDUgMCAxIDAtMTQ1LjI3MzgyNiAwIDcyLjY1ODI0NSA3Mi42NTgyNDUgMCAwIDAgMTQ1LjI3MzgyNiAweiBtNzUuNDMxNDYxIDE0NC4zNzc4NjRhNzMuMTI3NTU5IDczLjEyNzU1OSAwIDEgMCA3Mi42NTgyNDUtNzMuMTI3NTU5IDcyLjYxNTU4MSA3Mi42MTU1ODEgMCAwIDAtNzIuNjU4MjQ1IDczLjEyNzU1OXogbTM3LjcxNTczIDE0NS4yNzM4MjZhMTA5LjQzNTM0OSAxMDkuNDM1MzQ5IDAgMSAwIDAgMjE4Ljg3MDY5OCAxMDkuNDM1MzQ5IDEwOS40MzUzNDkgMCAwIDAgMC0yMTguODcwNjk4ek03NS40NTI3OTQgOTUwLjg3MjQ0MWE3Mi42NTgyNDUgNzIuNjU4MjQ1IDAgMSAwIDE0NS4yNzM4MjYgMCA3Mi42NTgyNDUgNzIuNjU4MjQ1IDAgMCAwLTE0NS4yNzM4MjYgMHoiIHAtaWQ9Ijk4MzczIiBmaWxsPSIjMTI5NmRiIj48L3BhdGg+PHBhdGggZD0iTTczMC4xNDQ5NyAzMzguNTg5MTNhNzIuNjU4MjQ1IDcyLjY1ODI0NSAwIDEgMCAwIDE0NS4yNzM4MjYgNzIuNjU4MjQ1IDcyLjY1ODI0NSAwIDAgMCAwLTE0NS4yNzM4MjZ6IG0yMjAuNzA1Mjg2IDE0NS4yNzM4MjZhNzIuNjU4MjQ1IDcyLjY1ODI0NSAwIDEgMCAwLTE0NS4yNzM4MjYgNzIuNjU4MjQ1IDcyLjY1ODI0NSAwIDAgMCAwIDE0NS4yNzM4MjZ6TTY1Ny40ODY3MjQgNjE3Ljk1ODU5YTcyLjY1ODI0NSA3Mi42NTgyNDUgMCAxIDAgMTQ1LjMxNjQ5MSAwIDcyLjY1ODI0NSA3Mi42NTgyNDUgMCAwIDAtMTQ1LjI3MzgyNiAwek04NzguMjM0Njc2IDYxNy45NTg1OWE3Mi42NTgyNDUgNzIuNjU4MjQ1IDAgMSAwIDE0NS4yNzM4MjYgMCA3Mi42NTgyNDUgNzIuNjU4MjQ1IDAgMCAwLTE0NS4yNzM4MjYgMHoiIHAtaWQ9Ijk4Mzc0IiBmaWxsPSIjMTI5NmRiIj48L3BhdGg+PC9zdmc+',
  eval_context: 'data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/PjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+PHN2ZyB0PSIxNjEwNDM2OTkwMDE3IiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjEwMjk1MyIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIj48ZGVmcz48c3R5bGUgdHlwZT0idGV4dC9jc3MiPjwvc3R5bGU+PC9kZWZzPjxwYXRoIGQ9Ik02MTkuMDcyIDk2djIzMi41MTJoMTE2LjIyNHYtNTguMTEyaDE3NC40VjE1NC4yNGgtMTc0LjRWOTZINjE5LjA3MnpNOTYgMjcwLjRoNDA2Ljg0OFYxNTQuMjRIOTZ2MTE2LjIyNHogbTE3NC4zMzYgMTE2LjIyNHY1OC4xMTJIOTZ2MTE2LjIyNGgxNzQuMzM2djU4LjExMmgxMTYuMjg4VjM4Ni42MjRIMjcwLjMzNnogbTIzMi41MTIgMTc0LjMzNmg0MDYuODQ4VjQ0NC43MzZINTAyLjg0OHYxMTYuMjI0eiBtMTE2LjIyNCAxMTYuMjg4djIzMi40NDhoMTE2LjIyNHYtNTguMTEyaDE3NC40VjczNS4zNmgtMTc0LjR2LTU4LjExMkg2MTkuMDcyek05NiA4NTEuNTg0aDQwNi44NDhWNzM1LjM2SDk2djExNi4yMjR6IiBmaWxsPSIjODI1MjlkIiBwLWlkPSIxMDI5NTQiPjwvcGF0aD48L3N2Zz4=',
  conditional_expression: 'data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/PjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+PHN2ZyB0PSIxNjEwNDM3NTY0OTg2IiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjEyMTAyNCIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIj48ZGVmcz48c3R5bGUgdHlwZT0idGV4dC9jc3MiPjwvc3R5bGU+PC9kZWZzPjxwYXRoIGQ9Ik01NzUuMTY4IDc2Ny4xMDR2LTYzLjkzNmgzODMuMzZ2MjU1LjYxNmgtMzgzLjM2di0xMjcuODA4SDQ0Ny4zNlYxOTJoMTI3LjgwOFY2NC4yNTZoMzgzLjM2djI1NS41NTJoLTM4My4zNlYyNTZoLTYzLjg3MnY1MTEuMTA0aDYzLjg3MnpNNDQ3LjM2IDUxMS4zNmwtMTkxLjY4IDE5MS42OEw2NCA1MTEuNDI0bDE5MS42OC0xOTEuNjhMNDQ3LjM2IDUxMS40MjR6IiBwLWlkPSIxMjEwMjUiIGZpbGw9IiMxYWFiYTgiPjwvcGF0aD48L3N2Zz4=',
  additional_expression: 'data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/PjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+PHN2ZyB0PSIxNjEwNDM3ODI1Mzk5IiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjEyMTk2OSIgd2lkdGg9IjIwMCIgaGVpZ2h0PSIyMDAiIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIj48ZGVmcz48c3R5bGUgdHlwZT0idGV4dC9jc3MiPjwvc3R5bGU+PC9kZWZzPjxwYXRoIGQ9Ik03MTYuOCA4MGMwIDE0LjQtNC44IDI4LjgtMTQuNCA0MC05LjYgMTEuMi0yMi40IDE3LjYtMzYuOCAxNy42LTE0LjQgMC0yNS42LTQuOC0zMi0xNC40LTYuNC05LjYtMTIuOC0yNS42LTIwLjgtNDgtMS42LTYuNC00LjgtMTQuNC05LjYtMTkuMi0zLjItNi40LTgtOC0xNi04cy0xNiA0LjgtMjAuOCAxMS4yYy00LjggOS42LTkuNiAxOS4yLTExLjIgMzAuNEw0OTQuNCAzMDRoODQuOGwtMTQuNCA0OS42SDQ4MGwtODggMzEwLjRjLTI0IDgxLjYtNDQuOCAxNDQtNjQgMTkwLjQtMTcuNiA0NC44LTQ0LjggODQuOC04MCAxMjBDMjEyLjggMTAwOCAxNjkuNiAxMDI0IDExOC40IDEwMjRjLTM2LjggMC02Ny4yLTgtODYuNC0yNy4yLTIyLjQtMTcuNi0zMi0zNi44LTMyLTU5LjIgMC0xNiA0LjgtMzIgMTYtNDMuMiAxMS4yLTEyLjggMjUuNi0xOS4yIDQxLjYtMTkuMiAyMC44IDAgMzMuNiA2LjQgNDAgMTcuNiA2LjQgMTEuMiAxMS4yIDI3LjIgMTIuOCA0Ni40IDMuMiAyNCAxMi44IDM2LjggMzAuNCAzNi44IDE3LjYgMCAzMi05LjYgNDMuMi0zMC40IDExLjItMjAuOCAyMi40LTUyLjggMzMuNi05NC40bDE0Mi40LTQ5NmgtODkuNmwxNC40LTQ5LjZoODkuNmw5LjYtMzUuMmMxMi44LTQ5LjYgMjcuMi05Mi44IDQzLjItMTI5LjYgMTYtMzYuOCA0MC02OC44IDcwLjQtOTcuNkM1MjggMTQuNCA1NjggMCA2MTQuNCAwYzE3LjYgMCAzMy42IDMuMiA0OS42IDkuNiAxNiA2LjQgMjguOCAxNiAzOC40IDI3LjIgMTEuMiAxMS4yIDE0LjQgMjUuNiAxNC40IDQzLjJ6TTEwMjQgNTMyLjhjMCAyMi40LTMuMiA0Ni40LTkuNiA2Ny4yaC00OGM2LjQtMjIuNCA5LjYtNDQuOCA5LjYtNjguOCAwLTQuOC0xLjYtMTEuMi00LjgtMTQuNC0zLjItNC44LTgtOC0xNC40LTgtNC44IDAtOS42IDEuNi0xNC40IDQuOC0zLjIgMy4yLTEyLjggMTIuOC0yOC44IDMwLjRsLTk0LjQgMTA3LjIgNDMuMiAxNDRjNC44IDE2IDkuNiAyOC44IDE0LjQgMzguNCA0LjggOS42IDExLjIgMTYgMTcuNiAxNiAxMS4yIDAgMjIuNC05LjYgMzItMjguOCA5LjYtMTkuMiAxNi0zNi44IDIwLjgtNTIuOGg0Ni40Yy05LjYgNDAtMjQgNzItNDAgOTcuNi0xNiAyNS42LTMzLjYgNDMuMi01Mi44IDU0LjQtMTkuMiAxMS4yLTM2LjggMTcuNi01Mi44IDE3LjYtMjcuMiAwLTQ2LjQtOS42LTYwLjgtMzAuNC0xNC40LTE5LjItMjcuMi00OC0zNi44LTg0LjhsLTE5LjItNjguOC0xMTIgMTI2LjRjLTMyIDM2LjgtNTkuMiA1NC40LTgxLjYgNTQuNC0zMC40IDAtNDQuOC0yMC44LTQ0LjgtNjQgMC0zMC40IDYuNC02MC44IDE3LjYtOTEuMmg1NC40Yy0xMi44IDM4LjQtMTkuMiA2Mi40LTE5LjIgNzIgMCAxMS4yIDMuMiAxNy42IDkuNiAxNy42IDQuOCAwIDE0LjQtNi40IDI4LjgtMjAuOGwxMjkuNi0xNDcuMi0yNS42LTg0LjhjLTMuMi0xMi44LTgtMjUuNi0xMi44LTM4LjQtMy4yLTgtNi40LTE0LjQtMTEuMi0xOS4yLTQuOC00LjgtMTEuMi04LTE3LjYtOC0xOS4yIDAtMzUuMiAyNy4yLTQ4IDgwaC00Ni40YzExLjItMzguNCAyMi40LTcwLjQgMzYuOC05NC40IDE0LjQtMjUuNiAzMC40LTQ0LjggNDgtNTcuNiAxNy42LTEyLjggMzYuOC0yMC44IDU2LTIwLjggMjcuMiAwIDQ4IDkuNiA2NCAzMC40IDE2IDIwLjggMjguOCA0OCA0MCA4My4ybDggMjcuMiA3OC40LTkxLjJjMzAuNC0zMy42IDU3LjYtNTEuMiA4MS42LTUxLjIgMjQgMCA0MCA4IDQ2LjQgMjUuNiA5LjYgMTYgMTIuOCAzMy42IDEyLjggNTEuMnoiIHAtaWQ9IjEyMTk3MCIgZmlsbD0iIzg3YzM4ZiI+PC9wYXRoPjwvc3ZnPg==',
  simple_copy_fields: 'data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/PjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+PHN2ZyB0PSIxNjEwNDM4MTc3ODM0IiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjEzMTYwNCIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIj48ZGVmcz48c3R5bGUgdHlwZT0idGV4dC9jc3MiPjwvc3R5bGU+PC9kZWZzPjxwYXRoIGQ9Ik05MTIgMTcuMjhIMzQwLjQ4YTk2IDk2IDAgMCAwLTk2IDk2djgzLjJoNjR2LTgzLjJhMzIgMzIgMCAwIDEgMzItMzJIOTEyYTMyIDMyIDAgMCAxIDMyIDMydjY1MC44OGEzMS4zNiAzMS4zNiAwIDAgMS0zMiAzMS4zNkg3NDcuNTJ2NjRIOTEyYTk2IDk2IDAgMCAwIDk2LTk1LjM2VjExMy4yOGE5NiA5NiAwIDAgMC05Ni05NnoiIHAtaWQ9IjEzMTYwNSIgZmlsbD0iIzU5NGQ5YyI+PC9wYXRoPjxwYXRoIGQ9Ik02ODMuNTIgMTAwNi43MkgxMTJhOTYgOTYgMCAwIDEtOTYtOTZWMjU5Ljg0YTk2IDk2IDAgMCAxIDk2LTk1LjM2aDU3MS41MmE5NiA5NiAwIDAgMSA5NiA5NS4zNnY2NTAuODhhOTYgOTYgMCAwIDEtOTYgOTZ6TTExMiAyMjguNDhhMzEuMzYgMzEuMzYgMCAwIDAtMzIgMzEuMzZ2NjUwLjg4YTMyIDMyIDAgMCAwIDMyIDMyaDU3MS41MmEzMiAzMiAwIDAgMCAzMi0zMlYyNTkuODRhMzIgMzIgMCAwIDAtMzItMzEuMzZ6IiBwLWlkPSIxMzE2MDYiIGZpbGw9IiM1OTRkOWMiPjwvcGF0aD48cGF0aCBkPSJNNjAzLjUyIDQyMy42OEgxOTJhMzIgMzIgMCAwIDEtMzItMzIgMzIgMzIgMCAwIDEgMzItMzJoNDExLjUyYTMyIDMyIDAgMCAxIDMyIDMyIDMyIDMyIDAgMCAxLTMyIDMyeiBtMCAxOTMuOTJIMTkyYTMyIDMyIDAgMCAxIDAtNjRoNDExLjUyYTMyIDMyIDAgMCAxIDAgNjR6IG0wIDE5My4yOEgxOTJhMzIgMzIgMCAwIDEtMzItMzIgMzIgMzIgMCAwIDEgMzItMzJoNDExLjUyYTMyIDMyIDAgMCAxIDMyIDMyIDMyIDMyIDAgMCAxLTMyIDMyeiIgcC1pZD0iMTMxNjA3IiBmaWxsPSIjNTk0ZDljIj48L3BhdGg+PC9zdmc+',
  simple_convert: 'data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/PjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+PHN2ZyB0PSIxNjEwNDM4MzUzNzI2IiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjEzNTI4MiIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIj48ZGVmcz48c3R5bGUgdHlwZT0idGV4dC9jc3MiPjwvc3R5bGU+PC9kZWZzPjxwYXRoIGQ9Ik03MDguMjY3IDM5OC45MzNjMTUxLjQ2NiAwIDIzNi44IDExOS40NjcgMjM2LjggMjM2LjhzLTg3LjQ2NyAyMzYuOC0yMzYuOCAyMzYuOGMwIDAtOTguMTM0IDIuMTM0LTE1Ny44NjctNjEuODY2bDM0LjEzMy00NC44UzYxOC42NjcgODE5LjIgNzA4LjI2NyA4MTkuMnMxODMuNDY2LTc2LjggMTgzLjQ2Ni0xODMuNDY3YzAtMTAwLjI2Ni03Ni44LTE4My40NjYtMTgzLjQ2Ni0xODMuNDY2LTM0LjEzNCAwLTU5LjczNCA2LjQtNzguOTM0IDEyLjhsMjcuNzM0IDU1LjQ2Ni0xMzQuNC0yNy43MzNMNTgyLjQgMzczLjMzM2wyMS4zMzMgNDQuOGMyNS42LTEwLjY2NiA1Ny42LTE5LjIgMTA0LjUzNC0xOS4yeiBtLTc4LjkzNCAzMjguNTM0TDU4MC4yNjcgNzA0Yy0yNS42IDc0LjY2Ny0xMTAuOTM0IDE2OC41MzMtMjMyLjUzNCAxNjguNTMzLTEyOCAwLTIzNi44LTg5LjYtMjM2LjgtMjQ1LjMzM3MxMzYuNTM0LTIxMy4zMzMgMTM2LjUzNC0yMTMuMzMzbDQuMjY2IDI5Ljg2NiA4LjUzNCAyNy43MzRjLTQ2LjkzNCAxOS4yLTk2IDcyLjUzMy05NiAxNTUuNzMzczU5LjczMyAxOTIgMTgzLjQ2NiAxOTJjMTA4LjggMCAxNjYuNC0xMTMuMDY3IDE4MS4zMzQtMTQyLjkzM2wtNDQuOC0yMS4zMzQgMTE5LjQ2Ni02MS44NjYgMjUuNiAxMzQuNHogbS0zMjQuMjY2LTEzNC40bDM2LjI2Ni0zNC4xMzRjLTI5Ljg2Ni0zMi02NC04OS42LTY0LTE3Ny4wNjYgMC0xMDQuNTM0IDgzLjItMjI4LjI2NyAyMzYuOC0yMjguMjY3IDE4NS42IDAgMjM2LjggMTYyLjEzMyAyMzYuOCAyMDAuNTMzIDAgMTAuNjY3IDIuMTM0IDE5LjIgMi4xMzQgMTkuMi0zOC40LTguNTMzLTUzLjMzNC00LjI2Ni01My4zMzQtNC4yNjYgNi40LTE5LjIgMC0xNC45MzQgMC0xNC45MzQgMC01OS43MzMtODkuNi0xNDkuMzMzLTE4My40NjYtMTQ5LjMzMy05NiAwLTE4My40NjcgNzIuNTMzLTE4My40NjcgMTc0LjkzMyAwIDc0LjY2NyAyOS44NjcgMTE5LjQ2NyA0OS4wNjcgMTQwLjhsMzguNC0zNC4xMzMgMTcuMDY2IDEyMy43MzMtMTMyLjI2Ni0xNy4wNjZ6IiBwLWlkPSIxMzUyODMiIGZpbGw9IiM4ODE0N2YiPjwvcGF0aD48L3N2Zz4=',
  export_to_rdb: 'data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/PjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+PHN2ZyB0PSIxNjEwNDM4NTc5NzQ1IiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjE0MTIxMCIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIj48ZGVmcz48c3R5bGUgdHlwZT0idGV4dC9jc3MiPjwvc3R5bGU+PC9kZWZzPjxwYXRoIGQ9Ik02NjIuNTg4IDY2Mi41ODh2MTIwLjQ3aDMwMS4xNzd2NjAuMjM2SDY2Mi41ODh2MTIwLjQ3TDUxMiA4MTMuMTc3bDE1MC41ODgtMTUwLjU4OHogbTkwLjM1My02MDIuMzUzYTkwLjM1MyA5MC4zNTMgMCAwIDEgODkuOTMxIDgxLjY4bDAuNDIyIDguNjczdjUxMkg3ODMuMDZ2LTYwLjIzNUgzNjEuNDEydi02MC4yMzVoNDIxLjY0N1YzNjEuNDEySDM2MS40MTJ2LTYwLjIzNmg0MjEuNjQ3VjE1MC41ODhhMzAuMTE4IDMwLjExOCAwIDAgMC0yNC42OTctMjkuNjM2bC01LjQyLTAuNDgxSDIxMC44MjNhMzAuMTE4IDMwLjExOCAwIDAgMC0yOS42MzYgMjQuNjk2bC0wLjQ4MiA1LjQyMXYxNTAuNTg4aDEyMC40N3Y2MC4yMzZoLTEyMC40N3YxODAuNzA2aDEyMC40N3Y2MC4yMzVoLTEyMC40N1Y3NTIuOTRhMzAuMTE4IDMwLjExOCAwIDAgMCAyNC42OTYgMjkuNjM2bDUuNDIyIDAuNDgyaDIxMC44MjN2NjAuMjM1SDIxMC44MjRhOTAuMzUzIDkwLjM1MyAwIDAgMS04OS45MzItODEuNjc5bC0wLjQyMS04LjY3NFYxNTAuNTg4YTkwLjM1MyA5MC4zNTMgMCAwIDEgODEuNjc5LTg5LjkzMWw4LjY3NC0wLjQyMkg3NTIuOTR6IiBwLWlkPSIxNDEyMTEiIGZpbGw9IiMwZTkzMmUiPjwvcGF0aD48L3N2Zz4=',
  export_to_mq: 'data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/PjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+PHN2ZyB0PSIxNjEwNDM4NzQ4MzM1IiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjE0NDUyNyIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIj48ZGVmcz48c3R5bGUgdHlwZT0idGV4dC9jc3MiPjwvc3R5bGU+PC9kZWZzPjxwYXRoIGQ9Ik05MjcuNjQ3MDQ3IDgzMi4wOTM3MDRjLTE0LjA5MzExOSAwLTI3LjM4NjYyOCAyLjk5ODUzNi0zOS40ODA3MjIgOC40OTU4NTJMNzU5LjYyOTA4NyA3MTIuMDUyMzE4YzQ1Ljg3NzU5OS01Ni41NzIzNzcgNzAuODY1Mzk4LTEyNi41MzgyMTQgNzAuODY1Mzk4LTIwMC40MDIxNDcgMC03NC45NjMzOTctMjUuNjg3NDU3LTE0NS45Mjg3NDYtNzIuOTY0MzczLTIwMi45MDA5MjdsNzIuMTY0NzY0LTcyLjE2NDc2NGMxOS40OTA0ODMgMTIuMTk0MDQ2IDQyLjQ3OTI1OCAxOS4xOTA2MyA2Ny4xNjcyMDMgMTkuMTkwNjMgNzAuMTY1NzM5IDAgMTI2LjkzODAxOS01Ni44NzIyMyAxMjYuOTM4MDE5LTEyNi45MzgwMTlzLTU2Ljg3MjIzLTEyNi45MzgwMTktMTI2LjkzODAxOS0xMjYuOTM4MDE4LTEyNi45MzgwMTkgNTYuODcyMjMtMTI2LjkzODAxOCAxMjYuOTM4MDE4YzAgMjIuMTg5MTY1IDUuNjk3MjE4IDQzLjE3ODkxNyAxNS43OTIyODggNjEuMjcwMDgzTDcxMi4wNTIzMTggMjYzLjg3MTE1N2MtNTYuNDcyNDI2LTQ1Ljc3NzY0OC0xMjYuMzM4MzExLTcwLjY2NTQ5NS0yMDAuMDAyMzQyLTcwLjY2NTQ5Ni03NC4zNjM2OSAwLTE0NC44MjkyODMgMjUuMjg3NjUzLTIwMS41MDE2MTEgNzEuODY0OTFsLTYzLjk2ODc2NS02My45Njg3NjVjNS44OTcxMjEtMTIuMzkzOTQ4IDkuMDk1NTU5LTI2LjE4NzIxMyA5LjA5NTU1OS00MC43ODAwODggMC01Mi42NzQyOC00Mi43NzkxMTItOTUuNDUzMzkyLTk1LjQ1MzM5Mi05NS40NTMzOTJzLTk1LjQ1MzM5MiA0Mi43NzkxMTItOTUuNDUzMzkyIDk1LjQ1MzM5MiA0Mi43NzkxMTIgOTUuNDUzMzkyIDk1LjQ1MzM5MiA5NS40NTMzOTJjMTQuNzkyNzc3IDAgMjguNjg1OTkzLTMuMzk4MzQxIDQxLjE3OTg5Mi05LjI5NTQ2MWw2My44Njg4MTQgNjMuODY4ODE0Yy00Ni40NzczMDYgNTYuNjcyMzI4LTcxLjc2NDk1OSAxMjcuMTM3OTIxLTcxLjc2NDk1OCAyMDEuNDAxNjU5IDAgNzQuMTYzNzg3IDI1LjE4NzcwMSAxNDQuNDI5NDc4IDcxLjQ2NTEwNSAyMDEuMTAxODA2bC02Ny40NjcwNTcgNjcuNDY3MDU3Yy0yMC4wOTAxOS0xMy4wOTM2MDctNDMuOTc4NTI2LTIwLjY4OTg5OC02OS43NjU5MzUtMjAuNjg5ODk4QzU3LjE3MjA4NCA3NTkuNjI5MDg3IDAgODE2LjgwMTE3MSAwIDg4Ny4zNjY3MTVzNTcuMTcyMDg0IDEyNy43Mzc2MjggMTI3LjczNzYyOCAxMjcuNzM3NjI5IDEyNy43Mzc2MjgtNTcuMTcyMDg0IDEyNy43Mzc2MjgtMTI3LjczNzYyOWMwLTIxLjU4OTQ1OC01LjM5NzM2NS00MS45Nzk1MDItMTQuNzkyNzc3LTU5Ljc3MDgxNWw2OS4zNjYxMy02OS4zNjYxMjljNTYuNzcyMjc5IDQ2LjY3NzIwOCAxMjcuMzM3ODIzIDcyLjA2NDgxMiAyMDEuODAxNDY0IDcyLjA2NDgxMiA3NC43NjM0OTQgMCAxNDUuNjI4ODkyLTI1LjU4NzUwNiAyMDIuNjAxMDc0LTcyLjY2NDUybDEyNy40Mzc3NzQgMTI3LjQzNzc3NWMtNi40OTY4MjggMTIuOTkzNjU1LTEwLjE5NTAyMiAyNy41ODY1My0xMC4xOTUwMjIgNDMuMDc4OTY1IDAgNTIuOTc0MTM0IDQyLjg3OTA2MyA5NS44NTMxOTcgOTUuODUzMTk3IDk1Ljg1MzE5N3M5NS44NTMxOTctNDIuODc5MDYzIDk1Ljg1MzE5Ny05NS44NTMxOTdjMC4wOTk5NTEtNTMuMTc0MDM2LTQyLjg3OTA2My05Ni4wNTMwOTktOTUuNzUzMjQ2LTk2LjA1MzA5OXpNMzMyLjEzNzgyMyA2OTEuNjYyMjc0Yy00OC4wNzY1MjUtNDguMDc2NTI1LTc0LjU2MzU5Mi0xMTEuOTQ1MzM5LTc0LjU2MzU5Mi0xODAuMDEyMTAzIDAtNjcuOTY2ODEzIDI2LjQ4NzA2Ny0xMzEuOTM1NTc4IDc0LjU2MzU5Mi0xODAuMDEyMTA0IDQ4LjA3NjUyNS00OC4wNzY1MjUgMTExLjk0NTMzOS03NC41NjM1OTIgMTgwLjAxMjEwNC03NC41NjM1OTIgNjcuOTY2ODEzIDAgMTMxLjkzNTU3OCAyNi40ODcwNjcgMTgwLjAxMjEwMyA3NC41NjM1OTIgNDguMDc2NTI1IDQ4LjA3NjUyNSA3NC41NjM1OTIgMTExLjk0NTMzOSA3NC41NjM1OTIgMTgwLjAxMjEwNCAwIDY3Ljk2NjgxMy0yNi40ODcwNjcgMTMxLjkzNTU3OC03NC41NjM1OTIgMTgwLjAxMjEwMy00OC4wNzY1MjUgNDguMDc2NTI1LTExMS45NDUzMzkgNzQuNTYzNTkyLTE4MC4wMTIxMDMgNzQuNTYzNTkycy0xMzEuOTM1NTc4LTI2LjQ4NzA2Ny0xODAuMDEyMTA0LTc0LjU2MzU5MnoiIHAtaWQ9IjE0NDUyOCIgZmlsbD0iI2QzMjE3YiI+PC9wYXRoPjxwYXRoIGQ9Ik01MTIuMjQ5ODc4IDUxMS45NTAwMjRtLTMxLjk4NDM4MyAwYTMxLjk4NDM4MyAzMS45ODQzODMgMCAxIDAgNjMuOTY4NzY2IDAgMzEuOTg0MzgzIDMxLjk4NDM4MyAwIDEgMC02My45Njg3NjYgMFoiIHAtaWQ9IjE0NDUyOSIgZmlsbD0iI2QzMjE3YiI+PC9wYXRoPjxwYXRoIGQ9Ik0zNTEuMzI4NDUzIDUxMC45NTA1MTJtLTMxLjk4NDM4MyAwYTMxLjk4NDM4MyAzMS45ODQzODMgMCAxIDAgNjMuOTY4NzY2IDAgMzEuOTg0MzgzIDMxLjk4NDM4MyAwIDEgMC02My45Njg3NjYgMFoiIHAtaWQ9IjE0NDUzMCIgZmlsbD0iI2QzMjE3YiI+PC9wYXRoPjxwYXRoIGQ9Ik02NzEuMjcyMjMgNTEwLjk1MDUxMm0tMzEuOTg0MzgyIDBhMzEuOTg0MzgzIDMxLjk4NDM4MyAwIDEgMCA2My45Njg3NjUgMCAzMS45ODQzODMgMzEuOTg0MzgzIDAgMSAwLTYzLjk2ODc2NSAwWiIgcC1pZD0iMTQ0NTMxIiBmaWxsPSIjZDMyMTdiIj48L3BhdGg+PC9zdmc+'
};

@Component({
  selector: 'app-flow-list',
  templateUrl: './flow-list.component.html',
  styleUrls: ['./flow-list.component.css']
})
export class FlowListComponent implements OnInit {

  constructor(public http: HttpClient,
              public appService: AppService) {
  }

  chartOptionList = [];

  ngOnInit(): void {
    this.appService.allFlowConfigList.subscribe( (data: []) => this.drawFlowChart(data));
  }

  drawFlowChart = (flowConfigList: Object[]) => {
    const flowGroupList = Tools.groupBy(flowConfigList, o => [o['source'], o['schema'], o['name']].join('.'));
    const chartOptions = {};
    for (const namespace of Object.keys(flowGroupList)) {
      chartOptions[namespace] = this.drawOneNamespaceFlow(namespace, flowGroupList[namespace]);
      this.chartOptionList.push(chartOptions[namespace]);
    }
  };

  getFlowNodeResolverIcon = (resolverName: string): string => {
    const iconDataUrl = svgIcons[resolverName]
    return !!iconDataUrl ? iconDataUrl : svgIcons.dataProcess
  }

  drawOneNamespaceFlow = (namespace: string, flows: object[]): object => {
    const chartNodes = [];
    const chartLinks = [];
    // 根据当前namespace下flow的个数计算高度
    const height = (flows.length + 1) * 80;
    let width = 0;
    const namespaceNodeSymbolSize = 160
    const endpointNodeSymbolSize = 45
    const resolverNodeSymbolSize = 36
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
          const arr = params['name'].split('.');
          return `source: ${arr[0]}<br>schema: ${arr[1]}<br>name: ${arr[2]}`;
        },
      },
      symbol: `image://${svgIcons.dataSource}`,
      symbolSize: namespaceNodeSymbolSize
    });
    const chartOption: EChartsOption = {
      tooltip: {
        backgroundColor: 'rgba(50,50,50,0.7)',
        textStyle: {
          color: '#fff'
        },
        // extraCssText: `max-width: 600px; max-height: ${height}px;word-wrap:break-word;`,
        confine: true,
        enterable: true,
        borderWidth: 0
      },
      animationDurationUpdate: 2000,
      animationDelay: function (idx) {
        // 越往后的数据延迟越大
        return idx * 100;
      },
      animationEasingUpdate: 'quinticInOut',
      series: [
        {
          type: 'graph',
          symbolSize: resolverNodeSymbolSize,
          left: 120,
          top: 40,
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
          links: [],
        }],
      initOpts: {
        height: height,
        width: 0
      }
    };
    let yStartPos = 80;
    for (const flow of flows) {
      const flowNodes: object[] = flow['node_list'];
      let xStartPos = 300;

      // 图表宽度
      width = chartOption['initOpts']['width'] = Math.max(600 + flowNodes.length * 600, width);
      // 上一个流节点的结束节点名称

      let lastNodeEndPointName = namespace;

      for (const flowNode of flowNodes) {
        xStartPos += 200;
        // 避免图的节点name重复，加上id前缀
        const nodeName = `${flow['_id']}.${flowNode['node_name']}`;
        let resolverList: string[] = Object.keys(flowNode).filter(k => ['skip_if_exception', 'node_name', 'resolve_order'].indexOf(k) === -1);
        const resolverOrder: string[] = flowNode['resolve_order'];
        if (!!resolverOrder && resolverOrder instanceof Array && resolverOrder.length > 0) {
          resolverList = resolverList.sort((a, b) => {
            let iA, iB;
            return ((iA = resolverOrder.indexOf(a)) == -1 ? Number.MAX_VALUE : iA)
              - ((iB = resolverOrder.indexOf(b)) == -1 ? Number.MAX_VALUE : iB);
          });
        }

        // 进入的chart节点
        chartNodes.push({
          name: `${nodeName}.__in`,
          x: xStartPos,
          y: yStartPos,
          label: {
            formatter: flowNode['node_name']
          },
          tooltip: {
            formatter: `input to flow node: <span style="font-weight: bolder; color: darkturquoise">${flowNode['node_name']}</span>`
          },
          symbol: `image://${svgIcons.in}`,
          symbolSize: endpointNodeSymbolSize
        });
        if (lastNodeEndPointName === namespace) {
          // 源节点与入口节点的连线
          chartLinks.push({
            source: lastNodeEndPointName,
            target: `${nodeName}.__in`,
            name: `flow: ${flow['_id']}`,
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
            },
            lineStyle: {
              curveness: '0.08',
              width: 2
            },
            emphasis: {
              lineStyle: {
                width: 4
              }
            },
          });
        } else {
          // 上一个节点的输出点与当前节点的输入点的连线
          chartLinks.push({
            source: lastNodeEndPointName,
            target: `${nodeName}.__in`,
            tooltip: {
              formatter: `--> ${flowNode['node_name']}`
            },
            emphasis: {
              lineStyle: {
                width: 3
              }
            },
          });
        }
        xStartPos += 200;

        let lastNodeName = `${nodeName}.__in`;

        for (const resolverName of resolverList) {
          const resolverFullName = `${nodeName}.${resolverName}`;
          // 解析器节点
          chartNodes.push({
            name: resolverFullName,
            x: xStartPos,
            y: yStartPos,
            label: {
              formatter: resolverName
            },
            symbol: `image://${this.getFlowNodeResolverIcon(resolverName)}`,
            tooltip: {
              formatter: JSON.stringify(flowNode[resolverName], null, '\t')
                  .replace(/\n/g, '<br>').replace(/\t/g, '&nbsp;&nbsp;'),
              textStyle: {
                fontFamily: 'Hack'
              }
            }
          });
          chartLinks.push({
            source: lastNodeName,
            target: resolverFullName,
            tooltip: {
              show: false
            },
            emphasis: {
              lineStyle: {
                width: 3
              }
            }
          });
          xStartPos += 240;
          lastNodeName = resolverFullName;
        }
        // 流节点的输出chart节点
        chartNodes.push({
          name: `${nodeName}.__out`,
          x: xStartPos,
          y: yStartPos,
          label: {
            formatter: flowNode['node_name']
          },
          tooltip: {
            formatter: `out from flow node: <span style="font-weight: bolder; color: darkturquoise">${flowNode['node_name']}</span>`
          },
          symbol: `image://${svgIcons.out}`,
          symbolSize: endpointNodeSymbolSize
        });
        chartLinks.push({
          source: lastNodeName,
          target: `${nodeName}.__out`,
          tooltip: {
            show: false
          },
          emphasis: {
            lineStyle: {
              width: 3
            }
          },
        });
        xStartPos += 200;
        lastNodeEndPointName = `${nodeName}.__out`;
      }
      yStartPos += 160;
    }

    chartOption['initOpts']['width'] = width;

    chartOption.series[0].data = chartNodes;
    chartOption.series[0].links = chartLinks;

    return chartOption;
  };
}
