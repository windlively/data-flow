import {Injectable} from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor, HttpResponseBase, HttpResponse, HttpErrorResponse
} from '@angular/common/http';
import {Observable, of, throwError} from 'rxjs';
import {catchError, debounceTime, finalize, mergeMap, retry} from 'rxjs/operators';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AppService} from '../service/app.service';

@Injectable()
export class DataFlowInterceptor implements HttpInterceptor {

  private processingHttpCount = 0;

  constructor(
    private snackBar: MatSnackBar,
    public app: AppService
  ) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    this.app.showLoadingBarSubject.next(true);
    this.processingHttpCount++;
    return next.handle(req.clone({
      url: `/data-flow${req.url.startsWith('/') ? req.url : '/' + req.url}`
    }))
      .pipe(
        debounceTime(1000),
        // 失败时重试2次
        retry(2),
        mergeMap((event: any) => {
          if (event instanceof HttpResponseBase) {
            // HTTP返回代码正常
            if (event.status >= 200 && event.status < 400) {
              if (event instanceof HttpResponse) {
                const body = event.body;
                if (body && body.success) {
                  // 取出响应体数据的data部分
                  return of(new HttpResponse(Object.assign(event, {body: body.data})));
                } else {
                  throw Error(body.msg);
                }
              }
            }
          }
          return of(event);
        }), catchError((err: HttpErrorResponse) => {
          this.app.showSnackBar(err.message);
          console.error(err.message);
          return throwError(err);
        }), finalize(() => {
          setTimeout(() => this.app.showLoadingBarSubject.next(--this.processingHttpCount !== 0), 2000);
        }));
  }
}
